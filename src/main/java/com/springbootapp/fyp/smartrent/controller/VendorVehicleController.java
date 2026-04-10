package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.dto.VehicleRequestDto;
import com.springbootapp.fyp.smartrent.dto.VehicleResponseDto;
import com.springbootapp.fyp.smartrent.dto.VendorResponseDto;
import com.springbootapp.fyp.smartrent.model.Vendor;
import com.springbootapp.fyp.smartrent.repository.VendorRepository;
import com.springbootapp.fyp.smartrent.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class VendorVehicleController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VendorRepository vendorRepository;

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
