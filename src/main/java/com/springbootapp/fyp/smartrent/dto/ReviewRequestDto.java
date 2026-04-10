package com.springbootapp.fyp.smartrent.dto;

import lombok.Data;

@Data
public class ReviewRequestDto {
    private Integer vehicleId;
    private Integer rating;   // 1–5
    private String comment;
}
