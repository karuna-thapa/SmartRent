package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.dto.BookingResponseDto;
import com.springbootapp.fyp.smartrent.model.Booking;
import com.springbootapp.fyp.smartrent.model.CancellationRequest;
import com.springbootapp.fyp.smartrent.repository.BookingRepository;
import com.springbootapp.fyp.smartrent.repository.CancellationRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vendor/bookings")
@CrossOrigin(origins = "*")
public class VendorBookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CancellationRequestRepository cancellationRequestRepository;

    /**
     * GET /api/vendor/bookings
     * Optional filters: vehicleId, dateFrom (yyyy-MM-dd), dateTo (yyyy-MM-dd), status (PENDING|CONFIRMED|CANCELLED)
     */
    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getBookings(
            Principal principal,
            @RequestParam(required = false) Integer vehicleId,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String status) {

        LocalDate from   = dateFrom != null && !dateFrom.isBlank() ? LocalDate.parse(dateFrom) : null;
        LocalDate to     = dateTo   != null && !dateTo.isBlank()   ? LocalDate.parse(dateTo)   : null;
        Booking.BookingStatus bs = null;
        if (status != null && !status.isBlank()) {
            try { bs = Booking.BookingStatus.valueOf(status.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }

        List<BookingResponseDto> result = bookingRepository
                .findVendorBookings(principal.getName(), vehicleId, from, to, bs)
                .stream()
                .map(BookingResponseDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/vendor/notifications
     * Returns the 30 most recent bookings for this vendor's vehicles (newest first).
     * PENDING = requires action; CONFIRMED / CANCELLED = resolved.
     */
    @GetMapping("/notifications")
    public ResponseEntity<List<BookingResponseDto>> getNotifications(Principal principal) {
        List<BookingResponseDto> result = bookingRepository
                .findVendorBookings(principal.getName(), null, null, null, null)
                .stream()
                .limit(30)
                .map(BookingResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * PUT /api/vendor/bookings/{id}/approve
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveBooking(@PathVariable Integer id, Principal principal) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getVehicle().getVendor().getEmail().equals(principal.getName())) {
            return ResponseEntity.status(403).body("Not authorized.");
        }
        booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        return ResponseEntity.ok(BookingResponseDto.from(booking));
    }

    /**
     * PUT /api/vendor/bookings/{id}/reject
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectBooking(@PathVariable Integer id, Principal principal) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getVehicle().getVendor().getEmail().equals(principal.getName())) {
            return ResponseEntity.status(403).body("Not authorized.");
        }

        if (booking.getPaymentStatus() == Booking.PaymentStatus.PAID) {
            return ResponseEntity.badRequest().body("Client has already paid. You must request cancellation from admin.");
        }

        booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        return ResponseEntity.ok(BookingResponseDto.from(booking));
    }

    /**
     * POST /api/vendor/bookings/{id}/cancel-request
     */
    @PostMapping("/{id}/cancel-request")
    public ResponseEntity<?> requestCancellation(@PathVariable Integer id, @RequestBody Map<String, String> body, Principal principal) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getVehicle().getVendor().getEmail().equals(principal.getName())) {
            return ResponseEntity.status(403).body("Not authorized.");
        }

        if (booking.getPaymentStatus() != Booking.PaymentStatus.PAID) {
            return ResponseEntity.badRequest().body("Cancellation request only needed for paid bookings. Use reject for unpaid ones.");
        }

        if (cancellationRequestRepository.findByBooking(booking).isPresent()) {
            return ResponseEntity.badRequest().body("Cancellation request already exists for this booking.");
        }

        CancellationRequest request = new CancellationRequest();
        request.setBooking(booking);
        request.setReason(body.get("reason"));
        request.setStatus(CancellationRequest.RequestStatus.PENDING);
        cancellationRequestRepository.save(request);

        return ResponseEntity.ok("Cancellation request sent to admin.");
    }
}
