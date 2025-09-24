package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service
public class PaymentProofArchivingService {
    
    public String archiveProof(EmailAttachment attachment, PaymentProofData data, MatchingResult match) {
        // Simple archiving logic - in real implementation this would save to storage
        return "PROOF-" + System.currentTimeMillis();
    }
    
    public void archiveReceipt(byte[] receipt, String invoiceId) {
        // Simple archiving logic for receipts
        System.out.println("Archiving receipt for invoice: " + invoiceId);
    }
} 