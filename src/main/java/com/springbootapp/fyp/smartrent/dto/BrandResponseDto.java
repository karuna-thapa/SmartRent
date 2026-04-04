package com.springbootapp.fyp.smartrent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BrandResponseDto {

    private Integer brandId;
    private String brandName;
    private long vehicleCount;
    private String brandLogoUrl;
}
