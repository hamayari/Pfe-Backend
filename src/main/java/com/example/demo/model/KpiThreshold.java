package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Modèle pour les seuils de KPI configurables
 */
@Data
@Document(collection = "kpi_thresholds")
public class KpiThreshold {
    
    @Id
    private String id;
    
    // Nom du KPI (ex: "TAUX_RETARD", "TAUX_PAIEMENT", "MONTANT_IMPAYE")
    private String kpiName;
    
    // Description du KPI
    private String description;
    
    // Seuil bas (🟡 À surveiller)
    private Double lowThreshold;
    
    // Seuil haut (🔴 Anormal)
    private Double highThreshold;
    
    // Valeur normale/moyenne historique
    private Double normalValue;
    
    // Tolérance en pourcentage (ex: 10 pour ±10%)
    private Double tolerancePercent;
    
    // Unité de mesure (%, DT, jours, etc.)
    private String unit;
    
    // Dimension (GLOBAL, GOUVERNORAT, STRUCTURE)
    private String dimension;
    
    // Valeur de la dimension (ex: "Tunis", "STRUCT-001", null pour GLOBAL)
    private String dimensionValue;
    
    // Actif ou non
    private boolean enabled = true;
    
    // Priorité (HIGH, MEDIUM, LOW)
    private String priority = "MEDIUM";
    
    // Métadonnées
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
