package com.springbootapp.fyp.smartrent.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CustomerProfileDto {
    private Integer customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dob;
    private String address;
    private String profileImage;
    private String licenseImage;
    private String licenseNo;
}
