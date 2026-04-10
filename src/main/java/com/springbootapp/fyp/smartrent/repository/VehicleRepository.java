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

    // Public search: only active + approved vehicles
    @Query("SELECT v FROM Vehicle v WHERE v.active = true AND v.approvalStatus = 'APPROVED' " +
           "AND (:brandId IS NULL OR v.brand.brandId = :brandId) " +
           "AND (:categoryId IS NULL OR v.vehicleCategory.vehicleCategoryId = :categoryId)")
    List<Vehicle> searchAvailable(@Param("brandId") Integer brandId,
                                  @Param("categoryId") Integer categoryId);

    // Public listing: active + approved only
    @Query("SELECT v FROM Vehicle v WHERE v.active = true AND v.approvalStatus = 'APPROVED'")
    List<Vehicle> findActiveApproved();

    @Query("SELECT v FROM Vehicle v WHERE v.active = true AND v.approvalStatus = 'APPROVED'")
    List<Vehicle> findActiveApproved(Pageable pageable);

    // Vendor: get own vehicles with optional filters
    @Query("SELECT v FROM Vehicle v WHERE v.vendor.email = :email " +
           "AND (:brandId IS NULL OR v.brand.brandId = :brandId) " +
           "AND (:categoryId IS NULL OR v.vehicleCategory.vehicleCategoryId = :categoryId) " +
           "AND (:status IS NULL OR v.status = :status)")
    List<Vehicle> findVendorVehicles(@Param("email") String email,
                                     @Param("brandId") Integer brandId,
                                     @Param("categoryId") Integer categoryId,
                                     @Param("status") Vehicle.Status status);

    // Admin: get pending approval vehicles
    List<Vehicle> findByApprovalStatusAndActiveTrue(Vehicle.ApprovalStatus approvalStatus);

    long countByApprovalStatus(Vehicle.ApprovalStatus approvalStatus);
}
