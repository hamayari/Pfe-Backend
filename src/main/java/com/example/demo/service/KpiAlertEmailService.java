package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service d'envoi d'emails pour les alertes KPI
 * Utilise EmailService existant pour la cohérence
 */
@Service
@Slf4j
public class KpiAlertEmailService {

    private final EmailService emailService;

    @Value("${mail.from.address:noreply@gestionpro.com}")
    private String fromEmail;

    @Value("${mail.from.name:GestionPro}")
    private String appName;

    @Autowired
    public KpiAlertEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Envoyer un email d'alerte KPI
     */
    public void sendKpiAlertEmail(
            String toEmail,
            String recipientName,
            String kpiName,
            String message,
            String recommendation,
            String severity,
            String priority,
            String alertId) {
        
        try {
            log.info("📧 Envoi email alerte KPI à: {}", toEmail);

            String subject = getEmailSubject(severity, kpiName);
            String htmlContent = buildHtmlContent(recipientName, kpiName, message, recommendation, severity, priority, alertId);

            // Utiliser EmailService existant
            emailService.sendEmail(toEmail, subject, htmlContent);
            
            log.info("✅ Email alerte KPI envoyé avec succès à: {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Erreur envoi email alerte KPI à {}: {}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * Envoyer un email de délégation au Chef de Projet
     */
    public void sendDelegationEmail(
            String toEmail,
            String projectManagerName,
            String decideurName,
            String kpiName,
            String message,
            String comment,
            String priority,
            String alertId) {
        
        try {
            log.info("📧 Envoi email délégation à: {}", toEmail);

            String subject = "🔔 Alerte KPI Déléguée: " + kpiName;
            String htmlContent = buildDelegationHtmlContent(projectManagerName, decideurName, kpiName, message, comment, priority, alertId);

            // Utiliser EmailService existant
            emailService.sendEmail(toEmail, subject, htmlContent);
            
            log.info("✅ Email délégation envoyé avec succès à: {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Erreur envoi email délégation à {}: {}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * Générer le sujet de l'email selon la sévérité
     */
    private String getEmailSubject(String severity, String kpiName) {
        String emoji = switch (severity) {
            case "CRITICAL", "HIGH" -> "🚨";
            case "MEDIUM" -> "⚠️";
            default -> "ℹ️";
        };
        
        return String.format("%s Alerte KPI %s: %s", emoji, severity, kpiName);
    }

    /**
     * Construire le contenu HTML de l'email d'alerte
     */
    private String buildHtmlContent(
            String recipientName,
            String kpiName,
            String message,
            String recommendation,
            String severity,
            String priority,
            String alertId) {
        
        String severityColor = switch (severity) {
            case "CRITICAL", "HIGH" -> "#f44336";
            case "MEDIUM" -> "#ff9800";
            default -> "#4caf50";
        };

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: %s; color: white; padding: 20px; border-radius: 8px 8px 0 0; }
                    .header h1 { margin: 0; font-size: 24px; }
                    .content { background: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                    .alert-box { background: white; padding: 15px; margin: 15px 0; border-left: 4px solid %s; border-radius: 4px; }
                    .recommendation-box { background: #e3f2fd; padding: 15px; margin: 15px 0; border-radius: 4px; }
                    .footer { background: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; padding: 12px 24px; background: #1976d2; color: white; text-decoration: none; border-radius: 4px; margin: 10px 0; }
                    .badge { display: inline-block; padding: 4px 12px; background: %s; color: white; border-radius: 12px; font-size: 12px; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🚨 Alerte KPI Détectée</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        
                        <p>Une anomalie a été détectée sur le KPI suivant :</p>
                        
                        <div class="alert-box">
                            <h2 style="margin-top: 0; color: %s;">%s</h2>
                            <p><span class="badge">%s</span> <span class="badge" style="background: #666;">%s</span></p>
                            <p style="margin: 15px 0;"><strong>Message:</strong></p>
                            <p>%s</p>
                        </div>
                        
                        <div class="recommendation-box">
                            <h3 style="margin-top: 0;">💡 Recommandation</h3>
                            <p>%s</p>
                        </div>
                        
                        <p style="text-align: center;">
                            <a href="http://localhost:4200/dashboard" class="button">Voir dans le Dashboard</a>
                        </p>
                        
                        <p style="font-size: 12px; color: #666;">
                            <strong>ID Alerte:</strong> %s<br>
                            <strong>Date:</strong> %s
                        </p>
                    </div>
                    <div class="footer">
                        <p>Cet email a été envoyé automatiquement par %s</p>
                        <p>Merci de ne pas répondre à cet email.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            severityColor,
            severityColor,
            severityColor,
            recipientName,
            severityColor,
            kpiName,
            severity,
            priority,
            message,
            recommendation,
            alertId,
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            appName
        );
    }

    /**
     * Construire le contenu HTML de l'email de délégation
     */
    private String buildDelegationHtmlContent(
            String projectManagerName,
            String decideurName,
            String kpiName,
            String message,
            String comment,
            String priority,
            String alertId) {
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #1976d2; color: white; padding: 20px; border-radius: 8px 8px 0 0; }
                    .header h1 { margin: 0; font-size: 24px; }
                    .content { background: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                    .alert-box { background: white; padding: 15px; margin: 15px 0; border-left: 4px solid #1976d2; border-radius: 4px; }
                    .comment-box { background: #fff3cd; padding: 15px; margin: 15px 0; border-radius: 4px; border-left: 4px solid #ffc107; }
                    .footer { background: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; padding: 12px 24px; background: #1976d2; color: white; text-decoration: none; border-radius: 4px; margin: 10px 0; }
                    .badge { display: inline-block; padding: 4px 12px; background: #ff9800; color: white; border-radius: 12px; font-size: 12px; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔔 Alerte KPI Déléguée</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        
                        <p>Le Décideur <strong>%s</strong> vous a délégué une alerte KPI pour traitement :</p>
                        
                        <div class="alert-box">
                            <h2 style="margin-top: 0; color: #1976d2;">%s</h2>
                            <p><span class="badge">%s</span></p>
                            <p style="margin: 15px 0;"><strong>Description:</strong></p>
                            <p>%s</p>
                        </div>
                        
                        %s
                        
                        <p style="text-align: center;">
                            <a href="http://localhost:4200/pm-dashboard/kpi-alerts" class="button">Prendre en Charge</a>
                        </p>
                        
                        <p style="font-size: 12px; color: #666;">
                            <strong>ID Alerte:</strong> %s<br>
                            <strong>Date:</strong> %s
                        </p>
                    </div>
                    <div class="footer">
                        <p>Cet email a été envoyé automatiquement par %s</p>
                        <p>Merci de ne pas répondre à cet email.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            projectManagerName,
            decideurName,
            kpiName,
            priority,
            message,
            comment != null && !comment.isEmpty() 
                ? String.format("<div class=\"comment-box\"><h3 style=\"margin-top: 0;\">💬 Commentaire du Décideur</h3><p>%s</p></div>", comment)
                : "",
            alertId,
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            appName
        );
    }
}
