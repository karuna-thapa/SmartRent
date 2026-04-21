package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.dto.AuthResponseDto;
import com.springbootapp.fyp.smartrent.dto.CustomerDto;
import com.springbootapp.fyp.smartrent.dto.LoginDto;
import com.springbootapp.fyp.smartrent.dto.VendorDto;
import com.springbootapp.fyp.smartrent.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    // ===================== REGISTER =====================
    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(@RequestBody CustomerDto customerDto) {
        try {
            String result = authService.registerCustomer(customerDto);
            if (result.equals("Email already registered!")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }

    // ===================== REGISTER VENDOR =====================
    @PostMapping("/register-vendor")
    public ResponseEntity<?> registerVendor(
            @ModelAttribute VendorDto vendorDto,
            @RequestParam(value = "registrationDocument", required = false) MultipartFile registrationDocument) {
        try {
            String result = authService.registerVendor(vendorDto, registrationDocument);
            if (result.equals("Email already registered!")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }

    // ===================== LOGIN — STEP 1 =====================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        try {
            Object response = authService.login(loginDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg == null) msg = "Login failed.";

            // Map to user-friendly messages
            if (msg.contains("No account found"))      msg = "No account found with this email. Please register first.";
            else if (msg.contains("Invalid password")) msg = "Incorrect password. Please try again.";
            else if (msg.contains("pending admin"))    msg = "Your vendor account is awaiting admin approval.";
            else if (msg.contains("rejected"))         msg = "Your vendor application was not approved.";
            else if (msg.contains("Mail") || msg.contains("mail") || msg.contains("SMTP") || msg.contains("smtp"))
                msg = "Could not send verification code. Please try again.";

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(msg);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not send verification code. Please try again.");
        }
    }

    // ===================== LOGIN — STEP 2: verify OTP =====================
    @PostMapping("/login/verify-otp")
    public ResponseEntity<?> verifyLoginOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp   = body.get("otp");
        if (email == null || otp == null) {
            return ResponseEntity.badRequest().body("Email and OTP are required.");
        }
        try {
            AuthResponseDto response = authService.verifyLoginOtp(email, otp);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // ===================== RESEND LOGIN OTP =====================
    @PostMapping("/resend-login-otp")
    public ResponseEntity<?> resendLoginOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }
        try {
            authService.resendLoginOtp(email);
            return ResponseEntity.ok("Verification code resent. Check your inbox.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
