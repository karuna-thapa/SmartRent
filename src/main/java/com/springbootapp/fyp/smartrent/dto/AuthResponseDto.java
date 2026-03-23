package com.springbootapp.fyp.smartrent.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class AuthResponseDto {

    private String token;
    private String role;
    private String email;
    private String firstName;
}