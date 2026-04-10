package com.springbootapp.fyp.smartrent.repository;

import com.springbootapp.fyp.smartrent.model.VehicleImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface VehicleImageRepository extends JpaRepository<VehicleImage, Integer> {

    List<VehicleImage> findByVehicle_VehicleId(Integer vehicleId);

    @Modifying
    @Transactional
    @Query("DELETE FROM VehicleImage vi WHERE vi.vehicle.vehicleId = :vehicleId")
    void deleteByVehicleId(@Param("vehicleId") Integer vehicleId);
}
