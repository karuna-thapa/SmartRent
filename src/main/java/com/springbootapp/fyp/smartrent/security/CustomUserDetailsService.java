package com.springbootapp.fyp.smartrent.security;

import com.springbootapp.fyp.smartrent.model.Customer;
import com.springbootapp.fyp.smartrent.model.Vendor;
import com.springbootapp.fyp.smartrent.repository.CustomerRepository;
import com.springbootapp.fyp.smartrent.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        // Check vendor table first
        Optional<Vendor> vendorOpt = vendorRepository.findByEmail(email);
        if (vendorOpt.isPresent()) {
            Vendor vendor = vendorOpt.get();
            return new User(
                    vendor.getEmail(),
                    vendor.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_VENDOR"))
            );
        }

        // Fall back to customer table
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email)
                );

        return new User(
                customer.getEmail(),
                customer.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + customer.getRole().name().toUpperCase()))
        );
    }
}