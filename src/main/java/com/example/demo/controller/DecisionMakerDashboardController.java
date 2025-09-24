package com.example.demo.controller;

import com.example.demo.dto.dashboard.*;
import com.example.demo.service.DecisionMakerDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/decision-maker/dashboard")
@PreAuthorize("hasRole('DECISION_MAKER')")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DecisionMakerDashboardController {

    @Autowired
    private DecisionMakerDashboardService dashboardService;

    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> getKPIs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(dashboardService.getKPIs(startDate, endDate));
    }

    @GetMapping("/revenue-trend")
    public ResponseEntity<List<RevenueTrendDTO>> getRevenueTrend(
            @RequestParam String period,
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(dashboardService.getRevenueTrend(period, region, months));
    }

    @GetMapping("/performance-metrics")
    public ResponseEntity<PerformanceMetricsDTO> getPerformanceMetrics() {
        return ResponseEntity.ok(dashboardService.getPerformanceMetrics());
    }

    @GetMapping("/comparative-analysis")
    public ResponseEntity<ComparativeAnalysisDTO> getComparativeAnalysis(
            @RequestParam(required = false) String dimension,
            @RequestParam(required = false) String metric,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // Set default dates if not provided
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(12);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        return ResponseEntity.ok(dashboardService.getComparativeAnalysis(
            dimension != null ? dimension : "default", 
            metric != null ? metric : "revenue",
            startDate, 
            endDate
        ));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportDashboardData(
            @RequestParam String format,
            @RequestParam(required = false) String reportType) {
        byte[] report = dashboardService.exportDashboardData(format, reportType);
        return ResponseEntity.ok()
                .header("Content-Type", "application/octet-stream")
                .header("Content-Disposition", "attachment; filename=\"dashboard-export." + format.toLowerCase())
                .body(report);
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<AlertDTO>> getAlerts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getAlerts(limit));
    }

    @GetMapping("/predictive-analytics")
    public ResponseEntity<PredictiveAnalyticsDTO> getPredictiveAnalytics(
            @RequestParam(defaultValue = "6") int forecastMonths) {
        return ResponseEntity.ok(dashboardService.getPredictiveAnalytics(forecastMonths));
    }

    @PostMapping("/custom-report")
    public ResponseEntity<byte[]> generateCustomReport(
            @RequestBody ReportRequestDTO reportRequest) {
        byte[] report = dashboardService.generateCustomReport(reportRequest);
        return ResponseEntity.ok()
                .header("Content-Type", "application/octet-stream")
                .header("Content-Disposition", "attachment; filename=\"custom-report.pdf\"")
                .body(report);
    }
}
