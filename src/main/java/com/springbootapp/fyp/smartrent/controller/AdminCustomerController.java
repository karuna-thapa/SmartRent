package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.model.Booking;
import com.springbootapp.fyp.smartrent.model.Customer;
import com.springbootapp.fyp.smartrent.repository.BookingRepository;
import com.springbootapp.fyp.smartrent.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/customers")
@CrossOrigin(origins = "*")
public class AdminCustomerController {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private BookingRepository  bookingRepository;

    // GET /api/admin/customers — all customers with booking summary
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Customer c : customers) {
            if (c.getRole() != Customer.Role.customer) continue; // skip admin/vendor
            result.add(buildSummary(c));
        }

        result.sort(Comparator.comparing(
                m -> ((String) m.getOrDefault("createdAt", "")),
                Comparator.reverseOrder()
        ));

        return ResponseEntity.ok(result);
    }

    // GET /api/admin/customers/{id} — full customer detail + booking history
    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomer(@PathVariable Integer id) {
        return customerRepository.findById(id).map(c -> {
            Map<String, Object> detail = buildSummary(c);
            detail.put("dob",         c.getDob() != null ? c.getDob().toString() : null);
            detail.put("address",     c.getAddress());
            detail.put("licenseNo",   c.getLicenseNo());
            detail.put("profileImage", c.getProfileImage());

            List<Booking> bookings = bookingRepository
                    .findByCustomer_CustomerIdOrderByCreatedAtDesc(id);
            List<Map<String, Object>> bookingList = new ArrayList<>();
            for (Booking b : bookings) {
                Map<String, Object> bm = new LinkedHashMap<>();
                bm.put("bookingId",    b.getBookingId());
                bm.put("vehicleName",  b.getVehicle() != null ? b.getVehicle().getVehicleName() : "—");
                bm.put("startDate",    b.getStartDate() != null ? b.getStartDate().toString() : null);
                bm.put("endDate",      b.getEndDate()   != null ? b.getEndDate().toString()   : null);
                bm.put("totalPrice",   b.getTotalPrice());
                bm.put("status",       b.getBookingStatus() != null ? b.getBookingStatus().name() : null);
                bm.put("createdAt",    b.getCreatedAt() != null ? b.getCreatedAt().toString() : null);
                bookingList.add(bm);
            }
            detail.put("bookings", bookingList);
            return ResponseEntity.ok(detail);
        }).orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/admin/customers/{id}/disable
    @PutMapping("/{id}/disable")
    public ResponseEntity<?> disableCustomer(@PathVariable Integer id) {
        return customerRepository.findById(id).map(c -> {
            c.setActive(false);
            customerRepository.save(c);
            return ResponseEntity.ok("Customer account disabled.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/admin/customers/{id}/enable
    @PutMapping("/{id}/enable")
    public ResponseEntity<?> enableCustomer(@PathVariable Integer id) {
        return customerRepository.findById(id).map(c -> {
            c.setActive(true);
            customerRepository.save(c);
            return ResponseEntity.ok("Customer account enabled.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Map<String, Object> buildSummary(Customer c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("customerId",     c.getCustomerId());
        m.put("firstName",      c.getFirstName());
        m.put("lastName",       c.getLastName());
        m.put("email",          c.getEmail());
        m.put("phoneNumber",    c.getPhoneNumber());
        m.put("active",         c.getActive() == null || c.getActive());
        m.put("createdAt",      c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);
        m.put("totalBookings",  bookingRepository.countByCustomer_CustomerId(c.getCustomerId()));
        m.put("totalSpent",     bookingRepository.sumSpentByCustomer(c.getCustomerId()));
        m.put("activeBookings", bookingRepository.countActiveBookingsByCustomer(c.getCustomerId()));
        return m;
    }
}
