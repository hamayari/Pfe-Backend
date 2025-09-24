package com.example.demo.service;

import com.example.demo.dto.ConversationDTO;
import com.example.demo.model.Conversation;
import com.example.demo.model.Message;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    // Créer une nouvelle conversation
    public ConversationDTO createConversation(ConversationDTO conversationDTO) {
        Conversation conversation = new Conversation();
        conversation.setName(conversationDTO.getName());
        conversation.setDescription(conversationDTO.getDescription());
        conversation.setType(conversationDTO.getType());
        conversation.setParticipantIds(conversationDTO.getParticipantIds());
        conversation.setParticipantNames(conversationDTO.getParticipantNames());
        conversation.setCreatedBy(conversationDTO.getCreatedBy());
        conversation.setCreatedByName(conversationDTO.getCreatedByName());
        conversation.setIsPublic(conversationDTO.getIsPublic());
        conversation.setActive(true);

        Conversation savedConversation = conversationRepository.save(conversation);
        return convertToDTO(savedConversation);
    }

    public ConversationDTO updateConversation(String conversationId, ConversationDTO update, String currentUsername) {
        Optional<Conversation> optional = conversationRepository.findById(conversationId);
        if (optional.isEmpty()) return null;
        Conversation conversation = optional.get();
        // Optionnel: vérifier que currentUsername est owner ou membre
        if (update.getName() != null) conversation.setName(update.getName());
        if (update.getDescription() != null) conversation.setDescription(update.getDescription());
        conversation.setIsPublic(update.getIsPublic());
        conversation.setUpdatedAt(LocalDateTime.now());
        Conversation saved = conversationRepository.save(conversation);
        return convertToDTO(saved);
    }

    // Récupérer les conversations d'un utilisateur
    public List<ConversationDTO> getUserConversations(String userId) {
        if (userId == null || userId.isBlank()) return List.of();
        List<Conversation> conversations = conversationRepository.findByParticipantIdsContaining(userId);
        if (conversations == null) return List.of();
        // Si le champ lastMessageAt existe, on trie en mémoire pour éviter les échecs de mapping
        conversations = conversations.stream()
            .sorted((a,b) -> {
                if (a.getLastMessageAt() == null && b.getLastMessageAt() == null) return 0;
                if (a.getLastMessageAt() == null) return 1;
                if (b.getLastMessageAt() == null) return -1;
                return b.getLastMessageAt().compareTo(a.getLastMessageAt());
            })
            .collect(Collectors.toList());
        return conversations.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Récupérer les conversations actives d'un utilisateur
    public List<ConversationDTO> getActiveConversations(String userId) {
        List<Conversation> conversations = conversationRepository.findActiveConversationsByParticipantId(userId);
        return conversations.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Récupérer une conversation directe entre deux utilisateurs
    public ConversationDTO getDirectConversation(String userId1, String userId2) {
        List<Conversation> conversations = conversationRepository.findDirectConversationBetweenUsers(userId1, userId2);
        if (!conversations.isEmpty()) {
            return convertToDTO(conversations.get(0));
        }
        return null;
    }

    // Créer ou mettre à jour une conversation directe
    public void updateOrCreateDirectConversation(String userId1, String userId2, Message message) {
        List<Conversation> existingConversations = conversationRepository.findDirectConversationBetweenUsers(userId1, userId2);
        
        Conversation conversation;
        if (existingConversations.isEmpty()) {
            // Créer une nouvelle conversation directe
            conversation = new Conversation();
            conversation.setType("DIRECT");
            conversation.setParticipantIds(List.of(userId1, userId2));
            conversation.setCreatedBy(userId1);
            conversation.setActive(true);
            
            // Générer un nom pour la conversation
            String userName1 = getUserName(userId1);
            String userName2 = getUserName(userId2);
            conversation.setName(userName1 + " - " + userName2);
            conversation.setParticipantNames(List.of(userName1, userName2));
        } else {
            conversation = existingConversations.get(0);
        }

        // Mettre à jour les informations du dernier message
        conversation.setLastMessageAt(message.getSentAt());
        conversation.setLastMessageId(message.getId());
        conversation.setLastMessageContent(message.getContent());
        conversation.setLastMessageSenderId(message.getSenderId());
        conversation.setLastMessageSenderName(message.getSenderName());
        conversation.setUpdatedAt(LocalDateTime.now());

        conversationRepository.save(conversation);
    }

    // Récupérer les conversations de groupe
    public List<ConversationDTO> getGroupConversations() {
        List<Conversation> conversations = conversationRepository.findGroupConversations();
        return conversations.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Récupérer une conversation par ID
    public Conversation getConversationById(String conversationId) {
        return conversationRepository.findById(conversationId).orElse(null);
    }

    // Compter le nombre total de conversations
    public long getTotalConversations() {
        return conversationRepository.count();
    }

    // Récupérer les conversations de groupe d'un utilisateur
    public List<ConversationDTO> getUserGroupConversations(String userId) {
        List<Conversation> conversations = conversationRepository.findGroupConversationsByParticipantId(userId);
        return conversations.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Récupérer les conversations système
    public List<ConversationDTO> getSystemConversations() {
        List<Conversation> conversations = conversationRepository.findSystemConversations();
        return conversations.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Rechercher des conversations par nom
    public List<ConversationDTO> searchConversations(String name) {
        List<Conversation> conversations = conversationRepository.findByNameContainingIgnoreCase(name);
        return conversations.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Ajouter un participant à une conversation
    public ConversationDTO addParticipant(String conversationId, String userId) {
        Optional<Conversation> optionalConversation = conversationRepository.findById(conversationId);
        if (optionalConversation.isPresent()) {
            Conversation conversation = optionalConversation.get();
            if (!conversation.getParticipantIds().contains(userId)) {
                conversation.getParticipantIds().add(userId);
                conversation.getParticipantNames().add(getUserName(userId));
                conversation.setUpdatedAt(LocalDateTime.now());
                Conversation savedConversation = conversationRepository.save(conversation);
                return convertToDTO(savedConversation);
            }
        }
        return null;
    }

    // Retirer un participant d'une conversation
    public ConversationDTO removeParticipant(String conversationId, String userId) {
        Optional<Conversation> optionalConversation = conversationRepository.findById(conversationId);
        if (optionalConversation.isPresent()) {
            Conversation conversation = optionalConversation.get();
            if (conversation.getParticipantIds().contains(userId)) {
                int index = conversation.getParticipantIds().indexOf(userId);
                conversation.getParticipantIds().remove(userId);
                if (index < conversation.getParticipantNames().size()) {
                    conversation.getParticipantNames().remove(index);
                }
                conversation.setUpdatedAt(LocalDateTime.now());
                Conversation savedConversation = conversationRepository.save(conversation);
                return convertToDTO(savedConversation);
            }
        }
        return null;
    }

    // Archiver une conversation
    public ConversationDTO archiveConversation(String conversationId) {
        Optional<Conversation> optionalConversation = conversationRepository.findById(conversationId);
        if (optionalConversation.isPresent()) {
            Conversation conversation = optionalConversation.get();
            conversation.setActive(false);
            conversation.setUpdatedAt(LocalDateTime.now());
            Conversation savedConversation = conversationRepository.save(conversation);
            return convertToDTO(savedConversation);
        }
        return null;
    }

    // Supprimer une conversation
    public void deleteConversation(String conversationId) {
        conversationRepository.deleteById(conversationId);
    }

    // Compter les conversations d'un utilisateur
    public long countUserConversations(String userId) {
        return conversationRepository.countByParticipantIdsContaining(userId);
    }

    // Compter les conversations actives d'un utilisateur
    public long countActiveUserConversations(String userId) {
        return conversationRepository.countActiveConversationsByParticipantId(userId);
    }

    // Nettoyer les conversations inactives anciennes
    public void cleanupInactiveConversations(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        conversationRepository.deleteInactiveConversationsOlderThan(cutoffDate);
    }

    // Récupérer le nom d'un utilisateur
    private String getUserName(String userId) {
        return userRepository.findById(userId)
                .map(user -> user.getUsername())
                .orElse("Utilisateur inconnu");
    }

    // Convertir Conversation en ConversationDTO
    private ConversationDTO convertToDTO(Conversation conversation) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setName(conversation.getName());
        dto.setDescription(conversation.getDescription());
        dto.setType(conversation.getType());
        dto.setParticipantIds(conversation.getParticipantIds());
        dto.setParticipantNames(conversation.getParticipantNames());
        dto.setCreatedBy(conversation.getCreatedBy());
        dto.setCreatedByName(conversation.getCreatedByName());
        dto.setCreatedAt(conversation.getCreatedAt());
        dto.setUpdatedAt(conversation.getUpdatedAt());
        dto.setLastMessageAt(conversation.getLastMessageAt());
        dto.setLastMessageId(conversation.getLastMessageId());
        dto.setLastMessageContent(conversation.getLastMessageContent());
        dto.setLastMessageSenderId(conversation.getLastMessageSenderId());
        dto.setLastMessageSenderName(conversation.getLastMessageSenderName());
        dto.setActive(conversation.isActive());
        dto.setAvatar(conversation.getAvatar());
        dto.setIsPublic(conversation.getIsPublic());
        return dto;
    }

    // Convertir ConversationDTO en Conversation
    private Conversation convertToEntity(ConversationDTO dto) {
        Conversation conversation = new Conversation();
        conversation.setId(dto.getId());
        conversation.setName(dto.getName());
        conversation.setDescription(dto.getDescription());
        conversation.setType(dto.getType());
        conversation.setParticipantIds(dto.getParticipantIds());
        conversation.setParticipantNames(dto.getParticipantNames());
        conversation.setCreatedBy(dto.getCreatedBy());
        conversation.setCreatedByName(dto.getCreatedByName());
        conversation.setCreatedAt(dto.getCreatedAt());
        conversation.setUpdatedAt(dto.getUpdatedAt());
        conversation.setLastMessageAt(dto.getLastMessageAt());
        conversation.setLastMessageId(dto.getLastMessageId());
        conversation.setLastMessageContent(dto.getLastMessageContent());
        conversation.setLastMessageSenderId(dto.getLastMessageSenderId());
        conversation.setLastMessageSenderName(dto.getLastMessageSenderName());
        conversation.setActive(dto.isActive());
        conversation.setAvatar(dto.getAvatar());
        conversation.setIsPublic(dto.getIsPublic());
        return conversation;
    }
} 