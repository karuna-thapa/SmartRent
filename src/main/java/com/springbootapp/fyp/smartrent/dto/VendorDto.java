package com.springbootapp.fyp.smartrent.dto;

import lombok.Data;

@Data
public class VendorDto {
    private String vendorName;
    private String companyName;
    private String email;
    private String password;
    private String phoneNumber;
    private String registrationNo;
    private String brandName;   // looked up by name to resolve Brand entity
}
