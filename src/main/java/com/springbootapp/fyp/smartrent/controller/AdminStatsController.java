package com.springbootapp.fyp.smartrent.controller;

import com.springbootapp.fyp.smartrent.repository.BookingRepository;
import com.springbootapp.fyp.smartrent.repository.CustomerRepository;
import com.springbootapp.fyp.smartrent.repository.PaymentRepository;
import com.springbootapp.fyp.smartrent.repository.RefundRepository;
import com.springbootapp.fyp.smartrent.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminStatsController {

    @Autowired private BookingRepository  bookingRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private VehicleRepository  vehicleRepository;
    @Autowired private PaymentRepository  paymentRepository;
    @Autowired private RefundRepository   refundRepository;

    // GET /api/admin/stats — summary cards
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalBookings", bookingRepository.countAllBookings());
        stats.put("totalUsers",    customerRepository.count());
        stats.put("totalVehicles", vehicleRepository.count());
        stats.put("totalRevenue",  paymentRepository.sumTotalCollected().subtract(refundRepository.sumAllRefunds()));
        return ResponseEntity.ok(stats);
    }

    // GET /api/admin/revenue?period=monthly[&year=2025]  or  period=yearly
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenue(
            @RequestParam(defaultValue = "monthly") String period,
            @RequestParam(required = false) Integer year) {

        Map<String, Object> result = new LinkedHashMap<>();

        if ("yearly".equalsIgnoreCase(period)) {
            List<Object[]> rows = bookingRepository.yearlyRevenueGlobal();
            List<String>  labels = new ArrayList<>();
            List<Number>  values = new ArrayList<>();
            for (Object[] row : rows) {
                labels.add(String.valueOf(row[0]));
                values.add(((Number) row[1]).longValue());
            }
            // Ensure at least the current year appears
            if (labels.isEmpty()) {
                labels.add(String.valueOf(LocalDate.now().getYear()));
                values.add(0L);
            }
            result.put("labels", labels);
            result.put("values", values);

        } else {
            // monthly — default to current year
            int targetYear = (year != null) ? year : LocalDate.now().getYear();
            List<Object[]> rows = bookingRepository.monthlyRevenueGlobal(targetYear);

            // Build a map month→revenue, then fill all 12 months
            Map<Integer, Long> monthMap = new LinkedHashMap<>();
            for (Object[] row : rows) {
                monthMap.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
            }

            String[] monthNames = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
            List<String> labels = new ArrayList<>();
            List<Number> values = new ArrayList<>();
            for (int m = 1; m <= 12; m++) {
                labels.add(monthNames[m - 1]);
                values.add(monthMap.getOrDefault(m, 0L));
            }
            result.put("labels", labels);
            result.put("values", values);
            result.put("year",   targetYear);
        }

        return ResponseEntity.ok(result);
    }
}
