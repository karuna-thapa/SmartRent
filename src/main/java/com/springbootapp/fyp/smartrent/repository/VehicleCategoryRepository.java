package com.springbootapp.fyp.smartrent.repository;

import com.springbootapp.fyp.smartrent.model.VehicleCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleCategoryRepository extends JpaRepository<VehicleCategory, Integer> {
}
