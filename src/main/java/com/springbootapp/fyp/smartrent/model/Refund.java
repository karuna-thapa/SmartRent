package com.springbootapp.fyp.smartrent.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "refund")
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id")
    private Integer refundId;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal refundPercentage;

    @Column(name = "refund_reason")
    private String refundReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", columnDefinition = "ENUM('PENDING','PROCESSED') DEFAULT 'PENDING'")
    private RefundStatus refundStatus = RefundStatus.PENDING;

    public enum RefundStatus { PENDING, PROCESSED }

    @Column(name = "initiated_by", length = 50)
    private String initiatedBy; // "CUSTOMER", "VENDOR", "ADMIN"

    @Column(name = "created_at", nullable = true)
    private LocalDateTime createdAt;

    @Column(name = "refund_timestamp", nullable = false)
    private LocalDateTime refundTimestamp;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        refundTimestamp = now;
    }
}