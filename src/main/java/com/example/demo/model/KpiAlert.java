package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Modèle pour les alertes KPI générées automatiquement
 */
@Data
@Document(collection = "kpi_alerts")
public class KpiAlert {
    
    @Id
    private String id;
    
    // Nom du KPI concerné
    private String kpiName;
    
    // Valeur actuelle du KPI
    private Double currentValue;
    
    // Valeur normale/attendue
    private Double expectedValue;
    
    // Seuil dépassé
    private Double thresholdValue;
    
    // Statut (🟢 SAIN, 🟡 A_SURVEILLER, 🔴 ANORMAL)
    private String status;
    
    // Niveau de sévérité (LOW, MEDIUM, HIGH, CRITICAL)
    private String severity;
    
    // Dimension (GLOBAL, GOUVERNORAT, STRUCTURE)
    private String dimension;
    
    // Valeur de la dimension (ex: "Tunis", "STRUCT-001")
    private String dimensionValue;
    
    // Message d'alerte
    private String message;
    
    // Recommandation automatique
    private String recommendation;
    
    // Destinataires (IDs des chefs de projet à notifier)
    private java.util.List<String> recipients;
    
    // Statut de l'alerte (NEW, IN_PROGRESS, RESOLVED, ARCHIVED)
    private String alertStatus = "NEW";
    
    // Date de détection
    private LocalDateTime detectedAt;
    
    // Date de résolution
    private LocalDateTime resolvedAt;
    
    // Résolu par (userId)
    private String resolvedBy;
    
    // Résolu par (nom complet)
    private String resolvedByName;
    
    // Commentaire de résolution
    private String resolutionComment;
    
    // Actions prises
    private String actionsTaken;
    
    // Date d'archivage
    private LocalDateTime archivedAt;
    
    // Archivé par
    private String archivedBy;
    
    // Notification envoyée
    private boolean notificationSent = false;
    
    // Date d'envoi de la notification
    private LocalDateTime notificationSentAt;
    
    // Date de prise de connaissance (Chef de Projet marque comme "Informé")
    private LocalDateTime acknowledgedAt;
    
    // Canaux de notification utilisés (EMAIL, SMS, DASHBOARD)
    private java.util.List<String> notificationChannels;
    
    // Historique des actions (traçabilité complète)
    private java.util.List<AlertAction> actionHistory = new java.util.ArrayList<>();
    
    // Priorité (LOW, MEDIUM, HIGH, CRITICAL)
    private String priority;
    
    // Tags pour catégorisation
    private java.util.List<String> tags;
    
    // ID de la facture concernée (pour les alertes de type FACTURE_IMPAYEE)
    private String relatedInvoiceId;
    
    // ID de la convention concernée
    private String relatedConventionId;
    
    // Types d'anomalies détectées pour cette facture (RETARD, REGULARISATION, CONVERSION, etc.)
    private java.util.List<String> anomalyTypes = new java.util.ArrayList<>();
    
    // Détails de chaque anomalie
    private java.util.List<AnomalyDetail> anomalyDetails = new java.util.ArrayList<>();
    
    // Envoyé au chef de projet
    private boolean sentToProjectManager = false;
    
    // Date d'envoi au chef de projet
    private LocalDateTime sentToProjectManagerAt;
    
    // ID du chef de projet destinataire
    private String projectManagerId;
    
    // Métadonnées supplémentaires (JSON flexible)
    private java.util.Map<String, Object> metadata;
    
    // Classe interne pour les détails d'anomalie
    @Data
    public static class AnomalyDetail {
        private String type; // RETARD, REGULARISATION, CONVERSION
        private String description;
        private Double amount;
        private Integer daysOverdue;
        private LocalDateTime detectedAt;
        private String severity;
        
        public AnomalyDetail() {}
        
        public AnomalyDetail(String type, String description, String severity) {
            this.type = type;
            this.description = description;
            this.severity = severity;
            this.detectedAt = LocalDateTime.now();
        }
    }
    
    // Classe interne pour l'historique des actions
    @Data
    public static class AlertAction {
        private String actionType; // CREATED, VIEWED, IN_PROGRESS, RESOLVED, ARCHIVED, COMMENTED
        private String performedBy;
        private String performedByName;
        private LocalDateTime performedAt;
        private String comment;
        private String previousStatus;
        private String newStatus;
        
        public AlertAction() {}
        
        public AlertAction(String actionType, String performedBy, String performedByName, String comment) {
            this.actionType = actionType;
            this.performedBy = performedBy;
            this.performedByName = performedByName;
            this.performedAt = LocalDateTime.now();
            this.comment = comment;
        }
    }
}
