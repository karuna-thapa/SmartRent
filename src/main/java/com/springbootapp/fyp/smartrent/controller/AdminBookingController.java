package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.model.*;
import com.springbootapp.fyp.smartrent.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/bookings")
@CrossOrigin(origins = "*")
public class AdminBookingController {

    @Autowired private BookingRepository bookingRepository;
    @Autowired private CancellationRequestRepository cancellationRequestRepository;
    @Autowired private RefundRepository refundRepository;

    // GET /api/admin/bookings — filtered list
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllBookings(
            @RequestParam(required = false) Integer vendorId,
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String status
    ) {
        Booking.BookingStatus bookingStatus = null;
        if (status != null && !status.isBlank()) {
            try { bookingStatus = Booking.BookingStatus.valueOf(status); } catch (IllegalArgumentException ignored) {}
        }

        List<Booking> bookings = bookingRepository.findAllForAdmin(
                vendorId, customerId, dateFrom, dateTo, bookingStatus);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Booking b : bookings) {
            result.add(buildSummary(b));
        }
        return ResponseEntity.ok(result);
    }

    // GET /api/admin/bookings/{id} — full detail
    @GetMapping("/{id}")
    public ResponseEntity<?> getBooking(@PathVariable Integer id) {
        return bookingRepository.findById(id)
                .map(b -> ResponseEntity.ok(buildDetail(b)))
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/admin/bookings/{id}/cancel
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Integer id) {
        return bookingRepository.findById(id).map(b -> {
            if (b.getBookingStatus() == Booking.BookingStatus.CANCELLED) {
                return ResponseEntity.badRequest().body("Booking is already cancelled.");
            }
            b.setBookingStatus(Booking.BookingStatus.CANCELLED);
            bookingRepository.save(b);
            return ResponseEntity.ok("Booking cancelled.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/admin/bookings/{id}/confirm
    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmBooking(@PathVariable Integer id) {
        return bookingRepository.findById(id).map(b -> {
            if (b.getBookingStatus() != Booking.BookingStatus.PENDING) {
                return ResponseEntity.badRequest().body("Only PENDING bookings can be confirmed.");
            }
            b.setBookingStatus(Booking.BookingStatus.CONFIRMED);
            bookingRepository.save(b);
            return ResponseEntity.ok("Booking confirmed.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // GET /api/admin/bookings/cancellation-requests
    @GetMapping("/cancellation-requests")
    public ResponseEntity<?> getCancellationRequests() {
        List<CancellationRequest> requests = cancellationRequestRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (CancellationRequest r : requests) {
            Map<String, Object> m = new HashMap<>();
            m.put("requestId", r.getRequestId());
            m.put("bookingId", r.getBooking().getBookingId());
            m.put("vendorName", r.getBooking().getVehicle().getVendor().getVendorName());
            m.put("reason", r.getReason());
            m.put("status", r.getStatus());
            m.put("requestedAt", r.getRequestedAt());
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    // PUT /api/admin/bookings/cancellation-requests/{id}/approve
    @PutMapping("/cancellation-requests/{id}/approve")
    public ResponseEntity<?> approveCancellation(@PathVariable Integer id) {
        return cancellationRequestRepository.findById(id).map(r -> {
            if (r.getStatus() != CancellationRequest.RequestStatus.PENDING) {
                return ResponseEntity.badRequest().body("Request already processed.");
            }

            Booking b = r.getBooking();
            
            // 100% Refund for Vendor-initiated cancellation approved by Admin
            Refund refund = new Refund();
            refund.setBooking(b);
            refund.setRefundAmount(b.getTotalPrice());
            refund.setRefundPercentage(new BigDecimal("100.00"));
            refund.setRefundReason("Vendor cancellation approved: " + r.getReason());
            refund.setInitiatedBy("VENDOR");
            refund.setRefundStatus(Refund.RefundStatus.PENDING);
            refundRepository.save(refund);

            b.setBookingStatus(Booking.BookingStatus.REFUNDED);
            b.setPaymentStatus(Booking.PaymentStatus.REFUNDED);
            bookingRepository.save(b);

            r.setStatus(CancellationRequest.RequestStatus.APPROVED);
            r.setProcessedAt(LocalDateTime.now());
            cancellationRequestRepository.save(r);

            return ResponseEntity.ok("Cancellation approved and 100% refund initiated.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/admin/bookings/cancellation-requests/{id}/reject
    @PutMapping("/cancellation-requests/{id}/reject")
    public ResponseEntity<?> rejectCancellation(@PathVariable Integer id) {
        return cancellationRequestRepository.findById(id).map(r -> {
            if (r.getStatus() != CancellationRequest.RequestStatus.PENDING) {
                return ResponseEntity.badRequest().body("Request already processed.");
            }
            r.setStatus(CancellationRequest.RequestStatus.REJECTED);
            r.setProcessedAt(LocalDateTime.now());
            cancellationRequestRepository.save(r);
            return ResponseEntity.ok("Cancellation request rejected.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<String, Object> buildSummary(Booking b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("bookingId",    b.getBookingId());
        m.put("customerId",   b.getCustomer() != null ? b.getCustomer().getCustomerId() : null);
        m.put("customerName", b.getCustomer() != null
                ? b.getCustomer().getFirstName() + " " + b.getCustomer().getLastName() : "—");
        m.put("customerEmail", b.getCustomer() != null ? b.getCustomer().getEmail() : null);
        m.put("vehicleId",    b.getVehicle() != null ? b.getVehicle().getVehicleId() : null);
        m.put("vehicleName",  b.getVehicle() != null ? b.getVehicle().getVehicleName() : "—");
        m.put("vehicleNo",    b.getVehicle() != null ? b.getVehicle().getVehicleNo() : "—");
        m.put("vendorId",     b.getVehicle() != null && b.getVehicle().getVendor() != null
                ? b.getVehicle().getVendor().getVendorId() : null);
        m.put("vendorName",   b.getVehicle() != null && b.getVehicle().getVendor() != null
                ? b.getVehicle().getVendor().getVendorName() : "—");
        m.put("startDate",    b.getStartDate() != null ? b.getStartDate().toString() : null);
        m.put("endDate",      b.getEndDate()   != null ? b.getEndDate().toString()   : null);
        m.put("totalPrice",   b.getTotalPrice());
        m.put("status",       b.getBookingStatus() != null ? b.getBookingStatus().name() : null);
        m.put("createdAt",    b.getCreatedAt() != null ? b.getCreatedAt().toString() : null);
        return m;
    }

    private Map<String, Object> buildDetail(Booking b) {
        Map<String, Object> m = buildSummary(b);
        if (b.getCustomer() != null) {
            m.put("customerPhone",   b.getCustomer().getPhoneNumber());
            m.put("customerLicense", b.getCustomer().getLicenseNo());
        }
        if (b.getVehicle() != null) {
            m.put("brandName",    b.getVehicle().getBrand() != null ? b.getVehicle().getBrand().getBrandName() : "—");
            m.put("categoryName", b.getVehicle().getVehicleCategory() != null
                    ? b.getVehicle().getVehicleCategory().getVehicleCategoryName() : "—");
            m.put("rentalPrice",  b.getVehicle().getRentalPrice());
            m.put("imageUrl",     null); // image handling via separate endpoint if needed
        }
        if (b.getVehicle() != null && b.getVehicle().getVendor() != null) {
            m.put("vendorEmail",   b.getVehicle().getVendor().getEmail());
            m.put("vendorPhone",   b.getVehicle().getVendor().getPhoneNumber());
            m.put("companyName",   b.getVehicle().getVendor().getCompanyName());
        }
        m.put("durationType", b.getRentalDurationType() != null ? b.getRentalDurationType().name() : null);
        m.put("updatedAt",    b.getUpdatedAt() != null ? b.getUpdatedAt().toString() : null);
        return m;
    }
}
