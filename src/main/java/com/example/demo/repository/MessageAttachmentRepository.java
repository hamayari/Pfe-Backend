package com.example.demo.repository;

import com.example.demo.model.MessageAttachment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageAttachmentRepository extends MongoRepository<MessageAttachment, String> {

    // Trouver les pièces jointes d'un message
    List<MessageAttachment> findByMessageIdAndDeletedFalse(String messageId);

    // Trouver les pièces jointes d'une conversation
    List<MessageAttachment> findByConversationIdAndDeletedFalseOrderByUploadedAtDesc(String conversationId);

    // Trouver les pièces jointes uploadées par un utilisateur
    List<MessageAttachment> findByUploadedByAndDeletedFalseOrderByUploadedAtDesc(String uploadedBy);

    // Trouver toutes les pièces jointes non supprimées
    List<MessageAttachment> findByDeletedFalse();

    // Trouver les pièces jointes supprimées anciennes (pour nettoyage)
    List<MessageAttachment> findByDeletedTrueAndDeletedAtBefore(LocalDateTime cutoff);

    // Trouver les pièces jointes par type de contenu
    @Query("{'contentType': {$regex: ?0, $options: 'i'}, 'deleted': false}")
    List<MessageAttachment> findByContentTypeContainingIgnoreCaseAndDeletedFalse(String contentType);

    // Statistiques
    @Query(value = "{'deleted': false}", count = true)
    long countByDeletedFalse();

    @Query(value = "{'uploadedBy': ?0, 'deleted': false}", count = true)
    long countByUploadedByAndDeletedFalse(String uploadedBy);

    @Query(value = "{'conversationId': ?0, 'deleted': false}", count = true)
    long countByConversationIdAndDeletedFalse(String conversationId);

    // Recherche par nom de fichier
    @Query("{'originalFileName': {$regex: ?0, $options: 'i'}, 'deleted': false}")
    List<MessageAttachment> findByOriginalFileNameContainingIgnoreCaseAndDeletedFalse(String fileName);

    // Trouver les images
    @Query("{'contentType': {$regex: '^image/', $options: 'i'}, 'deleted': false}")
    List<MessageAttachment> findImagesByDeletedFalse();

    // Trouver les pièces jointes récentes
    List<MessageAttachment> findByDeletedFalseAndUploadedAtAfterOrderByUploadedAtDesc(LocalDateTime since);

    // Trouver les grosses pièces jointes
    @Query("{'fileSize': {$gt: ?0}, 'deleted': false}")
    List<MessageAttachment> findByFileSizeGreaterThanAndDeletedFalse(long minSize);
}





