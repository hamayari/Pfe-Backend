package com.example.demo.dto.convention;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;
import com.example.demo.model.PaymentTerms;

@Data
public class ConventionRequest {
    @NotBlank
    private String title;
    
    private String description;
    
    @NotBlank
    private String clientId;
    
    private String client;
    
    private String reference;
    
    @NotNull
    private BigDecimal amount;
    
    private String currency = "TND";
    
    @NotNull
    private LocalDateTime startDate;
    
    @NotNull
    private LocalDateTime endDate;
    
    private LocalDate dueDate;
    
    private String status = "DRAFT";
    
    private String terms;
    
    private String notes;
    
    private String structure;
    
    private String geographicZone;
    
    private PaymentTerms paymentTerms;
    
    private String type;
    
    private String tag;
}
