package com.example.demo.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialReportDTO {
    private Double totalRevenue;
    private Double paidAmount;
    private Double pendingAmount;
    private Double overdueAmount;
    private Double collectionRate;
}
