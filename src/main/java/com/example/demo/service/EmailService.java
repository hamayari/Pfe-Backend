package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${mail.from.address:noreply@gestionpro.com}")
    private String fromAddress;
    
    @Value("${mail.from.name:GestionPro}")
    private String fromName;

    /**
     * Méthode principale d'envoi d'email avec template
     */
    public void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Utiliser l'adresse professionnelle configurée
            // Pour Brevo: utiliser l'email vérifié dans Brevo
            try {
                helper.setFrom(fromEmail, fromName);
            } catch (java.io.UnsupportedEncodingException e) {
                // Fallback sans nom si encodage échoue
                helper.setFrom(fromEmail);
            }
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true pour HTML
            
            mailSender.send(message);
            System.out.println("✅ Email envoyé avec succès à: " + to + " depuis " + fromEmail);
            
        } catch (MessagingException e) {
            System.err.println("❌ Erreur envoi email à " + to + ": " + e.getMessage());
            e.printStackTrace();
            // Ne pas lancer d'exception pour ne pas bloquer le processus
            System.err.println("⚠️ L'envoi d'email a échoué mais le processus continue");
        }
    }

    /**
     * Envoi d'email de notification avec template professionnel
     */
    public void sendNotificationEmail(String email, String title, String message) {
        String htmlContent = buildNotificationTemplate(title, message, "info");
        sendEmail(email, "🔔 " + title, htmlContent);
    }

    /**
     * Envoi d'email de bienvenue pour nouveaux utilisateurs
     */
    public void sendNewUserEmail(String email, String username) {
        String subject = "🎉 Bienvenue sur GestionPro";
        String htmlContent = buildWelcomeTemplate(username, email);
        sendEmail(email, subject, htmlContent);
    }

    /**
     * Envoi d'email de réinitialisation de mot de passe
     */
    public void sendPasswordResetEmail(String email, String resetToken) {
        String subject = "🔐 Réinitialisation de votre mot de passe";
        // Inclure un paramètre de rôle par défaut dans l'URL
        String resetLink = "http://localhost:4200/auth/reset-password?token=" + resetToken + "&role=decision-maker";
        String htmlContent = buildPasswordResetTemplate(email, resetLink);
        sendEmail(email, subject, htmlContent);
    }

    /**
     * Envoi d'email de code 2FA
     */
    public void sendTwoFactorCode(String email, String code) {
        String subject = "🔐 Code de vérification à deux facteurs";
        String htmlContent = buildTwoFactorTemplate(code);
        sendEmail(email, subject, htmlContent);
    }

    /**
     * Alias pour send2FACode (compatibilité)
     */
    public void send2FACode(String email, String code, String username) {
        sendTwoFactorCode(email, code);
    }

    /**
     * Alias pour sendWelcomeEmail (compatibilité)
     */
    public void sendWelcomeEmail(String email, String username) {
        sendNewUserEmail(email, username);
    }

    /**
     * Envoi d'email avec credentials client
     */
    public void sendClientCredentials(String email, String clientName, String password) {
        String subject = "👤 Vos identifiants d'accès GestionPro";
        String htmlContent = buildClientCredentialsTemplate(clientName, email, password);
        sendEmail(email, subject, htmlContent);
    }

    /**
     * Envoi d'email de réinitialisation mot de passe client
     */
    public void sendClientPasswordReset(String email, String clientName, String newPassword) {
        String subject = "🔐 Nouveau mot de passe GestionPro";
        String htmlContent = buildClientPasswordResetTemplate(clientName, newPassword);
        sendEmail(email, subject, htmlContent);
    }

    /**
     * Envoi d'email de facture avec credentials
     */
    public void sendInvoiceWithClientCredentials(String email, String invoiceId, String invoiceNumber, String amount, String credentials) {
        String subject = "📄 Facture " + invoiceNumber + " - GestionPro";
        String htmlContent = buildInvoiceTemplate(email, invoiceNumber, amount, credentials);
        sendEmail(email, subject, htmlContent);
    }

    /**
     * Génération de mot de passe sécurisé
     */
    public String generateSecurePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }

    /**
     * Template de notification générique
     */
    private String buildNotificationTemplate(String title, String message, String type) {
        String color = getTypeColor(type);
        String icon = getTypeIcon(type);
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, %s, %s); color: white; padding: 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 24px; font-weight: 600; }
                    .content { padding: 30px; }
                    .message { background-color: #f8f9fa; border-left: 4px solid %s; padding: 20px; border-radius: 4px; margin: 20px 0; }
                    .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 14px; }
                    .button { display: inline-block; background-color: %s; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: 500; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s %s</h1>
                    </div>
                    <div class="content">
                        <div class="message">
                            %s
                        </div>
                        <p>Cette notification a été générée automatiquement par le système GestionPro.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 GestionPro - Système de Gestion Professionnel</p>
                        <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """, title, color, color, color, color, icon, title, message);
    }

    /**
     * Template de bienvenue
     */
    private String buildWelcomeTemplate(String username, String email) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Bienvenue sur GestionPro</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #4CAF50, #45a049); color: white; padding: 40px; text-align: center; }
                    .header h1 { margin: 0; font-size: 28px; font-weight: 600; }
                    .content { padding: 40px; }
                    .welcome-box { background-color: #e8f5e8; border: 2px solid #4CAF50; border-radius: 8px; padding: 30px; text-align: center; margin: 20px 0; }
                    .features { margin: 30px 0; }
                    .feature { display: flex; align-items: center; margin: 15px 0; }
                    .feature-icon { background-color: #4CAF50; color: white; width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin-right: 15px; }
                    .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Bienvenue sur GestionPro !</h1>
                    </div>
                    <div class="content">
                        <div class="welcome-box">
                            <h2>Bonjour %s,</h2>
                            <p>Votre compte a été créé avec succès !</p>
                            <p><strong>Email :</strong> %s</p>
                        </div>
                        
                        <h3>🚀 Fonctionnalités disponibles :</h3>
                        <div class="features">
                            <div class="feature">
                                <div class="feature-icon">📊</div>
                                <span>Dashboard de gestion complet</span>
                            </div>
                            <div class="feature">
                                <div class="feature-icon">📄</div>
                                <span>Gestion des conventions et factures</span>
                            </div>
                            <div class="feature">
                                <div class="feature-icon">🔔</div>
                                <span>Notifications automatiques</span>
                            </div>
                            <div class="feature">
                                <div class="feature-icon">📱</div>
                                <span>Interface responsive et moderne</span>
                            </div>
                        </div>
                        
                        <p>Vous pouvez maintenant vous connecter et commencer à utiliser toutes les fonctionnalités de GestionPro.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 GestionPro - Système de Gestion Professionnel</p>
                    </div>
                </div>
            </body>
            </html>
            """, username, email);
    }

    /**
     * Template de réinitialisation de mot de passe
     */
    private String buildPasswordResetTemplate(String email, String resetLink) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Réinitialisation de mot de passe</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #FF9800, #F57C00); color: white; padding: 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 24px; font-weight: 600; }
                    .content { padding: 30px; }
                    .reset-box { background-color: #fff3e0; border: 2px solid #FF9800; border-radius: 8px; padding: 30px; text-align: center; margin: 20px 0; }
                    .button { display: inline-block; background-color: #FF9800; color: white; padding: 15px 30px; text-decoration: none; border-radius: 6px; font-weight: 500; margin: 20px 0; }
                    .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Réinitialisation de mot de passe</h1>
                    </div>
                    <div class="content">
                        <div class="reset-box">
                            <h2>Demande de réinitialisation</h2>
                            <p>Une demande de réinitialisation de mot de passe a été effectuée pour votre compte :</p>
                            <p><strong>%s</strong></p>
                            <p>Cliquez sur le bouton ci-dessous pour créer un nouveau mot de passe :</p>
                            <a href="%s" class="button">Réinitialiser mon mot de passe</a>
                            <p><small>Ce lien est valide pendant 1 heure.</small></p>
                        </div>
                        <p>Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 GestionPro - Système de Gestion Professionnel</p>
                    </div>
                </div>
            </body>
            </html>
            """, email, resetLink);
    }

    /**
     * Template de code 2FA
     */
    private String buildTwoFactorTemplate(String code) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Code de vérification</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #2196F3, #1976D2); color: white; padding: 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 24px; font-weight: 600; }
                    .content { padding: 30px; }
                    .code-box { background-color: #e3f2fd; border: 2px solid #2196F3; border-radius: 8px; padding: 30px; text-align: center; margin: 20px 0; }
                    .code { font-size: 32px; font-weight: bold; color: #2196F3; letter-spacing: 5px; margin: 20px 0; }
                    .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Code de vérification</h1>
                    </div>
                    <div class="content">
                        <div class="code-box">
                            <h2>Votre code de vérification</h2>
                            <div class="code">%s</div>
                            <p>Ce code est valide pendant 5 minutes.</p>
                        </div>
                        <p>Entrez ce code dans l'application pour finaliser votre connexion.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 GestionPro - Système de Gestion Professionnel</p>
                    </div>
                </div>
            </body>
            </html>
            """, code);
    }

    /**
     * Template de credentials client
     */
    private String buildClientCredentialsTemplate(String clientName, String email, String password) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Vos identifiants d'accès</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #9C27B0, #7B1FA2); color: white; padding: 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 24px; font-weight: 600; }
                    .content { padding: 30px; }
                    .credentials-box { background-color: #f3e5f5; border: 2px solid #9C27B0; border-radius: 8px; padding: 30px; margin: 20px 0; }
                    .credential-item { margin: 15px 0; padding: 15px; background-color: white; border-radius: 6px; }
                    .credential-label { font-weight: bold; color: #9C27B0; }
                    .credential-value { font-family: monospace; background-color: #f8f9fa; padding: 8px; border-radius: 4px; margin-top: 5px; }
                    .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>👤 Vos identifiants d'accès</h1>
                    </div>
                    <div class="content">
                        <h2>Bonjour %s,</h2>
                        <p>Votre compte client a été créé avec succès sur GestionPro.</p>
                        
                        <div class="credentials-box">
                            <h3>🔑 Vos identifiants de connexion :</h3>
                            <div class="credential-item">
                                <div class="credential-label">Email :</div>
                                <div class="credential-value">%s</div>
                            </div>
                            <div class="credential-item">
                                <div class="credential-label">Mot de passe temporaire :</div>
                                <div class="credential-value">%s</div>
                            </div>
                        </div>
                        
                        <p><strong>⚠️ Important :</strong> Vous devrez changer ce mot de passe lors de votre première connexion.</p>
                        <p>Vous pouvez maintenant accéder à votre espace client pour consulter vos factures et documents.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 GestionPro - Système de Gestion Professionnel</p>
                    </div>
                </div>
            </body>
            </html>
            """, clientName, email, password);
    }

    /**
     * Template de réinitialisation mot de passe client
     */
    private String buildClientPasswordResetTemplate(String clientName, String newPassword) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Nouveau mot de passe</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #FF5722, #D84315); color: white; padding: 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 24px; font-weight: 600; }
                    .content { padding: 30px; }
                    .password-box { background-color: #fbe9e7; border: 2px solid #FF5722; border-radius: 8px; padding: 30px; text-align: center; margin: 20px 0; }
                    .new-password { font-size: 24px; font-weight: bold; color: #FF5722; font-family: monospace; letter-spacing: 2px; margin: 20px 0; padding: 15px; background-color: white; border-radius: 6px; }
                    .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Nouveau mot de passe</h1>
                    </div>
                    <div class="content">
                        <h2>Bonjour %s,</h2>
                        <p>Votre mot de passe a été réinitialisé avec succès.</p>
                        
                        <div class="password-box">
                            <h3>🔑 Votre nouveau mot de passe :</h3>
                            <div class="new-password">%s</div>
                            <p><strong>⚠️ Important :</strong> Changez ce mot de passe lors de votre prochaine connexion.</p>
                        </div>
                        
                        <p>Vous pouvez maintenant vous connecter avec ce nouveau mot de passe.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 GestionPro - Système de Gestion Professionnel</p>
                    </div>
                </div>
            </body>
            </html>
            """, clientName, newPassword);
    }

    /**
     * Template de facture avec credentials
     */
    private String buildInvoiceTemplate(String email, String invoiceNumber, String amount, String credentials) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Facture %s</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #607D8B, #455A64); color: white; padding: 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 24px; font-weight: 600; }
                    .content { padding: 30px; }
                    .invoice-box { background-color: #eceff1; border: 2px solid #607D8B; border-radius: 8px; padding: 30px; margin: 20px 0; }
                    .invoice-details { display: flex; justify-content: space-between; margin: 20px 0; }
                    .invoice-item { flex: 1; text-align: center; }
                    .invoice-label { font-weight: bold; color: #607D8B; }
                    .invoice-value { font-size: 18px; margin-top: 5px; }
                    .credentials-box { background-color: #e8f5e8; border: 2px solid #4CAF50; border-radius: 8px; padding: 20px; margin: 20px 0; }
                    .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📄 Facture %s</h1>
                    </div>
                    <div class="content">
                        <h2>Bonjour,</h2>
                        <p>Votre facture a été générée avec succès.</p>
                        
                        <div class="invoice-box">
                            <h3>📋 Détails de la facture :</h3>
                            <div class="invoice-details">
                                <div class="invoice-item">
                                    <div class="invoice-label">Numéro</div>
                                    <div class="invoice-value">%s</div>
                                </div>
                                <div class="invoice-item">
                                    <div class="invoice-label">Montant</div>
                                    <div class="invoice-value">%s €</div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="credentials-box">
                            <h3>👤 Accès à votre espace client :</h3>
                            <p>%s</p>
                            <p>Vous pouvez maintenant vous connecter pour consulter et télécharger votre facture.</p>
                        </div>
                        
                        <p>Merci pour votre confiance.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 GestionPro - Système de Gestion Professionnel</p>
                    </div>
                </div>
            </body>
            </html>
            """, invoiceNumber, invoiceNumber, invoiceNumber, amount, credentials);
    }

    /**
     * Couleurs selon le type de notification
     */
    private String getTypeColor(String type) {
        return switch (type.toLowerCase()) {
            case "success" -> "#4CAF50";
            case "warning" -> "#FF9800";
            case "error" -> "#F44336";
            case "info" -> "#2196F3";
            default -> "#607D8B";
        };
    }

    /**
     * Icônes selon le type de notification
     */
    private String getTypeIcon(String type) {
        return switch (type.toLowerCase()) {
            case "success" -> "✅";
            case "warning" -> "⚠️";
            case "error" -> "❌";
            case "info" -> "ℹ️";
            default -> "📢";
        };
    }

    /**
     * Envoyer un email de rappel de convention
     */
    public void sendConventionReminderEmail(String to, Map<String, String> variables) throws MessagingException, IOException {
        String subject = "⏰ Rappel Convention - Échéance Approchante";
        String template = buildConventionReminderTemplate(variables);
        sendEmail(to, subject, template);
    }

    /**
     * Envoyer un email de rappel de facture
     */
    public void sendInvoiceReminderEmail(String to, Map<String, String> variables) throws MessagingException, IOException {
        String subject = "💰 Rappel Facture - Échéance Approchante";
        String template = buildInvoiceReminderTemplate(variables);
        sendEmail(to, subject, template);
    }

    /**
     * Envoyer un email de facture en retard
     */
    public void sendOverdueInvoiceEmail(String to, Map<String, String> variables) throws MessagingException, IOException {
        String subject = "🚨 URGENT - Facture en Retard";
        String template = buildOverdueInvoiceTemplate(variables);
        sendEmail(to, subject, template);
    }

    /**
     * Envoyer un email de création de convention
     */
    public void sendConventionCreatedEmail(String to, Map<String, String> variables) throws MessagingException, IOException {
        String subject = "✅ Nouvelle Convention Créée - GestionPro";
        String template = buildConventionCreatedTemplate(variables);
        sendEmail(to, subject, template);
    }

    /**
     * Template de rappel de convention
     */
    private String buildConventionReminderTemplate(Map<String, String> variables) {
        String commercialName = variables.getOrDefault("commercialName", "Utilisateur");
        String conventionReference = variables.getOrDefault("conventionReference", "N/A");
        String conventionTitle = variables.getOrDefault("conventionTitle", "N/A");
        String dueDate = variables.getOrDefault("dueDate", "N/A");
        String daysBefore = variables.getOrDefault("daysBefore", "0");
        String amount = variables.getOrDefault("amount", "0");
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Rappel Convention - GestionPro</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4;">
                <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                    <div style="background: linear-gradient(135deg, #FF9800, #F57C00); color: white; padding: 30px; text-align: center;">
                        <h1 style="margin: 0; font-size: 24px;">⏰ Rappel Convention</h1>
                        <p>Échéance Approchante</p>
                    </div>
                    
                    <div style="padding: 30px;">
                        <p>Bonjour <strong>%s</strong>,</p>
                        
                        <div style="background: #FFF3E0; border-left: 4px solid #FF9800; padding: 20px; margin: 20px 0; border-radius: 4px;">
                            <h3>🚨 Convention à Suivre</h3>
                            <p>Une convention approche de son échéance et nécessite votre attention.</p>
                        </div>
                        
                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin: 20px 0;">
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Référence Convention</div>
                                <div style="font-size: 16px; margin-top: 5px;">%s</div>
                            </div>
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Titre</div>
                                <div style="font-size: 16px; margin-top: 5px;">%s</div>
                            </div>
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Date d'Échéance</div>
                                <div style="font-size: 16px; margin-top: 5px;">%s</div>
                            </div>
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Jours Restants</div>
                                <div style="font-size: 16px; margin-top: 5px;">%s jour(s)</div>
                            </div>
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Montant</div>
                                <div style="font-size: 16px; margin-top: 5px;">%s€</div>
                            </div>
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Statut</div>
                                <div style="font-size: 16px; margin-top: 5px;">En cours</div>
                            </div>
                        </div>
                        
                        <p><strong>Action Requise :</strong> Veuillez vérifier l'état de cette convention et prendre les mesures nécessaires avant l'échéance.</p>
                        
                        <a href="http://localhost:4200/dashboard/commercial" style="display: inline-block; background: #FF9800; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; margin: 20px 0;">
                            📊 Accéder au Dashboard
                        </a>
                        
                        <p>Si vous avez des questions, n'hésitez pas à contacter l'équipe support.</p>
                    </div>
                    
                    <div style="background: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 14px;">
                        <p>© 2024 GestionPro - Système de Gestion Professionnel</p>
                        <p>Cet email a été envoyé automatiquement par le système de notifications.</p>
                    </div>
                </div>
            </body>
            </html>
            """, commercialName, conventionReference, conventionTitle, dueDate, daysBefore, amount);
    }

    /**
     * Template de rappel de facture
     */
    private String buildInvoiceReminderTemplate(Map<String, String> variables) {
        String commercialName = variables.getOrDefault("commercialName", "Utilisateur");
        String invoiceNumber = variables.getOrDefault("invoiceNumber", "N/A");
        String clientName = variables.getOrDefault("clientName", "N/A");
        String dueDate = variables.getOrDefault("dueDate", "N/A");
        String daysBefore = variables.getOrDefault("daysBefore", "0");
        String amount = variables.getOrDefault("amount", "0");
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Rappel Facture - GestionPro</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4;">
                <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                    <div style="background: linear-gradient(135deg, #2196F3, #1976D2); color: white; padding: 30px; text-align: center;">
                        <h1 style="margin: 0; font-size: 24px;">💰 Rappel Facture</h1>
                        <p>Échéance Approchante</p>
                    </div>
                    
                    <div style="padding: 30px;">
                        <p>Bonjour <strong>%s</strong>,</p>
                        
                        <div style="background: #E3F2FD; border-left: 4px solid #2196F3; padding: 20px; margin: 20px 0; border-radius: 4px;">
                            <h3>📄 Facture à Suivre</h3>
                            <p>Une facture approche de son échéance et nécessite votre attention.</p>
                        </div>
                        
                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin: 20px 0;">
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Numéro Facture</div>
                                <div style="font-size: 16px; margin-top: 5px;">%s</div>
                            </div>
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Client</div>
                                <div style="font-size: 16px; margin-top: 5px;">%s</div>
                            </div>
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Date d'Échéance</div>
                                <div style="font-size: 16px; margin-top: 5px;">%s</div>
                            </div>
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Jours Restants</div>
                                <div style="font-size: 16px; margin-top: 5px;">%s jour(s)</div>
                            </div>
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Montant</div>
                                <div style="font-size: 20px; font-weight: bold; color: #2196F3; margin-top: 5px;">%s€</div>
                            </div>
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Statut</div>
                                <div style="font-size: 16px; margin-top: 5px;">En attente</div>
                            </div>
                        </div>
                        
                        <p><strong>Action Requise :</strong> Veuillez vérifier l'état de cette facture et prendre les mesures nécessaires avant l'échéance.</p>
                        
                        <a href="http://localhost:4200/dashboard/commercial" style="display: inline-block; background: #2196F3; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; margin: 20px 0;">
                            📊 Accéder au Dashboard
                        </a>
                        
                        <p>Si vous avez des questions, n'hésitez pas à contacter l'équipe support.</p>
                    </div>
                    
                    <div style="background: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 14px;">
                        <p>© 2024 GestionPro - Système de Gestion Professionnel</p>
                        <p>Cet email a été envoyé automatiquement par le système de notifications.</p>
                    </div>
                </div>
            </body>
            </html>
            """, commercialName, invoiceNumber, clientName, dueDate, daysBefore, amount);
    }

    /**
     * Template de facture en retard
     */
    private String buildOverdueInvoiceTemplate(Map<String, String> variables) {
        String commercialName = variables.getOrDefault("commercialName", "Utilisateur");
        String invoiceNumber = variables.getOrDefault("invoiceNumber", "N/A");
        String clientName = variables.getOrDefault("clientName", "N/A");
        String dueDate = variables.getOrDefault("dueDate", "N/A");
        String daysOverdue = variables.getOrDefault("daysOverdue", "0");
        String amount = variables.getOrDefault("amount", "0");
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>URGENT - Facture en Retard - GestionPro</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4;">
                <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                    <div style="background: linear-gradient(135deg, #F44336, #D32F2F); color: white; padding: 30px; text-align: center;">
                        <h1 style="margin: 0; font-size: 24px;">🚨 URGENT - Facture en Retard</h1>
                        <p>Action Immédiate Requise</p>
                    </div>
                    
                    <div style="padding: 30px;">
                        <p>Bonjour <strong>%s</strong>,</p>
                        
                        <div style="background: #FFEBEE; border-left: 4px solid #F44336; padding: 20px; margin: 20px 0; border-radius: 4px;">
                            <h3>🚨 ALERTE CRITIQUE</h3>
                            <p>Une facture a dépassé son échéance et nécessite une action immédiate.</p>
                        </div>
                        
                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin: 20px 0;">
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Numéro Facture</div>
                                <div style="font-size: 16px; margin-top: 5px;">%s</div>
                            </div>
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Client</div>
                                <div style="font-size: 16px; margin-top: 5px;">%s</div>
                            </div>
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Date d'Échéance</div>
                                <div style="font-size: 16px; margin-top: 5px;">%s</div>
                            </div>
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Jours de Retard</div>
                                <div style="font-size: 18px; font-weight: bold; color: #F44336; margin-top: 5px;">%s jour(s)</div>
                            </div>
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Montant</div>
                                <div style="font-size: 20px; font-weight: bold; color: #F44336; margin-top: 5px;">%s€</div>
                            </div>
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Statut</div>
                                <div style="font-size: 16px; margin-top: 5px;">EN RETARD</div>
                            </div>
                        </div>
                        
                        <p><strong>Action Immédiate Requise :</strong></p>
                        <ul>
                            <li>Contacter le client immédiatement</li>
                            <li>Vérifier le statut du paiement</li>
                            <li>Mettre à jour le statut de la facture</li>
                            <li>Planifier un suivi de relance</li>
                        </ul>
                        
                        <a href="http://localhost:4200/dashboard/commercial" style="display: inline-block; background: #F44336; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; margin: 20px 0;">
                            🚨 Accéder au Dashboard
                        </a>
                        
                        <p><strong>Note :</strong> Cette alerte sera renvoyée quotidiennement jusqu'à résolution du problème.</p>
                    </div>
                    
                    <div style="background: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 14px;">
                        <p>© 2024 GestionPro - Système de Gestion Professionnel</p>
                        <p>Cet email a été envoyé automatiquement par le système de notifications.</p>
                    </div>
                </div>
            </body>
            </html>
            """, commercialName, invoiceNumber, clientName, dueDate, daysOverdue, amount);
    }

    /**
     * Template de création de convention
     */
    private String buildConventionCreatedTemplate(Map<String, String> variables) {
        String commercialName = variables.getOrDefault("commercialName", "Utilisateur");
        String conventionReference = variables.getOrDefault("conventionReference", "N/A");
        String conventionTitle = variables.getOrDefault("conventionTitle", "N/A");
        String amount = variables.getOrDefault("amount", "0");
        String dueDate = variables.getOrDefault("dueDate", "N/A");
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Nouvelle Convention Créée - GestionPro</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4;">
                <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                    <div style="background: linear-gradient(135deg, #4CAF50, #45a049); color: white; padding: 30px; text-align: center;">
                        <h1 style="margin: 0; font-size: 24px;">✅ Nouvelle Convention Créée</h1>
                        <p>Confirmation de création</p>
                    </div>
                    
                    <div style="padding: 30px;">
                        <p>Bonjour <strong>%s</strong>,</p>
                        
                        <div style="background: #E8F5E8; border-left: 4px solid #4CAF50; padding: 20px; margin: 20px 0; border-radius: 4px;">
                            <h3>🎉 Convention Créée avec Succès</h3>
                            <p>Votre nouvelle convention a été créée et enregistrée dans le système.</p>
                        </div>
                        
                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin: 20px 0;">
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Référence Convention</div>
                                <div style="font-size: 16px; margin-top: 5px;">%s</div>
                            </div>
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Titre</div>
                                <div style="font-size: 16px; margin-top: 5px;">%s</div>
                            </div>
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Montant</div>
                                <div style="font-size: 20px; font-weight: bold; color: #4CAF50; margin-top: 5px;">%s€</div>
                            </div>
                            <div style="background: #f8f9fa; padding: 15px; border-radius: 6px;">
                                <div style="font-weight: bold; color: #666; font-size: 14px;">Date d'Échéance</div>
                                <div style="font-size: 16px; margin-top: 5px;">%s</div>
                            </div>
                        </div>
                        
                        <p><strong>Prochaines étapes :</strong></p>
                        <ul>
                            <li>La convention est maintenant active dans le système</li>
                            <li>Les échéances de paiement ont été générées automatiquement</li>
                            <li>Vous recevrez des rappels avant chaque échéance</li>
                            <li>Vous pouvez générer les factures correspondantes</li>
                        </ul>
                        
                        <a href="http://localhost:4200/dashboard/commercial" style="display: inline-block; background: #4CAF50; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; margin: 20px 0;">
                            📊 Accéder au Dashboard
                        </a>
                        
                        <p>Si vous avez des questions, n'hésitez pas à contacter l'équipe support.</p>
                    </div>
                    
                    <div style="background: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 14px;">
                        <p>© 2024 GestionPro - Système de Gestion Professionnel</p>
                        <p>Cet email a été envoyé automatiquement par le système de notifications.</p>
                    </div>
                </div>
            </body>
            </html>
            """, commercialName, conventionReference, conventionTitle, amount, dueDate);
    }
}
