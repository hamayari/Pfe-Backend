package com.example.demo.service;

import com.example.demo.model.Conversation;
import com.example.demo.model.User;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SlackChannelService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void initializeDefaultChannels() {
        // Canaux par défaut à créer
        createChannelIfNotExists("général", "Discussions générales pour toute l'équipe", true);
        createChannelIfNotExists("commercial", "Canal dédié à l'équipe commerciale", false, "ROLE_COMMERCIAL");
        createChannelIfNotExists("projets", "Gestion et suivi des projets", false, "ROLE_PROJECT_MANAGER", "ROLE_COMMERCIAL");
        createChannelIfNotExists("urgences", "Messages critiques et urgents", true);
        createChannelIfNotExists("décisions", "Validation et prise de décisions", false, "ROLE_DECISION_MAKER", "ROLE_ADMIN");
        createChannelIfNotExists("support", "Support technique et assistance", false, "ROLE_ADMIN", "ROLE_SUPER_ADMIN");
    }

    private void createChannelIfNotExists(String name, String description, boolean isPublic, String... allowedRoles) {
        // Vérifier si le canal existe déjà
        List<Conversation> existingChannels = conversationRepository.findByNameAndType(name, "GROUP");
        if (!existingChannels.isEmpty()) {
            return; // Canal existe déjà
        }

        // Récupérer les utilisateurs autorisés
        List<String> participantIds;
        if (isPublic) {
            // Canal public : tous les utilisateurs
            participantIds = userRepository.findAll().stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
        } else {
            // Canal privé : utilisateurs avec les rôles spécifiés
            participantIds = userRepository.findAll().stream()
                    .filter(user -> user.getRoles().stream()
                            .anyMatch(role -> Arrays.asList(allowedRoles).contains(role.getName().toString())))
                    .map(User::getId)
                    .collect(Collectors.toList());
        }

        // Créer le canal
        Conversation channel = new Conversation();
        channel.setName(name);
        channel.setDescription(description);
        channel.setType("GROUP");
        channel.setParticipantIds(participantIds);
        channel.setCreatedAt(LocalDateTime.now());
        channel.setActive(true);
        channel.setCreatedBy("system"); // Créé par le système
        
        // Métadonnées Slack-like
        channel.setIsPublic(isPublic);
        channel.setUnreadCount(0);

        conversationRepository.save(channel);
        
        System.out.println("📋 Canal créé: #" + name + " (" + participantIds.size() + " membres)");
    }

    public List<Conversation> getChannelsForUser(String userId) {
        // Récupérer tous les canaux où l'utilisateur est participant
        return conversationRepository.findByParticipantIdsContainingAndType(userId, "GROUP");
    }

    public List<Conversation> getDirectMessagesForUser(String userId) {
        // Récupérer toutes les conversations directes de l'utilisateur
        return conversationRepository.findByParticipantIdsContainingAndType(userId, "DIRECT");
    }
}
