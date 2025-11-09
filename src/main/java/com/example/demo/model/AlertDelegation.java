package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Modèle pour l'historique des délégations d'alertes
 */
@Data
@Document(collection = "alert_delegations")
public class AlertDelegation {
    
    @Id
    private String id;
    
    // Informations sur l'alerte
    private String alertId;
    private String alertType;
    private String alertTitle;
    private String alertMessage;
    private String alertSeverity;
    
    // Informations sur la délégation
    private String delegatedBy;           // ID du décideur qui a délégué
    private String delegatedByName;       // Nom du décideur
    private String delegatedTo;           // ID du chef de projet
    private String delegatedToName;       // Nom du chef de projet
    private LocalDateTime delegatedAt;    // Date de délégation
    
    // Informations sur le traitement
    private String status;                // PENDING, IN_PROGRESS, RESOLVED, REJECTED
    private String resolution;            // Description de la résolution
    private LocalDateTime resolvedAt;     // Date de résolution
    private String resolvedBy;            // ID de celui qui a résolu
    private String resolvedByName;        // Nom de celui qui a résolu
    
    // Commentaires et notes
    private String delegationNote;        // Note du décideur lors de la délégation
    private String resolutionNote;        // Note du chef de projet lors de la résolution
    
    // Métadonnées
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public AlertDelegation() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "PENDING";
    }
}
