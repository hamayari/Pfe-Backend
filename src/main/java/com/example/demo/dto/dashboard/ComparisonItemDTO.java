package com.example.demo.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonItemDTO {
    private String name;
    private BigDecimal currentValue;
    private BigDecimal previousValue;
    private BigDecimal percentageChange;
    private String trend;
}
