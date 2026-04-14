package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.dto.VendorResponseDto;
import com.springbootapp.fyp.smartrent.model.Vendor;
import com.springbootapp.fyp.smartrent.repository.BookingRepository;
import com.springbootapp.fyp.smartrent.repository.VehicleRepository;
import com.springbootapp.fyp.smartrent.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/vendors")
@CrossOrigin(origins = "*")
public class AdminVendorController {

    @Autowired private VendorRepository  vendorRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private BookingRepository bookingRepository;

    private Map<String, Object> buildVendorMap(Vendor v) {
        Integer id = v.getVendorId();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("vendorId",       id);
        m.put("vendorName",     v.getVendorName());
        m.put("companyName",    v.getCompanyName());
        m.put("email",          v.getEmail());
        m.put("phoneNumber",    v.getPhoneNumber());
        m.put("registrationNo", v.getRegistrationNo());
        m.put("brandName",      v.getBrand() != null ? v.getBrand().getBrandName() : "—");
        m.put("status",         v.getStatus().name());
        m.put("active",         v.getActive() == null || v.getActive());
        m.put("createdAt",      v.getCreatedAt() != null ? v.getCreatedAt().toString() : null);
        m.put("vehicleCount",   vehicleRepository.countByVendor_VendorId(id));
        m.put("totalBookings",  bookingRepository.countByVendorId(id));
        m.put("totalRevenue",   bookingRepository.sumRevenueByVendorId(id));
        m.put("activeBookings", bookingRepository.countActiveByVendorId(id));
        return m;
    }

    // GET all vendors (all statuses) with fleet & performance stats
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllVendors() {
        List<Vendor> vendors = vendorRepository.findAll();
        List<Map<String, Object>> result = vendors.stream()
                .sorted(Comparator.comparing(
                        v -> v.getCreatedAt() != null ? v.getCreatedAt().toString() : "",
                        Comparator.reverseOrder()))
                .map(this::buildVendorMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // GET all pending vendor requests
    @GetMapping("/pending")
    public ResponseEntity<List<VendorResponseDto>> getPendingVendors() {
        List<VendorResponseDto> list = vendorRepository
                .findByStatus(Vendor.Status.PENDING)
                .stream()
                .map(VendorResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // GET vendor details by id (includes fleet & performance)
    @GetMapping("/{id}")
    public ResponseEntity<?> getVendor(@PathVariable Integer id) {
        return vendorRepository.findById(id)
                .map(v -> ResponseEntity.ok(buildVendorMap(v)))
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT approve vendor
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveVendor(@PathVariable Integer id) {
        return vendorRepository.findById(id).map(v -> {
            v.setStatus(Vendor.Status.APPROVED);
            vendorRepository.save(v);
            return ResponseEntity.ok("Vendor approved.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // PUT reject vendor
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectVendor(@PathVariable Integer id) {
        return vendorRepository.findById(id).map(v -> {
            v.setStatus(Vendor.Status.REJECTED);
            vendorRepository.save(v);
            return ResponseEntity.ok("Vendor rejected.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // PUT deactivate vendor account
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateVendor(@PathVariable Integer id) {
        return vendorRepository.findById(id).map(v -> {
            v.setActive(false);
            vendorRepository.save(v);
            return ResponseEntity.ok("Vendor account deactivated.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // PUT activate vendor account
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateVendor(@PathVariable Integer id) {
        return vendorRepository.findById(id).map(v -> {
            v.setActive(true);
            vendorRepository.save(v);
            return ResponseEntity.ok("Vendor account activated.");
        }).orElse(ResponseEntity.notFound().build());
    }
}
