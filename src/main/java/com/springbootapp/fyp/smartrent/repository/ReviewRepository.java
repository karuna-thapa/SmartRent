package com.springbootapp.fyp.smartrent.repository;

import com.springbootapp.fyp.smartrent.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findTop6ByOrderByCreatedAtDesc();

    List<Review> findByVehicle_VehicleId(Integer vehicleId);

    List<Review> findByCustomer_EmailOrderByCreatedAtDesc(String email);

    List<Review> findByVehicle_Brand_BrandIdOrderByCreatedAtDesc(Integer brandId);
}
