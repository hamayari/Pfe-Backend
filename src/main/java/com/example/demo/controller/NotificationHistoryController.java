package com.example.demo.controller;

import com.example.demo.model.KpiAlert;
import com.example.demo.service.NotificationHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour l'historique des notifications
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationHistoryController {
    
    @Autowired
    private NotificationHistoryService historyService;
    
    /**
     * Obtenir l'historique complet des notifications
     */
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'DECISION_MAKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getHistory(
            @RequestParam(defaultValue = "30") int days,
            Authentication auth) {
        try {
            String userId = auth.getName();
            Map<String, Object> history = historyService.getNotificationHistory(userId, days);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", history);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Obtenir les notifications non lues
     */
    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'DECISION_MAKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getUnread(Authentication auth) {
        try {
            String userId = auth.getName();
            List<KpiAlert> unread = historyService.getUnreadNotifications(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", unread.size());
            response.put("notifications", unread);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Obtenir le compteur de notifications non lues
     */
    @GetMapping("/unread/count")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'DECISION_MAKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication auth) {
        try {
            String userId = auth.getName();
            int count = historyService.getUnreadCount(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", count);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Marquer une notification comme lue
     */
    @PostMapping("/{alertId}/mark-read")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'DECISION_MAKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable String alertId,
            Authentication auth) {
        try {
            String userId = auth.getName();
            historyService.markAsRead(alertId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Notification marquée comme lue");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
