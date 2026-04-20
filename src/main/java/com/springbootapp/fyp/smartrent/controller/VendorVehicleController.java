package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.dto.ReviewResponseDto;
import com.springbootapp.fyp.smartrent.dto.VehicleRequestDto;
import com.springbootapp.fyp.smartrent.dto.VehicleResponseDto;
import com.springbootapp.fyp.smartrent.dto.VendorResponseDto;
import com.springbootapp.fyp.smartrent.model.Vendor;
import com.springbootapp.fyp.smartrent.repository.BookingRepository;
import com.springbootapp.fyp.smartrent.repository.PaymentRepository;
import com.springbootapp.fyp.smartrent.repository.RefundRepository;
import com.springbootapp.fyp.smartrent.repository.VendorRepository;
import com.springbootapp.fyp.smartrent.repository.VehicleRepository;
import com.springbootapp.fyp.smartrent.service.ReviewService;
import com.springbootapp.fyp.smartrent.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class VendorVehicleController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // GET /api/vendor/me
    @GetMapping("/api/vendor/me")
    public ResponseEntity<?> getMe(Principal principal) {
        Vendor vendor = vendorRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        return ResponseEntity.ok(VendorResponseDto.from(vendor));
    }

    // GET /api/vendor/vehicles?brandId=&categoryId=&status=
    @GetMapping("/api/vendor/vehicles")
    public ResponseEntity<List<VehicleResponseDto>> getMyVehicles(
            Principal principal,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(
                vehicleService.getVendorVehicles(principal.getName(), brandId, categoryId, status)
        );
    }

    // POST /api/vendor/vehicles  (multipart/form-data)
    @PostMapping(value = "/api/vendor/vehicles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addVehicle(
            Principal principal,
            @RequestParam("vehicleName")        String vehicleName,
            @RequestParam("brandId")            Integer brandId,
            @RequestParam("vehicleCategoryId")  Integer vehicleCategoryId,
            @RequestParam("vehicleNo")          String vehicleNo,
            @RequestParam("seatsCapacity")      Integer seatsCapacity,
            @RequestParam("rentalPrice")        BigDecimal rentalPrice,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "images",      required = false) List<MultipartFile> images) {

        try {
            VehicleRequestDto dto = new VehicleRequestDto();
            dto.setVehicleName(vehicleName);
            dto.setBrandId(brandId);
            dto.setVehicleCategoryId(vehicleCategoryId);
            dto.setVehicleNo(vehicleNo);
            dto.setSeatsCapacity(seatsCapacity);
            dto.setRentalPrice(rentalPrice);
            dto.setDescription(description);

            VehicleResponseDto result = vehicleService.addVehicle(dto, images, principal.getName());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT /api/vendor/vehicles/{id}  (multipart/form-data)
    @PutMapping(value = "/api/vendor/vehicles/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateVehicle(
            Principal principal,
            @PathVariable Integer id,
            @RequestParam(value = "vehicleName",       required = false) String vehicleName,
            @RequestParam(value = "brandId",           required = false) Integer brandId,
            @RequestParam(value = "vehicleCategoryId", required = false) Integer vehicleCategoryId,
            @RequestParam(value = "vehicleNo",         required = false) String vehicleNo,
            @RequestParam(value = "seatsCapacity",     required = false) Integer seatsCapacity,
            @RequestParam(value = "rentalPrice",       required = false) BigDecimal rentalPrice,
            @RequestParam(value = "description",       required = false) String description,
            @RequestParam(value = "status",            required = false) String status,
            @RequestParam(value = "images",            required = false) List<MultipartFile> images) {

        try {
            VehicleRequestDto dto = new VehicleRequestDto();
            dto.setVehicleName(vehicleName);
            dto.setBrandId(brandId);
            dto.setVehicleCategoryId(vehicleCategoryId);
            dto.setVehicleNo(vehicleNo);
            dto.setSeatsCapacity(seatsCapacity);
            dto.setRentalPrice(rentalPrice);
            dto.setDescription(description);
            dto.setStatus(status);

            VehicleResponseDto result = vehicleService.updateVehicle(id, dto, images, principal.getName());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT /api/vendor/vehicles/{id}/deactivate
    @PutMapping("/api/vendor/vehicles/{id}/deactivate")
    public ResponseEntity<?> deactivateVehicle(Principal principal, @PathVariable Integer id) {
        try {
            vehicleService.deactivateVehicle(id, principal.getName());
            return ResponseEntity.ok("Vehicle deactivated.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT /api/vendor/vehicles/{id}/reactivate
    @PutMapping("/api/vendor/vehicles/{id}/reactivate")
    public ResponseEntity<?> reactivateVehicle(Principal principal, @PathVariable Integer id) {
        try {
            vehicleService.reactivateVehicle(id, principal.getName());
            return ResponseEntity.ok("Vehicle reactivated and pending re-approval.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET /api/vendor/stats — dashboard stats + chart data
    @GetMapping("/api/vendor/stats")
    public ResponseEntity<?> getDashboardStats(Principal principal) {
        String email = principal.getName();
        Vendor vendor = vendorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        long vehicleCount       = vehicleRepository.findVendorVehicles(email, null, null, null).size();
        long activeBookingCount = bookingRepository.countActiveByVendor(email);
        long reviewCount        = vendor.getBrand() != null
                ? reviewService.getReviewsByBrand(vendor.getBrand().getBrandId()).size() : 0;
        BigDecimal totalRevenue = paymentRepository.sumVendorRevenue(email);

        // Monthly net revenue (payments minus refunds) — last 12 calendar months
        List<Object[]> rawMonthlyPayments = paymentRepository.monthlyVendorEarningsByVendor(email);
        List<Object[]> rawMonthlyRefunds = refundRepository.monthlyRefundsByVendorEmail(email);
        Map<String, BigDecimal> monthlyMap = new LinkedHashMap<>();
        java.time.LocalDate now = java.time.LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            java.time.LocalDate d = now.minusMonths(i);
            String key = d.getYear() + "-" + String.format("%02d", d.getMonthValue());
            monthlyMap.put(key, BigDecimal.ZERO);
        }
        for (Object[] row : rawMonthlyPayments) {
            int y = ((Number) row[0]).intValue();
            int m = ((Number) row[1]).intValue();
            String key = y + "-" + String.format("%02d", m);
            if (monthlyMap.containsKey(key)) monthlyMap.put(key, (BigDecimal) row[2]);
        }
        for (Object[] row : rawMonthlyRefunds) {
            int y = ((Number) row[0]).intValue();
            int m = ((Number) row[1]).intValue();
            String key = y + "-" + String.format("%02d", m);
            if (monthlyMap.containsKey(key)) {
                monthlyMap.put(key, monthlyMap.get(key).subtract((BigDecimal) row[2]));
            }
        }
        List<String>     monthLabels = new ArrayList<>();
        List<BigDecimal> monthValues = new ArrayList<>();
        String[] monthNames = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        for (Map.Entry<String, BigDecimal> e : monthlyMap.entrySet()) {
            int m = Integer.parseInt(e.getKey().split("-")[1]);
            monthLabels.add(monthNames[m - 1]);
            monthValues.add(e.getValue());
        }

        // Yearly net revenue (payments minus refunds)
        List<Object[]> rawYearlyPayments = paymentRepository.yearlyVendorEarningsByVendor(email);
        List<Object[]> rawYearlyRefunds = refundRepository.yearlyRefundsByVendorEmail(email);
        Map<Integer, BigDecimal> yearlyMap = new LinkedHashMap<>();
        for (Object[] row : rawYearlyPayments) {
            yearlyMap.put(((Number) row[0]).intValue(), (BigDecimal) row[1]);
        }
        for (Object[] row : rawYearlyRefunds) {
            int year = ((Number) row[0]).intValue();
            BigDecimal refund = (BigDecimal) row[1];
            yearlyMap.put(year, yearlyMap.getOrDefault(year, BigDecimal.ZERO).subtract(refund));
        }
        List<String>     yearLabels = new ArrayList<>();
        List<BigDecimal> yearValues = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> entry : yearlyMap.entrySet()) {
            yearLabels.add(String.valueOf(entry.getKey()));
            yearValues.add(entry.getValue());
        }
        if (yearLabels.isEmpty()) {
            yearLabels.add(String.valueOf(now.getYear()));
            yearValues.add(BigDecimal.ZERO);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("vehicleCount",       vehicleCount);
        stats.put("activeBookingCount", activeBookingCount);
        stats.put("reviewCount",        reviewCount);
        stats.put("totalRevenue",       totalRevenue);
        stats.put("monthlyLabels",      monthLabels);
        stats.put("monthlyValues",      monthValues);
        stats.put("yearlyLabels",       yearLabels);
        stats.put("yearlyValues",       yearValues);

        return ResponseEntity.ok(stats);
    }

    // GET /api/vendor/reviews — all reviews for the vendor's brand vehicles
    @GetMapping("/api/vendor/reviews")
    public ResponseEntity<?> getMyBrandReviews(Principal principal) {
        Vendor vendor = vendorRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        if (vendor.getBrand() == null)
            return ResponseEntity.ok(List.of());
        List<ReviewResponseDto> reviews = reviewService.getReviewsByBrand(vendor.getBrand().getBrandId());
        return ResponseEntity.ok(reviews);
    }

    // PUT /api/vendor/profile
    @PutMapping("/api/vendor/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body, Principal principal) {
        Vendor vendor = vendorRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        if (body.containsKey("vendorName")   && !body.get("vendorName").isBlank())
            vendor.setVendorName(body.get("vendorName").trim());
        if (body.containsKey("companyName"))
            vendor.setCompanyName(body.get("companyName").trim());
        if (body.containsKey("phoneNumber"))
            vendor.setPhoneNumber(body.get("phoneNumber").trim());

        vendorRepository.save(vendor);
        return ResponseEntity.ok(VendorResponseDto.from(vendor));
    }

    // PUT /api/vendor/password
    @PutMapping("/api/vendor/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, Principal principal) {
        String currentPassword = body.get("currentPassword");
        String newPassword     = body.get("newPassword");

        if (currentPassword == null || newPassword == null || newPassword.length() < 6)
            return ResponseEntity.badRequest().body("Invalid password data.");

        Vendor vendor = vendorRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        if (!passwordEncoder.matches(currentPassword, vendor.getPassword()))
            return ResponseEntity.badRequest().body("Current password is incorrect.");

        vendor.setPassword(passwordEncoder.encode(newPassword));
        vendorRepository.save(vendor);
        return ResponseEntity.ok("Password updated successfully.");
    }
}
