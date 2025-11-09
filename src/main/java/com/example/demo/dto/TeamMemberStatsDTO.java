package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberStatsDTO {
    // Informations de base
    private String id;
    private String username;
    private String name;
    private String email;
    private String role;
    private String avatar;
    
    // Statut
    private String status; // online, offline, busy
    private LocalDateTime lastActivity;
    
    // Statistiques réelles
    private int assignedConventions; // Nombre de conventions assignées
    private int activeConventions; // Conventions actives
    private int expiredConventions; // Conventions expirées
    
    private int totalInvoices; // Total factures
    private int overdueInvoices; // Factures en retard
    private int paidInvoices; // Factures payées
    private int pendingInvoices; // Factures en attente
    
    // Performance
    private double paymentRate; // Taux de paiement (%)
    private double performanceScore; // Score de performance global
    
    // Tâche en cours
    private String currentTask;
    private String currentTaskType; // convention, invoice, meeting
}
