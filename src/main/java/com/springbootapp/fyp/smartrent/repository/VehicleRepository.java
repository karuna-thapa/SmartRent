package com.springbootapp.fyp.smartrent.repository;

import com.springbootapp.fyp.smartrent.model.Vehicle;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {

    List<Vehicle> findByStatus(Vehicle.Status status);

    List<Vehicle> findByStatus(Vehicle.Status status, Pageable pageable);

    List<Vehicle> findByBrand_BrandId(Integer brandId);

    List<Vehicle> findByVehicleCategory_VehicleCategoryId(Integer categoryId);

    long countByStatus(Vehicle.Status status);

    long countByBrand_BrandId(Integer brandId);

    @Query("SELECT v FROM Vehicle v WHERE v.status = 'available' " +
           "AND (:brandId IS NULL OR v.brand.brandId = :brandId) " +
           "AND (:categoryId IS NULL OR v.vehicleCategory.vehicleCategoryId = :categoryId)")
    List<Vehicle> searchAvailable(@Param("brandId") Integer brandId,
                                  @Param("categoryId") Integer categoryId);
}
