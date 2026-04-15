package com.springbootapp.fyp.smartrent.repository;

import com.springbootapp.fyp.smartrent.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findByTransactionUuid(String transactionUuid);

    // All paid payments (admin view)
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'paid' ORDER BY p.createdAt DESC")
    List<Payment> findAllPaid();

    // Paid payments for a specific vendor's vehicles
    @Query("""
        SELECT p FROM Payment p
        WHERE p.paymentStatus = 'paid'
          AND p.booking.vehicle.vendor.email = :vendorEmail
        ORDER BY p.createdAt DESC
    """)
    List<Payment> findPaidByVendorEmail(@Param("vendorEmail") String vendorEmail);

    // Admin stats
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = 'paid'")
    BigDecimal sumTotalCollected();

    @Query("SELECT COALESCE(SUM(p.adminAmount), 0) FROM Payment p WHERE p.paymentStatus = 'paid'")
    BigDecimal sumAdminRevenue();

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.paymentStatus = 'paid'")
    long countPaid();

    // Vendor stats
    @Query("SELECT COALESCE(SUM(p.vendorAmount), 0) FROM Payment p WHERE p.paymentStatus = 'paid' AND p.booking.vehicle.vendor.email = :vendorEmail")
    BigDecimal sumVendorRevenue(@Param("vendorEmail") String vendorEmail);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.paymentStatus = 'paid' AND p.booking.vehicle.vendor.email = :vendorEmail")
    long countPaidByVendorEmail(@Param("vendorEmail") String vendorEmail);
}
