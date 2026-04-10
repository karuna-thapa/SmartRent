package com.springbootapp.fyp.smartrent.dto;

import com.springbootapp.fyp.smartrent.model.Vendor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VendorResponseDto {
    private Integer vendorId;
    private String vendorName;
    private String companyName;
    private String email;
    private String phoneNumber;
    private String registrationNo;
    private Integer brandId;
    private String brandName;
    private String status;
    private LocalDateTime createdAt;

    public static VendorResponseDto from(Vendor v) {
        VendorResponseDto dto = new VendorResponseDto();
        dto.setVendorId(v.getVendorId());
        dto.setVendorName(v.getVendorName());
        dto.setCompanyName(v.getCompanyName());
        dto.setEmail(v.getEmail());
        dto.setPhoneNumber(v.getPhoneNumber());
        dto.setRegistrationNo(v.getRegistrationNo());
        dto.setBrandId(v.getBrand() != null ? v.getBrand().getBrandId() : null);
        dto.setBrandName(v.getBrand() != null ? v.getBrand().getBrandName() : "—");
        dto.setStatus(v.getStatus().name());
        dto.setCreatedAt(v.getCreatedAt());
        return dto;
    }
}
