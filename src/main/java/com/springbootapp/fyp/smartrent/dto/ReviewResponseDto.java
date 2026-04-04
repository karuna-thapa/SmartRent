package com.springbootapp.fyp.smartrent.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewResponseDto {

    private Integer reviewId;
    private String customerName;
    private Integer rating;
    private String comment;
    private String vehicleName;
    private LocalDateTime createdAt;
}
