package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.dto.CustomerProfileDto;
import com.springbootapp.fyp.smartrent.model.Customer;
import com.springbootapp.fyp.smartrent.repository.CustomerRepository;
import com.springbootapp.fyp.smartrent.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // GET /api/customer/profile
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomerProfileDto dto = new CustomerProfileDto();
        dto.setCustomerId(customer.getCustomerId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setPhoneNumber(customer.getPhoneNumber());
        dto.setDob(customer.getDob());
        dto.setAddress(customer.getAddress());
        dto.setProfileImage(customer.getProfileImage());
        dto.setLicenseImage(customer.getLicenseImage());
        dto.setLicenseNo(customer.getLicenseNo());

        return ResponseEntity.ok(dto);
    }

    // PUT /api/customer/profile
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody CustomerProfileDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.getFirstName() != null)   customer.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)    customer.setLastName(dto.getLastName());
        if (dto.getPhoneNumber() != null) customer.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getDob() != null)         customer.setDob(dto.getDob());
        if (dto.getAddress() != null)     customer.setAddress(dto.getAddress());

        customerRepository.save(customer);
        return ResponseEntity.ok("Profile updated successfully");
    }

    // POST /api/customer/profile/photo
    @PostMapping("/profile/photo")
    public ResponseEntity<?> uploadProfilePhoto(@RequestParam("file") MultipartFile file) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        try {
            if (customer.getProfileImage() != null) {
                fileStorageService.deleteFile(customer.getProfileImage());
            }
            String path = fileStorageService.storeProfileImage(file);
            customer.setProfileImage(path);
            customerRepository.save(customer);
            return ResponseEntity.ok(java.util.Map.of("profileImage", path));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to upload photo");
        }
    }

    // POST /api/customer/profile/license-image
    @PostMapping("/profile/license-image")
    public ResponseEntity<?> uploadLicenseImage(@RequestParam("file") MultipartFile file) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        try {
            if (customer.getLicenseImage() != null) {
                fileStorageService.deleteFile(customer.getLicenseImage());
            }
            String path = fileStorageService.storeLicenseImage(file);
            customer.setLicenseImage(path);
            customerRepository.save(customer);
            return ResponseEntity.ok(java.util.Map.of("licenseImage", path));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to upload license image");
        }
    }

    // PUT /api/customer/password
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        String currentPassword = body.get("currentPassword");
        String newPassword     = body.get("newPassword");

        if (currentPassword == null || newPassword == null || newPassword.length() < 6)
            return ResponseEntity.badRequest().body("Invalid password data.");

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, customer.getPassword()))
            return ResponseEntity.badRequest().body("Current password is incorrect.");

        customer.setPassword(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);
        return ResponseEntity.ok("Password updated successfully.");
    }

    // DELETE /api/customer/account
    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        customerRepository.delete(customer);
        return ResponseEntity.ok("Account deleted successfully");
    }
}
