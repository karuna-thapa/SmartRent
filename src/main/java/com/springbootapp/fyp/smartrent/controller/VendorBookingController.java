package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.dto.BookingResponseDto;
import com.springbootapp.fyp.smartrent.model.Booking;
import com.springbootapp.fyp.smartrent.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vendor/bookings")
@CrossOrigin(origins = "*")
public class VendorBookingController {

    @Autowired
    private BookingRepository bookingRepository;

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
}
