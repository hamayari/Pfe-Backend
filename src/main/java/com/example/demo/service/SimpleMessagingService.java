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
public class SimpleMessagingService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    // ================ CANAUX ================

    public List<Conversation> getChannelsForUser(String userId) {
        try {
            // Récupérer tous les canaux où l'utilisateur est participant
            List<Conversation> channels = conversationRepository.findByParticipantIdsContainingAndType(userId, "GROUP");
            
            // Si aucun canal, créer les canaux par défaut
            if (channels.isEmpty()) {
                initializeDefaultChannels(userId);
                channels = conversationRepository.findByParticipantIdsContainingAndType(userId, "GROUP");
            }
            
            return channels;
        } catch (Exception e) {
            System.err.println("Erreur dans getChannelsForUser: " + e.getMessage());
            return createDefaultChannels();
        }
    }

    public Conversation createChannel(String name, String description, boolean isPublic, String createdBy) {
        try {
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
            channel.setParticipantIds(Arrays.asList(createdBy));
            
            return conversationRepository.save(channel);
        } catch (Exception e) {
            System.err.println("Erreur dans createChannel: " + e.getMessage());
            return createDummyChannel(name);
        }
    }

    private void initializeDefaultChannels(String userId) {
        try {
            // Canal général (public, tous les utilisateurs)
            createDefaultChannel("général", "Discussions générales pour toute l'équipe", true);
            
            // Canal commercial 
            createDefaultChannel("commercial", "Canal dédié à l'équipe commerciale", false);
            
            // Canal projets
            createDefaultChannel("projets", "Gestion et suivi des projets", false);
            
            // Canal urgences
            createDefaultChannel("urgences", "Messages critiques et urgents", true);
            
            System.out.println("📋 Canaux par défaut créés pour l'utilisateur: " + userId);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation des canaux: " + e.getMessage());
        }
    }

    private void createDefaultChannel(String name, String description, boolean isPublic) {
        try {
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
            channel.setParticipantIds(getAllUserIds());
            channel.setUnreadCount(0);

            conversationRepository.save(channel);
            System.out.println("📋 Canal créé: #" + name);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du canal " + name + ": " + e.getMessage());
        }
    }

    // ================ MESSAGES DIRECTS ================

    public List<Conversation> getDirectMessagesForUser(String userId) {
        try {
            List<Conversation> dms = conversationRepository.findByParticipantIdsContainingAndType(userId, "DIRECT");
            
            // Ajouter les informations de l'autre utilisateur
            for (Conversation dm : dms) {
                String otherUserId = dm.getParticipantIds().stream()
                    .filter(id -> !id.equals(userId))
                    .findFirst().orElse(null);
                
                if (otherUserId != null) {
                    User otherUser = userRepository.findById(otherUserId).orElse(null);
                    if (otherUser != null) {
                        dm.setName(otherUser.getUsername());
                        dm.setDescription("Message direct avec " + otherUser.getUsername());
                    }
                }
            }
            
            return dms;
        } catch (Exception e) {
            System.err.println("Erreur dans getDirectMessagesForUser: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Conversation createOrGetDirectMessage(String userId1, String userId2) {
        try {
            // Chercher une conversation directe existante entre ces deux utilisateurs
            List<Conversation> allConversations = conversationRepository.findByParticipantIdsContainingAndType(userId1, "DIRECT");
            
            for (Conversation conv : allConversations) {
                if (conv.getParticipantIds().contains(userId2)) {
                    return conv;
                }
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
        } catch (Exception e) {
            System.err.println("Erreur dans createOrGetDirectMessage: " + e.getMessage());
            return createDummyDM(userId1, userId2);
        }
    }

    // ================ MESSAGES ================

    public List<Message> getMessages(String conversationId, String userId, int page, int size) {
        try {
            // Vérifier que l'utilisateur a accès à cette conversation
            Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
            if (conversation == null || !conversation.getParticipantIds().contains(userId)) {
                return new ArrayList<>();
            }

            // Récupérer les messages
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
        } catch (Exception e) {
            System.err.println("Erreur dans getMessages: " + e.getMessage());
            return createDummyMessages(conversationId);
        }
    }

    public Message sendMessage(String conversationId, String content, String messageType, String senderId) {
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
        } catch (Exception e) {
            System.err.println("Erreur dans sendMessage: " + e.getMessage());
            return createDummyMessage(conversationId, content, senderId);
        }
    }

    // ================ UTILISATEURS ================

    public List<Map<String, Object>> getOnlineUsers() {
        try {
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
        } catch (Exception e) {
            System.err.println("Erreur dans getOnlineUsers: " + e.getMessage());
            return createDummyUsers();
        }
    }

    // ================ NOTIFICATIONS ================

    public void markConversationAsRead(String conversationId, String userId) {
        try {
            // Marquer tous les messages non lus de cette conversation comme lus
            List<Message> unreadMessages = messageRepository.findUnreadMessages(conversationId, userId);
            for (Message message : unreadMessages) {
                message.setRead(true);
                messageRepository.save(message);
            }
        } catch (Exception e) {
            System.err.println("Erreur dans markConversationAsRead: " + e.getMessage());
        }
    }

    public Map<String, Object> getUnreadCount(String userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
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
        } catch (Exception e) {
            System.err.println("Erreur dans getUnreadCount: " + e.getMessage());
            result.put("total", 0);
            result.put("byConversation", new HashMap<>());
        }
        
        return result;
    }

    // ================ MÉTHODES UTILITAIRES ================

    private List<String> getAllUserIds() {
        try {
            return userRepository.findAll().stream()
                .map(User::getId)
                .collect(Collectors.toList());
        } catch (Exception e) {
            return Arrays.asList("admin", "commercial", "projectmanager");
        }
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

    // ================ MÉTHODES DE FALLBACK ================

    private List<Conversation> createDefaultChannels() {
        List<Conversation> channels = new ArrayList<>();
        
        Conversation general = new Conversation();
        general.setId("general");
        general.setName("général");
        general.setType("GROUP");
        general.setDescription("Discussions générales");
        general.setIsPublic(true);
        general.setUnreadCount(0);
        channels.add(general);
        
        return channels;
    }

    private Conversation createDummyChannel(String name) {
        Conversation channel = new Conversation();
        channel.setId(UUID.randomUUID().toString());
        channel.setName(name);
        channel.setType("GROUP");
        channel.setDescription("Canal " + name);
        channel.setIsPublic(true);
        channel.setUnreadCount(0);
        return channel;
    }

    private Conversation createDummyDM(String userId1, String userId2) {
        Conversation dm = new Conversation();
        dm.setId(UUID.randomUUID().toString());
        dm.setName("Direct Message");
        dm.setType("DIRECT");
        dm.setParticipantIds(Arrays.asList(userId1, userId2));
        dm.setUnreadCount(0);
        return dm;
    }

    private List<Message> createDummyMessages(String conversationId) {
        List<Message> messages = new ArrayList<>();
        
        Message msg = new Message();
        msg.setId(UUID.randomUUID().toString());
        msg.setConversationId(conversationId);
        msg.setSenderId("system");
        msg.setSenderName("Système");
        msg.setContent("Bienvenue dans cette conversation !");
        msg.setSentAt(LocalDateTime.now());
        msg.setType("text");
        messages.add(msg);
        
        return messages;
    }

    private Message createDummyMessage(String conversationId, String content, String senderId) {
        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setSenderName("Utilisateur");
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());
        message.setType("text");
        return message;
    }

    private List<Map<String, Object>> createDummyUsers() {
        List<Map<String, Object>> users = new ArrayList<>();
        
        Map<String, Object> commercial = new HashMap<>();
        commercial.put("id", "commercial");
        commercial.put("username", "Commercial");
        commercial.put("avatar", generateDefaultAvatar("Commercial"));
        commercial.put("status", "online");
        commercial.put("statusMessage", "Disponible");
        commercial.put("roles", Arrays.asList("ROLE_COMMERCIAL"));
        users.add(commercial);
        
        Map<String, Object> projectManager = new HashMap<>();
        projectManager.put("id", "projectmanager");
        projectManager.put("username", "Project Manager");
        projectManager.put("avatar", generateDefaultAvatar("Project Manager"));
        projectManager.put("status", "online");
        projectManager.put("statusMessage", "En réunion");
        projectManager.put("roles", Arrays.asList("ROLE_PROJECT_MANAGER"));
        users.add(projectManager);
        
        Map<String, Object> admin = new HashMap<>();
        admin.put("id", "admin");
        admin.put("username", "Administrateur");
        admin.put("avatar", generateDefaultAvatar("Administrateur"));
        admin.put("status", "online");
        admin.put("statusMessage", "Disponible");
        admin.put("roles", Arrays.asList("ROLE_ADMIN"));
        users.add(admin);
        
        return users;
    }
}






