package com.springbootapp.fyp.smartrent.service;

import com.springbootapp.fyp.smartrent.dto.AuthResponseDto;
import com.springbootapp.fyp.smartrent.dto.CustomerDto;
import com.springbootapp.fyp.smartrent.dto.LoginDto;
import com.springbootapp.fyp.smartrent.model.Customer;
import com.springbootapp.fyp.smartrent.repository.CustomerRepository;
import com.springbootapp.fyp.smartrent.security.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthUtil authUtil;

    // ===================== REGISTER =====================
    public String registerCustomer(CustomerDto dto) {

        // Check if email already exists
        if (customerRepository.existsByEmail(dto.getEmail())) {
            return "Email already registered!";
        }

        // Create new customer
        Customer customer = new Customer();
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setEmail(dto.getEmail());
        customer.setPassword(passwordEncoder.encode(dto.getPassword())); // BCrypt hash
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setDob(dto.getDob());
        customer.setAddress(dto.getAddress());
        customer.setLicenseNo(dto.getLicenseNo());
        customer.setProfileImage(dto.getProfileImage());
        customer.setRole(Customer.Role.customer);

        customerRepository.save(customer);

        return "Customer registered successfully!";
    }

    // ===================== LOGIN =====================
    public AuthResponseDto login(LoginDto dto) {

        // Find customer by email
        Customer customer = customerRepository.findByEmail(dto.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found with email: "
                                + dto.getEmail())
                );

        // Check password
        if (!passwordEncoder.matches(dto.getPassword(), customer.getPassword())) {
            throw new RuntimeException("Invalid password!");
        }

        // Generate JWT token
        String token = authUtil.generateToken(
                customer.getEmail(),
                customer.getRole().name()
        );

        // Return token + role + info
        return new AuthResponseDto(
                token,
                customer.getRole().name(),
                customer.getEmail(),
                customer.getFirstName()
        );
    }
}