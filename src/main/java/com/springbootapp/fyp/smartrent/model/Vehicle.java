package com.springbootapp.fyp.smartrent.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Integer vehicleId;

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @ManyToOne
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "vehicle_category_id", nullable = false)
    private VehicleCategory vehicleCategory;

    @Column(name = "vehicle_name", nullable = false, length = 150)
    private String vehicleName;

    @Column(name = "rental_price", nullable = false)
    private BigDecimal rentalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('available','not_available') DEFAULT 'available'")
    private Status status = Status.available;

    @Column(name = "seats_capacity")
    private Integer seatsCapacity;

    @Column(name = "vehicle_no", nullable = false, unique = true, length = 50)
    private String vehicleNo;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "active")
    private Boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", columnDefinition = "ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING'")
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    public enum ApprovalStatus { PENDING, APPROVED, REJECTED }

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        available, not_available
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