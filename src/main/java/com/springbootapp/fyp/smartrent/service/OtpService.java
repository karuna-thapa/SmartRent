package com.springbootapp.fyp.smartrent.service;

import com.springbootapp.fyp.smartrent.model.OtpToken;
import com.springbootapp.fyp.smartrent.repository.OtpTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private EmailService emailService;

    private final SecureRandom random = new SecureRandom();

    public void generateAndSendVerificationOtp(String email) {
        String otp = generateOtp();
        save(email, otp, OtpToken.OtpType.VERIFY_EMAIL);
        emailService.sendVerificationOtp(email, otp);
    }

    public void generateAndSendLoginOtp(String email) {
        String otp = generateOtp();
        save(email, otp, OtpToken.OtpType.LOGIN);
        emailService.sendLoginOtp(email, otp);
    }

    public boolean verifyOtp(String email, String otp, OtpToken.OtpType type) {
        Optional<OtpToken> tokenOpt = otpTokenRepository
                .findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(email, type);

        if (tokenOpt.isEmpty()) return false;

        OtpToken token = tokenOpt.get();

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) return false;
        if (!token.getOtp().equals(otp)) return false;

        token.setUsed(true);
        otpTokenRepository.save(token);
        return true;
    }

    private String generateOtp() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    private void save(String email, String otp, OtpToken.OtpType type) {
        OtpToken token = new OtpToken();
        token.setEmail(email);
        token.setOtp(otp);
        token.setType(type);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        token.setUsed(false);
        otpTokenRepository.save(token);
    }
}
