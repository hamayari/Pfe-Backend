package com.example.demo.service;

import com.example.demo.model.NotificationLog;
import com.example.demo.repository.NotificationLogRepository;
import com.example.demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service d'analyse et de statistiques des notifications
 * Permet de suivre l'efficacité du système de notification
 */
@Service
public class NotificationAnalyticsService {

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Obtenir les statistiques globales des notifications
     */
    public Map<String, Object> getGlobalStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total des notifications envoyées
        long totalSent = notificationLogRepository.count();
        stats.put("totalSent", totalSent);
        
        // Notifications par statut
        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("sent", notificationLogRepository.countByStatus("SENT"));
        byStatus.put("delivered", notificationLogRepository.countByStatus("DELIVERED"));
        byStatus.put("failed", notificationLogRepository.countByStatus("FAILED"));
        byStatus.put("read", notificationLogRepository.countByStatus("READ"));
        stats.put("byStatus", byStatus);
        
        // Notifications par canal
        Map<String, Long> byChannel = new HashMap<>();
        byChannel.put("email", notificationLogRepository.countByChannel("EMAIL"));
        byChannel.put("sms", notificationLogRepository.countByChannel("SMS"));
        byChannel.put("inApp", notificationLogRepository.countByChannel("IN_APP"));
        stats.put("byChannel", byChannel);
        
        // Taux de succès
        double successRate = totalSent > 0 ? 
            (double) byStatus.get("delivered") / totalSent * 100 : 0;
        stats.put("successRate", Math.round(successRate * 100.0) / 100.0);
        
        // Taux de lecture
        double readRate = totalSent > 0 ? 
            (double) byStatus.get("read") / totalSent * 100 : 0;
        stats.put("readRate", Math.round(readRate * 100.0) / 100.0);
        
        return stats;
    }

    /**
     * Obtenir les statistiques par période
     */
    public Map<String, Object> getStatisticsByPeriod(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        List<NotificationLog> logs = notificationLogRepository.findBySentAtBetween(start, end);
        
        stats.put("total", logs.size());
        
        // Grouper par jour
        Map<LocalDate, Long> byDay = logs.stream()
            .collect(Collectors.groupingBy(
                log -> log.getSentAt().toLocalDate(),
                Collectors.counting()
            ));
        stats.put("byDay", byDay);
        
        // Grouper par type
        Map<String, Long> byType = logs.stream()
            .collect(Collectors.groupingBy(
                NotificationLog::getType,
                Collectors.counting()
            ));
        stats.put("byType", byType);
        
        // Grouper par statut
        Map<String, Long> byStatus = logs.stream()
            .collect(Collectors.groupingBy(
                NotificationLog::getStatus,
                Collectors.counting()
            ));
        stats.put("byStatus", byStatus);
        
        return stats;
    }

    /**
     * Obtenir les statistiques par utilisateur
     */
    public Map<String, Object> getStatisticsByUser(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<NotificationLog> userLogs = notificationLogRepository.findByRecipientId(userId);
        
        stats.put("total", userLogs.size());
        
        // Notifications par type
        Map<String, Long> byType = userLogs.stream()
            .collect(Collectors.groupingBy(
                NotificationLog::getType,
                Collectors.counting()
            ));
        stats.put("byType", byType);
        
        // Notifications par canal préféré
        Map<String, Long> byChannel = userLogs.stream()
            .collect(Collectors.groupingBy(
                NotificationLog::getChannel,
                Collectors.counting()
            ));
        stats.put("byChannel", byChannel);
        
        // Taux de lecture
        long readCount = userLogs.stream()
            .filter(log -> "READ".equals(log.getStatus()))
            .count();
        double readRate = userLogs.size() > 0 ? 
            (double) readCount / userLogs.size() * 100 : 0;
        stats.put("readRate", Math.round(readRate * 100.0) / 100.0);
        
        // Dernière notification
        Optional<NotificationLog> lastNotification = userLogs.stream()
            .max(Comparator.comparing(NotificationLog::getSentAt));
        lastNotification.ifPresent(log -> 
            stats.put("lastNotificationDate", log.getSentAt())
        );
        
        return stats;
    }

    /**
     * Obtenir les statistiques par type de notification
     */
    public Map<String, Object> getStatisticsByType(String type) {
        Map<String, Object> stats = new HashMap<>();
        
        List<NotificationLog> typeLogs = notificationLogRepository.findByType(type);
        
        stats.put("total", typeLogs.size());
        
        // Par statut
        Map<String, Long> byStatus = typeLogs.stream()
            .collect(Collectors.groupingBy(
                NotificationLog::getStatus,
                Collectors.counting()
            ));
        stats.put("byStatus", byStatus);
        
        // Par canal
        Map<String, Long> byChannel = typeLogs.stream()
            .collect(Collectors.groupingBy(
                NotificationLog::getChannel,
                Collectors.counting()
            ));
        stats.put("byChannel", byChannel);
        
        // Taux de succès
        long successCount = typeLogs.stream()
            .filter(log -> "DELIVERED".equals(log.getStatus()) || "READ".equals(log.getStatus()))
            .count();
        double successRate = typeLogs.size() > 0 ? 
            (double) successCount / typeLogs.size() * 100 : 0;
        stats.put("successRate", Math.round(successRate * 100.0) / 100.0);
        
        return stats;
    }

    /**
     * Obtenir le rapport de performance des notifications
     */
    public Map<String, Object> getPerformanceReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        List<NotificationLog> logs = notificationLogRepository.findBySentAtBetween(start, end);
        
        // Métriques générales
        report.put("period", Map.of(
            "start", startDate,
            "end", endDate
        ));
        report.put("totalNotifications", logs.size());
        
        // Performance par canal
        Map<String, Map<String, Object>> channelPerformance = new HashMap<>();
        for (String channel : Arrays.asList("EMAIL", "SMS", "IN_APP")) {
            List<NotificationLog> channelLogs = logs.stream()
                .filter(log -> channel.equals(log.getChannel()))
                .collect(Collectors.toList());
            
            long total = channelLogs.size();
            long success = channelLogs.stream()
                .filter(log -> "DELIVERED".equals(log.getStatus()) || "READ".equals(log.getStatus()))
                .count();
            long failed = channelLogs.stream()
                .filter(log -> "FAILED".equals(log.getStatus()))
                .count();
            
            Map<String, Object> channelStats = new HashMap<>();
            channelStats.put("total", total);
            channelStats.put("success", success);
            channelStats.put("failed", failed);
            channelStats.put("successRate", total > 0 ? (double) success / total * 100 : 0);
            
            channelPerformance.put(channel, channelStats);
        }
        report.put("channelPerformance", channelPerformance);
        
        // Top 5 des types de notifications les plus envoyés
        Map<String, Long> topTypes = logs.stream()
            .collect(Collectors.groupingBy(
                NotificationLog::getType,
                Collectors.counting()
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
        report.put("topNotificationTypes", topTypes);
        
        // Tendance quotidienne
        Map<LocalDate, Long> dailyTrend = logs.stream()
            .collect(Collectors.groupingBy(
                log -> log.getSentAt().toLocalDate(),
                TreeMap::new,
                Collectors.counting()
            ));
        report.put("dailyTrend", dailyTrend);
        
        return report;
    }

    /**
     * Obtenir les notifications échouées récentes
     */
    public List<Map<String, Object>> getRecentFailures(int limit) {
        List<NotificationLog> failures = notificationLogRepository
            .findByStatusOrderBySentAtDesc("FAILED")
            .stream()
            .limit(limit)
            .collect(Collectors.toList());
        
        return failures.stream()
            .map(log -> {
                Map<String, Object> failure = new HashMap<>();
                failure.put("id", log.getId());
                failure.put("type", log.getType());
                failure.put("channel", log.getChannel());
                failure.put("recipientId", log.getRecipientId());
                failure.put("sentAt", log.getSentAt());
                failure.put("errorMessage", log.getErrorMessage());
                return failure;
            })
            .collect(Collectors.toList());
    }

    /**
     * Obtenir le taux d'engagement des utilisateurs
     */
    public Map<String, Object> getUserEngagementMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Utilisateurs actifs (qui ont lu au moins une notification)
        List<String> activeUsers = notificationLogRepository.findByStatus("READ")
            .stream()
            .map(NotificationLog::getRecipientId)
            .distinct()
            .collect(Collectors.toList());
        metrics.put("activeUsers", activeUsers.size());
        
        // Utilisateurs avec notifications non lues
        List<String> usersWithUnread = notificationRepository.findByReadFalse()
            .stream()
            .map(notification -> notification.getUserId())
            .distinct()
            .collect(Collectors.toList());
        metrics.put("usersWithUnread", usersWithUnread.size());
        
        // Taux d'engagement global
        long totalUsers = notificationLogRepository.findAll()
            .stream()
            .map(NotificationLog::getRecipientId)
            .distinct()
            .count();
        double engagementRate = totalUsers > 0 ? 
            (double) activeUsers.size() / totalUsers * 100 : 0;
        metrics.put("engagementRate", Math.round(engagementRate * 100.0) / 100.0);
        
        return metrics;
    }

    /**
     * Obtenir les recommandations d'amélioration
     */
    public List<String> getImprovementRecommendations() {
        List<String> recommendations = new ArrayList<>();
        
        Map<String, Object> globalStats = getGlobalStatistics();
        
        // Vérifier le taux de succès
        double successRate = (double) globalStats.get("successRate");
        if (successRate < 90) {
            recommendations.add("Le taux de succès des notifications est inférieur à 90%. Vérifiez la configuration SMTP et SMS.");
        }
        
        // Vérifier le taux de lecture
        double readRate = (double) globalStats.get("readRate");
        if (readRate < 50) {
            recommendations.add("Le taux de lecture est faible. Considérez d'améliorer le contenu et la pertinence des notifications.");
        }
        
        // Vérifier les échecs récents
        List<Map<String, Object>> recentFailures = getRecentFailures(10);
        if (recentFailures.size() > 5) {
            recommendations.add("Nombre élevé d'échecs récents. Vérifiez les logs pour identifier les problèmes.");
        }
        
        // Vérifier l'équilibre des canaux
        @SuppressWarnings("unchecked")
        Map<String, Long> byChannel = (Map<String, Long>) globalStats.get("byChannel");
        long emailCount = byChannel.getOrDefault("email", 0L);
        long smsCount = byChannel.getOrDefault("sms", 0L);
        
        if (emailCount > smsCount * 10) {
            recommendations.add("Les notifications SMS sont sous-utilisées. Considérez d'utiliser les SMS pour les alertes urgentes.");
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("✅ Le système de notifications fonctionne de manière optimale.");
        }
        
        return recommendations;
    }
}
