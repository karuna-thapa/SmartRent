package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.dto.CustomerProfileDto;
import com.springbootapp.fyp.smartrent.model.Customer;
import com.springbootapp.fyp.smartrent.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

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
        dto.setLicenseNo(customer.getLicenseNo());
        dto.setGender(customer.getGender());

        return ResponseEntity.ok(dto);
    }

    // PUT /api/customer/profile
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody CustomerProfileDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.getFirstName() != null) customer.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)  customer.setLastName(dto.getLastName());
        if (dto.getPhoneNumber() != null) customer.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getDob() != null)       customer.setDob(dto.getDob());
        if (dto.getAddress() != null)   customer.setAddress(dto.getAddress());
        if (dto.getGender() != null)    customer.setGender(dto.getGender());

        customerRepository.save(customer);
        return ResponseEntity.ok("Profile updated successfully");
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
