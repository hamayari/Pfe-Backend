package com.example.demo.repository;

import com.example.demo.model.SmsNotification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SmsNotificationRepository extends MongoRepository<SmsNotification, String> {

    // Trouver les SMS par utilisateur
    List<SmsNotification> findByUserId(String userId);

    // Trouver les SMS par utilisateur, triés par date d'envoi décroissante
    List<SmsNotification> findByUserIdOrderBySentAtDesc(String userId);

    // Trouver les SMS par statut
    List<SmsNotification> findByStatus(String status);

    // Trouver les SMS par type
    List<SmsNotification> findByType(String type);

    // Trouver les SMS par utilisateur et type
    List<SmsNotification> findByUserIdAndType(String userId, String type);

    // Trouver les SMS par utilisateur et statut
    List<SmsNotification> findByUserIdAndStatus(String userId, String status);

    // Trouver les SMS envoyés entre deux dates
    List<SmsNotification> findBySentAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Trouver les SMS envoyés par un utilisateur entre deux dates
    List<SmsNotification> findByUserIdAndSentAtBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);

    // Trouver les SMS échoués
    @Query("{'status': 'FAILED'}")
    List<SmsNotification> findFailedSms();

    // Trouver les SMS échoués par utilisateur
    @Query("{'userId': ?0, 'status': 'FAILED'}")
    List<SmsNotification> findFailedSmsByUser(String userId);

    // Compter les SMS par statut pour un utilisateur
    @Query(value = "{'userId': ?0}", count = true)
    long countByUserId(String userId);

    // Compter les SMS par statut
    @Query(value = "{'status': ?0}", count = true)
    long countByStatus(String status);

    // Compter les SMS par type
    @Query(value = "{'type': ?0}", count = true)
    long countByType(String type);

    // Supprimer les SMS anciens (plus de X jours)
    @Query("{'sentAt': {$lt: ?0}}")
    void deleteBySentAtBefore(LocalDateTime cutoffDate);

    // Trouver les SMS par numéro de téléphone
    List<SmsNotification> findByTo(String phoneNumber);

    // Trouver les SMS par Twilio SID
    SmsNotification findByTwilioSid(String twilioSid);

    // Trouver les SMS non livrés
    @Query("{'status': {$in: ['SENT', 'UNDELIVERED']}}")
    List<SmsNotification> findPendingDeliverySms();
} 