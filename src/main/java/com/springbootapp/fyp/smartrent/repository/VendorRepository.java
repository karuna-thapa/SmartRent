package com.springbootapp.fyp.smartrent.repository;

import com.springbootapp.fyp.smartrent.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Integer> {
}
