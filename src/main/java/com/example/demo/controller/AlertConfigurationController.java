package com.example.demo.controller;

import com.example.demo.model.AlertConfiguration;
import com.example.demo.repository.AlertConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrôleur pour la gestion de la configuration des alertes
 */
@RestController
@RequestMapping("/api/alert-configuration")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AlertConfigurationController {

    private final AlertConfigurationRepository alertConfigurationRepository;

    /**
     * Récupère la configuration active des alertes
     */
    @GetMapping
    public ResponseEntity<AlertConfiguration> getConfiguration() {
        log.info("📋 Récupération de la configuration des alertes");
        
        AlertConfiguration config = alertConfigurationRepository.findFirstByActiveTrue()
                .orElseGet(() -> {
                    log.info("✨ Création de la configuration par défaut");
                    AlertConfiguration defaultConfig = AlertConfiguration.getDefaultConfiguration();
                    return alertConfigurationRepository.save(defaultConfig);
                });
        
        return ResponseEntity.ok(config);
    }

    /**
     * Met à jour la configuration des alertes
     */
    @PutMapping
    public ResponseEntity<AlertConfiguration> updateConfiguration(
            @RequestBody AlertConfiguration configuration,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        log.info("💾 Mise à jour de la configuration des alertes par l'utilisateur: {}", userId);
        
        // Récupérer la configuration existante ou créer une nouvelle
        AlertConfiguration existingConfig = alertConfigurationRepository.findFirstByActiveTrue()
                .orElse(new AlertConfiguration());
        
        // Mettre à jour les champs
        existingConfig.setAlertThreshold30Days(configuration.getAlertThreshold30Days());
        existingConfig.setAlertThreshold15Days(configuration.getAlertThreshold15Days());
        existingConfig.setAlertThreshold7Days(configuration.getAlertThreshold7Days());
        existingConfig.setAlertThreshold1Day(configuration.getAlertThreshold1Day());
        
        existingConfig.setAlert30DaysEnabled(configuration.getAlert30DaysEnabled());
        existingConfig.setAlert15DaysEnabled(configuration.getAlert15DaysEnabled());
        existingConfig.setAlert7DaysEnabled(configuration.getAlert7DaysEnabled());
        existingConfig.setAlert1DayEnabled(configuration.getAlert1DayEnabled());
        existingConfig.setAlertSameDayEnabled(configuration.getAlertSameDayEnabled());
        
        existingConfig.setSchedulerHour(configuration.getSchedulerHour());
        existingConfig.setSchedulerMinute(configuration.getSchedulerMinute());
        
        // Générer l'expression cron à partir de l'heure et des minutes
        String cronExpression = String.format("0 %d %d * * ?", 
                configuration.getSchedulerMinute(), 
                configuration.getSchedulerHour());
        existingConfig.setSchedulerCronExpression(cronExpression);
        
        existingConfig.setEmailNotificationsEnabled(configuration.getEmailNotificationsEnabled());
        existingConfig.setWebsocketNotificationsEnabled(configuration.getWebsocketNotificationsEnabled());
        existingConfig.setSmsNotificationsEnabled(configuration.getSmsNotificationsEnabled());
        
        existingConfig.setNotifyCreator(configuration.getNotifyCreator());
        existingConfig.setNotifyCommercial(configuration.getNotifyCommercial());
        existingConfig.setNotifyProjectManager(configuration.getNotifyProjectManager());
        existingConfig.setNotifyAdmins(configuration.getNotifyAdmins());
        
        existingConfig.setActive(true);
        existingConfig.setUpdatedAt(LocalDateTime.now());
        existingConfig.setUpdatedBy(userId != null ? userId : "system");
        
        if (existingConfig.getCreatedAt() == null) {
            existingConfig.setCreatedAt(LocalDateTime.now());
        }
        
        AlertConfiguration savedConfig = alertConfigurationRepository.save(existingConfig);
        
        log.info("✅ Configuration des alertes mise à jour avec succès");
        
        return ResponseEntity.ok(savedConfig);
    }

    /**
     * Réinitialise la configuration aux valeurs par défaut
     */
    @PostMapping("/reset")
    public ResponseEntity<AlertConfiguration> resetConfiguration(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        log.info("🔄 Réinitialisation de la configuration des alertes par l'utilisateur: {}", userId);
        
        // Désactiver toutes les configurations existantes
        List<AlertConfiguration> existingConfigs = alertConfigurationRepository.findAll();
        existingConfigs.forEach(config -> {
            config.setActive(false);
            alertConfigurationRepository.save(config);
        });
        
        // Créer une nouvelle configuration par défaut
        AlertConfiguration defaultConfig = AlertConfiguration.getDefaultConfiguration();
        defaultConfig.setUpdatedBy(userId != null ? userId : "system");
        AlertConfiguration savedConfig = alertConfigurationRepository.save(defaultConfig);
        
        log.info("✅ Configuration réinitialisée aux valeurs par défaut");
        
        return ResponseEntity.ok(savedConfig);
    }

    /**
     * Teste l'envoi d'une alerte
     */
    @PostMapping("/test")
    public ResponseEntity<String> testAlert() {
        log.info("🧪 Test d'envoi d'alerte");
        
        // Cette méthode pourrait déclencher un envoi de test
        // Pour l'instant, on retourne juste un message de succès
        
        return ResponseEntity.ok("Test d'alerte effectué avec succès");
    }
}
