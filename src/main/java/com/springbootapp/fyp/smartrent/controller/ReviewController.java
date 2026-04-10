package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.dto.ReviewRequestDto;
import com.springbootapp.fyp.smartrent.dto.ReviewResponseDto;
import com.springbootapp.fyp.smartrent.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // GET /api/reviews/recent — 6 most recent reviews for home page
    @GetMapping("/recent")
    public ResponseEntity<List<ReviewResponseDto>> getRecentReviews() {
        return ResponseEntity.ok(reviewService.getRecentReviews());
    }

    // GET /api/reviews/vehicle/{vehicleId} — all reviews for a specific vehicle
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByVehicle(@PathVariable Integer vehicleId) {
        return ResponseEntity.ok(reviewService.getReviewsByVehicle(vehicleId));
    }

    // POST /api/reviews — submit a review (requires authentication)
    @PostMapping
    public ResponseEntity<?> submitReview(@RequestBody ReviewRequestDto request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in to submit a review.");
        }
        try {
            ReviewResponseDto saved = reviewService.submitReview(request, auth.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
