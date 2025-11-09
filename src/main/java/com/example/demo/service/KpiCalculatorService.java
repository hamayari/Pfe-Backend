package com.example.demo.service;

import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de calcul automatique des KPI
 */
@Service
public class KpiCalculatorService {
    
    @Autowired
    private ConventionRepository conventionRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    /**
     * Calcule tous les KPI globaux
     */
    public Map<String, KpiResult> calculateGlobalKpis() {
        Map<String, KpiResult> kpis = new HashMap<>();
        
        List<Invoice> allInvoices = invoiceRepository.findAll();
        List<Convention> allConventions = conventionRepository.findAll();
        
        // 1. Taux de retard
        kpis.put("TAUX_RETARD", calculateRetardRate(allInvoices));
        
        // 2. Taux de paiement
        kpis.put("TAUX_PAIEMENT", calculatePaymentRate(allInvoices));
        
        // 3. Montant non payé (%)
        kpis.put("MONTANT_IMPAYE_PERCENT", calculateUnpaidAmountPercent(allInvoices));
        
        // 4. Durée moyenne de paiement
        kpis.put("DUREE_MOYENNE_PAIEMENT", calculateAveragePaymentTime(allInvoices));
        
        // 5. Taux de conversion
        kpis.put("TAUX_CONVERSION", calculateConversionRate(allConventions));
        
        return kpis;
    }
    
    /**
     * Calcule les KPI par gouvernorat
     */
    public Map<String, Map<String, KpiResult>> calculateKpisByGouvernorat() {
        Map<String, Map<String, KpiResult>> kpisByRegion = new HashMap<>();
        
        List<Convention> allConventions = conventionRepository.findAll();
        
        // Grouper par gouvernorat
        Map<String, List<Convention>> conventionsByGov = allConventions.stream()
            .filter(c -> c.getGovernorate() != null)
            .collect(Collectors.groupingBy(Convention::getGovernorate));
        
        for (Map.Entry<String, List<Convention>> entry : conventionsByGov.entrySet()) {
            String gouvernorat = entry.getKey();
            List<Convention> conventions = entry.getValue();
            
            // Récupérer les factures liées
            List<String> conventionIds = conventions.stream()
                .map(Convention::getId)
                .collect(Collectors.toList());
            
            List<Invoice> invoices = invoiceRepository.findAll().stream()
                .filter(inv -> conventionIds.contains(inv.getConventionId()))
                .collect(Collectors.toList());
            
            Map<String, KpiResult> kpis = new HashMap<>();
            kpis.put("TAUX_RETARD", calculateRetardRate(invoices));
            kpis.put("TAUX_PAIEMENT", calculatePaymentRate(invoices));
            kpis.put("MONTANT_IMPAYE_PERCENT", calculateUnpaidAmountPercent(invoices));
            kpis.put("DUREE_MOYENNE_PAIEMENT", calculateAveragePaymentTime(invoices));
            
            kpisByRegion.put(gouvernorat, kpis);
        }
        
        return kpisByRegion;
    }
    
    /**
     * Calcule les KPI par structure
     */
    public Map<String, Map<String, KpiResult>> calculateKpisByStructure() {
        Map<String, Map<String, KpiResult>> kpisByStructure = new HashMap<>();
        
        List<Convention> allConventions = conventionRepository.findAll();
        
        // Grouper par structure
        Map<String, List<Convention>> conventionsByStruct = allConventions.stream()
            .filter(c -> c.getStructureId() != null)
            .collect(Collectors.groupingBy(Convention::getStructureId));
        
        for (Map.Entry<String, List<Convention>> entry : conventionsByStruct.entrySet()) {
            String structure = entry.getKey();
            List<Convention> conventions = entry.getValue();
            
            // Récupérer les factures liées
            List<String> conventionIds = conventions.stream()
                .map(Convention::getId)
                .collect(Collectors.toList());
            
            List<Invoice> invoices = invoiceRepository.findAll().stream()
                .filter(inv -> conventionIds.contains(inv.getConventionId()))
                .collect(Collectors.toList());
            
            Map<String, KpiResult> kpis = new HashMap<>();
            kpis.put("TAUX_RETARD", calculateRetardRate(invoices));
            kpis.put("TAUX_PAIEMENT", calculatePaymentRate(invoices));
            kpis.put("MONTANT_IMPAYE_PERCENT", calculateUnpaidAmountPercent(invoices));
            kpis.put("DUREE_MOYENNE_PAIEMENT", calculateAveragePaymentTime(invoices));
            
            kpisByStructure.put(structure, kpis);
        }
        
        return kpisByStructure;
    }
    
    // ==================== CALCULS INDIVIDUELS ====================
    
    /**
     * Taux de retard = (factures en retard / total factures) × 100
     */
    private KpiResult calculateRetardRate(List<Invoice> invoices) {
        if (invoices.isEmpty()) {
            return new KpiResult(0.0, "%", "Aucune facture");
        }
        
        LocalDate today = LocalDate.now();
        long overdueCount = invoices.stream()
            .filter(inv -> inv.getDueDate() != null)
            .filter(inv -> inv.getDueDate().isBefore(today))
            .filter(inv -> !"PAID".equalsIgnoreCase(inv.getStatus()))
            .count();
        
        double rate = (overdueCount * 100.0) / invoices.size();
        
        return new KpiResult(
            Math.round(rate * 10.0) / 10.0,
            "%",
            overdueCount + " facture(s) en retard sur " + invoices.size()
        );
    }
    
    /**
     * Taux de paiement = (factures payées / total factures) × 100
     */
    private KpiResult calculatePaymentRate(List<Invoice> invoices) {
        if (invoices.isEmpty()) {
            return new KpiResult(0.0, "%", "Aucune facture");
        }
        
        long paidCount = invoices.stream()
            .filter(inv -> "PAID".equalsIgnoreCase(inv.getStatus()))
            .count();
        
        double rate = (paidCount * 100.0) / invoices.size();
        
        return new KpiResult(
            Math.round(rate * 10.0) / 10.0,
            "%",
            paidCount + " facture(s) payée(s) sur " + invoices.size()
        );
    }
    
    /**
     * Montant non payé (%) = (montant impayé / total facturé) × 100
     */
    private KpiResult calculateUnpaidAmountPercent(List<Invoice> invoices) {
        if (invoices.isEmpty()) {
            return new KpiResult(0.0, "%", "Aucune facture");
        }
        
        BigDecimal totalAmount = invoices.stream()
            .map(inv -> inv.getAmount() != null ? inv.getAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal unpaidAmount = invoices.stream()
            .filter(inv -> !"PAID".equalsIgnoreCase(inv.getStatus()))
            .map(inv -> inv.getAmount() != null ? inv.getAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return new KpiResult(0.0, "%", "Montant total = 0");
        }
        
        double percent = unpaidAmount.multiply(BigDecimal.valueOf(100))
            .divide(totalAmount, 2, RoundingMode.HALF_UP)
            .doubleValue();
        
        return new KpiResult(
            Math.round(percent * 10.0) / 10.0,
            "%",
            unpaidAmount.doubleValue() + " DT impayé sur " + totalAmount.doubleValue() + " DT"
        );
    }
    
    /**
     * Durée moyenne de paiement = moyenne(nombre de jours entre émission et paiement)
     */
    private KpiResult calculateAveragePaymentTime(List<Invoice> invoices) {
        List<Invoice> paidInvoices = invoices.stream()
            .filter(inv -> "PAID".equalsIgnoreCase(inv.getStatus()))
            .filter(inv -> inv.getIssueDate() != null && inv.getPaymentDate() != null)
            .collect(Collectors.toList());
        
        if (paidInvoices.isEmpty()) {
            return new KpiResult(0.0, "jours", "Aucune facture payée avec dates");
        }
        
        double averageDays = paidInvoices.stream()
            .mapToLong(inv -> ChronoUnit.DAYS.between(inv.getIssueDate(), inv.getPaymentDate()))
            .average()
            .orElse(0.0);
        
        return new KpiResult(
            Math.round(averageDays * 10.0) / 10.0,
            "jours",
            "Basé sur " + paidInvoices.size() + " facture(s) payée(s)"
        );
    }
    
    /**
     * Taux de conversion = (conventions actives / total conventions) × 100
     */
    private KpiResult calculateConversionRate(List<Convention> conventions) {
        if (conventions.isEmpty()) {
            return new KpiResult(0.0, "%", "Aucune convention");
        }
        
        long activeCount = conventions.stream()
            .filter(conv -> "ACTIVE".equalsIgnoreCase(conv.getStatus()))
            .count();
        
        double rate = (activeCount * 100.0) / conventions.size();
        
        return new KpiResult(
            Math.round(rate * 10.0) / 10.0,
            "%",
            activeCount + " convention(s) active(s) sur " + conventions.size()
        );
    }
    
    /**
     * Classe pour stocker le résultat d'un KPI
     */
    @Data
    public static class KpiResult {
        private Double value;
        private String unit;
        private String description;
        
        public KpiResult(Double value, String unit, String description) {
            this.value = value;
            this.unit = unit;
            this.description = description;
        }
    }
}
