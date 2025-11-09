package com.example.demo.service;

import com.example.demo.dto.KPIMetricsDTO;
import com.example.demo.dto.report.*;
import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ConventionRepository conventionRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

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

    /**
     * Rapport financier global
     */
    public FinancialReportDTO getFinancialReport() {
        List<Convention> conventions = conventionRepository.findAll();
        List<Invoice> invoices = invoiceRepository.findAll();

        double totalRevenue = conventions.stream()
                .mapToDouble(conv -> conv.getAmount().doubleValue())
                .sum();

        double paidAmount = invoices.stream()
                .filter(inv -> "PAID".equalsIgnoreCase(inv.getStatus()) || "PAYÉE".equalsIgnoreCase(inv.getStatus()))
                .mapToDouble(inv -> inv.getAmount().doubleValue())
                .sum();

        double pendingAmount = invoices.stream()
                .filter(inv -> "PENDING".equalsIgnoreCase(inv.getStatus()) || "EN_ATTENTE".equalsIgnoreCase(inv.getStatus()))
                .mapToDouble(inv -> inv.getAmount().doubleValue())
                .sum();

        double overdueAmount = invoices.stream()
                .filter(inv -> "OVERDUE".equalsIgnoreCase(inv.getStatus()) || "EN_RETARD".equalsIgnoreCase(inv.getStatus()))
                .mapToDouble(inv -> inv.getAmount().doubleValue())
                .sum();

        double collectionRate = totalRevenue > 0 ? (paidAmount / totalRevenue) * 100 : 0;

        return new FinancialReportDTO(totalRevenue, paidAmount, pendingAmount, overdueAmount, collectionRate);
    }

    /**
     * Rapport de performance
     */
    public PerformanceReportDTO getPerformanceReport() {
        List<Convention> conventions = conventionRepository.findAll();
        List<Invoice> invoices = invoiceRepository.findAll();

        int totalConventions = conventions.size();
        int totalInvoices = invoices.size();

        long paidInvoices = invoices.stream()
                .filter(inv -> "PAID".equalsIgnoreCase(inv.getStatus()) || "PAYÉE".equalsIgnoreCase(inv.getStatus()))
                .count();

        long overdueInvoices = invoices.stream()
                .filter(inv -> "OVERDUE".equalsIgnoreCase(inv.getStatus()) || "EN_RETARD".equalsIgnoreCase(inv.getStatus()))
                .count();

        double paymentRate = totalInvoices > 0 ? ((double) paidInvoices / totalInvoices) * 100 : 0;
        double overdueRate = totalInvoices > 0 ? ((double) overdueInvoices / totalInvoices) * 100 : 0;

        double averageConventionAmount = totalConventions > 0
                ? conventions.stream().mapToDouble(conv -> conv.getAmount().doubleValue()).average().orElse(0)
                : 0;

        return new PerformanceReportDTO(
                totalConventions,
                totalInvoices,
                (int) paidInvoices,
                (int) overdueInvoices,
                paymentRate,
                overdueRate,
                averageConventionAmount
        );
    }

    /**
     * Rapport par gouvernorat
     */
    public List<GovernorateReportDTO> getReportByGovernorate() {
        List<Convention> conventions = conventionRepository.findAll();

        Map<String, GovernorateReportDTO> governorateMap = new HashMap<>();

        for (Convention conv : conventions) {
            String gov = conv.getGovernorate();
            if (gov == null || gov.isEmpty()) {
                gov = "Non spécifié";
            }

            GovernorateReportDTO report = governorateMap.getOrDefault(gov,
                    new GovernorateReportDTO(gov, 0, 0.0, 0, 0));

            report.setCount(report.getCount() + 1);
            report.setTotalAmount(report.getTotalAmount() + conv.getAmount().doubleValue());

            if ("ACTIVE".equalsIgnoreCase(conv.getStatus()) || "ACTIF".equalsIgnoreCase(conv.getStatus())) {
                report.setActiveCount(report.getActiveCount() + 1);
            }
            if ("EXPIRED".equalsIgnoreCase(conv.getStatus()) || "EXPIRÉ".equalsIgnoreCase(conv.getStatus())) {
                report.setExpiredCount(report.getExpiredCount() + 1);
            }

            governorateMap.put(gov, report);
        }

        return governorateMap.values().stream()
                .sorted(Comparator.comparing(GovernorateReportDTO::getTotalAmount).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Rapport par mois
     */
    public List<MonthlyReportDTO> getReportByMonth() {
        List<Convention> conventions = conventionRepository.findAll();

        Map<String, MonthlyReportDTO> monthMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String[] monthNames = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};

        for (Convention conv : conventions) {
            if (conv.getDueDate() == null) continue;

            LocalDate date = conv.getDueDate();
            String monthKey = date.format(formatter);
            String monthName = monthNames[date.getMonthValue() - 1] + " " + date.getYear();

            MonthlyReportDTO report = monthMap.getOrDefault(monthKey,
                    new MonthlyReportDTO(monthKey, monthName, 0, 0.0));

            report.setConventionsCount(report.getConventionsCount() + 1);
            report.setTotalAmount(report.getTotalAmount() + conv.getAmount().doubleValue());

            monthMap.put(monthKey, report);
        }

        return monthMap.values().stream()
                .sorted(Comparator.comparing(MonthlyReportDTO::getMonth))
                .collect(Collectors.toList());
    }
}