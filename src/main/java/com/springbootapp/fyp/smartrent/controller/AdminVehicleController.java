package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.dto.VehicleRequestDto;
import com.springbootapp.fyp.smartrent.dto.VehicleResponseDto;
import com.springbootapp.fyp.smartrent.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vehicles")
@CrossOrigin(origins = "*")
public class AdminVehicleController {

    @Autowired
    private VehicleService vehicleService;

    // GET /api/admin/vehicles/pending
    @GetMapping("/pending")
    public ResponseEntity<List<VehicleResponseDto>> getPendingVehicles() {
        return ResponseEntity.ok(vehicleService.getPendingVehicles());
    }

    // GET /api/admin/vehicles/all?vendorId=&brandId=&categoryId=&approvalStatus=&active=
    @GetMapping("/all")
    public ResponseEntity<List<VehicleResponseDto>> getAllVehicles(
            @RequestParam(required = false) Integer vendorId,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(required = false) String active) {
        return ResponseEntity.ok(
                vehicleService.getAllVehiclesAdmin(vendorId, brandId, categoryId, approvalStatus, active));
    }

    // PUT /api/admin/vehicles/{id}  — edit vehicle fields
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicle(@PathVariable Integer id,
                                            @RequestBody VehicleRequestDto dto) {
        try {
            return ResponseEntity.ok(vehicleService.adminUpdateVehicle(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT /api/admin/vehicles/{id}/approve
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveVehicle(@PathVariable Integer id) {
        try {
            vehicleService.approveVehicle(id);
            return ResponseEntity.ok("Vehicle approved.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT /api/admin/vehicles/{id}/reject
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectVehicle(@PathVariable Integer id) {
        try {
            vehicleService.rejectVehicle(id);
            return ResponseEntity.ok("Vehicle rejected.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT /api/admin/vehicles/{id}/deactivate — admin deactivate (checks active bookings)
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateVehicle(@PathVariable Integer id) {
        try {
            vehicleService.adminDeactivateVehicle(id);
            return ResponseEntity.ok("Vehicle deactivated.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT /api/admin/vehicles/{id}/reactivate
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<?> reactivateVehicle(@PathVariable Integer id) {
        try {
            vehicleService.adminReactivateVehicle(id);
            return ResponseEntity.ok("Vehicle reactivated.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
