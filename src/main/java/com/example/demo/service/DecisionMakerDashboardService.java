package com.example.demo.service;

import com.example.demo.dto.dashboard.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DecisionMakerDashboardService {
    
    Map<String, Object> getKPIs(LocalDate startDate, LocalDate endDate);
    
    List<RevenueTrendDTO> getRevenueTrend(String period, String region, int months);
    
    PerformanceMetricsDTO getPerformanceMetrics();
    
    ComparativeAnalysisDTO getComparativeAnalysis(String dimension, String metric, LocalDate startDate, LocalDate endDate);
    
    byte[] exportDashboardData(String format, String reportType);
    
    List<AlertDTO> getAlerts(int limit);
    
    PredictiveAnalyticsDTO getPredictiveAnalytics(int forecastMonths);
    
    byte[] generateCustomReport(ReportRequestDTO reportRequest);
}
