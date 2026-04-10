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

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.vehicle.vendor.email = :vendorEmail AND b.bookingStatus = 'CONFIRMED'")
    long countActiveByVendor(@Param("vendorEmail") String vendorEmail);
}
