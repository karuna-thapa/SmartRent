package com.springbootapp.fyp.smartrent.repository;

import com.springbootapp.fyp.smartrent.model.Booking;
import com.springbootapp.fyp.smartrent.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Integer> {
    Optional<Refund> findByBooking(Booking booking);
    Optional<Refund> findByBooking_BookingId(Integer bookingId);

    List<Refund> findByBooking_BookingIdIn(List<Integer> bookingIds);

    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM Refund r")
    BigDecimal sumAllRefunds();

    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM Refund r WHERE r.booking.vehicle.vendor.email = :vendorEmail")
    BigDecimal sumRefundByVendorEmail(@Param("vendorEmail") String vendorEmail);

    @Query("SELECT r FROM Refund r WHERE r.booking.vehicle.vendor.email = :vendorEmail ORDER BY r.refundTimestamp DESC")
    List<Refund> findByVendorEmailOrderByRefundTimestampDesc(@Param("vendorEmail") String vendorEmail);

    @Query("""
        SELECT YEAR(r.refundTimestamp), MONTH(r.refundTimestamp), COALESCE(SUM(r.refundAmount), 0)
        FROM Refund r
        WHERE r.booking.vehicle.vendor.email = :vendorEmail
        GROUP BY YEAR(r.refundTimestamp), MONTH(r.refundTimestamp)
        ORDER BY YEAR(r.refundTimestamp), MONTH(r.refundTimestamp)
    """)
    List<Object[]> monthlyRefundsByVendorEmail(@Param("vendorEmail") String vendorEmail);

    @Query("""
        SELECT YEAR(r.refundTimestamp), COALESCE(SUM(r.refundAmount), 0)
        FROM Refund r
        WHERE r.booking.vehicle.vendor.email = :vendorEmail
        GROUP BY YEAR(r.refundTimestamp)
        ORDER BY YEAR(r.refundTimestamp)
    """)
    List<Object[]> yearlyRefundsByVendorEmail(@Param("vendorEmail") String vendorEmail);

    List<Refund> findAllByOrderByRefundTimestampDesc();
}