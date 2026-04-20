package com.springbootapp.fyp.smartrent.service;

import com.springbootapp.fyp.smartrent.model.Booking;
import com.springbootapp.fyp.smartrent.model.Refund;
import com.springbootapp.fyp.smartrent.model.CancellationRequest;
import com.springbootapp.fyp.smartrent.repository.BookingRepository;
import com.springbootapp.fyp.smartrent.repository.RefundRepository;
import com.springbootapp.fyp.smartrent.repository.CancellationRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private CancellationRequestRepository cancellationRequestRepository;

    @Transactional
    public String cancelBooking(Integer id, Integer customerId) {
        System.out.println("Processing cancellation for Booking ID: " + id + ", Customer ID: " + customerId);
        
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));

        System.out.println("Booking found. Owner ID: " + booking.getCustomer().getCustomerId());

        if (!booking.getCustomer().getCustomerId().equals(customerId)) {
            throw new SecurityException("Access denied: Not your booking. (Expected " + booking.getCustomer().getCustomerId() + ", got " + customerId + ")");
        }

        if (booking.getBookingStatus() == Booking.BookingStatus.CANCELLED || 
            booking.getBookingStatus() == Booking.BookingStatus.REFUNDED) {
            throw new IllegalStateException("Booking is already cancelled or refunded.");
        }

        // If there is an active vendor cancellation request, avoid conflicting actions
        // between customer cancellation and admin approval flow.
        if (cancellationRequestRepository
            .findByBooking_BookingIdAndStatus(id, CancellationRequest.RequestStatus.PENDING)
            .isPresent()) {
            throw new IllegalStateException("A vendor cancellation request is already pending for this booking. Please wait for admin approval.");
        }

        if (booking.getPaymentStatus() == Booking.PaymentStatus.PAID) {
            processRefund(booking);
            booking.setBookingStatus(Booking.BookingStatus.REFUNDED);
            booking.setPaymentStatus(Booking.PaymentStatus.REFUNDED);
        } else {
            booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
        }

        bookingRepository.save(booking);
        return "Booking cancelled. Refund " + (booking.getBookingStatus() == Booking.BookingStatus.REFUNDED ? "initiated" : "not applicable") + ".";
    }

    private void processRefund(Booking booking) {
        if (refundRepository.findByBooking_BookingId(booking.getBookingId()).isPresent()) {
            throw new IllegalStateException("Refund has already been initiated for this booking.");
        }

        if (booking.getTotalPrice() == null || booking.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Cannot process refund: booking total price is missing or invalid.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime bookingCreatedAt = booking.getCreatedAt() != null ? booking.getCreatedAt() : now;
        long hoursSinceBooking = ChronoUnit.HOURS.between(bookingCreatedAt, now);

        BigDecimal refundPercentage = (hoursSinceBooking <= 24) 
                ? new BigDecimal("0.90") 
                : new BigDecimal("0.80");

        BigDecimal refundAmount = booking.getTotalPrice()
                .multiply(refundPercentage)
                .setScale(2, RoundingMode.HALF_UP);

        Refund refund = new Refund();
        refund.setBooking(booking);
        refund.setRefundAmount(refundAmount);
        refund.setRefundPercentage(refundPercentage.multiply(new BigDecimal("100")));
        refund.setRefundReason("Customer cancelled booking.");
        refund.setInitiatedBy("CUSTOMER");
        refund.setRefundStatus(Refund.RefundStatus.PENDING);
        
        refundRepository.save(refund);
    }
}
