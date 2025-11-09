package com.example.demo.controller;

import com.example.demo.model.Notification;
import com.example.demo.service.InAppNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des notifications in-app
 * Tous les endpoints sont sécurisés et filtrent par userId du token JWT
 */
@RestController
@RequestMapping("/api/notifications/in-app")
@CrossOrigin(origins = "*")
public class InAppNotificationController {
    
    @Autowired
    private InAppNotificationService notificationService;
    
    /**
     * 1️⃣ Obtenir toutes les notifications de l'utilisateur connecté
     * GET /api/notifications
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications(Authentication authentication) {
        String userId = authentication.getName();
        List<Notification> notifications = notificationService.getAllNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * 2️⃣ Obtenir uniquement les notifications non lues
     * GET /api/notifications/unread
     */
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(Authentication authentication) {
        String userId = authentication.getName();
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * 3️⃣ Obtenir le compteur de notifications non lues
     * GET /api/notifications/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        String userId = authentication.getName();
        long count = notificationService.getUnreadCount(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", count);
        response.put("userId", userId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 4️⃣ Marquer une notification comme lue
     * POST /api/notifications/{id}/mark-read
     */
    @PostMapping("/{id}/mark-read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable String id,
            Authentication authentication) {
        
        String userId = authentication.getName();
        boolean success = notificationService.markAsRead(id, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("notificationId", id);
        
        if (success) {
            response.put("unreadCount", notificationService.getUnreadCount(userId));
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 5️⃣ Marquer TOUTES les notifications comme lues
     * POST /api/notifications/mark-all-read
     */
    @PostMapping("/mark-all-read")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        String userId = authentication.getName();
        int count = notificationService.markAllAsRead(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("markedCount", count);
        response.put("unreadCount", 0); // Devrait être 0 après cette opération
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 6️⃣ Marquer plusieurs notifications comme lues (bulk)
     * POST /api/notifications/mark-read-bulk
     * Body: { "ids": ["id1", "id2", "id3"] }
     */
    @PostMapping("/mark-read-bulk")
    public ResponseEntity<Map<String, Object>> markAsReadBulk(
            @RequestBody Map<String, List<String>> payload,
            Authentication authentication) {
        
        String userId = authentication.getName();
        List<String> ids = payload.getOrDefault("ids", List.of());
        
        int count = notificationService.markAsReadBulk(userId, ids);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("markedCount", count);
        response.put("unreadCount", notificationService.getUnreadCount(userId));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 7️⃣ Supprimer une notification
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(
            @PathVariable String id,
            Authentication authentication) {
        
        String userId = authentication.getName();
        boolean success = notificationService.deleteNotification(id, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("notificationId", id);
        
        if (success) {
            response.put("unreadCount", notificationService.getUnreadCount(userId));
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 8️⃣ Obtenir les notifications par catégorie
     * GET /api/notifications/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Notification>> getNotificationsByCategory(
            @PathVariable String category,
            Authentication authentication) {
        
        String userId = authentication.getName();
        List<Notification> notifications = notificationService
            .getNotificationsByCategory(userId, category);
        
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * 9️⃣ Obtenir les notifications par priorité
     * GET /api/notifications/priority/{priority}
     */
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<Notification>> getNotificationsByPriority(
            @PathVariable String priority,
            Authentication authentication) {
        
        String userId = authentication.getName();
        List<Notification> notifications = notificationService
            .getNotificationsByPriority(userId, priority);
        
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * 🔟 Créer une notification de test (pour développement)
     * POST /api/notifications/test
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> createTestNotification(
            @RequestBody Map<String, String> payload,
            Authentication authentication) {
        
        String userId = authentication.getName();
        
        String type = payload.getOrDefault("type", "info");
        String title = payload.getOrDefault("title", "Notification de test");
        String message = payload.getOrDefault("message", "Ceci est une notification de test");
        String priority = payload.getOrDefault("priority", "medium");
        String category = payload.getOrDefault("category", "system");
        
        Notification notification = notificationService.createNotification(
            userId, type, title, message, priority, category
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("notification", notification);
        response.put("unreadCount", notificationService.getUnreadCount(userId));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 📊 Obtenir les statistiques des notifications
     * GET /api/notifications/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getNotificationStats(Authentication authentication) {
        String userId = authentication.getName();
        
        List<Notification> allNotifications = notificationService.getAllNotifications(userId);
        long unreadCount = notificationService.getUnreadCount(userId);
        long readCount = allNotifications.size() - unreadCount;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", allNotifications.size());
        stats.put("unread", unreadCount);
        stats.put("read", readCount);
        stats.put("readPercentage", allNotifications.isEmpty() ? 0 : 
                  (readCount * 100.0 / allNotifications.size()));
        
        return ResponseEntity.ok(stats);
    }
}
