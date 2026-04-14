package com.springbootapp.fyp.smartrent.service;

import com.springbootapp.fyp.smartrent.dto.BrandResponseDto;
import com.springbootapp.fyp.smartrent.model.Brand;
import com.springbootapp.fyp.smartrent.repository.BrandRepository;
import com.springbootapp.fyp.smartrent.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    public List<BrandResponseDto> getAllBrands() {
        return brandRepository.findAll()
                .stream()
                .map(brand -> new BrandResponseDto(
                        brand.getBrandId(),
                        brand.getBrandName(),
                        vehicleRepository.countByBrand_BrandId(brand.getBrandId())
                ))
                .collect(Collectors.toList());
    }

    public Brand addBrand(String name) {
        if (name == null || name.isBlank())
            throw new RuntimeException("Brand name is required.");
        if (brandRepository.findByBrandNameIgnoreCase(name.trim()).isPresent())
            throw new RuntimeException("Brand '" + name.trim() + "' already exists.");
        Brand brand = new Brand();
        brand.setBrandName(name.trim());
        return brandRepository.save(brand);
    }

    public Brand updateBrand(Integer id, String name) {
        if (name == null || name.isBlank())
            throw new RuntimeException("Brand name is required.");
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found."));
        brandRepository.findByBrandNameIgnoreCase(name.trim()).ifPresent(existing -> {
            if (!existing.getBrandId().equals(id))
                throw new RuntimeException("Brand '" + name.trim() + "' already exists.");
        });
        brand.setBrandName(name.trim());
        return brandRepository.save(brand);
    }

    public void deleteBrand(Integer id) {
        if (!brandRepository.existsById(id))
            throw new RuntimeException("Brand not found.");
        long vehicleCount = vehicleRepository.countByBrand_BrandId(id);
        if (vehicleCount > 0)
            throw new RuntimeException("Cannot delete: " + vehicleCount + " vehicle(s) use this brand.");
        brandRepository.deleteById(id);
    }
}
