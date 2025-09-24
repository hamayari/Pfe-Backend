package com.example.demo.repository;

import com.example.demo.model.CalendarEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CalendarEventRepository extends MongoRepository<CalendarEvent, String> {

    // Trouver les événements par type
    List<CalendarEvent> findByType(String type);

    // Trouver les événements par statut
    List<CalendarEvent> findByStatus(String status);

    // Trouver les événements par utilisateur
    List<CalendarEvent> findByUserId(String userId);

    // Trouver les événements entre deux dates
    List<CalendarEvent> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Trouver les événements par type et statut
    List<CalendarEvent> findByTypeAndStatus(String type, String status);

    // Trouver les événements par utilisateur et type
    List<CalendarEvent> findByUserIdAndType(String userId, String type);

    // Trouver les événements en retard (date < maintenant)
    @Query("{'date': {$lt: ?0}}")
    List<CalendarEvent> findOverdueEvents(LocalDateTime now);

    // Trouver les événements en attente (date >= maintenant)
    @Query("{'date': {$gte: ?0}}")
    List<CalendarEvent> findUpcomingEvents(LocalDateTime now);

    // Trouver les événements pour une date spécifique
    @Query("{'date': {$gte: ?0, $lt: ?1}}")
    List<CalendarEvent> findEventsForDate(LocalDateTime startOfDay, LocalDateTime endOfDay);

    // Trouver les événements pour une semaine
    @Query("{'date': {$gte: ?0, $lt: ?1}}")
    List<CalendarEvent> findEventsForWeek(LocalDateTime startOfWeek, LocalDateTime endOfWeek);

    // Trouver les événements pour un mois
    @Query("{'date': {$gte: ?0, $lt: ?1}}")
    List<CalendarEvent> findEventsForMonth(LocalDateTime startOfMonth, LocalDateTime endOfMonth);

    // Trouver les événements avec filtres complexes
    @Query("{'$and': [" +
           "?0 == null || {'type': {'$in': ?0}}, " +
           "?1 == null || {'status': {'$in': ?1}}, " +
           "?2 == null || {'userId': ?2}, " +
           "?3 == null || {'date': {'$gte': ?3}}, " +
           "?4 == null || {'date': {'$lte': ?4}}" +
           "]}")
    List<CalendarEvent> findEventsWithFilters(
            List<String> types,
            List<String> statuses,
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // Compter les événements par type
    @Query(value = "{}", fields = "{'type': 1}")
    List<CalendarEvent> countByType();

    // Compter les événements par statut
    @Query(value = "{}", fields = "{'status': 1}")
    List<CalendarEvent> countByStatus();

    // Supprimer les événements par utilisateur
    void deleteByUserId(String userId);

    // Supprimer les événements par type
    void deleteByType(String type);

    // Supprimer les événements anciens (plus de X jours)
    @Query("{'date': {$lt: ?0}}")
    void deleteOldEvents(LocalDateTime cutoffDate);
} 