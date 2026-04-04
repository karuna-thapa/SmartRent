package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.dto.ReviewResponseDto;
import com.springbootapp.fyp.smartrent.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
}
