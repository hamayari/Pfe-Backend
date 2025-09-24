package com.example.demo.service;

import com.example.demo.dto.KPIMetricsDTO;
import com.example.demo.dto.ConventionStatsDTO;
import com.example.demo.dto.InvoiceStatsDTO;
import org.springframework.stereotype.Service;
import java.util.HashMap;

/**
 * Version simplifiée du service commercial dashboard
 * Sans dépendances aux repositories problématiques
 */
@Service("commercialDashboardServiceSimple")
public class CommercialDashboardServiceSimple {

    public KPIMetricsDTO getKPIMetrics(String userId, String startDate, String endDate, 
                                     String structureId, String governorate) {
        KPIMetricsDTO kpi = new KPIMetricsDTO();
        
        // Données de démonstration fonctionnelles
        kpi.setTotalConventions(5);
        kpi.setActiveConventions(3);
        kpi.setExpiredConventions(2);
        kpi.setTotalInvoices(8);
        kpi.setPaidInvoices(5);
        kpi.setOverdueInvoices(3);
        kpi.setCollectionRate(62.5);
        kpi.setAveragePaymentTime(15.0);
        kpi.setMonthlyRevenue(12500.0);
        kpi.setPendingAmount(7500.0);
        
        return kpi;
    }

    public ConventionStatsDTO getConventionStats(String userId) {
        ConventionStatsDTO stats = new ConventionStatsDTO();
        
        stats.setTotal(5);
        stats.setActive(3);
        stats.setPending(1);
        stats.setExpired(1);
        
        // Statistiques par gouvernorat
        HashMap<String, Integer> byGovernorate = new HashMap<>();
        byGovernorate.put("Tunis", 2);
        byGovernorate.put("Sfax", 2);
        byGovernorate.put("Sousse", 1);
        stats.setByGovernorate(byGovernorate);
        
        // Statistiques par structure
        HashMap<String, Integer> byStructure = new HashMap<>();
        byStructure.put("Structure A", 3);
        byStructure.put("Structure B", 2);
        stats.setByStructure(byStructure);
        
        return stats;
    }

    public InvoiceStatsDTO getInvoiceStats(String userId) {
        InvoiceStatsDTO stats = new InvoiceStatsDTO();
        
        stats.setTotal(8);
        stats.setPaid(5);
        stats.setPending(2);
        stats.setOverdue(1);
        stats.setTotalAmount(20000.0);
        stats.setPaidAmount(12500.0);
        stats.setPendingAmount(5000.0);
        stats.setOverdueAmount(2500.0);
        
        return stats;
    }
}









