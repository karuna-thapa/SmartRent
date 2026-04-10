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

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    // ===================== REGISTER =====================
    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(
            @RequestBody CustomerDto customerDto) {
        try {
            String result = authService.registerCustomer(customerDto);

            if (result.equals("Email already registered!")) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(result);
            }

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }

    // ===================== REGISTER VENDOR =====================
    @PostMapping("/register-vendor")
    public ResponseEntity<?> registerVendor(@RequestBody VendorDto vendorDto) {
        try {
            String result = authService.registerVendor(vendorDto);
            if (result.equals("Email already registered!")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }

    // ===================== LOGIN =====================
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginDto loginDto) {
        try {
            AuthResponseDto response = authService.login(loginDto);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        }
    }
}