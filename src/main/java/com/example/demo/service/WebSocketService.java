package com.example.demo.service;

import com.example.demo.model.ChatMessage;
import com.example.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserService userService;

    // Map pour stocker les utilisateurs connectés (username -> User)
    private final Map<String, User> connectedUsers = new ConcurrentHashMap<>();

    /**
     * Notifier qu'un utilisateur s'est connecté
     */
    public void notifyUserConnected(User user) {
        System.out.println("🔌 Adding user to connected list: " + user.getUsername());
        connectedUsers.put(user.getUsername(), user);
        
        ChatMessage message = new ChatMessage();
        message.setType(ChatMessage.MessageType.JOIN);
        message.setSender(user.getUsername());
        message.setContent(user.getUsername() + " s'est connecté");
        message.setTimestamp(LocalDateTime.now());
        
        // Envoyer à tous les utilisateurs connectés
        messagingTemplate.convertAndSend("/topic/public", message);
        
        // Envoyer la liste des utilisateurs connectés
        messagingTemplate.convertAndSend("/topic/connectedUsers", connectedUsers.values());
        
        System.out.println("👥 Total connected users: " + connectedUsers.size());
        System.out.println("👥 Connected users: " + connectedUsers.keySet());
    }

    /**
     * Notifier qu'un utilisateur s'est déconnecté
     */
    public void notifyUserDisconnected(String username) {
        System.out.println("🔌 Removing user from connected list: " + username);
        
        // Mettre à jour le statut en ligne dans la base de données
        User user = userService.getUserByUsername(username);
        if (user != null) {
            userService.setUserOnlineStatus(user.getId(), false);
        }
        
        connectedUsers.remove(username);
        
        ChatMessage message = new ChatMessage();
        message.setType(ChatMessage.MessageType.LEAVE);
        message.setSender(username);
        message.setContent(username + " s'est déconnecté");
        message.setTimestamp(LocalDateTime.now());
        
        // Envoyer à tous les utilisateurs connectés
        messagingTemplate.convertAndSend("/topic/public", message);
        
        // Envoyer la liste des utilisateurs connectés
        messagingTemplate.convertAndSend("/topic/connectedUsers", connectedUsers.values());
        
        System.out.println("👥 Total connected users: " + connectedUsers.size());
        System.out.println("👥 Connected users: " + connectedUsers.keySet());
    }

    /**
     * Envoyer un message privé
     */
    public void sendPrivateMessage(String from, String to, String content) {
        ChatMessage message = new ChatMessage();
        message.setType(ChatMessage.MessageType.CHAT);
        message.setSender(from);
        message.setRecipient(to);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        
        messagingTemplate.convertAndSendToUser(to, "/queue/private", message);
    }

    /**
     * Envoyer un message à un canal
     */
    public void sendChannelMessage(String from, String channelId, String content) {
        ChatMessage message = new ChatMessage();
        message.setType(ChatMessage.MessageType.CHAT);
        message.setSender(from);
        message.setConversationId(channelId);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        
        messagingTemplate.convertAndSend("/topic/channel." + channelId, message);
    }

    /**
     * Obtenir les utilisateurs connectés
     */
    public Map<String, User> getConnectedUsers() {
        return new ConcurrentHashMap<>(connectedUsers);
    }

    /**
     * Vérifier si un utilisateur est connecté
     */
    public boolean isUserConnected(String username) {
        return connectedUsers.containsKey(username);
    }

    /**
     * Envoyer un ping pour vérifier la connexion
     */
    public void sendPing(String username) {
        Map<String, Object> pong = Map.of(
            "status", "pong",
            "username", username,
            "timestamp", LocalDateTime.now(),
            "connectedUsers", connectedUsers.size()
        );
        
        messagingTemplate.convertAndSendToUser(username, "/queue/pong", pong);
    }
}
