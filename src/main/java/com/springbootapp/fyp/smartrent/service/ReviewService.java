package com.springbootapp.fyp.smartrent.service;

import com.springbootapp.fyp.smartrent.dto.ReviewRequestDto;
import com.springbootapp.fyp.smartrent.dto.ReviewResponseDto;
import com.springbootapp.fyp.smartrent.model.Customer;
import com.springbootapp.fyp.smartrent.model.Review;
import com.springbootapp.fyp.smartrent.model.Vehicle;
import com.springbootapp.fyp.smartrent.repository.CustomerRepository;
import com.springbootapp.fyp.smartrent.repository.ReviewRepository;
import com.springbootapp.fyp.smartrent.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    // Get 6 most recent reviews for home page
    public List<ReviewResponseDto> getRecentReviews() {
        return reviewRepository.findTop6ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Get all reviews for a specific vehicle
    public List<ReviewResponseDto> getReviewsByVehicle(Integer vehicleId) {
        return reviewRepository.findByVehicle_VehicleId(vehicleId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Get all reviews submitted by the currently logged-in customer
    public List<ReviewResponseDto> getReviewsByCustomer(String customerEmail) {
        return reviewRepository.findByCustomer_EmailOrderByCreatedAtDesc(customerEmail)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Delete a review — only the customer who wrote it can delete it
    public void deleteReview(Integer reviewId, String customerEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getCustomer().getEmail().equals(customerEmail)) {
            throw new RuntimeException("Not authorized to delete this review");
        }

        reviewRepository.delete(review);
    }

    // Get all reviews for vehicles belonging to a specific brand
    public List<ReviewResponseDto> getReviewsByBrand(Integer brandId) {
        return reviewRepository.findByVehicle_Brand_BrandIdOrderByCreatedAtDesc(brandId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Submit a new review from an authenticated customer
    public ReviewResponseDto submitReview(ReviewRequestDto request, String customerEmail) {
        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        Review review = new Review();
        review.setCustomer(customer);
        review.setVehicle(vehicle);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return toDto(reviewRepository.save(review));
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
