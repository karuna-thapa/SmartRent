package com.springbootapp.fyp.smartrent.service;

import com.springbootapp.fyp.smartrent.dto.VehicleRequestDto;
import com.springbootapp.fyp.smartrent.dto.VehicleResponseDto;
import com.springbootapp.fyp.smartrent.model.*;
import com.springbootapp.fyp.smartrent.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private VehicleImageRepository vehicleImageRepository;
    @Autowired private VendorRepository vendorRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private VehicleCategoryRepository vehicleCategoryRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private ReviewRepository reviewRepository;

    // ── Public APIs ─────────────────────────────────────────────────────────

    public List<VehicleResponseDto> getAvailableVehicles() {
        return vehicleRepository.findActiveApproved()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<VehicleResponseDto> getFeaturedVehicles() {
        return vehicleRepository.findActiveApproved(PageRequest.of(0, 4))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Optional<VehicleResponseDto> getVehicleById(Integer vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .filter(v -> Boolean.TRUE.equals(v.getActive())
                        && v.getApprovalStatus() == Vehicle.ApprovalStatus.APPROVED)
                .map(this::toDto);
    }

    public List<VehicleResponseDto> searchVehicles(Integer brandId, Integer categoryId) {
        return vehicleRepository.searchAvailable(brandId, categoryId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── Vendor APIs ─────────────────────────────────────────────────────────

    public List<VehicleResponseDto> getVendorVehicles(String vendorEmail,
                                                       Integer brandId,
                                                       Integer categoryId,
                                                       String statusStr) {
        Vehicle.Status status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try { status = Vehicle.Status.valueOf(statusStr); } catch (IllegalArgumentException ignored) {}
        }
        return vehicleRepository.findVendorVehicles(vendorEmail, brandId, categoryId, status)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public VehicleResponseDto addVehicle(VehicleRequestDto dto,
                                          List<MultipartFile> images,
                                          String vendorEmail) throws IOException {
        Vendor vendor = vendorRepository.findByEmail(vendorEmail)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        Brand brand = brandRepository.findById(dto.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        VehicleCategory category = vehicleCategoryRepository.findById(dto.getVehicleCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Vehicle vehicle = new Vehicle();
        vehicle.setVendor(vendor);
        vehicle.setBrand(brand);
        vehicle.setVehicleCategory(category);
        vehicle.setVehicleName(dto.getVehicleName());
        vehicle.setVehicleNo(dto.getVehicleNo());
        vehicle.setSeatsCapacity(dto.getSeatsCapacity());
        vehicle.setRentalPrice(dto.getRentalPrice());
        vehicle.setDescription(dto.getDescription());
        vehicle.setStatus(Vehicle.Status.available);
        vehicle.setActive(true);
        vehicle.setApprovalStatus(Vehicle.ApprovalStatus.PENDING);

        Vehicle saved = vehicleRepository.save(vehicle);
        saveImages(saved, images);

        return toDto(saved);
    }

    @Transactional
    public VehicleResponseDto updateVehicle(Integer vehicleId,
                                             VehicleRequestDto dto,
                                             List<MultipartFile> newImages,
                                             String vendorEmail) throws IOException {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (!vehicle.getVendor().getEmail().equals(vendorEmail)) {
            throw new RuntimeException("Access denied: you do not own this vehicle");
        }

        if (dto.getBrandId() != null) {
            Brand brand = brandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Brand not found"));
            vehicle.setBrand(brand);
        }
        if (dto.getVehicleCategoryId() != null) {
            VehicleCategory category = vehicleCategoryRepository.findById(dto.getVehicleCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            vehicle.setVehicleCategory(category);
        }
        if (dto.getVehicleName() != null) vehicle.setVehicleName(dto.getVehicleName());
        if (dto.getVehicleNo() != null)   vehicle.setVehicleNo(dto.getVehicleNo());
        if (dto.getSeatsCapacity() != null) vehicle.setSeatsCapacity(dto.getSeatsCapacity());
        if (dto.getRentalPrice() != null) vehicle.setRentalPrice(dto.getRentalPrice());
        if (dto.getDescription() != null) vehicle.setDescription(dto.getDescription());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                vehicle.setStatus(Vehicle.Status.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status value '" + dto.getStatus() + "'. Allowed: available, not_available");
            }
        }

        // If previously rejected, reset to PENDING so admin can re-review
        if (vehicle.getApprovalStatus() == Vehicle.ApprovalStatus.REJECTED) {
            vehicle.setApprovalStatus(Vehicle.ApprovalStatus.PENDING);
        }

        Vehicle saved = vehicleRepository.save(vehicle);

        // Replace images only if new ones are provided
        if (newImages != null && newImages.stream().anyMatch(f -> !f.isEmpty())) {
            List<VehicleImage> existing = vehicleImageRepository.findByVehicle_VehicleId(vehicleId);
            for (VehicleImage img : existing) {
                fileStorageService.deleteFile(img.getImageUrl());
            }
            vehicleImageRepository.deleteByVehicleId(vehicleId);
            saveImages(saved, newImages);
        }

        return toDto(saved);
    }

    @Transactional
    public void deactivateVehicle(Integer vehicleId, String vendorEmail) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (!vehicle.getVendor().getEmail().equals(vendorEmail)) {
            throw new RuntimeException("Access denied: you do not own this vehicle");
        }
        if (bookingRepository.hasActiveBookings(vehicleId)) {
            throw new RuntimeException("Cannot deactivate: vehicle has active bookings");
        }

        vehicle.setActive(false);
        vehicleRepository.save(vehicle);
    }

    @Transactional
    public void reactivateVehicle(Integer vehicleId, String vendorEmail) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (!vehicle.getVendor().getEmail().equals(vendorEmail)) {
            throw new RuntimeException("Access denied: you do not own this vehicle");
        }

        vehicle.setActive(true);
        vehicleRepository.save(vehicle);
    }

    // ── Admin APIs ──────────────────────────────────────────────────────────

    public List<VehicleResponseDto> getPendingVehicles() {
        return vehicleRepository.findByApprovalStatusAndActiveTrue(Vehicle.ApprovalStatus.PENDING)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<VehicleResponseDto> getAllVehiclesAdmin(Integer vendorId,
                                                         Integer brandId,
                                                         Integer categoryId,
                                                         String approvalStatusStr,
                                                         String activeStr) {
        Vehicle.ApprovalStatus approvalStatus = null;
        if (approvalStatusStr != null && !approvalStatusStr.isBlank()) {
            try { approvalStatus = Vehicle.ApprovalStatus.valueOf(approvalStatusStr.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }
        Boolean active = null;
        if ("true".equalsIgnoreCase(activeStr))  active = true;
        if ("false".equalsIgnoreCase(activeStr)) active = false;

        return vehicleRepository.findAllForAdmin(vendorId, brandId, categoryId, approvalStatus, active)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveVehicle(Integer vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        vehicle.setApprovalStatus(Vehicle.ApprovalStatus.APPROVED);
        vehicleRepository.save(vehicle);
    }

    @Transactional
    public void rejectVehicle(Integer vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        vehicle.setApprovalStatus(Vehicle.ApprovalStatus.REJECTED);
        vehicleRepository.save(vehicle);
    }

    @Transactional
    public void adminDeactivateVehicle(Integer vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        if (bookingRepository.hasActiveBookings(vehicleId))
            throw new RuntimeException("Cannot deactivate: vehicle has active bookings.");
        vehicle.setActive(false);
        vehicleRepository.save(vehicle);
    }

    @Transactional
    public void adminReactivateVehicle(Integer vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        vehicle.setActive(true);
        vehicleRepository.save(vehicle);
    }

    @Transactional
    public VehicleResponseDto adminUpdateVehicle(Integer vehicleId, VehicleRequestDto dto) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (dto.getVehicleName()    != null) vehicle.setVehicleName(dto.getVehicleName());
        if (dto.getVehicleNo()      != null) vehicle.setVehicleNo(dto.getVehicleNo());
        if (dto.getSeatsCapacity()  != null) vehicle.setSeatsCapacity(dto.getSeatsCapacity());
        if (dto.getRentalPrice()    != null) vehicle.setRentalPrice(dto.getRentalPrice());
        if (dto.getDescription()    != null) vehicle.setDescription(dto.getDescription());
        if (dto.getBrandId() != null) {
            Brand brand = brandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Brand not found"));
            vehicle.setBrand(brand);
        }
        if (dto.getVehicleCategoryId() != null) {
            VehicleCategory category = vehicleCategoryRepository.findById(dto.getVehicleCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            vehicle.setVehicleCategory(category);
        }
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                vehicle.setStatus(Vehicle.Status.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status value '" + dto.getStatus() + "'. Allowed: available, not_available");
            }
        }

        return toDto(vehicleRepository.save(vehicle));
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private void saveImages(Vehicle vehicle, List<MultipartFile> images) throws IOException {
        if (images == null) return;
        for (MultipartFile file : images) {
            if (file == null || file.isEmpty()) continue;
            String url = fileStorageService.storeVehicleImage(file);
            if (url != null) {
                VehicleImage img = new VehicleImage();
                img.setVehicle(vehicle);
                img.setImageUrl(url);
                vehicleImageRepository.save(img);
            }
        }
    }

    VehicleResponseDto toDto(Vehicle v) {
        VehicleResponseDto dto = new VehicleResponseDto();
        dto.setVehicleId(v.getVehicleId());
        dto.setVehicleName(v.getVehicleName());
        dto.setRentalPrice(v.getRentalPrice());
        dto.setStatus(v.getStatus() != null ? v.getStatus().name() : null);
        dto.setApprovalStatus(v.getApprovalStatus() != null ? v.getApprovalStatus().name() : null);
        dto.setActive(v.getActive());
        dto.setSeatsCapacity(v.getSeatsCapacity());
        dto.setVehicleNo(v.getVehicleNo());
        dto.setDescription(v.getDescription());
        dto.setCreatedAt(v.getCreatedAt());

        if (v.getBrand() != null) {
            dto.setBrandId(v.getBrand().getBrandId());
            dto.setBrandName(v.getBrand().getBrandName());
        }
        if (v.getVehicleCategory() != null) {
            dto.setCategoryId(v.getVehicleCategory().getVehicleCategoryId());
            dto.setCategoryName(v.getVehicleCategory().getVehicleCategoryName());
        }
        if (v.getVendor() != null) {
            dto.setVendorId(v.getVendor().getVendorId());
            dto.setVendorName(v.getVendor().getVendorName());
        }

        List<String> urls = vehicleImageRepository.findByVehicle_VehicleId(v.getVehicleId())
                .stream()
                .map(VehicleImage::getImageUrl)
                .collect(Collectors.toList());
        dto.setImageUrls(urls);
        dto.setImageUrl(urls.isEmpty() ? null : urls.get(0));

        List<com.springbootapp.fyp.smartrent.model.Review> reviews =
                reviewRepository.findByVehicle_VehicleId(v.getVehicleId());
        if (!reviews.isEmpty()) {
            double avg = reviews.stream().mapToInt(r -> r.getRating()).average().orElse(0);
            dto.setAverageRating(Math.round(avg * 10.0) / 10.0);
        }

        return dto;
    }
}
