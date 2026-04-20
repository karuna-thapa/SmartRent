package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.dto.BookingResponseDto;
import com.springbootapp.fyp.smartrent.model.Booking;
import com.springbootapp.fyp.smartrent.model.CancellationRequest;
import com.springbootapp.fyp.smartrent.model.Refund;
import com.springbootapp.fyp.smartrent.repository.BookingRepository;
import com.springbootapp.fyp.smartrent.repository.CancellationRequestRepository;
import com.springbootapp.fyp.smartrent.repository.RefundRepository;
import com.springbootapp.fyp.smartrent.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private EmailService emailService;

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
         * Returns recent actionable items for vendor:
         * 1) Pending booking requests from customers
         * 2) Pending customer-initiated refund requests
     */
    @GetMapping("/notifications")
        public ResponseEntity<List<Map<String, Object>>> getNotifications(Principal principal) {
        List<Map<String, Object>> result = new ArrayList<>();

        List<Booking> bookingRequests = bookingRepository
                .findVendorBookings(principal.getName(), null, null, null, null)
                .stream()
            .filter(b -> b.getBookingStatus() == Booking.BookingStatus.PENDING)
            .collect(Collectors.toList());

        for (Booking b : bookingRequests) {
            Map<String, Object> m = new HashMap<>();
            m.put("type", "BOOKING_REQUEST");
            m.put("bookingId", b.getBookingId());
            m.put("customerName", b.getCustomer() != null
                ? (b.getCustomer().getFirstName() + " " + b.getCustomer().getLastName()).trim()
                : null);
            m.put("vehicleName", b.getVehicle() != null ? b.getVehicle().getVehicleName() : null);
            m.put("bookingStatus", b.getBookingStatus() != null ? b.getBookingStatus().name() : null);
            m.put("createdAt", b.getCreatedAt());
            result.add(m);
        }

        List<Refund> customerRefundRequests = refundRepository.findPendingByVendorAndInitiator(
            principal.getName(),
            "CUSTOMER",
            Refund.RefundStatus.PENDING
        );

        for (Refund r : customerRefundRequests) {
            Booking b = r.getBooking();
            Map<String, Object> m = new HashMap<>();
            m.put("type", "REFUND_REQUEST");
            m.put("refundId", r.getRefundId());
            m.put("bookingId", b != null ? b.getBookingId() : null);
            m.put("customerName", b != null && b.getCustomer() != null
                ? (b.getCustomer().getFirstName() + " " + b.getCustomer().getLastName()).trim()
                : null);
            m.put("vehicleName", b != null && b.getVehicle() != null ? b.getVehicle().getVehicleName() : null);
            m.put("refundAmount", r.getRefundAmount());
            m.put("refundStatus", r.getRefundStatus() != null ? r.getRefundStatus().name() : null);
            m.put("createdAt", r.getRefundTimestamp());
            result.add(m);
        }

        result = result.stream()
            .sorted(Comparator.<Map<String, Object>, LocalDateTime>comparing(
                m -> (LocalDateTime) m.get("createdAt"),
                Comparator.nullsLast(LocalDateTime::compareTo)
            ).reversed())
            .limit(30)
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

        try {
            emailService.sendVendorCancellationRequestSubmitted(principal.getName(), booking.getBookingId());
        } catch (Exception ignored) {
        }

        return ResponseEntity.ok("Cancellation request sent to admin.");
    }

    /**
     * GET /api/vendor/bookings/cancel-requests
     */
    @GetMapping("/cancel-requests")
    public ResponseEntity<?> getMyCancellationRequests(Principal principal) {
        List<Map<String, Object>> result = cancellationRequestRepository
                .findByBooking_Vehicle_Vendor_EmailOrderByRequestedAtDesc(principal.getName())
                .stream()
                .map(r -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("requestId", r.getRequestId());
                    m.put("bookingId", r.getBooking() != null ? r.getBooking().getBookingId() : null);
                    m.put("vehicleName", r.getBooking() != null && r.getBooking().getVehicle() != null
                            ? r.getBooking().getVehicle().getVehicleName() : null);
                    m.put("customerName", r.getBooking() != null && r.getBooking().getCustomer() != null
                            ? (r.getBooking().getCustomer().getFirstName() + " " + r.getBooking().getCustomer().getLastName()).trim()
                            : null);
                    m.put("reason", r.getReason());
                    m.put("status", r.getStatus() != null ? r.getStatus().name() : null);
                    m.put("requestedAt", r.getRequestedAt());
                    m.put("processedAt", r.getProcessedAt());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
