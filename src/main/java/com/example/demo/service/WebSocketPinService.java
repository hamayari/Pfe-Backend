package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WebSocketPinService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast a pin/unpin update to all subscribers of the conversation topic
     * Topic: /topic/conversation/{conversationId}
     * Payload structure expected by frontend:
     * {
     *   "type": "PIN_UPDATE",
     *   "messageId": "...",
     *   "pinned": true|false,
     *   "userId": "...",
     *   "timestamp": 1695660000000
     * }
     */
    public void broadcastPinUpdate(String conversationId, String messageId, boolean pinned, String userId) {
        if (conversationId == null || conversationId.isBlank()) {
            // If we don't have a conversationId we cannot broadcast to the correct room
            return;
        }
        Map<String, Object> payload = Map.of(
            "type", "PIN_UPDATE",
            "messageId", messageId,
            "pinned", pinned,
            "userId", userId,
            "timestamp", System.currentTimeMillis()
        );
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, payload);
    }
}
