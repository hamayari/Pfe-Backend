package com.example.demo.controller;

import com.example.demo.model.NotificationLog;
import com.example.demo.service.NotificationService;
import com.example.demo.dto.NotificationHistoryDTO;
import com.example.demo.dto.NotificationSettingsDTO;
import com.example.demo.model.NotificationSettings;
import com.example.demo.repository.NotificationSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/notifications/paiement")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public List<NotificationLog> getNotifications(Authentication authentication) {
        String userId = authentication.getName();
        return notificationService.getNotificationsForUser(userId);
    }

    @PostMapping("/{id}/read")
    public void markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
    }
}

@RestController
@RequestMapping("/api/notifications")
class NotificationHistoryController {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;

    @GetMapping("/history")
    public ResponseEntity<List<NotificationHistoryDTO>> getNotificationHistory(Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<NotificationHistoryDTO> history = notificationService.getNotificationHistory(userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            // Retourner une liste vide en cas d'erreur
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }
    }

    @GetMapping("/settings")
    public ResponseEntity<NotificationSettingsDTO> getNotificationSettings(Authentication authentication) {
        try {
            // String userId = authentication.getName(); // reserved for per-user settings in future
            NotificationSettings settings = notificationSettingsRepository.findById("global")
                .orElseGet(() -> {
                    NotificationSettings s = new NotificationSettings();
                    s.setId("global");
                    s.setEmailEnabled(true);
                    s.setSmsEnabled(true);
                    s.setAutoReminderEnabled(true);
                    s.setReminderFrequency("daily");
                    s.setReminderDays(java.util.Arrays.asList(7,3,1));
                    return notificationSettingsRepository.save(s);
                });

            NotificationSettingsDTO dto = new NotificationSettingsDTO();
            dto.setEmailEnabled(settings.isEmailEnabled());
            dto.setSmsEnabled(settings.isSmsEnabled());
            dto.setAutoReminderEnabled(settings.isAutoReminderEnabled());
            dto.setReminderFrequency(settings.getReminderFrequency());
            dto.setReminderDays(settings.getReminderDays());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.ok(new NotificationSettingsDTO());
        }
    }

    @PutMapping("/settings")
    public ResponseEntity<NotificationSettingsDTO> updateNotificationSettings(Authentication authentication,
          @RequestBody NotificationSettingsDTO payload) {
        // Persist global settings for now (can be extended per-user later)
        NotificationSettings settings = notificationSettingsRepository.findById("global")
            .orElseGet(() -> { NotificationSettings s = new NotificationSettings(); s.setId("global"); return s; });
        settings.setEmailEnabled(payload.isEmailEnabled());
        settings.setSmsEnabled(payload.isSmsEnabled());
        settings.setAutoReminderEnabled(payload.isAutoReminderEnabled());
        settings.setReminderFrequency(payload.getReminderFrequency() != null ? payload.getReminderFrequency() : "daily");
        settings.setReminderDays(payload.getReminderDays() != null && !payload.getReminderDays().isEmpty() ? payload.getReminderDays() : java.util.Arrays.asList(7,3,1));
        // Quiet hours
        if (payload.getTimezone() != null) { /* placeholder to keep DTO compatibility */ }
        settings.setQuietHoursEnabled(payload.isQuietHoursEnabled());
        // Keep existing start/end if not provided at this stage
        if (settings.getQuietHoursStart() == null) settings.setQuietHoursStart("22:00");
        if (settings.getQuietHoursEnd() == null) settings.setQuietHoursEnd("08:00");
        notificationSettingsRepository.save(settings);

        return ResponseEntity.ok(payload);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        String userId = authentication.getName();
        long count = notificationService.getUnreadCount(userId);
        Map<String, Object> body = new HashMap<>();
        body.put("unreadCount", count);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/mark-read-bulk")
    public ResponseEntity<Map<String, Object>> markReadBulk(Authentication authentication, @RequestBody Map<String, List<String>> payload) {
        String userId = authentication.getName();
        List<String> ids = payload.getOrDefault("ids", java.util.Collections.emptyList());
        int updated = notificationService.markAsReadBulk(userId, ids);
        Map<String, Object> body = new HashMap<>();
        body.put("updated", updated);
        return ResponseEntity.ok(body);
    }
} 