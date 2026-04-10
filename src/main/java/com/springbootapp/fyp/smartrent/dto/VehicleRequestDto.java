package com.springbootapp.fyp.smartrent.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VehicleRequestDto {
    private String vehicleName;
    private Integer brandId;
    private Integer vehicleCategoryId;
    private String vehicleNo;
    private Integer seatsCapacity;
    private BigDecimal rentalPrice;
    private String description;
    private String status; // "available" or "not_available"
}
