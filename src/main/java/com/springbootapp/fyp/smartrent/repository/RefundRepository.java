package com.springbootapp.fyp.smartrent.repository;

import com.springbootapp.fyp.smartrent.model.Booking;
import com.springbootapp.fyp.smartrent.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Integer> {
    Optional<Refund> findByBooking(Booking booking);
    Optional<Refund> findByBooking_BookingId(Integer bookingId);
}