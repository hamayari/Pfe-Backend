package com.example.demo.repository;

import com.example.demo.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    // Trouver les conversations d'un utilisateur (ordre géré au service)
    @Query("{'participantIds': ?0}")
    List<Conversation> findByParticipantIdsContaining(String participantId);

    // Trouver les conversations actives d'un utilisateur
    @Query("{'participantIds': ?0, 'isActive': true}")
    List<Conversation> findActiveConversationsByParticipantId(String participantId);

    // Méthode simplifiée: l'ordre est appliqué côté service si nécessaire

    // Trouver les conversations de groupe
    @Query("{'type': 'GROUP'}")
    List<Conversation> findGroupConversations();

    // Trouver les conversations de groupe d'un utilisateur
    @Query("{'type': 'GROUP', 'participantIds': ?0}")
    List<Conversation> findGroupConversationsByParticipantId(String participantId);

    // Trouver les conversations système
    @Query("{'type': 'SYSTEM'}")
    List<Conversation> findSystemConversations();

    // Trouver les conversations par créateur
    List<Conversation> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    // Trouver les conversations par nom (recherche)
    @Query("{'name': {$regex: ?0, $options: 'i'}}")
    List<Conversation> findByNameContainingIgnoreCase(String name);

    // Trouver les conversations créées après une date
    List<Conversation> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime date);

    // Trouver les conversations avec des messages récents
    List<Conversation> findByLastMessageAtAfterOrderByLastMessageAtDesc(LocalDateTime date);

    // Trouver les conversations inactives
    @Query("{'isActive': false}")
    List<Conversation> findInactiveConversations();

    // Compter les conversations d'un utilisateur
    @Query(value = "{'participantIds': ?0}", count = true)
    long countByParticipantIdsContaining(String participantId);

    // Compter les conversations actives d'un utilisateur
    @Query(value = "{'participantIds': ?0, 'isActive': true}", count = true)
    long countActiveConversationsByParticipantId(String participantId);

    // Compter les conversations de groupe
    @Query(value = "{'type': 'GROUP'}", count = true)
    long countGroupConversations();

    // Supprimer les conversations inactives anciennes
    @Query("{'isActive': false, 'lastMessageAt': {$lt: ?0}}")
    void deleteInactiveConversationsOlderThan(LocalDateTime cutoffDate);

    // Trouver les conversations avec des messages non lus
    @Query("{'participantIds': ?0, 'lastMessageSenderId': {$ne: ?0}}")
    List<Conversation> findConversationsWithUnreadMessages(String participantId);

    // Trouver les conversations par type et participant
    @Query("{'type': ?0, 'participantIds': ?1}")
    List<Conversation> findByTypeAndParticipantIdsContaining(String type, String participantId);

    // Trouver les conversations créées par un utilisateur
    List<Conversation> findByCreatedByAndTypeOrderByCreatedAtDesc(String createdBy, String type);

    // Trouver les conversations avec un nom spécifique
    Conversation findByName(String name);

    // Trouver les conversations avec description
    @Query("{'description': {$regex: ?0, $options: 'i'}}")
    List<Conversation> findByDescriptionContainingIgnoreCase(String description);

    // ============ MÉTHODES SLACK-LIKE ============
    
    // Trouver les conversations par nom et type (pour vérifier l'existence de canaux)
    List<Conversation> findByNameAndType(String name, String type);
    
    // Trouver les conversations par participant et type (canaux vs messages directs)
    @Query("{'participantIds': ?0, 'type': ?1}")
    List<Conversation> findByParticipantIdsContainingAndType(String participantId, String type);
    
    // Trouver une conversation directe entre deux utilisateurs
    @Query("{'type': 'DIRECT', '$and': [{'participantIds': ?0}, {'participantIds': ?1}]}")
    List<Conversation> findDirectConversationBetweenUsers(String userId1, String userId2);
    
    // Trouver toutes les conversations d'un utilisateur (doublon maintenu pour compat)
    @Query("{'participantIds': ?0}")
    List<Conversation> findAllByParticipantIds(String participantId);
} 