package com.example.demo.dto.stripe;

import lombok.Data;
import java.util.Map;

@Data
public class StripeWebhookEvent {
    private String id;
    private String object;
    private String type;
    private StripeWebhookData data;
    private boolean livemode;
    private long created;
    private String apiVersion;
    private Map<String, Object> request;
    private Map<String, String> pendingWebhooks;
    
    // Additional methods for compatibility
    public boolean getLivemode() {
        return livemode;
    }
    
    public String getObject() {
        return object;
    }
    
    @Data
    public static class StripeWebhookData {
        private StripePaymentIntent object;
    }
}
