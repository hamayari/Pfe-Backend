package com.example.demo.dto;

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
}







































