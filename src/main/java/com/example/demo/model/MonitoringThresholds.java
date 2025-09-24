package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "monitoring_thresholds")
public class MonitoringThresholds {
    
    @Id
    private String id;
    
    private String metricName; // CPU, RAM, DISK, etc.
    private double warningThreshold; // Seuil d'avertissement
    private double criticalThreshold; // Seuil critique
    private boolean enabled;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public MonitoringThresholds() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public MonitoringThresholds(String metricName, double warningThreshold, double criticalThreshold, String description) {
        this();
        this.metricName = metricName;
        this.warningThreshold = warningThreshold;
        this.criticalThreshold = criticalThreshold;
        this.description = description;
        this.enabled = true;
    }
    
    // Getters et Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getMetricName() {
        return metricName;
    }
    
    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }
    
    public double getWarningThreshold() {
        return warningThreshold;
    }
    
    public void setWarningThreshold(double warningThreshold) {
        this.warningThreshold = warningThreshold;
    }
    
    public double getCriticalThreshold() {
        return criticalThreshold;
    }
    
    public void setCriticalThreshold(double criticalThreshold) {
        this.criticalThreshold = criticalThreshold;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Méthode utilitaire pour vérifier le niveau d'alerte
    public String getAlertLevel(double currentValue) {
        if (!enabled) return "NORMAL";
        if (currentValue >= criticalThreshold) return "CRITICAL";
        if (currentValue >= warningThreshold) return "WARNING";
        return "NORMAL";
    }
} 