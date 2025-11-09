package com.example.demo.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportDTO {
    private String month;
    private String monthName;
    private Integer conventionsCount;
    private Double totalAmount;
}
