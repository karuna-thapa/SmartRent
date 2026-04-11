package com.springbootapp.fyp.smartrent.config;

import com.springbootapp.fyp.smartrent.model.VehicleCategory;
import com.springbootapp.fyp.smartrent.repository.VehicleCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private VehicleCategoryRepository vehicleCategoryRepository;

    private static final List<String> DESIRED = List.of("Car", "Bike", "Scooter", "Bicycle");

    @Override
    public void run(ApplicationArguments args) {
        Set<String> existing = vehicleCategoryRepository.findAll()
                .stream()
                .map(VehicleCategory::getVehicleCategoryName)
                .collect(Collectors.toSet());

        // If the DB doesn't have exactly our 4 categories, wipe and reseed
        if (!existing.equals(Set.copyOf(DESIRED))) {
            vehicleCategoryRepository.deleteAll();
            DESIRED.forEach(name -> {
                VehicleCategory cat = new VehicleCategory();
                cat.setVehicleCategoryName(name);
                vehicleCategoryRepository.save(cat);
            });
        }
    }
}
