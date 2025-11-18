package com.example.demo.service.impl;

import com.example.demo.dto.dashboard.*;
import com.example.demo.dto.dashboard.AlertSeverity;
import com.example.demo.service.DecisionMakerDashboardService;
import com.example.demo.repository.KpiAlertRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@Transactional(readOnly = true)
public class DecisionMakerDashboardServiceImpl implements DecisionMakerDashboardService {
    
    @Autowired
    private KpiAlertRepository kpiAlertRepository;

    @Override
    public Map<String, Object> getKPIs(LocalDate startDate, LocalDate endDate) {
        // In a real implementation, this would fetch data from repositories
        // For now, we'll return mock data
        Map<String, Object> kpis = new LinkedHashMap<>();
        
        // Financial KPIs
        kpis.put("totalRevenue", generateRandomValue(1000000, 5000000));
        kpis.put("revenueGrowth", generateRandomValue(5, 20, 2));
        kpis.put("grossProfit", generateRandomValue(400000, 2000000));
        kpis.put("profitMargin", generateRandomValue(15, 30, 2));
        kpis.put("ebitda", generateRandomValue(300000, 1500000));
        
        // Operational KPIs
        kpis.put("customerCount", generateRandomValue(500, 5000));
        kpis.put("newCustomers", generateRandomValue(50, 500));
        kpis.put("churnRate", generateRandomValue(1, 10, 2));
        kpis.put("customerLifetimeValue", generateRandomValue(5000, 50000));
        kpis.put("averageDealSize", generateRandomValue(10000, 100000));
        
        // Performance KPIs
        kpis.put("conversionRate", generateRandomValue(5, 30, 2));
        kpis.put("averageResponseTime", generateRandomValue(1, 24));
        kpis.put("customerSatisfaction", generateRandomValue(70, 95));
        kpis.put("employeeProductivity", generateRandomValue(75, 100));
        
        // Add trend data
        kpis.put("revenueTrend", generateTrendData(12, 100000, 5000000, true));
        kpis.put("customerTrend", generateTrendData(12, 100, 5000, true));
        
        // Add date range information
        kpis.put("startDate", startDate != null ? startDate : LocalDate.now().minusMonths(12));
        kpis.put("endDate", endDate != null ? endDate : LocalDate.now());
        
        // Add last updated timestamp
        kpis.put("lastUpdated", new Date());
        
        return kpis;
    }

    @Override
    public List<RevenueTrendDTO> getRevenueTrend(String period, String region, int months) {
        // In a real implementation, this would fetch data from repositories based on parameters
        List<RevenueTrendDTO> trends = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        for (int i = months - 1; i >= 0; i--) {
            LocalDate periodDate = now.minusMonths(i);
            RevenueTrendDTO trend = new RevenueTrendDTO();
            trend.setPeriod(periodDate);
            trend.setRevenue(BigDecimal.valueOf(generateRandomValue(100000, 1000000)));
            trend.setTarget(BigDecimal.valueOf(generateRandomValue(120000, 1200000)));
            trend.setPreviousPeriodRevenue(BigDecimal.valueOf(generateRandomValue(90000, 900000)));
            trend.setYoyGrowth(BigDecimal.valueOf(generateRandomValue(-10, 30, 2)));
            trend.setRegion(region != null ? region : "Global");
            trend.setProductCategory("All Products");
            trends.add(trend);
        }
        
        return trends;
    }

    @Override
    public PerformanceMetricsDTO getPerformanceMetrics() {
        PerformanceMetricsDTO metrics = new PerformanceMetricsDTO();
        
        // Set basic metrics
        metrics.setRevenueGrowthRate(BigDecimal.valueOf(generateRandomValue(5, 25, 2)));
        metrics.setProfitMargin(BigDecimal.valueOf(generateRandomValue(10, 30, 2)));
        metrics.setCustomerAcquisitionCost(BigDecimal.valueOf(generateRandomValue(500, 5000)));
        metrics.setCustomerLifetimeValue(BigDecimal.valueOf(generateRandomValue(10000, 100000)));
        metrics.setConversionRate(BigDecimal.valueOf(generateRandomValue(1, 20, 2)));
        metrics.setAverageDealSize(BigDecimal.valueOf(generateRandomValue(5000, 50000)));
        metrics.setActiveCustomers((int)generateRandomValue(100, 5000));
        metrics.setNewCustomers((int)generateRandomValue(10, 500));
        metrics.setChurnedCustomers((int)generateRandomValue(5, 100));
        metrics.setNetPromoterScore(BigDecimal.valueOf(generateRandomValue(0, 100)));
        
        // Set metrics by region
        Map<String, BigDecimal> metricsByRegion = new HashMap<>();
        metricsByRegion.put("North", BigDecimal.valueOf(generateRandomValue(100000, 500000)));
        metricsByRegion.put("South", BigDecimal.valueOf(generateRandomValue(80000, 400000)));
        metricsByRegion.put("East", BigDecimal.valueOf(generateRandomValue(70000, 350000)));
        metricsByRegion.put("West", BigDecimal.valueOf(generateRandomValue(90000, 450000)));
        metrics.setMetricsByRegion(metricsByRegion);

        // Set metrics by product
        Map<String, BigDecimal> metricsByProduct = new HashMap<>();
        metricsByProduct.put("Product A", BigDecimal.valueOf(generateRandomValue(50000, 300000)));
        metricsByProduct.put("Product B", BigDecimal.valueOf(generateRandomValue(30000, 200000)));
        metricsByProduct.put("Product C", BigDecimal.valueOf(generateRandomValue(20000, 150000)));
        metricsByProduct.put("Product D", BigDecimal.valueOf(generateRandomValue(10000, 100000)));
        metrics.setMetricsByProduct(metricsByProduct);

        // Set metrics by segment
        Map<String, BigDecimal> metricsBySegment = new HashMap<>();
        metricsBySegment.put("Enterprise", BigDecimal.valueOf(generateRandomValue(100000, 500000)));
        metricsBySegment.put("SMB", BigDecimal.valueOf(generateRandomValue(50000, 250000)));
        metricsBySegment.put("Startup", BigDecimal.valueOf(generateRandomValue(10000, 100000)));
        metrics.setMetricsBySegment(metricsBySegment);
        
        return metrics;
    }

    @Override
    public ComparativeAnalysisDTO getComparativeAnalysis(String dimension, String metric, LocalDate startDate, LocalDate endDate) {
        log.info("Generating comparative analysis for dimension: {}, metric: {}, from {} to {}", 
                dimension, metric, startDate, endDate);
                
        // In a real implementation, this would fetch data from a repository
        // For now, we'll return mock data
        
        ComparativeAnalysisDTO analysis = new ComparativeAnalysisDTO();
        analysis.setDimension(dimension);
        analysis.setMetric(metric);
        
        // Mock comparison data
        List<ComparisonItemDTO> comparisonData = new ArrayList<>();
        comparisonData.add(createComparisonItem("Current Period", 1500000.0, 1200000.0, 25.0));
        comparisonData.add(createComparisonItem("Previous Period", 1200000.0, 1100000.0, 9.1));
        comparisonData.add(createComparisonItem("Same Period Last Year", 1300000.0, 1250000.0, 4.0));
        comparisonData.add(createComparisonItem("Budget", 1400000.0, 1400000.0, 0.0));
        analysis.setComparisonData(comparisonData);
        
        // Mock time series data
        List<TimeSeriesDataDTO> timeSeriesData = new ArrayList<>();
        LocalDate current = startDate;
        double value = 100000;
        
        while (!current.isAfter(endDate)) {
            TimeSeriesDataDTO dataPoint = new TimeSeriesDataDTO();
            dataPoint.setPeriod(current.toString());
            
            Map<String, BigDecimal> values = new HashMap<>();
            values.put("actual", BigDecimal.valueOf(value));
            values.put("forecast", BigDecimal.valueOf(value * 1.05));
            values.put("target", BigDecimal.valueOf(120000));
            
            dataPoint.setValues(values);
            timeSeriesData.add(dataPoint);
            
            value *= 1.1; // 10% growth each period
            current = current.plusMonths(1);
        }
        analysis.setTimeSeriesData(timeSeriesData);
        
        // Mock summary metrics
        Map<String, BigDecimal> summaryMetrics = new HashMap<>();
        summaryMetrics.put("yoyGrowth", BigDecimal.valueOf(25.0));
        summaryMetrics.put("qoqGrowth", BigDecimal.valueOf(9.1));
        summaryMetrics.put("varianceToTarget", BigDecimal.valueOf(7.1));
        summaryMetrics.put("marketShare", BigDecimal.valueOf(12.5));
        analysis.setSummaryMetrics(summaryMetrics);
        
        // Mock benchmarks
        List<BenchmarkDTO> benchmarks = new ArrayList<>();
        benchmarks.add(createBenchmark("Industry Average", 8.2, "Growth", -16.8));
        benchmarks.add(createBenchmark("Top Competitor", 28.5, "Growth", 3.5));
        benchmarks.add(createBenchmark("Market Leader", 32.1, "Growth", 7.1));
        analysis.setBenchmarks(benchmarks);
        
        // Mock segment performance
        List<SegmentPerformanceDTO> segmentPerformance = new ArrayList<>();
        segmentPerformance.add(createSegmentPerformance("Product A", 450000.0, 400000.0, 12.5, "exceeding"));
        segmentPerformance.add(createSegmentPerformance("Product B", 350000.0, 300000.0, 16.7, "exceeding"));
        segmentPerformance.add(createSegmentPerformance("Product C", 250000.0, 200000.0, 25.0, "exceeding"));
        segmentPerformance.add(createSegmentPerformance("Product D", 150000.0, 200000.0, -25.0, "underperforming"));
        analysis.setSegmentPerformance(segmentPerformance);
        
        return analysis;
    }

    @Override
    public byte[] exportDashboardData(String format, String reportType) {
        // In a real implementation, this would generate a file in the specified format
        // For now, return a simple byte array as a placeholder
        String content = "Dashboard Export - " + new Date() + "\n";
        content += "Format: " + format + "\n";
        content += "Report Type: " + (reportType != null ? reportType : "Standard") + "\n";
        
        // Add some sample data
        content += "\nKey Metrics:\n";
        content += String.format("%-20s %15s\n", "Metric", "Value");
        content += String.format("%-20s %15s\n", "------------", "-------------");
        content += String.format("%-20s %,15.2f\n", "Total Revenue", 1500000.0);
        content += String.format("%-20s %,15.2f\n", "Profit Margin", 25.5);
        content += String.format("%-20s %,15d\n", "Customers", 1245);
        content += String.format("%-20s %,15.2f\n", "Growth Rate", 12.3);
        
        return content.getBytes();
    }

    @Override
    public List<AlertDTO> getAlerts(int limit) {
        // ✅ Récupérer les VRAIES alertes depuis MongoDB
        // Filtrer pour afficher UNIQUEMENT les alertes de factures PENDING
        List<AlertDTO> alerts = new ArrayList<>();
        
        try {
            // Récupérer les alertes KPI depuis le repository
            List<com.example.demo.model.KpiAlert> kpiAlerts = kpiAlertRepository
                .findByAlertStatus("PENDING_DECISION");
            
            // Filtrer pour garder UNIQUEMENT les alertes de factures PENDING
            for (com.example.demo.model.KpiAlert kpiAlert : kpiAlerts) {
                // ✅ Afficher UNIQUEMENT les alertes de type FACTURE_PENDING
                if ("FACTURE_PENDING".equals(kpiAlert.getKpiName())) {
                    AlertDTO alert = new AlertDTO();
                    alert.setId(kpiAlert.getId());
                    alert.setTitle("Facture en attente: " + kpiAlert.getDimensionValue());
                    alert.setDescription(kpiAlert.getMessage());
                    
                    // Convertir la severity String en enum AlertSeverity
                    AlertSeverity severity = convertSeverity(kpiAlert.getSeverity());
                    alert.setSeverity(severity);
                    
                    alert.setTimestamp(kpiAlert.getDetectedAt());
                    alert.setSource("Factures");
                    alert.setActionUrl("/invoices/" + kpiAlert.getRelatedInvoiceId());
                    alert.setCategory("Invoice");
                    alert.setAcknowledged(false);
                    
                    // Ajouter les métadonnées de la facture
                    alert.setMetadata(kpiAlert.getMetadata());
                    
                    alerts.add(alert);
                }
            }
            
            System.out.println("✅ " + alerts.size() + " alertes de factures PENDING récupérées pour le Décideur");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur récupération alertes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return alerts.stream().limit(limit).collect(Collectors.toList());
    }
    
    /**
     * Convertir la severity String en enum AlertSeverity
     */
    private AlertSeverity convertSeverity(String severity) {
        if (severity == null) {
            return AlertSeverity.INFO;
        }
        
        switch (severity.toUpperCase()) {
            case "CRITICAL":
            case "HIGH":
                return AlertSeverity.HIGH;
            case "MEDIUM":
                return AlertSeverity.MEDIUM;
            case "LOW":
                return AlertSeverity.LOW;
            default:
                return AlertSeverity.INFO;
        }
    }

    @Override
    public PredictiveAnalyticsDTO getPredictiveAnalytics(int forecastMonths) {
        // In a real implementation, this would use a predictive model
        PredictiveAnalyticsDTO analytics = new PredictiveAnalyticsDTO();
        analytics.setForecastType("Revenue Forecast");
        analytics.setConfidenceLevel(BigDecimal.valueOf(0.85));
        analytics.setLastUpdated(LocalDate.now().toString());
        analytics.setNextUpdate(LocalDate.now().plusDays(7).toString());
        
        // Generate forecast data
        List<ForecastPointDTO> forecastData = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        double baseValue = 1500000;
        
        for (int i = 0; i < forecastMonths; i++) {
            LocalDate period = currentDate.plusMonths(i + 1);
            ForecastPointDTO point = new ForecastPointDTO();
            point.setPeriod(period.toString());
            double value = baseValue * (1 + (i * 0.1));
            point.setForecastValue(BigDecimal.valueOf(value));
            point.setLowerBound(BigDecimal.valueOf(value * 0.95));
            point.setUpperBound(BigDecimal.valueOf(value * 1.05));
            point.setActualValue(null);
            point.setStatus("FORECAST");
            forecastData.add(point);
        }
        analytics.setForecastData(forecastData);
        
        // Generate scenario analysis
        List<ScenarioAnalysisDTO> scenarios = new ArrayList<>();
        
        // Optimistic scenario
        scenarios.add(createScenario(
            "Optimistic",
            "Market conditions improve, leading to higher than expected growth",
            1.2,  // 20% increase
            0.3,   // 30% probability
            Arrays.asList(
                "Market growth rate increases by 15%",
                "Customer acquisition cost decreases by 10%",
                "Average order value increases by 8%"
            )
        ));
        analytics.setScenarios(scenarios);
        
        // Generate key drivers
        List<KeyDriverDTO> keyDrivers = new ArrayList<>();
        
        // Market Expansion driver
        keyDrivers.add(createKeyDriver(
            "Market Expansion",
            "Expansion into new geographic markets",
            0.85,
            "Positive",
            0.75,
            Arrays.asList("Market Share", "Customer Acquisition Cost", "Revenue Growth")
        ));
        
        // Product Adoption driver
        keyDrivers.add(createKeyDriver(
            "Product Adoption",
            "Adoption rate of new product features",
            0.75,
            "Positive",
            0.80,
            Arrays.asList("Feature Usage", "Customer Satisfaction", "Renewal Rate")
        ));
        
        // Competitive Pressure driver
        keyDrivers.add(createKeyDriver(
            "Competitive Pressure",
            "Increased competition in core markets",
            0.65,
            "Negative",
            0.70,
            Arrays.asList("Market Share", "Competitive Index", "Churn Rate")
        ));
        
        analytics.setKeyDrivers(keyDrivers);
        
        // Set model metrics
        Map<String, Object> modelMetrics = new HashMap<>();
        modelMetrics.put("rSquared", 0.92);
        modelMetrics.put("mape", 0.08);
        modelMetrics.put("lastTrained", LocalDateTime.now().minusDays(7).toString());
        analytics.setModelMetrics(modelMetrics);
        
        return analytics;
    }
    
    private AlertDTO createAlert(String id, String title, String description, 
                               String severity, LocalDateTime timestamp,
                               String source, String actionUrl, String category) {
        AlertDTO alert = new AlertDTO();
        alert.setId(id);
        alert.setTitle(title);
        alert.setDescription(description);
        alert.setSeverity(AlertSeverity.valueOf(severity));
        alert.setTimestamp(timestamp);
        alert.setSource(source);
        alert.setActionUrl(actionUrl);
        alert.setCategory(category);
        alert.setAcknowledged(false);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("priority", AlertSeverity.valueOf(severity).ordinal());
        metadata.put("assignedTo", "CRITICAL".equals(severity) ? "admin" : "team");
        metadata.put("sla", "CRITICAL".equals(severity) ? "4h" : "24h");
        alert.setMetadata(metadata);
        
        return alert;
    }
    
    private SegmentPerformanceDTO createSegmentPerformance(String segmentName, double value, double target, double variance, String status) {
        SegmentPerformanceDTO segment = new SegmentPerformanceDTO();
        segment.setSegmentName(segmentName);
        segment.setValue(BigDecimal.valueOf(value));
        segment.setTarget(BigDecimal.valueOf(target));
        segment.setVariance(BigDecimal.valueOf(variance));
        segment.setStatus(status);
        return segment;
    }
    private ScenarioAnalysisDTO createScenario(
            String name, String description, double multiplier, double probability, List<String> assumptions) {
        ScenarioAnalysisDTO scenario = new ScenarioAnalysisDTO();
        scenario.setScenarioName(name);
        scenario.setDescription(description);
        scenario.setProbability(BigDecimal.valueOf(probability));
        scenario.setAssumptions(assumptions);
        
        // Calculate projected value based on multiplier
        double baseValue = 1500000.0;
        double projectedValue = baseValue * multiplier;
        scenario.setProjectedValue(BigDecimal.valueOf(projectedValue));
        
        // Set impact analysis
        Map<String, Object> impact = new HashMap<>();
        impact.put("financialImpact", multiplier >= 1.0 ? "positive" : "negative");
        impact.put("confidenceLevel", probability >= 0.7 ? "high" : probability >= 0.4 ? "medium" : "low");
        impact.put("timeHorizon", "6-12 months");
        scenario.setImpactAnalysis(impact);
        
        return scenario;
    }
    private KeyDriverDTO createKeyDriver(
            String name, String description, double impactScore, 
            String direction, double confidence, List<String> relatedMetrics) {
        KeyDriverDTO driver = new KeyDriverDTO();
        driver.setDriverName(name);
        driver.setDescription(description);
        driver.setImpactScore(BigDecimal.valueOf(impactScore));
        driver.setImpactDirection(direction);
        driver.setConfidence(BigDecimal.valueOf(confidence));
        driver.setRelatedMetrics(relatedMetrics);
        return driver;
    }
    
    // Helper methods for generating random data
    private double generateRandomValue(double min, double max) {
        return min + (Math.random() * (max - min));
    }
    
    private double generateRandomValue(int min, int max) {
        return min + (int)(Math.random() * (max - min));
    }
    
    private double generateRandomValue(int min, int max, int decimalPlaces) {
        double value = min + (Math.random() * (max - min));
        double scale = Math.pow(10, decimalPlaces);
        return Math.round(value * scale) / scale;
    }
    
    private List<Map<String, Object>> generateTrendData(int months, double min, double max, boolean includeTarget) {
        return IntStream.range(0, months)
                .mapToObj(i -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("month", LocalDate.now().minusMonths(months - i - 1).getMonth().toString());
                    data.put("value", generateRandomValue(min, max));
                    if (includeTarget) {
                        data.put("target", generateRandomValue(min * 1.1, max * 1.1));
                    }
                    return data;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public byte[] generateCustomReport(ReportRequestDTO reportRequest) {
        // In a real implementation, this would generate a report based on the request
        // For now, we'll return a simple message
        String reportContent = "Custom report generated for " + reportRequest.getReportType() + 
                             " from " + reportRequest.getStartDate() + " to " + reportRequest.getEndDate();
        return reportContent.getBytes();
    }
    
    private ComparisonItemDTO createComparisonItem(String name, double current, double previous, double percentageChange) {
        ComparisonItemDTO item = new ComparisonItemDTO();
        item.setName(name);
        item.setCurrentValue(BigDecimal.valueOf(current));
        item.setPreviousValue(BigDecimal.valueOf(previous));
        item.setPercentageChange(BigDecimal.valueOf(percentageChange));
        // Determine trend based on percentage change
        String trend = percentageChange > 0 ? "up" : percentageChange < 0 ? "down" : "stable";
        item.setTrend(trend);
        return item;
    }
    
    private BenchmarkDTO createBenchmark(String name, double value, String category, double differenceFromCurrent) {
        BenchmarkDTO benchmark = new BenchmarkDTO();
        benchmark.setName(name);
        benchmark.setValue(BigDecimal.valueOf(value));
        benchmark.setCategory(category);
        benchmark.setDifferenceFromCurrent(BigDecimal.valueOf(differenceFromCurrent));
        return benchmark;
    }
}
