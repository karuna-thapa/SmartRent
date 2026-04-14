package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.dto.BrandResponseDto;
import com.springbootapp.fyp.smartrent.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class BrandController {

    @Autowired
    private BrandService brandService;

    // GET /api/brands — all brands with vehicle count
    @GetMapping("/api/brands")
    public ResponseEntity<List<BrandResponseDto>> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }

    // POST /api/admin/brands — add brand
    @PostMapping("/api/admin/brands")
    public ResponseEntity<?> addBrand(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(brandService.addBrand(body.getOrDefault("brandName", "")));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT /api/admin/brands/{id} — rename brand
    @PutMapping("/api/admin/brands/{id}")
    public ResponseEntity<?> updateBrand(@PathVariable Integer id,
                                          @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(brandService.updateBrand(id, body.getOrDefault("brandName", "")));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE /api/admin/brands/{id} — delete brand (only if no vehicles use it)
    @DeleteMapping("/api/admin/brands/{id}")
    public ResponseEntity<?> deleteBrand(@PathVariable Integer id) {
        try {
            brandService.deleteBrand(id);
            return ResponseEntity.ok("Brand deleted.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
