package com.springbootapp.fyp.smartrent.repository;

import com.springbootapp.fyp.smartrent.model.Booking;
import com.springbootapp.fyp.smartrent.model.CancellationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CancellationRequestRepository extends JpaRepository<CancellationRequest, Integer> {
    Optional<CancellationRequest> findByBooking(Booking booking);
    Optional<CancellationRequest> findByBooking_BookingId(Integer bookingId);
    Optional<CancellationRequest> findByBooking_BookingIdAndStatus(Integer bookingId, CancellationRequest.RequestStatus status);
    List<CancellationRequest> findByStatus(CancellationRequest.RequestStatus status);
}