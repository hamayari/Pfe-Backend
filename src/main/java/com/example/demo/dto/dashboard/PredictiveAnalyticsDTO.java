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
public class PredictiveAnalyticsDTO {
    private String forecastType;
    private List<ForecastPointDTO> forecastData;
    private BigDecimal confidenceLevel;
    private List<ScenarioAnalysisDTO> scenarios;
    private List<KeyDriverDTO> keyDrivers;
    private Map<String, Object> modelMetrics;
    private String lastUpdated;
    private String nextUpdate;
}
