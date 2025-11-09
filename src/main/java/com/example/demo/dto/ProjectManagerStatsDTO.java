package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectManagerStatsDTO {
    // Conventions
    private int totalConventions;
    private int expiredConventions;
    private int activeConventions;
    private int upcomingDeadlines; // Conventions expirant dans 30 jours
    
    // Factures
    private int totalInvoices;
    private double totalInvoicesAmount;
    private int overdueInvoices;
    private double overduePercentage;
    private int paidInvoices;
    private int pendingInvoices;
    
    // Performance
    private double teamPerformance;
    private double regularizationRate; // Taux de régularisation
    
    // Alertes
    private int pendingAlerts;
}
