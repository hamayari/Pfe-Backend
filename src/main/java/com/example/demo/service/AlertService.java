package com.example.demo.service;

import com.example.demo.model.MonitoringThresholds;
import com.example.demo.repository.MonitoringThresholdsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AlertService {

    @Autowired
    private MonitoringThresholdsRepository thresholdsRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Cache pour éviter les alertes répétitives
    private final Map<String, LocalDateTime> lastAlertTime = new ConcurrentHashMap<>();
    private final Map<String, String> lastAlertLevel = new ConcurrentHashMap<>();
    
    // Configuration des délais entre alertes (en minutes)
    private static final int WARNING_COOLDOWN_MINUTES = 5;
    private static final int CRITICAL_COOLDOWN_MINUTES = 2;

    public void checkThresholds(double cpuUsage, double ramUsage, double diskUsage) {
        checkMetricThreshold("CPU", cpuUsage);
        checkMetricThreshold("RAM", ramUsage);
        checkMetricThreshold("DISK", diskUsage);
    }

    private void checkMetricThreshold(String metricName, double currentValue) {
        Optional<MonitoringThresholds> thresholdOpt = thresholdsRepository.findByMetricName(metricName);
        
        if (thresholdOpt.isPresent()) {
            MonitoringThresholds threshold = thresholdOpt.get();
            
            if (!threshold.isEnabled()) {
                return; // Seuil désactivé
            }

            String alertLevel = threshold.getAlertLevel(currentValue);
            String alertKey = metricName + "_" + alertLevel;
            
            // Vérifier si on doit envoyer une alerte
            if (shouldSendAlert(alertKey, alertLevel)) {
                sendAlert(metricName, currentValue, threshold, alertLevel);
                updateLastAlertTime(alertKey);
            }
        }
    }

    private boolean shouldSendAlert(String alertKey, String alertLevel) {
        LocalDateTime lastTime = lastAlertTime.get(alertKey);
        String lastLevel = lastAlertLevel.get(alertKey);
        
        if (lastTime == null) {
            return true; // Première alerte
        }

        LocalDateTime now = LocalDateTime.now();
        int cooldownMinutes = "CRITICAL".equals(alertLevel) ? CRITICAL_COOLDOWN_MINUTES : WARNING_COOLDOWN_MINUTES;
        
        // Vérifier si le délai de refroidissement est écoulé
        if (lastTime.plusMinutes(cooldownMinutes).isBefore(now)) {
            return true;
        }
        
        // Si le niveau d'alerte a changé, envoyer immédiatement
        if (!alertLevel.equals(lastLevel)) {
            return true;
        }
        
        return false;
    }

    private void sendAlert(String metricName, double currentValue, MonitoringThresholds threshold, String alertLevel) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("id", UUID.randomUUID().toString());
        alert.put("timestamp", LocalDateTime.now());
        alert.put("metricName", metricName);
        alert.put("currentValue", currentValue);
        alert.put("threshold", "CRITICAL".equals(alertLevel) ? threshold.getCriticalThreshold() : threshold.getWarningThreshold());
        alert.put("alertLevel", alertLevel);
        alert.put("message", generateAlertMessage(metricName, currentValue, threshold, alertLevel));
        alert.put("description", threshold.getDescription());
        
        // Envoyer via WebSocket
        messagingTemplate.convertAndSend("/topic/alerts", alert);
        
        // Log de l'alerte
        System.out.println("ALERTE " + alertLevel + ": " + alert.get("message"));
        
        // Mettre à jour le cache
        lastAlertLevel.put(metricName + "_" + alertLevel, alertLevel);
    }

    private String generateAlertMessage(String metricName, double currentValue, MonitoringThresholds threshold, String alertLevel) {
        String levelText = "CRITICAL".equals(alertLevel) ? "CRITIQUE" : "AVERTISSEMENT";
        String thresholdValue = "CRITICAL".equals(alertLevel) ? 
            String.valueOf(threshold.getCriticalThreshold()) : 
            String.valueOf(threshold.getWarningThreshold());
        
        return String.format("%s %s: %s à %.1f%% (seuil: %s%%)", 
            metricName, levelText, threshold.getDescription(), currentValue, thresholdValue);
    }

    private void updateLastAlertTime(String alertKey) {
        lastAlertTime.put(alertKey, LocalDateTime.now());
    }

    public List<Map<String, Object>> getActiveAlerts() {
        List<Map<String, Object>> activeAlerts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // Récupérer les alertes récentes (dernières 24h)
        for (Map.Entry<String, LocalDateTime> entry : lastAlertTime.entrySet()) {
            if (entry.getValue().plusHours(24).isAfter(now)) {
                String[] parts = entry.getKey().split("_");
                String metricName = parts[0];
                String alertLevel = parts[1];
                
                Map<String, Object> alert = new HashMap<>();
                alert.put("metricName", metricName);
                alert.put("alertLevel", alertLevel);
                alert.put("timestamp", entry.getValue());
                alert.put("lastLevel", lastAlertLevel.get(entry.getKey()));
                
                activeAlerts.add(alert);
            }
        }
        
        return activeAlerts;
    }

    public void clearAlertHistory() {
        lastAlertTime.clear();
        lastAlertLevel.clear();
    }
} 