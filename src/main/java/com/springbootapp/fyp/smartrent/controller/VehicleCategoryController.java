package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.model.VehicleCategory;
import com.springbootapp.fyp.smartrent.repository.VehicleCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class VehicleCategoryController {

    @Autowired
    private VehicleCategoryRepository vehicleCategoryRepository;

    // Public — used by vendor add-vehicle dropdowns and vehicle-rentals filters
    @GetMapping("/api/categories")
    public ResponseEntity<List<VehicleCategory>> getAllCategories() {
        return ResponseEntity.ok(vehicleCategoryRepository.findAll());
    }

    // Admin — add a new category
    @PostMapping("/api/admin/categories")
    public ResponseEntity<?> addCategory(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("vehicleCategoryName", "").trim();
        if (name.isBlank()) return ResponseEntity.badRequest().body("Category name is required.");

        boolean exists = vehicleCategoryRepository.findAll()
                .stream()
                .anyMatch(c -> c.getVehicleCategoryName().equalsIgnoreCase(name));
        if (exists) return ResponseEntity.badRequest().body("Category already exists.");

        VehicleCategory cat = new VehicleCategory();
        cat.setVehicleCategoryName(name);
        return ResponseEntity.ok(vehicleCategoryRepository.save(cat));
    }

    // Admin — delete a category
    @DeleteMapping("/api/admin/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        if (!vehicleCategoryRepository.existsById(id))
            return ResponseEntity.badRequest().body("Category not found.");
        vehicleCategoryRepository.deleteById(id);
        return ResponseEntity.ok("Category deleted.");
    }
}
