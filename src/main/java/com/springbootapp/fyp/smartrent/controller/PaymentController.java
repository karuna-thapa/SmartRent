package com.springbootapp.fyp.smartrent.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springbootapp.fyp.smartrent.dto.PaymentResponseDto;
import com.springbootapp.fyp.smartrent.model.Booking;
import com.springbootapp.fyp.smartrent.model.Payment;
import com.springbootapp.fyp.smartrent.repository.BookingRepository;
import com.springbootapp.fyp.smartrent.repository.PaymentRepository;
import com.springbootapp.fyp.smartrent.service.EsewaPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class PaymentController {

    @Autowired private BookingRepository    bookingRepository;
    @Autowired private PaymentRepository    paymentRepository;
    @Autowired private EsewaPaymentService  esewaPaymentService;

    // ── Initiate eSewa payment ────────────────────────────────────────────────
    @GetMapping("/api/payment/esewa/initiate/{bookingId}")
    @ResponseBody
    public ResponseEntity<?> initiateEsewa(@PathVariable Integer bookingId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null)
            return ResponseEntity.notFound().build();
        if (!booking.getCustomer().getEmail().equals(email))
            return ResponseEntity.status(403).body("Not your booking.");
        if (booking.getBookingStatus() != Booking.BookingStatus.CONFIRMED)
            return ResponseEntity.badRequest().body("Booking not yet approved by vendor.");
        if (booking.getPaymentStatus() == Booking.PaymentStatus.PAID)
            return ResponseEntity.badRequest().body("Already paid.");

        Map<String, String> fields = esewaPaymentService.buildPayload(booking);
        Map<String, Object> response = new HashMap<>();
        response.put("esewaUrl", esewaPaymentService.getEsewaUrl());
        response.put("fields",   fields);
        return ResponseEntity.ok(response);
    }

    // ── eSewa success callback (GET, public — called by eSewa redirect) ───────
    @GetMapping("/payment/success")
    public String paymentSuccess(@RequestParam(required = false) String data) {
        if (data == null) return "redirect:/profile?payment=failed";
        try {
            String decoded = new String(Base64.getDecoder().decode(data));
            Map<String, String> cb = new ObjectMapper()
                    .readValue(decoded, new TypeReference<>() {});

            if (!esewaPaymentService.verifyCallback(cb))
                return "redirect:/profile?payment=failed&reason=signature";

            if (!"COMPLETE".equals(cb.get("status")))
                return "redirect:/profile?payment=failed&reason=incomplete";

            String uuid            = cb.get("transaction_uuid");
            String transactionCode = cb.get("transaction_code");
            String totalAmountStr  = cb.get("total_amount");

            // uuid format: bookingId-timestamp
            Integer bookingId = Integer.parseInt(uuid.split("-")[0]);
            Booking booking   = bookingRepository.findById(bookingId).orElse(null);
            if (booking == null) return "redirect:/profile?payment=failed&reason=not_found";

            // Idempotency: already processed
            if (booking.getPaymentStatus() == Booking.PaymentStatus.PAID)
                return "redirect:/profile?payment=success";

            // Revenue split
            BigDecimal total        = new BigDecimal(totalAmountStr);
            BigDecimal adminAmount  = total.multiply(BigDecimal.valueOf(0.10)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal vendorAmount = total.subtract(adminAmount);

            // Persist payment record
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(total);
            payment.setVendorAmount(vendorAmount);
            payment.setAdminAmount(adminAmount);
            payment.setPaymentMethod(Payment.PaymentMethod.esewa);
            payment.setPaymentStatus(Payment.PaymentStatus.paid);
            payment.setTransactionId(transactionCode);
            payment.setTransactionUuid(uuid);
            paymentRepository.save(payment);

            // Mark booking paid
            booking.setPaymentStatus(Booking.PaymentStatus.PAID);
            bookingRepository.save(booking);

            return "redirect:/profile?payment=success";
        } catch (Exception e) {
            return "redirect:/profile?payment=failed&reason=error";
        }
    }

    // ── eSewa failure callback ────────────────────────────────────────────────
    @GetMapping("/payment/failure")
    public String paymentFailure() {
        return "redirect:/profile?payment=failed";
    }

    // ── Admin: list all payments ──────────────────────────────────────────────
    @GetMapping("/api/admin/payments")
    @ResponseBody
    public ResponseEntity<List<PaymentResponseDto>> adminPayments() {
        List<PaymentResponseDto> list = paymentRepository.findAllPaid()
                .stream().map(PaymentResponseDto::from).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // ── Admin: payment summary stats ─────────────────────────────────────────
    @GetMapping("/api/admin/payments/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> adminPaymentStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCollected",   paymentRepository.sumTotalCollected());
        stats.put("adminEarnings",    paymentRepository.sumAdminRevenue());
        stats.put("totalTransactions", paymentRepository.countPaid());
        return ResponseEntity.ok(stats);
    }

    // ── Vendor: list their payments ───────────────────────────────────────────
    @GetMapping("/api/vendor/payments")
    @ResponseBody
    public ResponseEntity<List<PaymentResponseDto>> vendorPayments(Principal principal) {
        List<PaymentResponseDto> list = paymentRepository.findPaidByVendorEmail(principal.getName())
                .stream().map(PaymentResponseDto::from).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // ── Vendor: payment summary stats ────────────────────────────────────────
    @GetMapping("/api/vendor/payments/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> vendorPaymentStats(Principal principal) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEarned",       paymentRepository.sumVendorRevenue(principal.getName()));
        stats.put("totalTransactions", paymentRepository.countPaidByVendorEmail(principal.getName()));
        return ResponseEntity.ok(stats);
    }
}
