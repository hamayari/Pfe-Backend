package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service
public class PaymentProofMatchingService {
    
    public MatchingResult matchProofToInvoice(PaymentProofData data) {
        // Simple matching logic - in real implementation this would be more sophisticated
        MatchingResult result = new MatchingResult();
        result.setValid(true);
        result.setInvoiceId("INV-" + System.currentTimeMillis());
        result.setConfidence(0.85);
        return result;
    }
}