package com.springbootapp.fyp.smartrent.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaFixRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaFixRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Hibernate update does not reliably expand existing MySQL ENUM definitions.
        // Align booking enums so paid-cancel refund flow can persist REFUNDED states.
        runSafe("ALTER TABLE booking MODIFY COLUMN booking_status ENUM('PENDING','CONFIRMED','CANCELLED','REFUNDED') DEFAULT 'PENDING'");
        runSafe("ALTER TABLE booking MODIFY COLUMN payment_status ENUM('UNPAID','PAID','REFUNDED') DEFAULT 'UNPAID'");
        runSafe("ALTER TABLE booking ADD COLUMN warning_sent BIT(1) NOT NULL DEFAULT b'0'");
    }

    private void runSafe(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception ignored) {
            // Ignore to avoid startup failure in environments where schema is already correct.
        }
    }
}
