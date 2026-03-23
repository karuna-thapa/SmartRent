package com.springbootapp.fyp.smartrent.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "dob", nullable = true)
    private LocalDate dob;

    @Column(name = "password", nullable = false, length = 255)
    private String password; // BCrypt hashed

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "profile_image", length = 255)
    private String profileImage;

    @Column(name = "license_no", nullable = false, length = 100)
    private String licenseNo;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", columnDefinition = "ENUM('admin','vendor','customer') DEFAULT 'customer'")
    private Role role = Role.customer;

    public enum Role {
        admin, vendor, customer
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}