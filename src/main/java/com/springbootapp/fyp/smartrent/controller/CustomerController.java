package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.dto.BookingResponseDto;
import com.springbootapp.fyp.smartrent.dto.CustomerProfileDto;
import com.springbootapp.fyp.smartrent.model.Booking;
import com.springbootapp.fyp.smartrent.model.Customer;
import com.springbootapp.fyp.smartrent.model.Vehicle;
import com.springbootapp.fyp.smartrent.repository.BookingRepository;
import com.springbootapp.fyp.smartrent.repository.CustomerRepository;
import com.springbootapp.fyp.smartrent.repository.VehicleImageRepository;
import com.springbootapp.fyp.smartrent.repository.VehicleRepository;
import com.springbootapp.fyp.smartrent.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private VehicleImageRepository vehicleImageRepository;

    // GET /api/customer/profile
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomerProfileDto dto = new CustomerProfileDto();
        dto.setCustomerId(customer.getCustomerId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setPhoneNumber(customer.getPhoneNumber());
        dto.setDob(customer.getDob());
        dto.setAddress(customer.getAddress());
        dto.setProfileImage(customer.getProfileImage());
        dto.setLicenseImage(customer.getLicenseImage());
        dto.setLicenseNo(customer.getLicenseNo());

        return ResponseEntity.ok(dto);
    }

    // PUT /api/customer/profile
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody CustomerProfileDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.getFirstName() != null)   customer.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)    customer.setLastName(dto.getLastName());
        if (dto.getPhoneNumber() != null) customer.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getDob() != null)         customer.setDob(dto.getDob());
        if (dto.getAddress() != null)     customer.setAddress(dto.getAddress());

        customerRepository.save(customer);
        return ResponseEntity.ok("Profile updated successfully");
    }

    // POST /api/customer/profile/photo
    @PostMapping("/profile/photo")
    public ResponseEntity<?> uploadProfilePhoto(@RequestParam("file") MultipartFile file) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        try {
            if (customer.getProfileImage() != null) {
                fileStorageService.deleteFile(customer.getProfileImage());
            }
            String path = fileStorageService.storeProfileImage(file);
            customer.setProfileImage(path);
            customerRepository.save(customer);
            return ResponseEntity.ok(java.util.Map.of("profileImage", path));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to upload photo");
        }
    }

    // POST /api/customer/profile/license-image
    @PostMapping("/profile/license-image")
    public ResponseEntity<?> uploadLicenseImage(@RequestParam("file") MultipartFile file) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        try {
            if (customer.getLicenseImage() != null) {
                fileStorageService.deleteFile(customer.getLicenseImage());
            }
            String path = fileStorageService.storeLicenseImage(file);
            customer.setLicenseImage(path);
            customerRepository.save(customer);
            return ResponseEntity.ok(java.util.Map.of("licenseImage", path));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to upload license image");
        }
    }

    // PUT /api/customer/password
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        String currentPassword = body.get("currentPassword");
        String newPassword     = body.get("newPassword");

        if (currentPassword == null || newPassword == null || newPassword.length() < 6)
            return ResponseEntity.badRequest().body("Invalid password data.");

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, customer.getPassword()))
            return ResponseEntity.badRequest().body("Current password is incorrect.");

        customer.setPassword(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);
        return ResponseEntity.ok("Password updated successfully.");
    }

    // GET /api/customer/bookings
    @GetMapping("/bookings")
    public ResponseEntity<?> getMyBookings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Booking> bookings = bookingRepository.findByCustomer_CustomerIdOrderByCreatedAtDesc(customer.getCustomerId());
        List<BookingResponseDto> dtos = bookings.stream().map(b -> {
            BookingResponseDto dto = BookingResponseDto.from(b);
            if (b.getVehicle() != null) {
                List<com.springbootapp.fyp.smartrent.model.VehicleImage> images =
                        vehicleImageRepository.findByVehicle_VehicleId(b.getVehicle().getVehicleId());
                if (!images.isEmpty()) dto.setVehicleImageUrl(images.get(0).getImageUrl());
            }
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // POST /api/customer/bookings
    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> body) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Integer vehicleId = (Integer) body.get("vehicleId");
        String startDateStr  = (String) body.get("startDate");
        String endDateStr    = (String) body.get("endDate");
        String pickupLoc     = (String) body.get("pickupLocation");
        String dropoffLoc    = (String) body.get("dropoffLocation");
        String paymentMode   = (String) body.get("paymentMode"); // "pay" or "book"

        if (vehicleId == null || startDateStr == null || endDateStr == null)
            return ResponseEntity.badRequest().body("Missing required fields.");

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElse(null);
        if (vehicle == null)
            return ResponseEntity.badRequest().body("Vehicle not found.");

        // datetime-local sends "2026-04-15T14:00" — strip the time part if present
        LocalDate startDate = LocalDate.parse(startDateStr.length() > 10 ? startDateStr.substring(0, 10) : startDateStr);
        LocalDate endDate   = LocalDate.parse(endDateStr.length()   > 10 ? endDateStr.substring(0, 10)   : endDateStr);
        if (!endDate.isAfter(startDate))
            return ResponseEntity.badRequest().body("End date must be after start date.");

        long days = Math.max(1, ChronoUnit.DAYS.between(startDate, endDate));
        BigDecimal price    = vehicle.getRentalPrice().multiply(BigDecimal.valueOf(days));
        BigDecimal fee      = price.multiply(BigDecimal.valueOf(0.05)).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal total    = price.add(fee);

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setVehicle(vehicle);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setTotalPrice(total);
        booking.setPickupLocation(pickupLoc);
        booking.setDropoffLocation(dropoffLoc);
        booking.setBookingStatus(Booking.BookingStatus.PENDING);
        booking.setPaymentStatus("pay".equals(paymentMode)
                ? Booking.PaymentStatus.PAID : Booking.PaymentStatus.UNPAID);

        bookingRepository.save(booking);

        BookingResponseDto dto = BookingResponseDto.from(booking);
        List<com.springbootapp.fyp.smartrent.model.VehicleImage> images =
                vehicleImageRepository.findByVehicle_VehicleId(vehicleId);
        if (!images.isEmpty()) dto.setVehicleImageUrl(images.get(0).getImageUrl());

        return ResponseEntity.status(201).body(dto);
    }

    // PUT /api/customer/bookings/{id}/cancel
    @PutMapping("/bookings/{id}/cancel")
    public ResponseEntity<?> cancelMyBooking(@PathVariable Integer id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return bookingRepository.findById(id).map(b -> {
            if (!b.getCustomer().getCustomerId().equals(customer.getCustomerId()))
                return ResponseEntity.status(403).body("Not your booking.");
            if (b.getBookingStatus() == Booking.BookingStatus.CANCELLED)
                return ResponseEntity.badRequest().body("Already cancelled.");
            b.setBookingStatus(Booking.BookingStatus.CANCELLED);
            bookingRepository.save(b);
            return ResponseEntity.ok("Booking cancelled.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/customer/bookings/{id}/pay
    @PutMapping("/bookings/{id}/pay")
    public ResponseEntity<?> payMyBooking(@PathVariable Integer id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return bookingRepository.findById(id).map(b -> {
            if (!b.getCustomer().getCustomerId().equals(customer.getCustomerId()))
                return ResponseEntity.status(403).body("Not your booking.");
            if (b.getBookingStatus() == Booking.BookingStatus.CANCELLED)
                return ResponseEntity.badRequest().body("Cannot pay for a cancelled booking.");
            if (b.getPaymentStatus() == Booking.PaymentStatus.PAID)
                return ResponseEntity.badRequest().body("Already paid.");
            b.setPaymentStatus(Booking.PaymentStatus.PAID);
            bookingRepository.save(b);
            return ResponseEntity.ok("Payment confirmed.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/customer/account
    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        customerRepository.delete(customer);
        return ResponseEntity.ok("Account deleted successfully");
    }
}
