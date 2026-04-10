package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.dto.VendorResponseDto;
import com.springbootapp.fyp.smartrent.model.Vendor;
import com.springbootapp.fyp.smartrent.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/vendors")
@CrossOrigin(origins = "*")
public class AdminVendorController {

    @Autowired
    private VendorRepository vendorRepository;

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

    // GET vendor details by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getVendor(@PathVariable Integer id) {
        return vendorRepository.findById(id)
                .map(v -> ResponseEntity.ok(VendorResponseDto.from(v)))
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
}
