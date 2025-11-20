package com.example.demo.service;

import com.example.demo.dto.dashboard.*;
import com.example.demo.model.KpiAlert;
import com.example.demo.repository.KpiAlertRepository;
import com.example.demo.service.impl.DecisionMakerDashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DecisionMakerDashboardServiceTest {

    @Mock
    private KpiAlertRepository kpiAlertRepository;

    @InjectMocks
    private DecisionMakerDashboardServiceImpl service;

    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.now().minusMonths(1);
        endDate = LocalDate.now();
    }

    @Test
    void testGetKPIs_Success() {
        // When
        Map<String, Object> kpis = service.getKPIs(startDate, endDate);

        // Then
        assertNotNull(kpis);
        assertTrue(kpis.containsKey("totalRevenue"));
        assertTrue(kpis.containsKey("revenueGrowth"));
        assertTrue(kpis.containsKey("customerCount"));
        assertTrue(kpis.containsKey("conversionRate"));
        assertTrue(kpis.containsKey("startDate"));
        assertTrue(kpis.containsKey("endDate"));
        assertTrue(kpis.containsKey("lastUpdated"));
        
        assertEquals(startDate, kpis.get("startDate"));
        assertEquals(endDate, kpis.get("endDate"));
    }

    @Test
    void testGetKPIs_WithNullDates() {
        // When
        Map<String, Object> kpis = service.getKPIs(null, null);

        // Then
        assertNotNull(kpis);
        assertNotNull(kpis.get("startDate"));
        assertNotNull(kpis.get("endDate"));
    }

    @Test
    void testGetRevenueTrend_Success() {
        // Given
        String period = "monthly";
        String region = "North";
        int months = 6;

        // When
        List<RevenueTrendDTO> trends = service.getRevenueTrend(period, region, months);

        // Then
        assertNotNull(trends);
        assertEquals(months, trends.size());
        
        for (RevenueTrendDTO trend : trends) {
            assertNotNull(trend.getPeriod());
            assertNotNull(trend.getRevenue());
            assertNotNull(trend.getTarget());
            assertEquals(region, trend.getRegion());
        }
    }

    @Test
    void testGetRevenueTrend_WithNullRegion() {
        // When
        List<RevenueTrendDTO> trends = service.getRevenueTrend("monthly", null, 3);

        // Then
        assertNotNull(trends);
        assertEquals(3, trends.size());
        assertEquals("Global", trends.get(0).getRegion());
    }

    @Test
    void testGetPerformanceMetrics_Success() {
        // When
        PerformanceMetricsDTO metrics = service.getPerformanceMetrics();

        // Then
        assertNotNull(metrics);
        assertNotNull(metrics.getRevenueGrowthRate());
        assertNotNull(metrics.getProfitMargin());
        assertNotNull(metrics.getCustomerAcquisitionCost());
        assertNotNull(metrics.getCustomerLifetimeValue());
        assertNotNull(metrics.getConversionRate());
        assertNotNull(metrics.getMetricsByRegion());
        assertNotNull(metrics.getMetricsByProduct());
        assertNotNull(metrics.getMetricsBySegment());
        
        assertTrue(metrics.getMetricsByRegion().size() > 0);
        assertTrue(metrics.getMetricsByProduct().size() > 0);
        assertTrue(metrics.getMetricsBySegment().size() > 0);
    }

    @Test
    void testGetComparativeAnalysis_Success() {
        // Given
        String dimension = "region";
        String metric = "revenue";

        // When
        ComparativeAnalysisDTO analysis = service.getComparativeAnalysis(
            dimension, metric, startDate, endDate
        );

        // Then
        assertNotNull(analysis);
        assertEquals(dimension, analysis.getDimension());
        assertEquals(metric, analysis.getMetric());
        assertNotNull(analysis.getComparisonData());
        assertNotNull(analysis.getTimeSeriesData());
        assertNotNull(analysis.getSummaryMetrics());
        assertNotNull(analysis.getBenchmarks());
        assertNotNull(analysis.getSegmentPerformance());
        
        assertTrue(analysis.getComparisonData().size() > 0);
        assertTrue(analysis.getTimeSeriesData().size() > 0);
    }

    @Test
    void testExportDashboardData_CSV() {
        // Given
        String format = "CSV";
        String reportType = "summary";

        // When
        byte[] data = service.exportDashboardData(format, reportType);

        // Then
        assertNotNull(data);
        assertTrue(data.length > 0);
        
        String content = new String(data);
        assertTrue(content.contains("Dashboard Export"));
        assertTrue(content.contains(format));
        assertTrue(content.contains(reportType));
    }

    @Test
    void testExportDashboardData_PDF() {
        // When
        byte[] data = service.exportDashboardData("PDF", null);

        // Then
        assertNotNull(data);
        assertTrue(data.length > 0);
    }

    @Test
    void testGetAlerts_WithPendingInvoices() {
        // Given
        List<KpiAlert> mockAlerts = createMockKpiAlerts();
        when(kpiAlertRepository.findByAlertStatus("PENDING_DECISION"))
            .thenReturn(mockAlerts);

        // When
        List<AlertDTO> alerts = service.getAlerts(10);

        // Then
        assertNotNull(alerts);
        assertEquals(2, alerts.size()); // Only FACTURE_PENDING alerts
        
        AlertDTO alert = alerts.get(0);
        assertNotNull(alert.getId());
        assertNotNull(alert.getTitle());
        assertNotNull(alert.getDescription());
        assertNotNull(alert.getSeverity());
        assertEquals("Factures", alert.getSource());
        assertEquals("Invoice", alert.getCategory());
        assertFalse(alert.isAcknowledged());
        
        verify(kpiAlertRepository, times(1)).findByAlertStatus("PENDING_DECISION");
    }

    @Test
    void testGetAlerts_EmptyList() {
        // Given
        when(kpiAlertRepository.findByAlertStatus(anyString()))
            .thenReturn(Collections.emptyList());

        // When
        List<AlertDTO> alerts = service.getAlerts(10);

        // Then
        assertNotNull(alerts);
        assertTrue(alerts.isEmpty());
    }

    @Test
    void testGetAlerts_WithLimit() {
        // Given
        List<KpiAlert> mockAlerts = createManyMockKpiAlerts(20);
        when(kpiAlertRepository.findByAlertStatus("PENDING_DECISION"))
            .thenReturn(mockAlerts);

        // When
        List<AlertDTO> alerts = service.getAlerts(5);

        // Then
        assertNotNull(alerts);
        assertTrue(alerts.size() <= 5);
    }

    @Test
    void testGetPredictiveAnalytics_Success() {
        // Given
        int forecastMonths = 6;

        // When
        PredictiveAnalyticsDTO analytics = service.getPredictiveAnalytics(forecastMonths);

        // Then
        assertNotNull(analytics);
        assertNotNull(analytics.getForecastType());
        assertNotNull(analytics.getConfidenceLevel());
        assertNotNull(analytics.getForecastData());
        assertNotNull(analytics.getScenarios());
        assertNotNull(analytics.getKeyDrivers());
        assertNotNull(analytics.getModelMetrics());
        
        assertEquals(forecastMonths, analytics.getForecastData().size());
        assertTrue(analytics.getConfidenceLevel().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testGenerateCustomReport_Success() {
        // Given
        ReportRequestDTO request = new ReportRequestDTO();
        request.setReportType("financial");
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        // When
        byte[] report = service.generateCustomReport(request);

        // Then
        assertNotNull(report);
        assertTrue(report.length > 0);
        
        String content = new String(report);
        assertTrue(content.contains("financial"));
    }

    // Helper methods
    private List<KpiAlert> createMockKpiAlerts() {
        List<KpiAlert> alerts = new ArrayList<>();
        
        KpiAlert alert1 = new KpiAlert();
        alert1.setId("alert1");
        alert1.setKpiName("FACTURE_PENDING");
        alert1.setMessage("Facture en attente de validation");
        alert1.setSeverity("HIGH");
        alert1.setDetectedAt(LocalDateTime.now());
        alert1.setDimensionValue("INV-001");
        alert1.setRelatedInvoiceId("inv1");
        alert1.setMetadata(new HashMap<>());
        alerts.add(alert1);
        
        KpiAlert alert2 = new KpiAlert();
        alert2.setId("alert2");
        alert2.setKpiName("FACTURE_PENDING");
        alert2.setMessage("Facture en attente");
        alert2.setSeverity("MEDIUM");
        alert2.setDetectedAt(LocalDateTime.now());
        alert2.setDimensionValue("INV-002");
        alert2.setRelatedInvoiceId("inv2");
        alert2.setMetadata(new HashMap<>());
        alerts.add(alert2);
        
        // Add non-FACTURE_PENDING alert (should be filtered out)
        KpiAlert alert3 = new KpiAlert();
        alert3.setId("alert3");
        alert3.setKpiName("OTHER_KPI");
        alert3.setMessage("Other alert");
        alert3.setSeverity("LOW");
        alert3.setDetectedAt(LocalDateTime.now());
        alerts.add(alert3);
        
        return alerts;
    }

    private List<KpiAlert> createManyMockKpiAlerts(int count) {
        List<KpiAlert> alerts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            KpiAlert alert = new KpiAlert();
            alert.setId("alert" + i);
            alert.setKpiName("FACTURE_PENDING");
            alert.setMessage("Alert " + i);
            alert.setSeverity("MEDIUM");
            alert.setDetectedAt(LocalDateTime.now());
            alert.setDimensionValue("INV-" + i);
            alert.setRelatedInvoiceId("inv" + i);
            alert.setMetadata(new HashMap<>());
            alerts.add(alert);
        }
        return alerts;
    }
}
