package com.springbootapp.fyp.smartrent.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Integer bookingId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "rental_duration_type", nullable = false,
            columnDefinition = "ENUM('per_day','per_week','per_month') DEFAULT 'per_day'")
    private RentalDurationType rentalDurationType = RentalDurationType.per_day;

    public enum RentalDurationType {
        per_day, per_week, per_month
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", columnDefinition = "ENUM('PENDING','CONFIRMED','CANCELLED') DEFAULT 'PENDING'")
    private BookingStatus bookingStatus = BookingStatus.PENDING;

    public enum BookingStatus { PENDING, CONFIRMED, CANCELLED }

    @Column(name = "pickup_location", length = 200)
    private String pickupLocation;

    @Column(name = "dropoff_location", length = 200)
    private String dropoffLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", columnDefinition = "ENUM('UNPAID','PAID') DEFAULT 'UNPAID'")
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    public enum PaymentStatus { UNPAID, PAID }

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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
