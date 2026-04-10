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
}
