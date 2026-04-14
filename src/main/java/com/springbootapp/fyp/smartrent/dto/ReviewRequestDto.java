package com.springbootapp.fyp.smartrent.dto;

import lombok.Data;

@Data
public class ReviewRequestDto {
    private Integer vehicleId;
    private Integer rating;   
    private String comment;
}
