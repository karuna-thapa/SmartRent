package com.springbootapp.fyp.smartrent.repository;

import com.springbootapp.fyp.smartrent.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer> {
    Optional<Brand> findByBrandNameIgnoreCase(String brandName);
}
