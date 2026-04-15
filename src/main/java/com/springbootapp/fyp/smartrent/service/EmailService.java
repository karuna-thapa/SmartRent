package com.springbootapp.fyp.smartrent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendVerificationOtp(String to, String otp) {
        log.info("Sending verification OTP to: {}", to);
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("SmartRent — Verify Your Email");
        msg.setText(
            "Welcome to SmartRent!\n\n" +
            "Your email verification code is:\n\n" +
            "  " + otp + "\n\n" +
            "This code expires in 5 minutes.\n\n" +
            "If you did not register on SmartRent, please ignore this email."
        );
        try {
            mailSender.send(msg);
            log.info("Verification OTP sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("SMTP ERROR sending to {}: {}", to, e.getMessage(), e);
            throw e;
        }
    }

    public void sendLoginOtp(String to, String otp) {
        log.info("Sending login OTP to: {}", to);
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("SmartRent — Login Verification Code");
        msg.setText(
            "Your SmartRent login verification code is:\n\n" +
            "  " + otp + "\n\n" +
            "This code expires in 5 minutes.\n\n" +
            "If you did not attempt to log in, please change your password immediately."
        );
        try {
            mailSender.send(msg);
            log.info("Login OTP sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("SMTP ERROR sending to {}: {}", to, e.getMessage(), e);
            throw e;
        }
    }
}
