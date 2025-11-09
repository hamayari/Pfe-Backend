package com.example.demo.controller;

import com.example.demo.model.NotificationPreferences;
import com.example.demo.repository.NotificationPreferencesRepository;
import com.example.demo.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Contrôleur pour la gestion des préférences de notifications par utilisateur
 */
@RestController
@RequestMapping("/api/user/notification-preferences")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserNotificationPreferencesController {

    private final NotificationPreferencesRepository notificationPreferencesRepository;

    /**
     * Récupère les préférences de notification de l'utilisateur connecté
     */
    @GetMapping
    public ResponseEntity<NotificationPreferences> getUserPreferences(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("📋 Récupération des préférences de notification pour l'utilisateur: {}", 
                userPrincipal.getUsername());
        
        NotificationPreferences preferences = notificationPreferencesRepository
                .findByUserId(userPrincipal.getId())
                .orElseGet(() -> {
                    log.info("✨ Création des préférences par défaut pour l'utilisateur: {}", 
                            userPrincipal.getUsername());
                    NotificationPreferences defaultPrefs = new NotificationPreferences(userPrincipal.getId());
                    return notificationPreferencesRepository.save(defaultPrefs);
                });
        
        return ResponseEntity.ok(preferences);
    }

    /**
     * Met à jour les préférences de notification de l'utilisateur
     */
    @PutMapping
    public ResponseEntity<NotificationPreferences> updateUserPreferences(
            @RequestBody NotificationPreferences preferences,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("💾 Mise à jour des préférences de notification pour l'utilisateur: {}", 
                userPrincipal.getUsername());
        
        // Récupérer les préférences existantes ou créer nouvelles
        NotificationPreferences existingPrefs = notificationPreferencesRepository
                .findByUserId(userPrincipal.getId())
                .orElse(new NotificationPreferences(userPrincipal.getId()));
        
        // Mettre à jour les champs
        existingPrefs.setEmailEnabled(preferences.isEmailEnabled());
        existingPrefs.setEmailFrequency(preferences.getEmailFrequency());
        existingPrefs.setEmailTypes(preferences.getEmailTypes());
        
        existingPrefs.setSmsEnabled(preferences.isSmsEnabled());
        existingPrefs.setSmsTypes(preferences.getSmsTypes());
        
        existingPrefs.setPushEnabled(preferences.isPushEnabled());
        existingPrefs.setPushTypes(preferences.getPushTypes());
        
        existingPrefs.setQuietHoursEnabled(preferences.isQuietHoursEnabled());
        existingPrefs.setQuietHoursStart(preferences.getQuietHoursStart());
        existingPrefs.setQuietHoursEnd(preferences.getQuietHoursEnd());
        existingPrefs.setQuietHoursDays(preferences.getQuietHoursDays());
        
        existingPrefs.setThresholds(preferences.getThresholds());
        existingPrefs.setChannels(preferences.getChannels());
        existingPrefs.setTimezone(preferences.getTimezone());
        
        existingPrefs.setUpdatedAt(LocalDateTime.now());
        
        NotificationPreferences savedPrefs = notificationPreferencesRepository.save(existingPrefs);
        
        log.info("✅ Préférences de notification mises à jour avec succès");
        
        return ResponseEntity.ok(savedPrefs);
    }

    /**
     * Réinitialise les préférences aux valeurs par défaut
     */
    @PostMapping("/reset")
    public ResponseEntity<NotificationPreferences> resetUserPreferences(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("🔄 Réinitialisation des préférences pour l'utilisateur: {}", 
                userPrincipal.getUsername());
        
        // Supprimer les préférences existantes
        notificationPreferencesRepository.findByUserId(userPrincipal.getId())
                .ifPresent(notificationPreferencesRepository::delete);
        
        // Créer nouvelles préférences par défaut
        NotificationPreferences defaultPrefs = new NotificationPreferences(userPrincipal.getId());
        NotificationPreferences savedPrefs = notificationPreferencesRepository.save(defaultPrefs);
        
        log.info("✅ Préférences réinitialisées aux valeurs par défaut");
        
        return ResponseEntity.ok(savedPrefs);
    }
}
