package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Configuration des alertes pour les conventions
 */
@Data
@Document(collection = "alert_configurations")
public class AlertConfiguration {
    
    @Id
    private String id;
    
    // Seuils d'alerte (en jours avant l'échéance)
    private Integer alertThreshold30Days = 30;
    private Integer alertThreshold15Days = 15;
    private Integer alertThreshold7Days = 7;
    private Integer alertThreshold1Day = 1;
    
    // Activation des alertes par seuil
    private Boolean alert30DaysEnabled = true;
    private Boolean alert15DaysEnabled = true;
    private Boolean alert7DaysEnabled = true;
    private Boolean alert1DayEnabled = true;
    private Boolean alertSameDayEnabled = true;
    
    // Configuration de l'heure d'exécution du scheduler
    private String schedulerCronExpression = "0 0 9 * * ?"; // Par défaut: 9h00 tous les jours
    private Integer schedulerHour = 9;
    private Integer schedulerMinute = 0;
    
    // Canaux de notification
    private Boolean emailNotificationsEnabled = true;
    private Boolean websocketNotificationsEnabled = true;
    private Boolean smsNotificationsEnabled = false;
    
    // Destinataires des alertes
    private Boolean notifyCreator = true;
    private Boolean notifyCommercial = true;
    private Boolean notifyProjectManager = true;
    private Boolean notifyAdmins = false;
    
    // Métadonnées
    private Boolean active = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String updatedBy;
    
    // Configuration par défaut
    public static AlertConfiguration getDefaultConfiguration() {
        AlertConfiguration config = new AlertConfiguration();
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return config;
    }
}
