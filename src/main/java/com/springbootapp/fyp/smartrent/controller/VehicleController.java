package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.dto.VehicleResponseDto;
import com.springbootapp.fyp.smartrent.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    // GET /api/vehicles — all available vehicles
    @GetMapping
    public ResponseEntity<List<VehicleResponseDto>> getAvailableVehicles() {
        return ResponseEntity.ok(vehicleService.getAvailableVehicles());
    }

    // GET /api/vehicles/featured — top 4 for home page
    @GetMapping("/featured")
    public ResponseEntity<List<VehicleResponseDto>> getFeaturedVehicles() {
        return ResponseEntity.ok(vehicleService.getFeaturedVehicles());
    }

    // GET /api/vehicles/search?brandId=1&categoryId=2
    @GetMapping("/search")
    public ResponseEntity<List<VehicleResponseDto>> searchVehicles(
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) Integer categoryId) {
        return ResponseEntity.ok(vehicleService.searchVehicles(brandId, categoryId));
    }
}
