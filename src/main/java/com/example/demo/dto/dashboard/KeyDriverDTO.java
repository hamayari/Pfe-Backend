package com.example.demo.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyDriverDTO {
    private String driverName;
    private String description;
    private BigDecimal impactScore;
    private String impactDirection;
    private BigDecimal confidence;
    private List<String> relatedMetrics;
}
