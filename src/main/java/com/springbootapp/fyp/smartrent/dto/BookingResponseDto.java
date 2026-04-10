package com.springbootapp.fyp.smartrent.dto;

import com.springbootapp.fyp.smartrent.model.Booking;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
public class BookingResponseDto {

    private Integer bookingId;

    // Customer
    private Integer customerId;
    private String  customerName;
    private String  customerEmail;
    private String  customerPhone;

    // Vehicle
    private Integer vehicleId;
    private String  vehicleName;
    private String  vehicleNo;

    // Dates & pricing
    private LocalDate   startDate;
    private LocalDate   endDate;
    private Long        durationDays;
    private BigDecimal  totalPrice;
    private String      rentalDurationType;

    // Status & meta
    private String        bookingStatus;
    private LocalDateTime createdAt;

    public static BookingResponseDto from(Booking b) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setBookingId(b.getBookingId());

        if (b.getCustomer() != null) {
            dto.setCustomerId(b.getCustomer().getCustomerId());
            dto.setCustomerName(b.getCustomer().getFirstName() + " " + b.getCustomer().getLastName());
            dto.setCustomerEmail(b.getCustomer().getEmail());
            dto.setCustomerPhone(b.getCustomer().getPhoneNumber());
        }

        if (b.getVehicle() != null) {
            dto.setVehicleId(b.getVehicle().getVehicleId());
            dto.setVehicleName(b.getVehicle().getVehicleName());
            dto.setVehicleNo(b.getVehicle().getVehicleNo());
        }

        dto.setStartDate(b.getStartDate());
        dto.setEndDate(b.getEndDate());

        if (b.getStartDate() != null && b.getEndDate() != null) {
            dto.setDurationDays(ChronoUnit.DAYS.between(b.getStartDate(), b.getEndDate()) + 1);
        }

        dto.setTotalPrice(b.getTotalPrice());
        dto.setRentalDurationType(
                b.getRentalDurationType() != null ? b.getRentalDurationType().name() : null);
        dto.setBookingStatus(
                b.getBookingStatus() != null ? b.getBookingStatus().name() : "PENDING");
        dto.setCreatedAt(b.getCreatedAt());

        return dto;
    }
}
