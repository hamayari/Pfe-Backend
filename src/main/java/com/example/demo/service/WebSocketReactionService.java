package com.example.demo.service;

import com.example.demo.model.MessageReaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WebSocketReactionService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Diffuse une réaction ajoutée/modifiée à tous les participants de la conversation
     */
    public void broadcastReactionAdded(String conversationId, String messageId, MessageReaction reaction) {
        Map<String, Object> payload = Map.of(
            "type", "REACTION_ADDED",
            "messageId", messageId,
            "reaction", reaction,
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, payload);
    }

    /**
     * Diffuse une réaction supprimée à tous les participants de la conversation
     */
    public void broadcastReactionRemoved(String conversationId, String messageId, String userId) {
        Map<String, Object> payload = Map.of(
            "type", "REACTION_REMOVED",
            "messageId", messageId,
            "userId", userId,
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, payload);
    }

    /**
     * Diffuse une réaction modifiée à tous les participants de la conversation
     */
    public void broadcastReactionModified(String conversationId, String messageId, MessageReaction oldReaction, MessageReaction newReaction) {
        Map<String, Object> payload = Map.of(
            "type", "REACTION_MODIFIED",
            "messageId", messageId,
            "oldReaction", oldReaction,
            "newReaction", newReaction,
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, payload);
    }

    /**
     * Diffuse les statistiques mises à jour des réactions
     */
    public void broadcastReactionStats(String conversationId, String messageId, Map<String, Object> stats) {
        Map<String, Object> payload = Map.of(
            "type", "REACTION_STATS_UPDATED",
            "messageId", messageId,
            "stats", stats,
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, payload);
    }

    /**
     * Notification personnalisée à un utilisateur spécifique
     */
    public void notifyUser(String userId, String messageId, String emoji, String senderName) {
        Map<String, Object> payload = Map.of(
            "type", "REACTION_NOTIFICATION",
            "messageId", messageId,
            "emoji", emoji,
            "senderName", senderName,
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSendToUser(userId, "/queue/reactions", payload);
    }
}



































