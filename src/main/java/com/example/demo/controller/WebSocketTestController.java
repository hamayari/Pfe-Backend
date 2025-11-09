package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour tester les notifications WebSocket
 */
@RestController
@RequestMapping("/api/websocket-test")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class WebSocketTestController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Envoyer une notification de test à un utilisateur spécifique
     */
    @PostMapping("/send-notification/{userId}")
    public ResponseEntity<Map<String, String>> sendTestNotification(@PathVariable String userId) {
        log.info("📤 Envoi d'une notification de test à l'utilisateur: {}", userId);
        
        try {
            // Créer une notification de test
            Map<String, Object> notification = new HashMap<>();
            notification.put("id", "test-" + System.currentTimeMillis());
            notification.put("type", "TEST");
            notification.put("title", "🧪 Notification de Test");
            notification.put("message", "Ceci est une notification de test WebSocket envoyée à " + LocalDateTime.now());
            notification.put("priority", "MEDIUM");
            notification.put("timestamp", LocalDateTime.now().toString());
            notification.put("read", false);
            
            // Envoyer via WebSocket à l'utilisateur spécifique
            String destination = "/user/" + userId + "/queue/notifications";
            messagingTemplate.convertAndSend(destination, notification);
            
            log.info("✅ Notification de test envoyée avec succès à: {}", destination);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Notification de test envoyée à l'utilisateur " + userId,
                "destination", destination,
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi de la notification de test", e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Erreur: " + e.getMessage()
            ));
        }
    }

    /**
     * Envoyer une notification globale à tous les utilisateurs
     */
    @PostMapping("/send-global-notification")
    public ResponseEntity<Map<String, String>> sendGlobalNotification() {
        log.info("📢 Envoi d'une notification globale");
        
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("id", "global-" + System.currentTimeMillis());
            notification.put("type", "SYSTEM");
            notification.put("title", "📢 Notification Globale");
            notification.put("message", "Ceci est une notification globale envoyée à tous les utilisateurs connectés");
            notification.put("priority", "LOW");
            notification.put("timestamp", LocalDateTime.now().toString());
            notification.put("read", false);
            
            // Envoyer à tous via le topic
            messagingTemplate.convertAndSend("/topic/notifications", notification);
            
            log.info("✅ Notification globale envoyée avec succès");
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Notification globale envoyée à tous les utilisateurs",
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi de la notification globale", e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Erreur: " + e.getMessage()
            ));
        }
    }

    /**
     * Envoyer une notification d'échéance de convention
     */
    @PostMapping("/send-convention-alert/{userId}")
    public ResponseEntity<Map<String, String>> sendConventionAlert(
            @PathVariable String userId,
            @RequestParam(required = false, defaultValue = "CONV-2025-001") String conventionRef) {
        
        log.info("⚠️ Envoi d'une alerte de convention à l'utilisateur: {}", userId);
        
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("id", "alert-" + System.currentTimeMillis());
            notification.put("type", "CONVENTION_EXPIRATION");
            notification.put("title", "⚠️ Échéance de Convention");
            notification.put("message", String.format(
                "La convention %s expire dans 7 jours. Préparez le renouvellement si nécessaire.",
                conventionRef
            ));
            notification.put("priority", "HIGH");
            notification.put("timestamp", LocalDateTime.now().toString());
            notification.put("read", false);
            notification.put("relatedEntityId", conventionRef);
            notification.put("relatedEntityType", "CONVENTION");
            
            String destination = "/user/" + userId + "/queue/notifications";
            messagingTemplate.convertAndSend(destination, notification);
            
            log.info("✅ Alerte de convention envoyée avec succès");
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Alerte de convention envoyée",
                "conventionRef", conventionRef,
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi de l'alerte", e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Erreur: " + e.getMessage()
            ));
        }
    }

    /**
     * Vérifier le statut de la connexion WebSocket
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getWebSocketStatus() {
        log.info("🔍 Vérification du statut WebSocket");
        
        Map<String, Object> status = new HashMap<>();
        status.put("websocketEnabled", true);
        status.put("endpoint", "/ws");
        status.put("topics", new String[]{"/topic/notifications"});
        status.put("queues", new String[]{"/user/{userId}/queue/notifications"});
        status.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(status);
    }
}
