package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.model.VehicleCategory;
import com.springbootapp.fyp.smartrent.repository.VehicleCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class VehicleCategoryController {

    @Autowired
    private VehicleCategoryRepository vehicleCategoryRepository;

    @GetMapping
    public ResponseEntity<List<VehicleCategory>> getAllCategories() {
        return ResponseEntity.ok(vehicleCategoryRepository.findAll());
    }
}
