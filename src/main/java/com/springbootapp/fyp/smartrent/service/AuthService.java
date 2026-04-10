package com.springbootapp.fyp.smartrent.service;

import com.springbootapp.fyp.smartrent.dto.AuthResponseDto;
import com.springbootapp.fyp.smartrent.dto.CustomerDto;
import com.springbootapp.fyp.smartrent.dto.LoginDto;
import com.springbootapp.fyp.smartrent.dto.VendorDto;
import com.springbootapp.fyp.smartrent.model.Brand;
import com.springbootapp.fyp.smartrent.model.Customer;
import com.springbootapp.fyp.smartrent.model.Vendor;
import com.springbootapp.fyp.smartrent.repository.BrandRepository;
import com.springbootapp.fyp.smartrent.repository.CustomerRepository;
import com.springbootapp.fyp.smartrent.repository.VendorRepository;
import com.springbootapp.fyp.smartrent.security.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private BrandRepository brandRepository;

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

    // ===================== REGISTER VENDOR =====================
    public String registerVendor(VendorDto dto) {
        if (vendorRepository.existsByEmail(dto.getEmail())) {
            return "Email already registered!";
        }

        Vendor vendor = new Vendor();
        vendor.setVendorName(dto.getVendorName());
        vendor.setCompanyName(dto.getCompanyName());
        vendor.setEmail(dto.getEmail());
        vendor.setPassword(passwordEncoder.encode(dto.getPassword()));
        vendor.setPhoneNumber(dto.getPhoneNumber());
        vendor.setRegistrationNo(dto.getRegistrationNo());
        vendor.setStatus(Vendor.Status.PENDING);

        if (dto.getBrandName() != null && !dto.getBrandName().isBlank()) {
            // Find existing brand, or create it if it doesn't exist in the DB yet
            Brand brand = brandRepository.findByBrandNameIgnoreCase(dto.getBrandName())
                    .orElseGet(() -> {
                        Brand newBrand = new Brand();
                        newBrand.setBrandName(dto.getBrandName());
                        return brandRepository.save(newBrand);
                    });
            vendor.setBrand(brand);
        }

        vendorRepository.save(vendor);
        return "Vendor registration submitted! Awaiting admin approval.";
    }

    // ===================== LOGIN =====================
    public AuthResponseDto login(LoginDto dto) {

        // ── Try vendor table first ───────────────────────────────────────────
        java.util.Optional<Vendor> vendorOpt = vendorRepository.findByEmail(dto.getEmail());
        if (vendorOpt.isPresent()) {
            Vendor vendor = vendorOpt.get();

            if (!passwordEncoder.matches(dto.getPassword(), vendor.getPassword())) {
                throw new RuntimeException("Invalid password!");
            }

            if (vendor.getStatus() == Vendor.Status.PENDING) {
                throw new RuntimeException("Your account is pending admin approval.");
            }
            if (vendor.getStatus() == Vendor.Status.REJECTED) {
                throw new RuntimeException("Your vendor application was rejected.");
            }

            String token = authUtil.generateToken(vendor.getEmail(), "vendor");

            return new AuthResponseDto(
                    token,
                    "vendor",
                    vendor.getEmail(),
                    vendor.getVendorName()
            );
        }

        // ── Fall back to customer table ──────────────────────────────────────
        Customer customer = customerRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("No account found with that email."));

        if (!passwordEncoder.matches(dto.getPassword(), customer.getPassword())) {
            throw new RuntimeException("Invalid password!");
        }

        String token = authUtil.generateToken(
                customer.getEmail(),
                customer.getRole().name()
        );

        return new AuthResponseDto(
                token,
                customer.getRole().name(),
                customer.getEmail(),
                customer.getFirstName()
        );
    }
}