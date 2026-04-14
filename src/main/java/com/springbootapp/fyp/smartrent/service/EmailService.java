package com.springbootapp.fyp.smartrent.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendVerificationOtp(String to, String otp) {
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
        mailSender.send(msg);
    }

    public void sendLoginOtp(String to, String otp) {
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
        mailSender.send(msg);
    }
}
