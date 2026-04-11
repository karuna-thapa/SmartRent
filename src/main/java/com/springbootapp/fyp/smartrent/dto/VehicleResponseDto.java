package com.springbootapp.fyp.smartrent.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VehicleResponseDto {

    private Integer vehicleId;
    private String vehicleName;
    private BigDecimal rentalPrice;
    private String status;
    private String approvalStatus;
    private Boolean active;
    private Integer seatsCapacity;
    private String vehicleNo;
    private String description;
    private Integer brandId;
    private String brandName;
    private Integer categoryId;
    private String categoryName;
    private Integer vendorId;
    private String vendorName;
    private String imageUrl;          // first image (for backwards compat)
    private List<String> imageUrls;   // all images
    private LocalDateTime createdAt;
    private Double averageRating;
}
