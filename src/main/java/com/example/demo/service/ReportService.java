package com.example.demo.service;

import com.example.demo.dto.KPIMetricsDTO;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    public KPIMetricsDTO getKPIMetrics() {
        // Simulation d'une récupération de KPI depuis la base
        KPIMetricsDTO kpi = new KPIMetricsDTO();
        kpi.setTotalConventions(100);
        kpi.setActiveConventions(80);
        kpi.setExpiredConventions(20);
        kpi.setTotalInvoices(200);
        kpi.setPaidInvoices(150);
        kpi.setOverdueInvoices(10);
        kpi.setMonthlyRevenue(50000.0);
        return kpi;
    }
} 