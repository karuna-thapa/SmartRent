package com.springbootapp.fyp.smartrent.repository;

import com.springbootapp.fyp.smartrent.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.vehicle.vehicleId = :vehicleId AND b.endDate >= CURRENT_DATE")
    boolean hasActiveBookings(@Param("vehicleId") Integer vehicleId);

    boolean existsByVehicle_VehicleId(Integer vehicleId);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.vehicle.vendor.email = :vendorEmail
          AND (:vehicleId   IS NULL OR b.vehicle.vehicleId = :vehicleId)
          AND (:dateFrom    IS NULL OR b.startDate >= :dateFrom)
          AND (:dateTo      IS NULL OR b.endDate   <= :dateTo)
          AND (:status      IS NULL OR b.bookingStatus = :status)
        ORDER BY b.createdAt DESC
    """)
    List<Booking> findVendorBookings(
            @Param("vendorEmail") String vendorEmail,
            @Param("vehicleId")   Integer vehicleId,
            @Param("dateFrom")    LocalDate dateFrom,
            @Param("dateTo")      LocalDate dateTo,
            @Param("status")      Booking.BookingStatus status
    );

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.vehicle.vendor.email = :vendorEmail AND (b.bookingStatus = 'CONFIRMED' OR b.bookingStatus = 'REFUNDED')")
    long countActiveByVendor(@Param("vendorEmail") String vendorEmail);

    @Query("SELECT (COALESCE(SUM(b.totalPrice), 0) - (SELECT COALESCE(SUM(r.refundAmount), 0) FROM Refund r WHERE r.booking.vehicle.vendor.email = :vendorEmail)) FROM Booking b WHERE b.vehicle.vendor.email = :vendorEmail AND (b.bookingStatus = 'CONFIRMED' OR b.bookingStatus = 'REFUNDED')")
    java.math.BigDecimal sumRevenueByVendor(@Param("vendorEmail") String vendorEmail);

    @Query("""
        SELECT YEAR(b.startDate), MONTH(b.startDate), COALESCE(SUM(b.totalPrice), 0)
        FROM Booking b
        WHERE b.vehicle.vendor.email = :vendorEmail AND b.bookingStatus = 'CONFIRMED'
        GROUP BY YEAR(b.startDate), MONTH(b.startDate)
        ORDER BY YEAR(b.startDate), MONTH(b.startDate)
    """)
    List<Object[]> monthlyRevenueByVendor(@Param("vendorEmail") String vendorEmail);

    @Query("""
        SELECT YEAR(b.startDate), COALESCE(SUM(b.totalPrice), 0)
        FROM Booking b
        WHERE b.vehicle.vendor.email = :vendorEmail AND b.bookingStatus = 'CONFIRMED'
        GROUP BY YEAR(b.startDate)
        ORDER BY YEAR(b.startDate)
    """)
    List<Object[]> yearlyRevenueByVendor(@Param("vendorEmail") String vendorEmail);

    // ── Admin customer queries ────────────────────────────────────────────────

    long countByCustomer_CustomerId(Integer customerId);

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.customer.customerId = :customerId AND b.bookingStatus = 'CONFIRMED'")
    java.math.BigDecimal sumSpentByCustomer(@Param("customerId") Integer customerId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.customer.customerId = :customerId AND b.endDate >= CURRENT_DATE AND b.bookingStatus != 'CANCELLED'")
    long countActiveBookingsByCustomer(@Param("customerId") Integer customerId);

    List<Booking> findByCustomer_CustomerIdOrderByCreatedAtDesc(Integer customerId);

    // ── Admin vendor stats (by vendorId) ─────────────────────────────────────

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.vehicle.vendor.vendorId = :vendorId")
    long countByVendorId(@Param("vendorId") Integer vendorId);

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.vehicle.vendor.vendorId = :vendorId AND b.bookingStatus = 'CONFIRMED'")
    java.math.BigDecimal sumRevenueByVendorId(@Param("vendorId") Integer vendorId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.vehicle.vendor.vendorId = :vendorId AND b.bookingStatus = 'CONFIRMED'")
    long countActiveByVendorId(@Param("vendorId") Integer vendorId);

    // ── Admin all-bookings filtered query ────────────────────────────────────

    @Query("""
        SELECT b FROM Booking b
        WHERE (:vendorId   IS NULL OR b.vehicle.vendor.vendorId = :vendorId)
          AND (:customerId IS NULL OR b.customer.customerId = :customerId)
          AND (:dateFrom   IS NULL OR b.startDate >= :dateFrom)
          AND (:dateTo     IS NULL OR b.endDate   <= :dateTo)
          AND (:status     IS NULL OR b.bookingStatus = :status)
        ORDER BY b.createdAt DESC
    """)
    List<Booking> findAllForAdmin(
            @Param("vendorId")   Integer vendorId,
            @Param("customerId") Integer customerId,
            @Param("dateFrom")   LocalDate dateFrom,
            @Param("dateTo")     LocalDate dateTo,
            @Param("status")     Booking.BookingStatus status
    );

    // ── Admin global stats ────────────────────────────────────────────────────

    @Query("SELECT COUNT(b) FROM Booking b")
    long countAllBookings();

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.bookingStatus = 'CONFIRMED'")
    java.math.BigDecimal sumTotalRevenue();

    @Query("""
        SELECT MONTH(b.startDate), COALESCE(SUM(b.totalPrice), 0)
        FROM Booking b
        WHERE YEAR(b.startDate) = :year AND b.bookingStatus = 'CONFIRMED'
        GROUP BY MONTH(b.startDate)
        ORDER BY MONTH(b.startDate)
    """)
    List<Object[]> monthlyRevenueGlobal(@Param("year") int year);

    @Query("""
        SELECT YEAR(b.startDate), COALESCE(SUM(b.totalPrice), 0)
        FROM Booking b
        WHERE b.bookingStatus = 'CONFIRMED'
        GROUP BY YEAR(b.startDate)
        ORDER BY YEAR(b.startDate)
    """)
    List<Object[]> yearlyRevenueGlobal();
}
