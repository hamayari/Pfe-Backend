package com.example.demo.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SegmentPerformanceDTO {
    private String segmentName;
    private BigDecimal value;
    private BigDecimal target;
    private BigDecimal variance;
    private String status;
}
