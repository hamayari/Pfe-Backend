package com.example.demo.controller;

import com.example.demo.model.ChatMessage;
import com.example.demo.model.User;
import com.example.demo.service.AuthService;
import com.example.demo.service.WebSocketService;
import com.example.demo.service.UserService;
import com.example.demo.service.MessageService;
import com.example.demo.service.WebSocketReactionService;
import com.example.demo.service.WebSocketPinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@CrossOrigin(origins = "*")
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private AuthService authService;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private WebSocketReactionService webSocketReactionService;

    @Autowired
    private WebSocketPinService webSocketPinService;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now());
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String username = chatMessage.getSender();
        System.out.println("🔌 User joining WebSocket: " + username);
        
        // Add username in websocket session
        headerAccessor.getSessionAttributes().put("username", username);
        
        // Récupérer l'utilisateur depuis la base de données
        User user = userService.getUserByUsername(username);
        if (user != null) {
            // Notifier via le service WebSocket
            webSocketService.notifyUserConnected(user);
            
            // Mettre à jour le statut en ligne
            userService.setUserOnlineStatus(user.getId(), true);
            
            chatMessage.setTimestamp(LocalDateTime.now());
            chatMessage.setType(ChatMessage.MessageType.JOIN);
            chatMessage.setContent(username + " s'est connecté");
            
            System.out.println("✅ User " + username + " successfully added to WebSocket");
        } else {
            System.err.println("❌ User not found: " + username);
        }
        
        return chatMessage;
    }

    @MessageMapping("/chat.sendPrivateMessage")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now());
        // Send to specific user
        messagingTemplate.convertAndSendToUser(
            chatMessage.getRecipient(), 
            "/queue/private", 
            chatMessage
        );
    }

    @MessageMapping("/chat.typing")
    @SendTo("/topic/public")
    public ChatMessage typing(@Payload ChatMessage chatMessage) {
        chatMessage.setType(ChatMessage.MessageType.TYPING);
        return chatMessage;
    }

    @MessageMapping("/chat.stopTyping")
    @SendTo("/topic/public")
    public ChatMessage stopTyping(@Payload ChatMessage chatMessage) {
        chatMessage.setType(ChatMessage.MessageType.STOP_TYPING);
        return chatMessage;
    }

    // ================== Reactions over WebSocket ==================
    // Front sends payload: { type: 'REACTION', messageId, emoji, userId, userName }
    @MessageMapping("/chat.reaction")
    public void handleReaction(@Payload Map<String, Object> payload) {
        try {
            String messageId = payload.get("messageId") != null ? payload.get("messageId").toString() : null;
            String emoji = payload.get("emoji") != null ? payload.get("emoji").toString() : null;
            String userId = payload.get("userId") != null ? payload.get("userId").toString() : null;
            String userName = payload.get("userName") != null ? payload.get("userName").toString() : null;

            if (messageId == null || emoji == null || userId == null) return;

            // Persist + server-side broadcast (REACTION_ADDED)
            var saved = messageService.addReaction(messageId, emoji, userId, userName);

            // Echo a compatibility event the frontend already expects: type 'REACTION'
            Map<String, Object> echo = Map.of(
                "type", "REACTION",
                "messageId", messageId,
                "emoji", emoji,
                "userId", userId,
                "userName", userName,
                "timestamp", java.time.Instant.now().toString()
            );
            if (saved != null && saved.getConversationId() != null) {
                messagingTemplate.convertAndSend("/topic/conversation/" + saved.getConversationId(), echo);
            }
        } catch (Exception ignored) {}
    }

    // Front sends payload: { messageId, emoji, userId }
    @MessageMapping("/chat.reaction.remove")
    public void handleReactionRemove(@Payload Map<String, Object> payload) {
        try {
            String messageId = payload.get("messageId") != null ? payload.get("messageId").toString() : null;
            String emoji = payload.get("emoji") != null ? payload.get("emoji").toString() : null;
            String userId = payload.get("userId") != null ? payload.get("userId").toString() : null;
            if (messageId == null || emoji == null || userId == null) return;

            var saved = messageService.removeReaction(messageId, emoji, userId);
            if (saved != null && saved.getConversationId() != null) {
                Map<String, Object> echo = Map.of(
                    "type", "REACTION_REMOVED",
                    "messageId", messageId,
                    "emoji", emoji,
                    "userId", userId,
                    "timestamp", java.time.Instant.now().toString()
                );
                messagingTemplate.convertAndSend("/topic/conversation/" + saved.getConversationId(), echo);
            }
        } catch (Exception ignored) {}
    }

    // ================== Pin over WebSocket ==================
    // Front sends payload: { messageId, pinned (bool), userId }
    @MessageMapping("/chat.pin")
    public void handlePin(@Payload Map<String, Object> payload) {
        try {
            String messageId = payload.get("messageId") != null ? payload.get("messageId").toString() : null;
            String userId = payload.get("userId") != null ? payload.get("userId").toString() : null;
            if (messageId == null || userId == null) return;

            var saved = messageService.togglePin(messageId, userId);
            if (saved != null && saved.getConversationId() != null) {
                Map<String, Object> echo = Map.of(
                    "type", "PIN_UPDATE",
                    "messageId", saved.getId(),
                    "pinned", saved.isPinned(),
                    "userId", userId,
                    "timestamp", java.time.Instant.now().toString()
                );
                messagingTemplate.convertAndSend("/topic/conversation/" + saved.getConversationId(), echo);
            }
        } catch (Exception ignored) {}
    }

    // Endpoint pour vérifier la connexion WebSocket
    @MessageMapping("/ping")
    @SendTo("/topic/pong")
    public Map<String, Object> ping(SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        int connectedUsersCount = webSocketService.getConnectedUsers().size();
        
        return Map.of(
            "status", "connected",
            "username", username != null ? username : "anonymous",
            "timestamp", LocalDateTime.now(),
            "connectedUsersCount", connectedUsersCount
        );
    }

    // Endpoint pour obtenir la liste des utilisateurs connectés
    @MessageMapping("/getConnectedUsers")
    @SendTo("/topic/connectedUsers")
    public Object getConnectedUsers() {
        return webSocketService.getConnectedUsers().values();
    }
}
