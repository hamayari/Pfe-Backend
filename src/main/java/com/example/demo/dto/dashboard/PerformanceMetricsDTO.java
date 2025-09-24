package com.example.demo.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceMetricsDTO {
    private BigDecimal revenueGrowthRate;
    private BigDecimal profitMargin;
    private BigDecimal customerAcquisitionCost;
    private BigDecimal customerLifetimeValue;
    private BigDecimal conversionRate;
    private BigDecimal averageDealSize;
    private int activeCustomers;
    private int newCustomers;
    private int churnedCustomers;
    private BigDecimal netPromoterScore;
    private Map<String, BigDecimal> metricsByRegion;
    private Map<String, BigDecimal> metricsByProduct;
    private Map<String, BigDecimal> metricsBySegment;
}
