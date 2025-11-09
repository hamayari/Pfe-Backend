package com.example.demo.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GovernorateReportDTO {
    private String governorate;
    private Integer count;
    private Double totalAmount;
    private Integer activeCount;
    private Integer expiredCount;
}
