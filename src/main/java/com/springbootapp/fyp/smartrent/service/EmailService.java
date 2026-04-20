package com.springbootapp.fyp.smartrent.service;

import java.math.BigDecimal;

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

    public void sendPaymentDeadlineReminder(String to, Integer bookingId) {
        log.info("Sending payment deadline reminder for booking {} to {}", bookingId, to);
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("SmartRent — Payment Reminder (1 Hour Left)");
        msg.setText(
            "Booking ID: #" + bookingId + "\n\n" +
            "If payment is not completed within 1 hour, your booking will be cancelled.\n\n" +
            "कृपया १ घण्टाभित्र भुक्तानी पूरा गर्नुहोस्, नत्र बुकिङ स्वतः रद्द हुनेछ।"
        );
        try {
            mailSender.send(msg);
            log.info("Payment reminder sent successfully for booking {}", bookingId);
        } catch (Exception e) {
            log.error("SMTP ERROR sending payment reminder for booking {}: {}", bookingId, e.getMessage(), e);
            throw e;
        }
    }

    public void sendAutoCancellationNotice(String to, Integer bookingId) {
        log.info("Sending auto-cancellation notice for booking {} to {}", bookingId, to);
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("SmartRent — Booking Auto-Cancelled (Payment Deadline Missed)");
        msg.setText(
            "Booking ID: #" + bookingId + "\n\n" +
            "Your booking has been cancelled because payment was not completed within 24 hours.\n" +
            "Status: Cancelled - भुक्तानी नभएको"
        );
        try {
            mailSender.send(msg);
            log.info("Auto-cancellation notice sent successfully for booking {}", bookingId);
        } catch (Exception e) {
            log.error("SMTP ERROR sending auto-cancellation notice for booking {}: {}", bookingId, e.getMessage(), e);
            throw e;
        }
    }

    public void sendCancellationSuccessNotice(String to, Integer bookingId, boolean refundInitiated) {
        log.info("Sending cancellation success notice for booking {} to {}", bookingId, to);
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("SmartRent — Booking Cancellation Successful");
        msg.setText(
            "Booking ID: #" + bookingId + "\n\n" +
            "Your cancellation request was successful.\n" +
            (refundInitiated ? "Refund has been initiated.\n" : "No refund is applicable for this booking.\n")
        );
        mailSender.send(msg);
    }

    public void sendRefundInitiatedNotice(String to, Integer bookingId, BigDecimal refundAmount, String initiatedBy) {
        log.info("Sending refund initiated notice for booking {} to {}", bookingId, to);
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("SmartRent — Refund Request Successful");
        msg.setText(
            "Booking ID: #" + bookingId + "\n\n" +
            "Refund request is successful and has been initiated.\n" +
            "Amount: NPR " + (refundAmount != null ? refundAmount : "0") + "\n" +
            "Initiated By: " + (initiatedBy != null ? initiatedBy : "SYSTEM")
        );
        mailSender.send(msg);
    }

    public void sendVendorCancellationRequestSubmitted(String to, Integer bookingId) {
        log.info("Sending vendor cancellation request submitted notice for booking {} to {}", bookingId, to);
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("SmartRent — Cancellation Request Submitted");
        msg.setText(
            "Booking ID: #" + bookingId + "\n\n" +
            "Your cancellation/refund request was submitted successfully and is pending admin review."
        );
        mailSender.send(msg);
    }

    public void sendVendorCancellationRequestDecision(String to, Integer bookingId, boolean approved, String reason) {
        log.info("Sending vendor cancellation request decision for booking {} to {}", bookingId, to);
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("SmartRent — Cancellation Request " + (approved ? "Approved" : "Rejected"));
        msg.setText(
            "Booking ID: #" + bookingId + "\n\n" +
            "Your cancellation/refund request was " + (approved ? "approved" : "rejected") + ".\n" +
            (reason != null && !reason.isBlank() ? ("Reason: " + reason) : "")
        );
        mailSender.send(msg);
    }
}
