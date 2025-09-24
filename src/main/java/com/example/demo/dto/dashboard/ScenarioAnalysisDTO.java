package com.example.demo.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioAnalysisDTO {
    private String scenarioName;
    private String description;
    private BigDecimal projectedValue;
    private BigDecimal probability;
    private List<String> assumptions;
    private Map<String, Object> impactAnalysis;
}
