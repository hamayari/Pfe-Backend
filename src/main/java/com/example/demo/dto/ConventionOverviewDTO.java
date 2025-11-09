package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
public class ConventionOverviewDTO {
    private String id;
    private String reference;
    private String label;
    private LocalDate startDate;
    private LocalDate endDate;
    private String structureName;
    private String governorate;
    private String status;
    private BigDecimal amount;
    private String commercialName;
    private int daysUntilExpiry;
    private boolean isOverdue;
}






































