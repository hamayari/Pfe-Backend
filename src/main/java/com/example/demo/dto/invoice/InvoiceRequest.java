package com.example.demo.dto.invoice;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class InvoiceRequest {
    @NotBlank
    private String clientId;
    
    @NotBlank
    private String description;
    
    @NotNull
    private BigDecimal amount;
    
    private String currency = "TND";
    
    @NotNull
    private LocalDateTime dueDate;
    
    private String status = "PENDING";
    
    private String notes;
    
    private String conventionId;
    
    // Additional fields for compatibility
    private String reference;
    private String clientEmail;
    
    // Alias methods for compatibility
    public String getReference() {
        return reference;
    }
    
    public String getClientEmail() {
        return clientEmail;
    }
}
