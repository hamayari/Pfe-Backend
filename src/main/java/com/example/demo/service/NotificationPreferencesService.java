package com.example.demo.service;

import com.example.demo.model.NotificationPreferences;
import com.example.demo.repository.NotificationPreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotificationPreferencesService {

    @Autowired
    private NotificationPreferencesRepository preferencesRepository;

    @Cacheable(value = "userPreferences", key = "#userId")
    public NotificationPreferences getUserPreferences(String userId) {
        return preferencesRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultPreferences(userId));
    }

    private NotificationPreferences createDefaultPreferences(String userId) {
        NotificationPreferences prefs = new NotificationPreferences(userId);
        
        // Valeurs par défaut
        prefs.setEmailEnabled(true);
        prefs.setSmsEnabled(false);
        prefs.setPushEnabled(true);
        prefs.setEmailFrequency("immediate");
        prefs.setTimezone(ZoneId.systemDefault().getId());
        
        // Heures calmes par défaut
        prefs.setQuietHoursEnabled(true);
        prefs.setQuietHoursStart("22:00");
        prefs.setQuietHoursEnd("07:00");
        prefs.setQuietHoursDays(new String[]{"saturday", "sunday"});
        
        return preferencesRepository.save(prefs);
    }

    @CacheEvict(value = "userPreferences", key = "#preferences.userId")
    public NotificationPreferences updatePreferences(NotificationPreferences preferences) {
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    public boolean shouldSendNotification(String userId, String notificationType, String channel) {
        NotificationPreferences prefs = getUserPreferences(userId);
        
        // Vérifier les heures calmes
        if (isInQuietHours(prefs)) {
            return false;
        }
        
        // Vérifier le type de notification
        switch (channel.toLowerCase()) {
            case "email":
                return prefs.isEmailEnabled() && isNotificationTypeEnabled(prefs.getEmailTypes(), notificationType);
            case "sms":
                return prefs.isSmsEnabled() && isNotificationTypeEnabled(prefs.getSmsTypes(), notificationType);
            case "push":
                return prefs.isPushEnabled() && isNotificationTypeEnabled(prefs.getPushTypes(), notificationType);
            default:
                return false;
        }
    }

    private boolean isInQuietHours(NotificationPreferences prefs) {
        if (!prefs.isQuietHoursEnabled()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of(prefs.getTimezone()));
        String currentDay = now.getDayOfWeek().name().toLowerCase();
        
        // Vérifier si c'est un jour calme
        if (!List.of(prefs.getQuietHoursDays()).contains(currentDay)) {
            return false;
        }
        
        // Parser les heures calmes
        int startHour = Integer.parseInt(prefs.getQuietHoursStart().split(":")[0]);
        int startMinute = Integer.parseInt(prefs.getQuietHoursStart().split(":")[1]);
        int endHour = Integer.parseInt(prefs.getQuietHoursEnd().split(":")[0]);
        int endMinute = Integer.parseInt(prefs.getQuietHoursEnd().split(":")[1]);
        
        int currentHour = now.getHour();
        int currentMinute = now.getMinute();
        
        // Convertir en minutes pour comparaison facile
        int start = startHour * 60 + startMinute;
        int end = endHour * 60 + endMinute;
        int current = currentHour * 60 + currentMinute;
        
        return current >= start && current <= end;
    }

    private boolean isNotificationTypeEnabled(Object types, String notificationType) {
        if (types == null) {
            return true; // Par défaut si pas de configuration spécifique
        }
        
        try {
            // Utiliser la réflexion pour accéder aux champs dynamiquement
            return (boolean) types.getClass().getMethod("is" + capitalize(notificationType)).invoke(types);
        } catch (Exception e) {
            return true; // Par défaut si le type n'est pas configuré
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // Nettoyer le cache périodiquement
    @Scheduled(fixedRate = 3600000) // 1 heure
    @CacheEvict(value = "userPreferences", allEntries = true)
    public void clearPreferencesCache() {
        // Le cache est automatiquement vidé
    }
}