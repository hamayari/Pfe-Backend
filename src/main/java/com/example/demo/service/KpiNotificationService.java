package com.example.demo.service;

import com.example.demo.model.KpiAlert;
import com.example.demo.model.User;
import com.example.demo.repository.KpiAlertRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.enums.ERole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de notification des alertes KPI
 */
@Service
public class KpiNotificationService {
    
    @Autowired
    private KpiAlertRepository alertRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired(required = false)
    private EmailService emailService;
    
    @Autowired(required = false)
    private SmsService smsService;
    
    @Autowired(required = false)
    private RealTimeNotificationService realTimeNotificationService;
    
    /**
     * Envoie les notifications pour une liste d'alertes
     */
    public void sendAlertNotifications(List<KpiAlert> alerts) {
        for (KpiAlert alert : alerts) {
            if (!alert.isNotificationSent()) {
                sendNotification(alert);
            }
        }
    }
    
    /**
     * Envoie une notification pour une alerte
     */
    private void sendNotification(KpiAlert alert) {
        System.out.println("========================================");
        System.out.println("📨 [KPI NOTIFICATION] Envoi notification pour alerte KPI");
        System.out.println("📊 KPI: " + alert.getKpiName());
        System.out.println("📈 Valeur: " + alert.getCurrentValue());
        System.out.println("🔴 Statut: " + alert.getStatus());
        System.out.println("🚨 Sévérité: " + alert.getSeverity());
        
        // 1. Déterminer les destinataires
        List<User> recipients = getRecipients(alert);
        
        if (recipients.isEmpty()) {
            System.out.println("⚠️ [NOTIFICATION] Aucun destinataire trouvé");
            System.out.println("========================================");
            return;
        }
        
        // 2. Préparer le message
        String subject = generateSubject(alert);
        String message = generateMessage(alert);
        
        List<String> channels = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        
        // 3. Envoyer notification interne (RealTimeNotificationService)
        try {
            sendInternalNotification(alert, recipients);
            channels.add("INTERNAL");
            successCount++;
            System.out.println("✅ [NOTIFICATION] Notification interne envoyée");
        } catch (Exception e) {
            System.err.println("❌ [NOTIFICATION] Erreur notification interne: " + e.getMessage());
            errorCount++;
        }
        
        // 4. Envoyer via WebSocket (Dashboard en temps réel)
        try {
            sendWebSocketNotification(alert, recipients);
            channels.add("WEBSOCKET");
            successCount++;
            System.out.println("✅ [NOTIFICATION] WebSocket envoyé");
        } catch (Exception e) {
            System.err.println("❌ [NOTIFICATION] Erreur WebSocket: " + e.getMessage());
            errorCount++;
        }
        
        // 5. Envoyer par email
        if (emailService != null && shouldSendEmail(alert)) {
            try {
                sendEmailNotification(alert, recipients, subject, message);
                channels.add("EMAIL");
                successCount++;
                System.out.println("✅ [NOTIFICATION] Emails envoyés");
            } catch (Exception e) {
                System.err.println("❌ [NOTIFICATION] Erreur email: " + e.getMessage());
                errorCount++;
            }
        }
        
        // 6. Envoyer par SMS si critique
        if (smsService != null && "HIGH".equals(alert.getSeverity())) {
            try {
                sendSmsNotification(alert, recipients, message);
                channels.add("SMS");
                successCount++;
                System.out.println("✅ [NOTIFICATION] SMS envoyés");
            } catch (Exception e) {
                System.err.println("❌ [NOTIFICATION] Erreur SMS: " + e.getMessage());
                errorCount++;
            }
        }
        
        // 7. Marquer comme envoyée
        alert.setNotificationSent(true);
        alert.setNotificationSentAt(LocalDateTime.now());
        alert.setNotificationChannels(channels);
        alert.setRecipients(recipients.stream().map(User::getId).collect(Collectors.toList()));
        alertRepository.save(alert);
        
        System.out.println("========================================");
        System.out.println("✅ [NOTIFICATION] Résumé:");
        System.out.println("   - Canaux utilisés: " + String.join(", ", channels));
        System.out.println("   - Succès: " + successCount);
        System.out.println("   - Erreurs: " + errorCount);
        System.out.println("   - Destinataires: " + recipients.size());
        System.out.println("========================================");
    }
    
    /**
     * Détermine les destinataires selon l'alerte
     */
    private List<User> getRecipients(KpiAlert alert) {
        List<User> recipients = new ArrayList<>();
        
        // Toujours notifier les DECIDEURS
        List<User> decideurs = userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER);
        recipients.addAll(decideurs);
        System.out.println("📧 [NOTIFICATION] Décideurs ajoutés: " + decideurs.size());
        
        // Toujours notifier les PROJECT_MANAGER pour toutes les alertes
        List<User> projectManagers = userRepository.findByRoles_Name(ERole.ROLE_PROJECT_MANAGER);
        recipients.addAll(projectManagers);
        System.out.println("📧 [NOTIFICATION] Chefs de projet ajoutés: " + projectManagers.size());
        
        // Notifier les ADMIN pour les alertes critiques
        if ("HIGH".equals(alert.getSeverity())) {
            List<User> admins = userRepository.findByRoles_Name(ERole.ROLE_ADMIN);
            recipients.addAll(admins);
            System.out.println("📧 [NOTIFICATION] Admins ajoutés (alerte HIGH): " + admins.size());
        }
        
        System.out.println("📧 [NOTIFICATION] Total destinataires: " + recipients.stream().distinct().count());
        return recipients.stream().distinct().collect(Collectors.toList());
    }
    
    /**
     * Génère le sujet de la notification
     */
    private String generateSubject(KpiAlert alert) {
        String emoji = "HIGH".equals(alert.getSeverity()) ? "🚨" : "⚠️";
        String dimension = alert.getDimensionValue() != null ? " — " + alert.getDimensionValue() : "";
        
        return String.format("%s Alerte KPI%s (%s)", emoji, dimension, alert.getKpiName());
    }
    
    /**
     * Génère le message de notification
     */
    private String generateMessage(KpiAlert alert) {
        StringBuilder msg = new StringBuilder();
        
        msg.append("🔔 **Alerte KPI Détectée**\n\n");
        msg.append("**KPI:** ").append(alert.getKpiName()).append("\n");
        msg.append("**Valeur actuelle:** ").append(alert.getCurrentValue()).append("\n");
        msg.append("**Statut:** ").append(getStatusEmoji(alert.getStatus())).append(" ").append(alert.getStatus()).append("\n");
        msg.append("**Sévérité:** ").append(getSeverityEmoji(alert.getSeverity())).append(" ").append(alert.getSeverity()).append("\n\n");
        
        if (alert.getDimension() != null) {
            msg.append("**Dimension:** ").append(alert.getDimension());
            if (alert.getDimensionValue() != null) {
                msg.append(" (").append(alert.getDimensionValue()).append(")");
            }
            msg.append("\n\n");
        }
        
        msg.append("**Message:**\n").append(alert.getMessage()).append("\n\n");
        
        if (alert.getRecommendation() != null) {
            msg.append("**Recommandation:**\n").append(alert.getRecommendation()).append("\n\n");
        }
        
        msg.append("**Détecté le:** ").append(alert.getDetectedAt()).append("\n");
        
        return msg.toString();
    }
    
    /**
     * Envoie via WebSocket pour le dashboard
     */
    private void sendWebSocketNotification(KpiAlert alert, List<User> recipients) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", alert.getId());
        notification.put("type", "KPI_ALERT");
        notification.put("kpiName", alert.getKpiName());
        notification.put("status", alert.getStatus());
        notification.put("severity", alert.getSeverity());
        notification.put("message", alert.getMessage());
        notification.put("recommendation", alert.getRecommendation());
        notification.put("dimension", alert.getDimension());
        notification.put("dimensionValue", alert.getDimensionValue());
        notification.put("timestamp", alert.getDetectedAt());
        
        // Envoyer à tous les destinataires
        for (User user : recipients) {
            messagingTemplate.convertAndSendToUser(
                user.getUsername(),
                "/queue/kpi-alerts",
                notification
            );
        }
        
        // Envoyer aussi au topic général
        messagingTemplate.convertAndSend("/topic/kpi-alerts", notification);
    }
    
    /**
     * Envoie par email
     */
    private void sendEmailNotification(KpiAlert alert, List<User> recipients, String subject, String message) {
        for (User user : recipients) {
            if (user.getEmail() != null && emailService != null) {
                try {
                    emailService.sendEmail(user.getEmail(), subject, message);
                } catch (Exception e) {
                    System.err.println("❌ Erreur envoi email à " + user.getEmail() + ": " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Envoie notification interne via RealTimeNotificationService
     */
    private void sendInternalNotification(KpiAlert alert, List<User> recipients) {
        if (realTimeNotificationService == null) {
            System.out.println("⚠️ [NOTIFICATION] RealTimeNotificationService non disponible");
            return;
        }
        
        for (User user : recipients) {
            try {
                com.example.demo.dto.NotificationDTO notification = new com.example.demo.dto.NotificationDTO();
                notification.setType(alert.getSeverity().equals("HIGH") ? "error" : "warning");
                notification.setTitle(generateSubject(alert));
                notification.setMessage(alert.getMessage());
                notification.setPriority(alert.getSeverity().equals("HIGH") ? "high" : "medium");
                notification.setCategory("kpi_alert");
                notification.setUserId(user.getId());
                notification.setSource("KpiNotificationService");
                
                realTimeNotificationService.createNotification(notification);
                System.out.println("✅ [INTERNAL] Notification créée pour " + user.getUsername());
            } catch (Exception e) {
                System.err.println("❌ [INTERNAL] Erreur pour " + user.getUsername() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Envoie par SMS
     */
    private void sendSmsNotification(KpiAlert alert, List<User> recipients, String message) {
        if (smsService == null) {
            System.out.println("⚠️ [SMS] SmsService non disponible");
            return;
        }
        
        for (User user : recipients) {
            try {
                String phoneNumber = user.getPhoneNumber();
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    Map<String, String> variables = new HashMap<>();
                    variables.put("kpiName", alert.getKpiName());
                    variables.put("currentValue", String.valueOf(alert.getCurrentValue()));
                    variables.put("message", alert.getMessage().substring(0, Math.min(100, alert.getMessage().length())));
                    
                    smsService.sendSmsWithTemplate(phoneNumber, "kpi_alert", variables);
                    System.out.println("✅ [SMS] SMS envoyé à " + phoneNumber + " pour " + user.getUsername());
                } else {
                    System.out.println("⚠️ [SMS] Pas de numéro pour " + user.getUsername());
                }
            } catch (Exception e) {
                System.err.println("❌ [SMS] Erreur pour " + user.getUsername() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Détermine si on doit envoyer un email
     */
    private boolean shouldSendEmail(KpiAlert alert) {
        // Envoyer email pour MEDIUM et HIGH
        return "MEDIUM".equals(alert.getSeverity()) || "HIGH".equals(alert.getSeverity());
    }
    
    /**
     * Envoie un rapport hebdomadaire
     */
    public void sendWeeklyReport(List<KpiAlert> alerts) {
        System.out.println("📅 [NOTIFICATION] Envoi rapport hebdomadaire");
        
        List<User> decideurs = userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER);
        
        String subject = "📊 Rapport Hebdomadaire KPI";
        String message = generateWeeklyReport(alerts);
        
        for (User user : decideurs) {
            if (user.getEmail() != null && emailService != null) {
                emailService.sendEmail(user.getEmail(), subject, message);
            }
        }
    }
    
    /**
     * Envoie un rapport mensuel
     */
    public void sendMonthlyReport(List<KpiAlert> alerts) {
        System.out.println("📆 [NOTIFICATION] Envoi rapport mensuel");
        
        List<User> decideurs = userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER);
        
        String subject = "📈 Rapport Mensuel KPI";
        String message = generateMonthlyReport(alerts);
        
        for (User user : decideurs) {
            if (user.getEmail() != null && emailService != null) {
                emailService.sendEmail(user.getEmail(), subject, message);
            }
        }
    }
    
    /**
     * Envoie des alertes urgentes
     */
    public void sendUrgentAlerts(List<KpiAlert> alerts) {
        System.out.println("🚨 [NOTIFICATION] Envoi alertes urgentes");
        
        for (KpiAlert alert : alerts) {
            if (!alert.isNotificationSent()) {
                sendNotification(alert);
            }
        }
    }
    
    private String generateWeeklyReport(List<KpiAlert> alerts) {
        StringBuilder report = new StringBuilder();
        report.append("📊 RAPPORT HEBDOMADAIRE KPI\n\n");
        report.append("Période : Semaine du ").append(java.time.LocalDate.now()).append("\n\n");
        report.append("📈 Résumé :\n");
        report.append("- Total alertes : ").append(alerts.size()).append("\n");
        report.append("- Alertes critiques : ").append(alerts.stream().filter(a -> "HIGH".equals(a.getSeverity())).count()).append("\n");
        report.append("- Alertes moyennes : ").append(alerts.stream().filter(a -> "MEDIUM".equals(a.getSeverity())).count()).append("\n\n");
        
        if (!alerts.isEmpty()) {
            report.append("🚨 Alertes détectées :\n\n");
            for (KpiAlert alert : alerts) {
                report.append("• ").append(alert.getKpiName()).append(" : ").append(alert.getMessage()).append("\n");
            }
        }
        
        return report.toString();
    }
    
    private String generateMonthlyReport(List<KpiAlert> alerts) {
        StringBuilder report = new StringBuilder();
        report.append("📆 RAPPORT MENSUEL KPI\n\n");
        report.append("Mois : ").append(java.time.LocalDate.now().getMonth()).append(" ").append(java.time.LocalDate.now().getYear()).append("\n\n");
        report.append("📊 Statistiques du mois :\n");
        report.append("- Total alertes générées : ").append(alerts.size()).append("\n");
        report.append("- Alertes critiques : ").append(alerts.stream().filter(a -> "HIGH".equals(a.getSeverity())).count()).append("\n");
        report.append("- Alertes moyennes : ").append(alerts.stream().filter(a -> "MEDIUM".equals(a.getSeverity())).count()).append("\n");
        report.append("- Alertes basses : ").append(alerts.stream().filter(a -> "LOW".equals(a.getSeverity())).count()).append("\n\n");
        
        if (!alerts.isEmpty()) {
            report.append("📋 Détail des alertes :\n\n");
            for (KpiAlert alert : alerts) {
                report.append("━━━━━━━━━━━━━━━━━━━━\n");
                report.append("KPI : ").append(alert.getKpiName()).append("\n");
                report.append("Statut : ").append(alert.getStatus()).append("\n");
                report.append("Message : ").append(alert.getMessage()).append("\n");
                if (alert.getRecommendation() != null) {
                    report.append("Recommandation : ").append(alert.getRecommendation()).append("\n");
                }
                report.append("\n");
            }
        }
        
        return report.toString();
    }
    
    private String getStatusEmoji(String status) {
        switch (status) {
            case "SAIN": return "🟢";
            case "A_SURVEILLER": return "🟡";
            case "ANORMAL": return "🔴";
            default: return "⚪";
        }
    }
    
    private String getSeverityEmoji(String severity) {
        switch (severity) {
            case "LOW": return "ℹ️";
            case "MEDIUM": return "⚠️";
            case "HIGH": return "🚨";
            case "CRITICAL": return "🔥";
            default: return "❓";
        }
    }
}
