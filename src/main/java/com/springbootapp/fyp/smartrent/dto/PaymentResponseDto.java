package com.springbootapp.fyp.smartrent.dto;

import com.springbootapp.fyp.smartrent.model.Payment;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponseDto {

    private Integer paymentId;
    private Integer bookingId;
    private String  customerName;
    private String  customerEmail;
    private String  vehicleName;
    private String  vendorName;
    private BigDecimal totalAmount;
    private BigDecimal vendorAmount;
    private BigDecimal adminAmount;
    private String  paymentMethod;
    private String  paymentStatus;
    private String  transactionId;
    private LocalDateTime paidAt;

    public static PaymentResponseDto from(Payment p) {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setPaymentId(p.getPaymentId());
        dto.setTotalAmount(p.getAmount());
        dto.setVendorAmount(p.getVendorAmount());
        dto.setAdminAmount(p.getAdminAmount());
        dto.setPaymentMethod(p.getPaymentMethod() != null ? p.getPaymentMethod().name() : null);
        dto.setPaymentStatus(p.getPaymentStatus() != null ? p.getPaymentStatus().name() : null);
        dto.setTransactionId(p.getTransactionId());
        dto.setPaidAt(p.getCreatedAt());

        if (p.getBooking() != null) {
            dto.setBookingId(p.getBooking().getBookingId());
            if (p.getBooking().getCustomer() != null) {
                String name = (p.getBooking().getCustomer().getFirstName() + " "
                        + p.getBooking().getCustomer().getLastName()).trim();
                dto.setCustomerName(name);
                dto.setCustomerEmail(p.getBooking().getCustomer().getEmail());
            }
            if (p.getBooking().getVehicle() != null) {
                dto.setVehicleName(p.getBooking().getVehicle().getVehicleName());
                if (p.getBooking().getVehicle().getVendor() != null) {
                    dto.setVendorName(p.getBooking().getVehicle().getVendor().getVendorName());
                }
            }
        }
        return dto;
    }
}
