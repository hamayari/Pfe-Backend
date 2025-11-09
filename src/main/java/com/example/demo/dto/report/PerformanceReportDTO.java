package com.example.demo.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReportDTO {
    private Integer totalConventions;
    private Integer totalInvoices;
    private Integer paidInvoices;
    private Integer overdueInvoices;
    private Double paymentRate;
    private Double overdueRate;
    private Double averageConventionAmount;
}
