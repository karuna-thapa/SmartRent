package com.springbootapp.fyp.smartrent.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false,
            columnDefinition = "ENUM('paid','pending','failed') DEFAULT 'pending'")
    private PaymentStatus paymentStatus = PaymentStatus.pending;

    public enum PaymentStatus {
        paid, pending, failed
    }

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false,
            columnDefinition = "ENUM('esewa','khalti','card','cash')")
    private PaymentMethod paymentMethod;

    public enum PaymentMethod {
        esewa, khalti, card, cash
    }

    @Column(name = "transaction_id", unique = true, length = 100)
    private String transactionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
