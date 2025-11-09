package com.example.demo.controller;

import com.example.demo.model.Notification;
import com.example.demo.service.InAppNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    
    @Autowired
    private InAppNotificationService notificationService;
    
    /**
     * Récupérer toutes les notifications d'un utilisateur (limitées aux 50 plus récentes)
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Notification>> getUserNotifications(
            @PathVariable String userId,
            @RequestParam(defaultValue = "50") int limit) {
        System.out.println("📥 GET /api/notifications/user/" + userId + " (limit: " + limit + ")");
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        
        // Limiter le nombre de notifications retournées
        List<Notification> limitedNotifications = notifications.stream()
            .limit(Math.min(limit, 100)) // Maximum 100
            .collect(java.util.stream.Collectors.toList());
        
        System.out.println("✅ Retour de " + limitedNotifications.size() + " notifications (total: " + notifications.size() + ")");
        
        if (notifications.size() > 100) {
            System.out.println("⚠️ ATTENTION: " + notifications.size() + " notifications en base - Nettoyage recommandé!");
        }
        
        return ResponseEntity.ok(limitedNotifications);
    }
    
    /**
     * Récupérer les notifications non lues
     */
    @GetMapping("/user/{userId}/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable String userId) {
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Compter les notifications non lues
     */
    @GetMapping("/user/{userId}/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getUnreadCount(@PathVariable String userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }
    
    /**
     * Marquer une notification comme lue
     */
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable String notificationId,
            @RequestParam String userId) {
        boolean success = notificationService.markAsRead(notificationId, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Marquer toutes les notifications comme lues
     */
    @PutMapping("/user/{userId}/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> markAllAsRead(@PathVariable String userId) {
        int count = notificationService.markAllAsRead(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Supprimer une notification
     */
    @DeleteMapping("/{notificationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteNotification(@PathVariable String notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Récupérer les alertes déléguées pour le Chef de Projet
     */
    @GetMapping("/user/{userId}/delegated-alerts")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<List<Notification>> getDelegatedAlerts(@PathVariable String userId) {
        System.out.println("📥 GET /api/notifications/user/" + userId + "/delegated-alerts");
        List<Notification> delegatedAlerts = notificationService.getDelegatedAlerts(userId);
        System.out.println("✅ Retour de " + delegatedAlerts.size() + " alertes déléguées");
        return ResponseEntity.ok(delegatedAlerts);
    }
    
    /**
     * Nettoyer les anciennes notifications (admin uniquement)
     */
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cleanupOldNotifications(
            @RequestParam(defaultValue = "30") int daysOld) {
        System.out.println("🧹 Nettoyage des notifications de plus de " + daysOld + " jours");
        
        java.time.LocalDateTime cutoffDate = java.time.LocalDateTime.now().minusDays(daysOld);
        
        // Compter avant
        long totalBefore = notificationService.countAllNotifications();
        
        // Supprimer les anciennes notifications lues
        int deleted = notificationService.deleteOldReadNotifications(cutoffDate);
        
        // Compter après
        long totalAfter = notificationService.countAllNotifications();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("deletedCount", deleted);
        response.put("totalBefore", totalBefore);
        response.put("totalAfter", totalAfter);
        response.put("cutoffDate", cutoffDate.toString());
        
        System.out.println("✅ Nettoyage terminé: " + deleted + " notifications supprimées");
        
        return ResponseEntity.ok(response);
    }
}
