package com.example.demo.controller;

import com.example.demo.model.NotificationPreferences;
import com.example.demo.repository.NotificationPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour tester les préférences de notifications
 */
@RestController
@RequestMapping("/api/test/notification-preferences")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificationPreferencesTestController {

    private final NotificationPreferencesRepository preferencesRepository;

    /**
     * Créer des préférences de test pour un utilisateur
     */
    @PostMapping("/create-test/{userId}")
    public ResponseEntity<NotificationPreferences> createTestPreferences(@PathVariable String userId) {
        log.info("🧪 Création de préférences de test pour userId: {}", userId);
        
        // Supprimer les préférences existantes
        preferencesRepository.findByUserId(userId).ifPresent(preferencesRepository::delete);
        
        // Créer nouvelles préférences avec valeurs de test
        NotificationPreferences prefs = new NotificationPreferences(userId);
        
        // Personnaliser pour le test
        prefs.setEmailEnabled(true);
        prefs.setEmailFrequency("daily");
        prefs.setSmsEnabled(true);
        prefs.setPushEnabled(true);
        prefs.setQuietHoursEnabled(true);
        prefs.setQuietHoursStart("22:00");
        prefs.setQuietHoursEnd("08:00");
        
        NotificationPreferences saved = preferencesRepository.save(prefs);
        
        log.info("✅ Préférences de test créées: {}", saved.getId());
        
        return ResponseEntity.ok(saved);
    }

    /**
     * Lister toutes les préférences (pour debug)
     */
    @GetMapping("/all")
    public ResponseEntity<List<NotificationPreferences>> getAllPreferences() {
        log.info("📋 Récupération de toutes les préférences");
        List<NotificationPreferences> allPrefs = preferencesRepository.findAll();
        log.info("✅ {} préférences trouvées", allPrefs.size());
        return ResponseEntity.ok(allPrefs);
    }

    /**
     * Vérifier les préférences d'un utilisateur
     */
    @GetMapping("/check/{userId}")
    public ResponseEntity<Map<String, Object>> checkUserPreferences(@PathVariable String userId) {
        log.info("🔍 Vérification des préférences pour userId: {}", userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        
        boolean exists = preferencesRepository.existsByUserId(userId);
        result.put("exists", exists);
        
        if (exists) {
            NotificationPreferences prefs = preferencesRepository.findByUserId(userId).orElse(null);
            result.put("preferences", prefs);
            result.put("emailEnabled", prefs.isEmailEnabled());
            result.put("smsEnabled", prefs.isSmsEnabled());
            result.put("pushEnabled", prefs.isPushEnabled());
            result.put("quietHoursEnabled", prefs.isQuietHoursEnabled());
        } else {
            result.put("message", "Aucune préférence trouvée - Valeurs par défaut seront utilisées");
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Supprimer toutes les préférences (pour reset)
     */
    @DeleteMapping("/delete-all")
    public ResponseEntity<Map<String, String>> deleteAllPreferences() {
        log.info("🗑️ Suppression de toutes les préférences");
        long count = preferencesRepository.count();
        preferencesRepository.deleteAll();
        log.info("✅ {} préférences supprimées", count);
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", count + " préférences supprimées"
        ));
    }

    /**
     * Tester la validation des préférences
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validatePreferences(
            @RequestBody NotificationPreferences preferences) {
        
        log.info("✅ Validation des préférences");
        
        Map<String, Object> validation = new HashMap<>();
        validation.put("valid", true);
        
        // Vérifier la cohérence
        if (preferences.isQuietHoursEnabled()) {
            if (preferences.getQuietHoursStart() == null || preferences.getQuietHoursEnd() == null) {
                validation.put("valid", false);
                validation.put("error", "Les heures de silence doivent être définies");
            }
        }
        
        if (preferences.getEmailFrequency() != null) {
            List<String> validFrequencies = List.of("immediate", "hourly", "daily", "weekly");
            if (!validFrequencies.contains(preferences.getEmailFrequency())) {
                validation.put("valid", false);
                validation.put("error", "Fréquence email invalide");
            }
        }
        
        validation.put("preferences", preferences);
        
        return ResponseEntity.ok(validation);
    }
}
