package com.example.demo.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour le taux de conformité des paiements
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceRateDTO {
    
    private double complianceRate; // Taux de conformité en pourcentage (0-100)
    private int totalInvoices; // Nombre total de factures
    private int paidOnTime; // Nombre de factures payées à temps
    private int paidLate; // Nombre de factures payées en retard
    private int unpaid; // Nombre de factures non payées
    private int overdue; // Nombre de factures en retard
    
    // Statistiques supplémentaires
    private double averageDelayDays; // Délai moyen de paiement en jours
    private double onTimePaymentRate; // Taux de paiement à temps (%)
    private double latePaymentRate; // Taux de paiement en retard (%)
    private double unpaidRate; // Taux de non-paiement (%)
    
    /**
     * Calcule le taux de conformité
     * Conformité = (Factures payées à temps / Total factures) * 100
     */
    public void calculateRates() {
        if (totalInvoices > 0) {
            this.complianceRate = ((double) paidOnTime / totalInvoices) * 100;
            this.onTimePaymentRate = ((double) paidOnTime / totalInvoices) * 100;
            this.latePaymentRate = ((double) paidLate / totalInvoices) * 100;
            this.unpaidRate = ((double) unpaid / totalInvoices) * 100;
        } else {
            this.complianceRate = 0;
            this.onTimePaymentRate = 0;
            this.latePaymentRate = 0;
            this.unpaidRate = 0;
        }
    }
    
    /**
     * Retourne un niveau de conformité basé sur le taux
     */
    public String getComplianceLevel() {
        if (complianceRate >= 90) {
            return "EXCELLENT";
        } else if (complianceRate >= 75) {
            return "BON";
        } else if (complianceRate >= 60) {
            return "MOYEN";
        } else if (complianceRate >= 40) {
            return "FAIBLE";
        } else {
            return "CRITIQUE";
        }
    }
    
    /**
     * Retourne une couleur selon le niveau de conformité
     */
    public String getComplianceColor() {
        if (complianceRate >= 90) {
            return "#4CAF50"; // Vert
        } else if (complianceRate >= 75) {
            return "#8BC34A"; // Vert clair
        } else if (complianceRate >= 60) {
            return "#FFC107"; // Orange
        } else if (complianceRate >= 40) {
            return "#FF9800"; // Orange foncé
        } else {
            return "#F44336"; // Rouge
        }
    }
}
