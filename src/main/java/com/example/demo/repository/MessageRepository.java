package com.example.demo.repository;

import com.example.demo.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    // Trouver les messages envoyés par un utilisateur
    List<Message> findBySenderIdOrderBySentAtDesc(String senderId);

    // Trouver les messages reçus par un utilisateur
    @Query("{'recipientIds': ?0}")
    List<Message> findByRecipientIdsContainingOrderBySentAtDesc(String recipientId);

    // Trouver les messages non lus pour un utilisateur
    @Query("{'recipientIds': ?0, 'status': {$ne: 'READ'}}")
    List<Message> findUnreadMessagesByRecipientId(String recipientId);

    // Trouver les messages d'une conversation
    @Query("{'$or': [{'senderId': ?0, 'recipientIds': ?1}, {'senderId': ?1, 'recipientIds': ?0}]}")
    List<Message> findConversationMessages(String userId1, String userId2);

    // Trouver les messages de groupe
    @Query("{'messageType': 'GROUP', 'recipientIds': ?0}")
    List<Message> findGroupMessagesByRecipientId(String recipientId);

    // Trouver les messages système
    @Query("{'messageType': 'SYSTEM', 'recipientIds': ?0}")
    List<Message> findSystemMessagesByRecipientId(String recipientId);

    // Trouver les messages par type
    List<Message> findByMessageTypeOrderBySentAtDesc(String messageType);

    // Trouver les messages par priorité
    List<Message> findByPriorityOrderBySentAtDesc(String priority);

    // Trouver les messages par statut
    List<Message> findByStatusOrderBySentAtDesc(String status);

    // Trouver les messages entre deux dates
    List<Message> findBySentAtBetweenOrderBySentAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    // Trouver les messages liés à une entité
    @Query("{'relatedEntityId': ?0, 'relatedEntityType': ?1}")
    List<Message> findByRelatedEntity(String entityId, String entityType);

    // Compter les messages non lus pour un utilisateur
    @Query(value = "{'recipientIds': ?0, 'status': {$ne: 'READ'}}", count = true)
    long countUnreadMessagesByRecipientId(String recipientId);

    // Compter les messages envoyés par un utilisateur
    long countBySenderId(String senderId);

    // Compter les messages reçus par un utilisateur
    @Query(value = "{'recipientIds': ?0}", count = true)
    long countByRecipientIdsContaining(String recipientId);

    // Supprimer les anciens messages (plus de X jours)
    @Query("{'sentAt': {$lt: ?0}}")
    void deleteBySentAtBefore(LocalDateTime cutoffDate);

    // Trouver les messages par expéditeur et destinataire
    @Query("{'senderId': ?0, 'recipientIds': ?1}")
    List<Message> findBySenderIdAndRecipientIdsContaining(String senderId, String recipientId);

    // Trouver les derniers messages pour chaque conversation
    @Query(value = "{}", sort = "{'sentAt': -1}")
    List<Message> findLatestMessages();

    // Trouver les messages avec pièces jointes
    @Query("{'attachments': {$exists: true, $ne: []}}")
    List<Message> findMessagesWithAttachments();

    // Trouver les messages urgents
    @Query("{'priority': 'URGENT'}")
    List<Message> findUrgentMessages();

    // ============ MÉTHODES POUR REAL-TIME MESSAGING ============
    
    // Trouver les messages d'une conversation par ID
    @Query("{'conversationId': ?0}")
    List<Message> findByConversationIdOrderBySentAtDesc(String conversationId);
    
    // Compter les messages non lus d'une conversation pour un utilisateur
    @Query(value = "{'conversationId': ?0, 'recipientIds': ?1, 'read': false}", count = true)
    int countUnreadMessages(String conversationId, String userId);
    
    // Trouver les messages non lus d'une conversation pour un utilisateur
    @Query("{'conversationId': ?0, 'recipientIds': ?1, 'read': false}")
    List<Message> findUnreadMessages(String conversationId, String userId);
    
    // ============ NOUVELLES MÉTHODES POUR FONCTIONNALITÉS AVANCÉES ============
    
    // Trouver les réponses à un message (thread)
    List<Message> findByParentMessageIdOrderBySentAtAsc(String parentMessageId);
    
    // Trouver les messages supprimés anciens
    @Query("{'deleted': true, 'deletedAt': {$lt: ?0}}")
    List<Message> findByDeletedTrueAndDeletedAtBefore(LocalDateTime cutoff);
    
    // Trouver les messages par mentions
    @Query("{'mentions': ?0}")
    List<Message> findByMentionsContaining(String userId);
    
    // Recherche textuelle dans le contenu
    @Query("{'content': {$regex: ?0, $options: 'i'}}")
    List<Message> findByContentContainingIgnoreCase(String searchText);
    
    // Trouver les messages avec réactions
    @Query("{'reactions': {$exists: true, $ne: []}}")
    List<Message> findMessagesWithReactions();
    
    // Trouver les messages modifiés
    @Query("{'edited': true}")
    List<Message> findEditedMessages();
    
    // Compter les messages par utilisateur
    @Query(value = "{'senderId': ?0}", count = true)
    long countBySenderIdCustom(String senderId);
    
    // Trouver les messages récents (dernières 24h)
    @Query("{'sentAt': {$gte: ?0}}")
    List<Message> findRecentMessages(LocalDateTime since);
    
    // Trouver les messages par priorité et non lus
    @Query("{'priority': ?0, 'read': false}")
    List<Message> findByPriorityAndReadFalse(String priority);
    
    // ============ MÉTHODES MANQUANTES POUR MESSAGINGSERVICE ============
    
    // Trouver les messages d'une conversation par ID (ordre ascendant)
    @Query("{'conversationId': ?0}")
    List<Message> findByConversationIdOrderBySentAtAsc(String conversationId);
    
    // Trouver les messages non lus d'une conversation
    @Query("{'conversationId': ?0, 'read': false}")
    List<Message> findByConversationIdAndReadFalse(String conversationId);
    
    // Compter les messages non lus
    @Query(value = "{'read': false}", count = true)
    long countByReadFalse();
} 