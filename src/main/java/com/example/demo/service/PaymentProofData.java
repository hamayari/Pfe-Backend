package com.example.demo.service;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentProofData {
    private String reference;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String description;
    private String rawText;
    private boolean hasDigitalSignature;
    
    // Additional fields for OCR processing
    private String extractedReference;
    private Double extractedAmount;
    private LocalDate extractedDate;
    private String bankName;
    private String accountNumber;
    private String transactionId;
} 