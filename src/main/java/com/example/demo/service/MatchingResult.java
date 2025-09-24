package com.example.demo.service;

import lombok.Data;

@Data
public class MatchingResult {
    private boolean valid;
    private String invoiceId;
    private double confidence;
    private String errorMessage;
    
    public MatchingResult() {
        this.valid = false;
        this.confidence = 0.0;
    }
    
    public static MatchingResult success(String invoiceId, double confidence) {
        MatchingResult result = new MatchingResult();
        result.setValid(true);
        result.setInvoiceId(invoiceId);
        result.setConfidence(confidence);
        return result;
    }
    
    public static MatchingResult failure(String errorMessage) {
        MatchingResult result = new MatchingResult();
        result.setValid(false);
        result.setErrorMessage(errorMessage);
        return result;
    }
}