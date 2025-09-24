package com.example.demo.dto.stripe;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class StripePaymentIntent {
    private String id;
    private String object;
    private BigDecimal amount;
    private BigDecimal amountReceived;
    private String status;
    private String currency;
    private String description;
    private Map<String, String> metadata;
    private String paymentMethod;
    private String clientSecret;
    private String receiptEmail;
    private String customer;
    
    // Additional methods for compatibility
    public Long getAmountReceived() {
        return amountReceived != null ? amountReceived.longValue() : null;
    }
    
    public Long getAmount() {
        return amount != null ? amount.longValue() : null;
    }
    
    public String getObject() {
        return object;
    }
}
