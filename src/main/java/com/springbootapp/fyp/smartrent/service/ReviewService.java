package com.springbootapp.fyp.smartrent.service;

import com.springbootapp.fyp.smartrent.dto.ReviewResponseDto;
import com.springbootapp.fyp.smartrent.model.Review;
import com.springbootapp.fyp.smartrent.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    // Get 6 most recent reviews for home page
    public List<ReviewResponseDto> getRecentReviews() {
        return reviewRepository.findTop6ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ReviewResponseDto toDto(Review r) {
        ReviewResponseDto dto = new ReviewResponseDto();
        dto.setReviewId(r.getReviewId());
        dto.setRating(r.getRating());
        dto.setComment(r.getComment());
        dto.setCreatedAt(r.getCreatedAt());

        if (r.getCustomer() != null) {
            dto.setCustomerName(
                    r.getCustomer().getFirstName() + " " + r.getCustomer().getLastName()
            );
        }

        if (r.getVehicle() != null) {
            dto.setVehicleName(r.getVehicle().getVehicleName());
        }

        return dto;
    }
}
