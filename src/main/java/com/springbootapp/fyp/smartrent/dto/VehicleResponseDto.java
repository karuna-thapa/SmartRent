package com.springbootapp.fyp.smartrent.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VehicleResponseDto {

    private Integer vehicleId;
    private String vehicleName;
    private BigDecimal rentalPrice;
    private String status;
    private Integer seatsCapacity;
    private String vehicleNo;
    private String brandName;
    private String categoryName;
    private String vendorName;
    private String imageUrl;
}
