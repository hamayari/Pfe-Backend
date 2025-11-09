package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
public class InvoiceTrackingDTO {
    private String id;
    private String invoiceNumber;
    private String conventionReference;
    private LocalDate dueDate;
    private BigDecimal amount;
    private String status;
    private String structureName;
    private String commercialName;
    private int daysOverdue;
    private boolean isOverdue;
    private LocalDate paymentDate;
    private String paymentMethod;
}






































