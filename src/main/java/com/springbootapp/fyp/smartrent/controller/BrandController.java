package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.dto.BrandResponseDto;
import com.springbootapp.fyp.smartrent.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@CrossOrigin(origins = "*")
public class BrandController {

    @Autowired
    private BrandService brandService;

    // GET /api/brands — all brands with vehicle count
    @GetMapping
    public ResponseEntity<List<BrandResponseDto>> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }
}
