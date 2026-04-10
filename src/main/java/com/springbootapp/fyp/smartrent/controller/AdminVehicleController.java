package com.springbootapp.fyp.smartrent.controller;

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
}
