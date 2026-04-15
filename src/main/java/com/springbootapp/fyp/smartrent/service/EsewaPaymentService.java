package com.springbootapp.fyp.smartrent.service;

import com.springbootapp.fyp.smartrent.model.Booking;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.RoundingMode;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class EsewaPaymentService {

    private static final String SECRET_KEY   = "8gBm/:&EnhH.1/q";
    private static final String PRODUCT_CODE = "EPAYTEST";
    private static final String ESEWA_URL    = "https://rc-epay.esewa.com.np/api/epay/main/v2/form";
    private static final String BASE_URL     = "http://localhost:8081";

    /**
     * Build the signed payload to POST to eSewa.
     * transactionUuid format: bookingId-currentTimeMillis (unique per attempt)
     */
    public Map<String, String> buildPayload(Booking booking) {
        String totalAmount = booking.getTotalPrice()
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();
        String uuid = booking.getBookingId() + "-" + System.currentTimeMillis();

        String message = "total_amount=" + totalAmount
                + ",transaction_uuid=" + uuid
                + ",product_code=" + PRODUCT_CODE;

        String signature = sign(message);

        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("amount",                  totalAmount);
        payload.put("failure_url",             BASE_URL + "/payment/failure");
        payload.put("product_delivery_charge", "0");
        payload.put("product_service_charge",  "0");
        payload.put("product_code",            PRODUCT_CODE);
        payload.put("signature",               signature);
        payload.put("signed_field_names",      "total_amount,transaction_uuid,product_code");
        payload.put("success_url",             BASE_URL + "/payment/success");
        payload.put("tax_amount",              "0");
        payload.put("total_amount",            totalAmount);
        payload.put("transaction_uuid",        uuid);
        return payload;
    }

    /**
     * Verify eSewa callback signature.
     * signed_field_names tells us which fields to include and in which order.
     */
    public boolean verifyCallback(Map<String, String> data) {
        try {
            String signedFields = data.get("signed_field_names");
            if (signedFields == null) return false;
            String[] fields = signedFields.split(",");
            StringBuilder msg = new StringBuilder();
            for (int i = 0; i < fields.length; i++) {
                if (i > 0) msg.append(",");
                msg.append(fields[i].trim()).append("=").append(data.getOrDefault(fields[i].trim(), ""));
            }
            return sign(msg.toString()).equals(data.get("signature"));
        } catch (Exception e) {
            return false;
        }
    }

    public String getEsewaUrl() {
        return ESEWA_URL;
    }

    private String sign(String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(message.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("eSewa signing failed", e);
        }
    }
}
