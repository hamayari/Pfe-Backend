package com.example.demo.service;

import com.example.demo.dto.MessageDTO;
import com.example.demo.model.Message;
import com.example.demo.model.MessageReaction;
import com.example.demo.model.User;
import com.example.demo.model.Conversation;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationService conversationService;

    // Envoyer un message
    public MessageDTO sendMessage(MessageDTO messageDTO) {
        Message message = new Message();
        // Safety: resolve sender by username if senderId absent or looks like username
        String senderId = messageDTO.getSenderId();
        if (senderId == null || senderId.isBlank()) {
            if (messageDTO.getSenderName() != null) {
                userRepository.findByUsername(messageDTO.getSenderName())
                        .ifPresent(u -> message.setSenderId(u.getId()));
            }
        } else {
            // If provided senderId is actually a username, resolve to id
            userRepository.findById(senderId)
                .ifPresentOrElse(
                    u -> message.setSenderId(u.getId()),
                    () -> userRepository.findByUsername(senderId)
                        .ifPresent(u -> message.setSenderId(u.getId()))
                );
            if (message.getSenderId() == null) {
                message.setSenderId(senderId);
            }
        }
        message.setSenderName(messageDTO.getSenderName());
        // Destinataires: s'assurer de ne pas inclure l'expéditeur
        List<String> safeRecipients = Optional.ofNullable(messageDTO.getRecipientIds()).orElse(List.of()).stream()
            .filter(id -> id != null && !id.equals(message.getSenderId()))
            .collect(Collectors.toList());
        message.setRecipientIds(safeRecipients);
        message.setRecipientNames(messageDTO.getRecipientNames());
        message.setSubject(messageDTO.getSubject());
        message.setContent(messageDTO.getContent());
        message.setMessageType(messageDTO.getMessageType());
        message.setPriority(messageDTO.getPriority());
        message.setAttachments(messageDTO.getAttachments());
        message.setRelatedEntityId(messageDTO.getRelatedEntityId());
        message.setRelatedEntityType(messageDTO.getRelatedEntityType());
        message.setMentions(messageDTO.getMentions());
        message.setStatus("SENT");
        message.setSentAt(LocalDateTime.now());
        // New fields for conversation-based messaging
        try { // tolerate nulls
            java.lang.reflect.Method getter = messageDTO.getClass().getMethod("getConversationId");
            Object conv = getter.invoke(messageDTO);
            if (conv != null) message.setConversationId(conv.toString());
        } catch (Exception ignored) {}
        message.setType("text");
        message.setRead(false);

        Message savedMessage = messageRepository.save(message);

        // Normaliser l'identifiant de conversation pour les messages directs
        if ("DIRECT".equalsIgnoreCase(messageDTO.getMessageType()) && message.getRecipientIds() != null && !message.getRecipientIds().isEmpty()) {
            String otherId = message.getRecipientIds().get(0);
            String a = message.getSenderId();
            String b = otherId;
            if (a != null && b != null) {
                String id1 = a.compareTo(b) <= 0 ? a : b;
                String id2 = a.compareTo(b) <= 0 ? b : a;
                message.setConversationId("dm_" + id1 + "_" + id2);
            }
        }

        // Mettre à jour la conversation
        if ("DIRECT".equalsIgnoreCase(messageDTO.getMessageType())) {
            updateDirectConversation(message);
        } else if ("GROUP".equalsIgnoreCase(messageDTO.getMessageType())) {
            updateGroupConversation(message);
        }

        return convertToDTO(savedMessage);
    }

    // Messages par conversation (compat front)
    public List<MessageDTO> getMessagesByConversation(String conversationId, String before, int limit) {
        if (conversationId == null || conversationId.isBlank()) return List.of();
        List<Message> messages = messageRepository.findByConversationIdOrderBySentAtDesc(conversationId);
        if (messages == null) return List.of();
        // pagination simple côté serveur (soft): before=timestamp ISO
        if (before != null && !before.isBlank()) {
            try {
                LocalDateTime beforeTs = LocalDateTime.parse(before);
                messages = messages.stream()
                        .filter(m -> m.getSentAt() != null && m.getSentAt().isBefore(beforeTs))
                        .collect(Collectors.toList());
            } catch (Exception ignored) {}
        }
        if (limit > 0 && messages.size() > limit) {
            messages = messages.subList(0, limit);
        }
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // ================ NOUVELLES FONCTIONNALITÉS ================

    // Ajouter une réaction à un message
    public Message addReaction(String messageId, String emoji, String userId, String userName) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message non trouvé"));

        if (message.getReactions() == null) {
            message.setReactions(new ArrayList<>());
        }

        // Vérifier si l'utilisateur a déjà réagi avec cet emoji
        MessageReaction existingReaction = message.getReactions().stream()
            .filter(r -> r.getEmoji().equals(emoji) && r.getUserId().equals(userId))
            .findFirst().orElse(null);

        if (existingReaction == null) {
            MessageReaction reaction = new MessageReaction(emoji, userId, userName);
            message.getReactions().add(reaction);
            return messageRepository.save(message);
        }

        return message; // Réaction déjà présente
    }

    // Supprimer une réaction
    public Message removeReaction(String messageId, String emoji, String userId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message non trouvé"));

        if (message.getReactions() != null) {
            message.getReactions().removeIf(r -> r.getEmoji().equals(emoji) && r.getUserId().equals(userId));
            return messageRepository.save(message);
        }

        return message;
    }

    // Répondre à un message (thread)
    public Message replyToMessage(String parentMessageId, String content, String conversationId, String senderId) {
        // Vérifier que le message parent existe
        messageRepository.findById(parentMessageId)
            .orElseThrow(() -> new RuntimeException("Message parent non trouvé"));

        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Message reply = new Message();
        reply.setParentMessageId(parentMessageId);
        reply.setConversationId(conversationId);
        reply.setSenderId(senderId);
        reply.setSenderName(sender.getUsername());
        reply.setContent(content);
        reply.setType("text");
        reply.setSentAt(LocalDateTime.now());
        reply.setRead(false);
        reply.setPriority("medium");

        // Récupérer les participants de la conversation
        Conversation conversation = conversationService.getConversationById(conversationId);
        if (conversation != null) {
            List<String> recipients = conversation.getParticipantIds().stream()
                .filter(id -> !id.equals(senderId))
                .collect(Collectors.toList());
            reply.setRecipientIds(recipients);
        }

        return messageRepository.save(reply);
    }

    // Obtenir le thread d'un message
    public List<Message> getMessageThread(String messageId) {
        return messageRepository.findByParentMessageIdOrderBySentAtAsc(messageId);
    }

    // Modifier un message
    public Message editMessage(String messageId, String newContent, String userId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message non trouvé"));

        if (!message.getSenderId().equals(userId)) {
            throw new RuntimeException("Vous ne pouvez modifier que vos propres messages");
        }

        message.setContent(newContent);
        message.setEdited(true);
        message.setEditedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());

        return messageRepository.save(message);
    }

    // Statistiques système
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalMessages", messageRepository.count());
        stats.put("totalConversations", conversationService.getTotalConversations());
        stats.put("activeUsers", userRepository.findByStatus("online").size());
        stats.put("messagesLast24h", getMessagesLast24Hours());
        stats.put("averageMessagesPerDay", getAverageMessagesPerDay());
        stats.put("topSenders", getTopMessageSenders(5));
        
        return stats;
    }

    // Modération de message
    public void moderateMessage(String messageId, String action) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message non trouvé"));

        switch (action.toLowerCase()) {
            case "hide":
                message.setStatus("HIDDEN");
                break;
            case "delete":
                message.setDeleted(true);
                message.setDeletedAt(LocalDateTime.now());
                break;
            case "flag":
                message.setStatus("FLAGGED");
                break;
            default:
                throw new IllegalArgumentException("Action non supportée: " + action);
        }

        messageRepository.save(message);
    }

    // Nettoyage des anciennes données
    public Map<String, Object> cleanupOldData(int daysOld) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysOld);
        Map<String, Object> result = new HashMap<>();

        // Supprimer les anciens messages supprimés
        List<Message> oldDeletedMessages = messageRepository.findByDeletedTrueAndDeletedAtBefore(cutoff);
        for (Message msg : oldDeletedMessages) {
            messageRepository.delete(msg);
        }

        result.put("deletedMessages", oldDeletedMessages.size());
        result.put("cleanupDate", LocalDateTime.now());
        result.put("cutoffDate", cutoff);

        return result;
    }

    // Méthodes utilitaires privées
    private long getMessagesLast24Hours() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return messageRepository.findAll().stream()
            .filter(msg -> msg.getSentAt() != null && msg.getSentAt().isAfter(yesterday))
            .count();
    }

    private double getAverageMessagesPerDay() {
        List<Message> allMessages = messageRepository.findAll();
        if (allMessages.isEmpty()) return 0.0;

        LocalDateTime oldest = allMessages.stream()
            .map(Message::getSentAt)
            .filter(Objects::nonNull)
            .min(LocalDateTime::compareTo)
            .orElse(LocalDateTime.now());

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(oldest, LocalDateTime.now());
        if (daysBetween == 0) daysBetween = 1;

        return (double) allMessages.size() / daysBetween;
    }

    private List<Map<String, Object>> getTopMessageSenders(int limit) {
        Map<String, Long> senderCounts = messageRepository.findAll().stream()
            .collect(Collectors.groupingBy(Message::getSenderId, Collectors.counting()));

        return senderCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(limit)
            .map(entry -> {
                Map<String, Object> sender = new HashMap<>();
                sender.put("userId", entry.getKey());
                sender.put("messageCount", entry.getValue());
                // Récupérer le nom de l'utilisateur
                userRepository.findById(entry.getKey()).ifPresent(user -> 
                    sender.put("userName", user.getUsername())
                );
                return sender;
            })
            .collect(Collectors.toList());
    }

    // Récupérer les messages d'un utilisateur
    public List<MessageDTO> getUserMessages(String userId) {
        List<Message> messages = messageRepository.findByRecipientIdsContainingOrderBySentAtDesc(userId);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Récupérer les messages envoyés par un utilisateur
    public List<MessageDTO> getSentMessages(String userId) {
        List<Message> messages = messageRepository.findBySenderIdOrderBySentAtDesc(userId);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Récupérer les messages non lus
    public List<MessageDTO> getUnreadMessages(String userId) {
        List<Message> messages = messageRepository.findUnreadMessagesByRecipientId(userId);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Récupérer les messages d'une conversation directe
    public List<MessageDTO> getDirectConversationMessages(String userId1, String userId2) {
        List<Message> messages = messageRepository.findConversationMessages(userId1, userId2);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Marquer les messages d'une conversation comme lus pour un utilisateur (par conversationId)
    public void markConversationReadByConversationId(String conversationId, String userId) {
        if (conversationId == null || userId == null) return;
        List<Message> unread = messageRepository.findUnreadMessages(conversationId, userId);
        for (Message m : unread) {
            m.setRead(true);
            m.setStatus("READ");
            m.setReadAt(LocalDateTime.now());
            m.setUpdatedAt(LocalDateTime.now());
            messageRepository.save(m);
        }
    }

    // Récupérer les messages de groupe
    public List<MessageDTO> getGroupMessages(String userId) {
        List<Message> messages = messageRepository.findGroupMessagesByRecipientId(userId);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Récupérer les messages système
    public List<MessageDTO> getSystemMessages(String userId) {
        List<Message> messages = messageRepository.findSystemMessagesByRecipientId(userId);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Marquer un message comme lu
    public MessageDTO markAsRead(String messageId, String userId) {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isPresent()) {
            Message message = optionalMessage.get();
            if (message.getRecipientIds().contains(userId)) {
                message.setStatus("READ");
                message.setReadAt(LocalDateTime.now());
                message.setUpdatedAt(LocalDateTime.now());
                Message savedMessage = messageRepository.save(message);
                return convertToDTO(savedMessage);
            }
        }
        return null;
    }

    // Marquer tous les messages comme lus pour une conversation
    public void markConversationAsRead(String userId1, String userId2) {
        List<Message> messages = messageRepository.findConversationMessages(userId1, userId2);
        messages.stream()
                .filter(message -> message.getRecipientIds().contains(userId1) && !"READ".equals(message.getStatus()))
                .forEach(message -> {
                    message.setStatus("READ");
                    message.setReadAt(LocalDateTime.now());
                    message.setUpdatedAt(LocalDateTime.now());
                    messageRepository.save(message);
                });
    }

    // Supprimer un message
    public void deleteMessage(String messageId, String userId) {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isPresent()) {
            Message message = optionalMessage.get();
            if (message.getSenderId().equals(userId)) {
                messageRepository.deleteById(messageId);
            }
        }
    }

    public void togglePin(String messageId, String userId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message non trouvé"));
        // Autoriser l'expéditeur à épingler; adapter selon besoins (owner/conversation)
        if (!message.getSenderId().equals(userId)) return;
        boolean newPinned = !message.isPinned();
        message.setPinned(newPinned);
        message.setPinnedByUserId(newPinned ? userId : null);
        message.setPinnedAt(newPinned ? LocalDateTime.now() : null);
        messageRepository.save(message);
    }

    // Récupérer les messages par type
    public List<MessageDTO> getMessagesByType(String messageType) {
        List<Message> messages = messageRepository.findByMessageTypeOrderBySentAtDesc(messageType);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Récupérer les messages par priorité
    public List<MessageDTO> getMessagesByPriority(String priority) {
        List<Message> messages = messageRepository.findByPriorityOrderBySentAtDesc(priority);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Récupérer les messages urgents
    public List<MessageDTO> getUrgentMessages() {
        List<Message> messages = messageRepository.findUrgentMessages();
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Récupérer les messages avec pièces jointes
    public List<MessageDTO> getMessagesWithAttachments() {
        List<Message> messages = messageRepository.findMessagesWithAttachments();
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Récupérer les messages liés à une entité
    public List<MessageDTO> getMessagesByEntity(String entityId, String entityType) {
        List<Message> messages = messageRepository.findByRelatedEntity(entityId, entityType);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Compter les messages non lus
    public long countUnreadMessages(String userId) {
        return messageRepository.countUnreadMessagesByRecipientId(userId);
    }

    // Nettoyer les anciens messages
    public void cleanupOldMessages(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        messageRepository.deleteBySentAtBefore(cutoffDate);
    }

    // Mettre à jour une conversation directe
    private void updateDirectConversation(Message message) {
        String senderId = message.getSenderId();
        String recipientId = message.getRecipientIds().get(0);
        
        conversationService.updateOrCreateDirectConversation(senderId, recipientId, message);
    }

    // Mettre à jour une conversation de groupe
    private void updateGroupConversation(Message message) {
        // Logique pour mettre à jour les conversations de groupe
        // À implémenter selon les besoins
    }

    // Convertir Message en MessageDTO
    private MessageDTO convertToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSenderId());
        dto.setSenderName(message.getSenderName());
        dto.setRecipientIds(message.getRecipientIds());
        dto.setRecipientNames(message.getRecipientNames());
        dto.setSubject(message.getSubject());
        dto.setContent(message.getContent());
        dto.setMessageType(message.getMessageType());
        dto.setPriority(message.getPriority());
        dto.setStatus(message.getStatus());
        dto.setAttachments(message.getAttachments());
        dto.setRelatedEntityId(message.getRelatedEntityId());
        dto.setRelatedEntityType(message.getRelatedEntityType());
        dto.setSentAt(message.getSentAt());
        dto.setDeliveredAt(message.getDeliveredAt());
        dto.setReadAt(message.getReadAt());
        dto.setUpdatedAt(message.getUpdatedAt());
        dto.setConversationId(message.getConversationId());
        dto.setType(message.getType());
        dto.setRead(message.isRead());
        dto.setSenderAvatar(message.getSenderAvatar());
        dto.setMentions(message.getMentions());
        dto.setPinned(message.isDeleted() ? false : message.isEdited() && false); // default
        dto.setPinnedAt(null);
        dto.setPinnedByUserId(null);
        return dto;
    }


} 