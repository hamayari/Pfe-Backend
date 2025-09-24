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
public class ComparativeAnalysisDTO {
    private String dimension;
    private String metric;
    private List<ComparisonItemDTO> comparisonData;
    private Map<String, BigDecimal> summaryMetrics;
    private List<TimeSeriesDataDTO> timeSeriesData;
    private List<BenchmarkDTO> benchmarks;
    private List<SegmentPerformanceDTO> segmentPerformance;
}
