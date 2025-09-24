package com.example.demo.controller;

import com.example.demo.model.Message;
import com.example.demo.model.Conversation;
import com.example.demo.service.SimpleMessagingService;
import com.example.demo.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simple-messaging")
public class SimpleMessagingController {

    @Autowired
    private SimpleMessagingService messagingService;

    // ================ CANAUX ================

    @GetMapping("/channels")
    public ResponseEntity<List<Conversation>> getChannelsForUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<Conversation> channels = messagingService.getChannelsForUser(userPrincipal.getId());
            return ResponseEntity.ok(channels);
        } catch (Exception e) {
            System.err.println("Erreur getChannelsForUser: " + e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    @PostMapping("/channels")
    public ResponseEntity<Conversation> createChannel(
            @RequestBody Map<String, Object> channelData,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            String name = (String) channelData.get("name");
            String description = (String) channelData.getOrDefault("description", "");
            Boolean isPublic = (Boolean) channelData.getOrDefault("isPublic", true);
            
            Conversation channel = messagingService.createChannel(name, description, isPublic, userPrincipal.getId());
            return ResponseEntity.ok(channel);
        } catch (Exception e) {
            System.err.println("Erreur createChannel: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ================ MESSAGES DIRECTS ================

    @GetMapping("/direct-messages")
    public ResponseEntity<List<Conversation>> getDirectMessagesForUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<Conversation> dms = messagingService.getDirectMessagesForUser(userPrincipal.getId());
            return ResponseEntity.ok(dms);
        } catch (Exception e) {
            System.err.println("Erreur getDirectMessagesForUser: " + e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    @PostMapping("/direct-messages")
    public ResponseEntity<Conversation> createDirectMessage(
            @RequestBody Map<String, String> dmData,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            String otherUserId = dmData.get("otherUserId");
            Conversation dm = messagingService.createOrGetDirectMessage(userPrincipal.getId(), otherUserId);
            return ResponseEntity.ok(dm);
        } catch (Exception e) {
            System.err.println("Erreur createDirectMessage: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ================ MESSAGES ================

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<Message>> getMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<Message> messages = messagingService.getMessages(conversationId, userPrincipal.getId(), page, size);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            System.err.println("Erreur getMessages: " + e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    @PostMapping("/messages")
    public ResponseEntity<Message> sendMessage(
            @RequestBody Map<String, Object> messageData,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            String conversationId = (String) messageData.get("conversationId");
            String content = (String) messageData.get("content");
            String messageType = (String) messageData.getOrDefault("messageType", "text");
            
            Message message = messagingService.sendMessage(conversationId, content, messageType, userPrincipal.getId());
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            System.err.println("Erreur sendMessage: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ================ UTILISATEURS ================

    @GetMapping("/online-users")
    public ResponseEntity<List<Map<String, Object>>> getOnlineUsers() {
        try {
            List<Map<String, Object>> onlineUsers = messagingService.getOnlineUsers();
            return ResponseEntity.ok(onlineUsers);
        } catch (Exception e) {
            System.err.println("Erreur getOnlineUsers: " + e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    // ================ NOTIFICATIONS ================

    @PostMapping("/conversations/{conversationId}/mark-read")
    public ResponseEntity<?> markAsRead(
            @PathVariable String conversationId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            messagingService.markConversationAsRead(conversationId, userPrincipal.getId());
            return ResponseEntity.ok(Map.of("message", "Conversation marquée comme lue"));
        } catch (Exception e) {
            System.err.println("Erreur markAsRead: " + e.getMessage());
            return ResponseEntity.ok(Map.of("message", "Erreur"));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Map<String, Object> unreadData = messagingService.getUnreadCount(userPrincipal.getId());
            return ResponseEntity.ok(unreadData);
        } catch (Exception e) {
            System.err.println("Erreur getUnreadCount: " + e.getMessage());
            return ResponseEntity.ok(Map.of("total", 0, "byConversation", Map.of()));
        }
    }
}






