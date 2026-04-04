package com.springbootapp.fyp.smartrent.service;

import com.springbootapp.fyp.smartrent.dto.VehicleResponseDto;
import com.springbootapp.fyp.smartrent.model.Vehicle;
import com.springbootapp.fyp.smartrent.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    // Get all available vehicles
    public List<VehicleResponseDto> getAvailableVehicles() {
        return vehicleRepository.findByStatus(Vehicle.Status.available)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Get top 4 available vehicles for home page
    public List<VehicleResponseDto> getFeaturedVehicles() {
        return vehicleRepository.findByStatus(
                        Vehicle.Status.available,
                        PageRequest.of(0, 4)
                )
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Search available vehicles by optional brand and category filters
    public List<VehicleResponseDto> searchVehicles(Integer brandId, Integer categoryId) {
        return vehicleRepository.searchAvailable(brandId, categoryId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private VehicleResponseDto toDto(Vehicle v) {
        VehicleResponseDto dto = new VehicleResponseDto();
        dto.setVehicleId(v.getVehicleId());
        dto.setVehicleName(v.getVehicleName());
        dto.setRentalPrice(v.getRentalPrice());
        dto.setStatus(v.getStatus().name());
        dto.setSeatsCapacity(v.getSeatsCapacity());
        dto.setVehicleNo(v.getVehicleNo());
        dto.setBrandName(v.getBrand() != null ? v.getBrand().getBrandName() : null);
        dto.setCategoryName(v.getVehicleCategory() != null
                ? v.getVehicleCategory().getVehicleCategoryName() : null);
        dto.setVendorName(v.getVendor() != null ? v.getVendor().getVendorName() : null);
        dto.setImageUrl(v.getImageUrl());
        return dto;
    }
}
