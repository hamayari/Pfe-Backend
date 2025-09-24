package com.example.demo.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueTrendDTO {
    private LocalDate period;
    private BigDecimal revenue;
    private BigDecimal target;
    private BigDecimal previousPeriodRevenue;
    private BigDecimal yoyGrowth;
    private String region;
    private String productCategory;
}
