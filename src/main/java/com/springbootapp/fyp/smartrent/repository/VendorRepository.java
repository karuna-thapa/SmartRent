package com.springbootapp.fyp.smartrent.repository;

import com.springbootapp.fyp.smartrent.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Integer> {
    boolean existsByEmail(String email);
    Optional<Vendor> findByEmail(String email);
    List<Vendor> findByStatus(Vendor.Status status);
}
