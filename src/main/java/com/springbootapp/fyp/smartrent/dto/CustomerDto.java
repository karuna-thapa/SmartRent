package com.springbootapp.fyp.smartrent.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CustomerDto {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    private LocalDate dob;
    private String address;
    private String licenseNo;
    private String profileImage;
}