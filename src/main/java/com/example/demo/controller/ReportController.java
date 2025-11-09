package com.example.demo.controller;

import com.example.demo.dto.report.*;
import com.example.demo.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * GET /api/reports/financial
     * Rapport financier global
     */
    @GetMapping("/financial")
    public ResponseEntity<FinancialReportDTO> getFinancialReport() {
        FinancialReportDTO report = reportService.getFinancialReport();
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/reports/performance
     * Rapport de performance
     */
    @GetMapping("/performance")
    public ResponseEntity<PerformanceReportDTO> getPerformanceReport() {
        PerformanceReportDTO report = reportService.getPerformanceReport();
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/reports/by-governorate
     * Rapport par gouvernorat
     */
    @GetMapping("/by-governorate")
    public ResponseEntity<List<GovernorateReportDTO>> getReportByGovernorate() {
        List<GovernorateReportDTO> report = reportService.getReportByGovernorate();
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/reports/by-month
     * Rapport par mois
     */
    @GetMapping("/by-month")
    public ResponseEntity<List<MonthlyReportDTO>> getReportByMonth() {
        List<MonthlyReportDTO> report = reportService.getReportByMonth();
        return ResponseEntity.ok(report);
    }
}
