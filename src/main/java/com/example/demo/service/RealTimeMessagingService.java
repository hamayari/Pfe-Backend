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
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RealTimeMessagingService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    // ================ CANAUX ================

    public List<Conversation> getChannelsForUser(String userId) {
        // Récupérer tous les canaux où l'utilisateur est participant
        List<Conversation> channels = conversationRepository.findByParticipantIdsContainingAndType(userId, "GROUP");
        
        // Si aucun canal, créer les canaux par défaut
        if (channels.isEmpty()) {
            initializeDefaultChannels(userId);
            channels = conversationRepository.findByParticipantIdsContainingAndType(userId, "GROUP");
        }
        
        // Calculer les messages non lus pour chaque canal
        for (Conversation channel : channels) {
            int unreadCount = messageRepository.countUnreadMessages(channel.getId(), userId);
            channel.setUnreadCount(unreadCount);
        }
        
        return channels;
    }

    public Conversation createChannel(String name, String description, boolean isPublic, List<String> participantIds, String createdBy) {
        Conversation channel = new Conversation();
        channel.setName(name);
        channel.setDescription(description);
        channel.setType("GROUP");
        channel.setCreatedBy(createdBy);
        channel.setCreatedAt(LocalDateTime.now());
        channel.setIsPublic(isPublic);
        channel.setIsArchived(false);
        channel.setActive(true);
        
        // Ajouter le créateur aux participants
        Set<String> participants = new HashSet<>(participantIds);
        participants.add(createdBy);
        channel.setParticipantIds(new ArrayList<>(participants));
        
        return conversationRepository.save(channel);
    }

    private void initializeDefaultChannels(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        // Canal général (public, tous les utilisateurs)
        createDefaultChannel("général", "Discussions générales pour toute l'équipe", true, getAllUserIds());

        // Canal commercial (privé, seulement les commerciaux et admins)
        if (hasRole(user, "ROLE_COMMERCIAL") || hasRole(user, "ROLE_ADMIN")) {
            List<String> commercialUsers = getUsersByRole("ROLE_COMMERCIAL");
            commercialUsers.addAll(getUsersByRole("ROLE_ADMIN"));
            createDefaultChannel("commercial", "Canal dédié à l'équipe commerciale", false, commercialUsers);
        }

        // Canal projets (privé, project managers et commerciaux)
        if (hasRole(user, "ROLE_PROJECT_MANAGER") || hasRole(user, "ROLE_COMMERCIAL") || hasRole(user, "ROLE_ADMIN")) {
            List<String> projectUsers = getUsersByRole("ROLE_PROJECT_MANAGER");
            projectUsers.addAll(getUsersByRole("ROLE_COMMERCIAL"));
            projectUsers.addAll(getUsersByRole("ROLE_ADMIN"));
            createDefaultChannel("projets", "Gestion et suivi des projets", false, projectUsers);
        }

        // Canal urgences (public, tous les utilisateurs)
        createDefaultChannel("urgences", "Messages critiques et urgents", true, getAllUserIds());
    }

    private void createDefaultChannel(String name, String description, boolean isPublic, List<String> participantIds) {
        // Vérifier si le canal existe déjà
        List<Conversation> existing = conversationRepository.findByNameAndType(name, "GROUP");
        if (!existing.isEmpty()) return;

        Conversation channel = new Conversation();
        channel.setName(name);
        channel.setDescription(description);
        channel.setType("GROUP");
        channel.setCreatedBy("system");
        channel.setCreatedAt(LocalDateTime.now());
        channel.setIsPublic(isPublic);
        channel.setIsArchived(false);
        channel.setActive(true);
        channel.setParticipantIds(participantIds);
        channel.setUnreadCount(0);

        conversationRepository.save(channel);
        System.out.println("📋 Canal créé: #" + name + " (" + participantIds.size() + " membres)");
    }

    // ================ MESSAGES DIRECTS ================

    public List<Conversation> getDirectMessagesForUser(String userId) {
        List<Conversation> dms = conversationRepository.findByParticipantIdsContainingAndType(userId, "DIRECT");
        
        // Calculer les messages non lus pour chaque DM
        for (Conversation dm : dms) {
            int unreadCount = messageRepository.countUnreadMessages(dm.getId(), userId);
            dm.setUnreadCount(unreadCount);
            
            // Ajouter les informations de l'autre utilisateur
            String otherUserId = dm.getParticipantIds().stream()
                .filter(id -> !id.equals(userId))
                .findFirst().orElse(null);
            
            if (otherUserId != null) {
                User otherUser = userRepository.findById(otherUserId).orElse(null);
                if (otherUser != null) {
                    dm.setName(otherUser.getUsername());
                    // Ajouter les infos pour l'affichage
                    dm.setDescription("Message direct avec " + otherUser.getUsername());
                }
            }
        }
        
        return dms;
    }

    public Conversation createOrGetDirectMessage(String userId1, String userId2) {
        // Chercher une conversation directe existante entre ces deux utilisateurs
        List<Conversation> existing = conversationRepository.findDirectConversationBetweenUsers(userId1, userId2);
        
        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        // Créer une nouvelle conversation directe
        Conversation dm = new Conversation();
        dm.setName("Direct Message");
        dm.setType("DIRECT");
        dm.setCreatedBy(userId1);
        dm.setCreatedAt(LocalDateTime.now());
        dm.setIsPublic(false);
        dm.setIsArchived(false);
        dm.setActive(true);
        dm.setParticipantIds(Arrays.asList(userId1, userId2));
        dm.setUnreadCount(0);

        return conversationRepository.save(dm);
    }

    // ================ MESSAGES ================

    public List<Message> getMessages(String conversationId, String userId, int page, int size) {
        // Vérifier que l'utilisateur a accès à cette conversation
        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation == null || !conversation.getParticipantIds().contains(userId)) {
            return new ArrayList<>();
        }

        // Récupérer les messages (pagination)
        List<Message> messages = messageRepository.findByConversationIdOrderBySentAtDesc(conversationId);
        
        // Limiter le nombre de messages
        int start = page * size;
        int end = Math.min(start + size, messages.size());
        
        if (start >= messages.size()) {
            return new ArrayList<>();
        }
        
        List<Message> paginatedMessages = messages.subList(start, end);
        Collections.reverse(paginatedMessages); // Ordre chronologique
        
        return paginatedMessages;
    }

    public Message sendMessage(String conversationId, String content, String messageType, String senderId) {
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
        message.setType(messageType);
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

        return savedMessage;
    }

    // ================ UTILISATEURS EN LIGNE ================

    public List<Map<String, Object>> getOnlineUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
            .map(user -> {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("avatar", user.getAvatar() != null ? user.getAvatar() : generateDefaultAvatar(user.getUsername()));
                userInfo.put("status", user.getStatus() != null ? user.getStatus() : "online");
                userInfo.put("statusMessage", user.getStatusMessage() != null ? user.getStatusMessage() : "Disponible");
                userInfo.put("roles", user.getRoles().stream()
                    .map(role -> role.getName().toString())
                    .collect(Collectors.toList()));
                return userInfo;
            })
            .collect(Collectors.toList());
    }

    public void updateUserStatus(String userId, String status, String statusMessage) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setStatus(status);
            user.setStatusMessage(statusMessage);
            userRepository.save(user);
        }
    }

    // ================ NOTIFICATIONS ================

    public void markConversationAsRead(String conversationId, String userId) {
        // Marquer tous les messages non lus de cette conversation comme lus
        List<Message> unreadMessages = messageRepository.findUnreadMessages(conversationId, userId);
        for (Message message : unreadMessages) {
            message.setRead(true);
            messageRepository.save(message);
        }
    }

    public Map<String, Object> getUnreadCount(String userId) {
        Map<String, Object> result = new HashMap<>();
        
        // Compter les messages non lus par conversation
        Map<String, Integer> unreadByConversation = new HashMap<>();
        int totalUnread = 0;
        
        List<Conversation> conversations = conversationRepository.findByParticipantIdsContaining(userId);
        for (Conversation conv : conversations) {
            int unread = messageRepository.countUnreadMessages(conv.getId(), userId);
            if (unread > 0) {
                unreadByConversation.put(conv.getId(), unread);
                totalUnread += unread;
            }
        }
        
        result.put("total", totalUnread);
        result.put("byConversation", unreadByConversation);
        
        return result;
    }

    // ================ MÉTHODES UTILITAIRES ================

    private boolean hasRole(User user, String roleName) {
        return user.getRoles().stream()
            .anyMatch(role -> role.getName().toString().equals(roleName));
    }

    private List<String> getUsersByRole(String roleName) {
        return userRepository.findAll().stream()
            .filter(user -> hasRole(user, roleName))
            .map(User::getId)
            .collect(Collectors.toList());
    }

    private List<String> getAllUserIds() {
        return userRepository.findAll().stream()
            .map(User::getId)
            .collect(Collectors.toList());
    }

    private String generateDefaultAvatar(String username) {
        String initials = username.length() >= 2 ? username.substring(0, 2).toUpperCase() : username.toUpperCase();
        String[] colors = {"#e91e63", "#9c27b0", "#673ab7", "#3f51b5", "#2196f3", "#00bcd4", "#009688", "#4caf50"};
        String color = colors[Math.abs(username.hashCode()) % colors.length];
        
        String svg = String.format(
            "<svg width='40' height='40' xmlns='http://www.w3.org/2000/svg'>" +
            "<rect width='40' height='40' fill='%s' rx='6'/>" +
            "<text x='20' y='26' text-anchor='middle' fill='white' font-family='Arial' font-size='14' font-weight='600'>%s</text>" +
            "</svg>", color, initials
        );
        
        return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.getBytes());
    }
}






