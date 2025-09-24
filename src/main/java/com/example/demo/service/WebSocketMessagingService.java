package com.example.demo.service;

import com.example.demo.model.Message;
import com.example.demo.model.Conversation;
import com.example.demo.model.User;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WebSocketMessagingService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    // ================ GESTION DES MESSAGES ================

    public Message saveMessage(String conversationId, String content, String type, String senderId) {
        try {
            // Vérifier que la conversation existe et que l'utilisateur y a accès
            Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
            if (conversation == null || !conversation.getParticipantIds().contains(senderId)) {
                throw new RuntimeException("Conversation non trouvée ou accès refusé");
            }

            // Récupérer les infos de l'expéditeur
            User sender = userRepository.findById(senderId).orElse(null);
            if (sender == null) {
                throw new RuntimeException("Utilisateur non trouvé");
            }

            // Créer le message
            Message message = new Message();
            message.setConversationId(conversationId);
            message.setSenderId(senderId);
            message.setSenderName(sender.getUsername());
            message.setContent(content);
            message.setType(type);
            message.setSentAt(LocalDateTime.now());
            message.setRead(false);
            message.setPriority("medium");
            
            // Destinataires = tous les participants sauf l'expéditeur
            List<String> recipients = conversation.getParticipantIds().stream()
                .filter(id -> !id.equals(senderId))
                .collect(Collectors.toList());
            message.setRecipientIds(recipients);

            Message savedMessage = messageRepository.save(message);

            // Mettre à jour la conversation
            conversation.setLastMessageAt(LocalDateTime.now());
            conversation.setLastMessageContent(content);
            conversation.setLastMessageSenderId(senderId);
            conversation.setLastMessageSenderName(sender.getUsername());
            conversationRepository.save(conversation);

            System.out.println("💾 Message sauvegardé: " + savedMessage.getId());
            return savedMessage;

        } catch (Exception e) {
            System.err.println("❌ Erreur sauvegarde message: " + e.getMessage());
            throw e;
        }
    }

    // ================ GESTION DU STATUT UTILISATEUR ================

    public void updateUserStatus(String userId, String status, String statusMessage) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setStatus(status);
                user.setStatusMessage(statusMessage);
                userRepository.save(user);
                System.out.println("👤 Statut utilisateur mis à jour: " + user.getUsername() + " -> " + status);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour statut: " + e.getMessage());
        }
    }

    public void setUserOnlineStatus(String userId, boolean isOnline) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setStatus(isOnline ? "online" : "offline");
                userRepository.save(user);
                System.out.println("🔌 Statut connexion: " + user.getUsername() + " -> " + (isOnline ? "en ligne" : "hors ligne"));
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur statut connexion: " + e.getMessage());
        }
    }

    // ================ VÉRIFICATIONS D'ACCÈS ================

    public boolean userHasAccessToConversation(String userId, String conversationId) {
        try {
            Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
            return conversation != null && conversation.getParticipantIds().contains(userId);
        } catch (Exception e) {
            System.err.println("❌ Erreur vérification accès: " + e.getMessage());
            return false;
        }
    }

    // ================ GESTION DES CONVERSATIONS ================

    public List<Conversation> getUserConversations(String userId) {
        try {
            return conversationRepository.findByParticipantIdsContaining(userId);
        } catch (Exception e) {
            System.err.println("❌ Erreur récupération conversations: " + e.getMessage());
            return List.of();
        }
    }

    public List<Message> getConversationMessages(String conversationId, String userId, int limit) {
        try {
            // Vérifier l'accès
            if (!userHasAccessToConversation(userId, conversationId)) {
                return List.of();
            }

            // Récupérer les messages récents
            List<Message> messages = messageRepository.findByConversationIdOrderBySentAtDesc(conversationId);
            
            // Limiter le nombre de messages
            return messages.stream()
                .limit(limit)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            System.err.println("❌ Erreur récupération messages: " + e.getMessage());
            return List.of();
        }
    }

    // ================ UTILITAIRES ================

    public String generateDefaultAvatar(String username) {
        String initials = username.length() >= 2 ? username.substring(0, 2).toUpperCase() : username.toUpperCase();
        String[] colors = {"#e91e63", "#9c27b0", "#673ab7", "#3f51b5", "#2196f3", "#00bcd4", "#009688", "#4caf50"};
        String color = colors[Math.abs(username.hashCode()) % colors.length];
        
        String svg = String.format(
            "<svg width='40' height='40' xmlns='http://www.w3.org/2000/svg'>" +
            "<rect width='40' height='40' fill='%s' rx='6'/>" +
            "<text x='20' y='26' text-anchor='middle' fill='white' font-family='Arial' font-size='14' font-weight='600'>%s</text>" +
            "</svg>", color, initials
        );
        
        return "data:image/svg+xml;base64," + java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }

    // ================ MÉTHODES DE STATISTIQUES ================

    public long getActiveUsersCount() {
        try {
            return userRepository.findByStatus("online").size();
        } catch (Exception e) {
            System.err.println("❌ Erreur comptage utilisateurs actifs: " + e.getMessage());
            return 0;
        }
    }

    public long getTotalMessagesToday() {
        try {
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            return messageRepository.findAll().stream()
                .filter(msg -> msg.getSentAt() != null && msg.getSentAt().isAfter(startOfDay))
                .count();
        } catch (Exception e) {
            System.err.println("❌ Erreur comptage messages: " + e.getMessage());
            return 0;
        }
    }
}






