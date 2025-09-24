package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.enums.ERole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationRecipientService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationPreferencesRepository preferencesRepository;
    
    @Autowired
    private ConventionRepository conventionRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;

    /**
     * Récupérer les destinataires pour une notification de facture
     */
    public List<NotificationRecipient> getRecipientsForInvoice(String invoiceId, String notificationType) {
        List<NotificationRecipient> recipients = new ArrayList<>();
        
        try {
            // Récupérer la facture
            Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
            if (invoiceOpt.isEmpty()) {
                return recipients;
            }
            
            Invoice invoice = invoiceOpt.get();
            
            // 1. Commercial responsable (toujours inclus)
            NotificationRecipient commercial = getCommercialRecipient(invoice.getCreatedBy());
            if (commercial != null) {
                recipients.add(commercial);
            }
            
            // 2. Chef de projet (en copie pour supervision)
            if (shouldNotifyProjectManager(notificationType)) {
                List<NotificationRecipient> projectManagers = getProjectManagerRecipients();
                recipients.addAll(projectManagers);
            }
            
            // 3. Décideur (pour les cas critiques uniquement)
            if (isCriticalNotification(notificationType)) {
                List<NotificationRecipient> decisionMakers = getDecisionMakerRecipients();
                recipients.addAll(decisionMakers);
            }
            
        } catch (Exception e) {
            System.err.println("❌ [RECIPIENTS] Erreur récupération destinataires facture: " + e.getMessage());
        }
        
        return recipients;
    }

    /**
     * Récupérer les destinataires pour une notification de convention
     */
    public List<NotificationRecipient> getRecipientsForConvention(String conventionId, String notificationType) {
        List<NotificationRecipient> recipients = new ArrayList<>();
        
        try {
            // Récupérer la convention
            Optional<Convention> conventionOpt = conventionRepository.findById(conventionId);
            if (conventionOpt.isEmpty()) {
                return recipients;
            }
            
            Convention convention = conventionOpt.get();
            
            // 1. Commercial responsable
            NotificationRecipient commercial = getCommercialRecipient(convention.getCreatedBy());
            if (commercial != null) {
                recipients.add(commercial);
            }
            
            // 2. Chef de projet (toujours en copie pour les conventions)
            List<NotificationRecipient> projectManagers = getProjectManagerRecipients();
            recipients.addAll(projectManagers);
            
            // 3. Décideur (pour les alertes de fin de convention)
            if ("convention_deadline".equals(notificationType)) {
                List<NotificationRecipient> decisionMakers = getDecisionMakerRecipients();
                recipients.addAll(decisionMakers);
            }
            
        } catch (Exception e) {
            System.err.println("❌ [RECIPIENTS] Erreur récupération destinataires convention: " + e.getMessage());
        }
        
        return recipients;
    }

    /**
     * Récupérer le commercial responsable
     */
    private NotificationRecipient getCommercialRecipient(String userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return null;
            }
            
            User user = userOpt.get();
            Optional<NotificationPreferences> prefsOpt = preferencesRepository.findByUserId(userId);
            
            NotificationRecipient recipient = new NotificationRecipient();
            recipient.setUserId(userId);
            recipient.setUsername(user.getUsername());
            recipient.setEmail(user.getEmail());
            recipient.setPhoneNumber(user.getPhoneNumber());
            recipient.setRole("ROLE_COMMERCIAL");
            recipient.setPrimary(true); // Destinataire principal
            recipient.setPreferences(prefsOpt.orElse(null));
            
            return recipient;
            
        } catch (Exception e) {
            System.err.println("❌ [RECIPIENTS] Erreur récupération commercial: " + e.getMessage());
            return null;
        }
    }

    /**
     * Récupérer les chefs de projet
     */
    private List<NotificationRecipient> getProjectManagerRecipients() {
        List<NotificationRecipient> recipients = new ArrayList<>();
        
        try {
            List<User> projectManagers = userRepository.findByRoles_Name(ERole.ROLE_PROJECT_MANAGER);
            
            for (User pm : projectManagers) {
                Optional<NotificationPreferences> prefsOpt = preferencesRepository.findByUserId(pm.getId());
                
                NotificationRecipient recipient = new NotificationRecipient();
                recipient.setUserId(pm.getId());
                recipient.setUsername(pm.getUsername());
                recipient.setEmail(pm.getEmail());
                recipient.setPhoneNumber(pm.getPhoneNumber());
                recipient.setRole("ROLE_PROJECT_MANAGER");
                recipient.setPrimary(false); // En copie
                recipient.setPreferences(prefsOpt.orElse(null));
                
                recipients.add(recipient);
            }
            
        } catch (Exception e) {
            System.err.println("❌ [RECIPIENTS] Erreur récupération chefs de projet: " + e.getMessage());
        }
        
        return recipients;
    }

    /**
     * Récupérer les décideurs
     */
    private List<NotificationRecipient> getDecisionMakerRecipients() {
        List<NotificationRecipient> recipients = new ArrayList<>();
        
        try {
            List<User> decisionMakers = userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER);
            
            for (User dm : decisionMakers) {
                Optional<NotificationPreferences> prefsOpt = preferencesRepository.findByUserId(dm.getId());
                
                NotificationRecipient recipient = new NotificationRecipient();
                recipient.setUserId(dm.getId());
                recipient.setUsername(dm.getUsername());
                recipient.setEmail(dm.getEmail());
                recipient.setPhoneNumber(dm.getPhoneNumber());
                recipient.setRole("ROLE_DECISION_MAKER");
                recipient.setPrimary(false); // En copie
                recipient.setPreferences(prefsOpt.orElse(null));
                
                recipients.add(recipient);
            }
            
        } catch (Exception e) {
            System.err.println("❌ [RECIPIENTS] Erreur récupération décideurs: " + e.getMessage());
        }
        
        return recipients;
    }

    /**
     * Déterminer si le chef de projet doit être notifié
     */
    private boolean shouldNotifyProjectManager(String notificationType) {
        // Le chef de projet est notifié pour tous les types sauf les confirmations
        return !"payment_confirmation".equals(notificationType);
    }

    /**
     * Déterminer si c'est une notification critique
     */
    private boolean isCriticalNotification(String notificationType) {
        return Arrays.asList("overdue", "critical", "convention_deadline").contains(notificationType);
    }

    /**
     * Récupérer les destinataires pour le résumé hebdomadaire
     */
    public List<NotificationRecipient> getWeeklySummaryRecipients() {
        List<NotificationRecipient> recipients = new ArrayList<>();
        
        try {
            // Seuls les décideurs reçoivent le résumé hebdomadaire
            List<User> decisionMakers = userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER);
            
            for (User dm : decisionMakers) {
                Optional<NotificationPreferences> prefsOpt = preferencesRepository.findByUserId(dm.getId());
                
                NotificationRecipient recipient = new NotificationRecipient();
                recipient.setUserId(dm.getId());
                recipient.setUsername(dm.getUsername());
                recipient.setEmail(dm.getEmail());
                recipient.setPhoneNumber(dm.getPhoneNumber());
                recipient.setRole("ROLE_DECISION_MAKER");
                recipient.setPrimary(true); // Destinataire principal pour le résumé
                recipient.setPreferences(prefsOpt.orElse(null));
                
                recipients.add(recipient);
            }
            
        } catch (Exception e) {
            System.err.println("❌ [RECIPIENTS] Erreur récupération destinataires résumé: " + e.getMessage());
        }
        
        return recipients;
    }

    /**
     * Filtrer les destinataires selon leurs préférences
     */
    public List<NotificationRecipient> filterRecipientsByPreferences(List<NotificationRecipient> recipients, String notificationType, String channel) {
        return recipients.stream()
            .filter(recipient -> shouldReceiveNotification(recipient, notificationType, channel))
            .collect(Collectors.toList());
    }

    /**
     * Vérifier si un destinataire doit recevoir une notification
     */
    private boolean shouldReceiveNotification(NotificationRecipient recipient, String notificationType, String channel) {
        if (recipient.getPreferences() == null) {
            return true; // Pas de préférences = recevoir toutes les notifications
        }
        
        NotificationPreferences prefs = recipient.getPreferences();
        
        // Vérifier le canal
        if ("EMAIL".equals(channel)) {
            if (!prefs.isEmailEnabled()) {
                return false;
            }
            
            // Vérifier le type de notification
            if ("reminder".equals(notificationType) && !prefs.getEmailTypes().isInvoices()) {
                return false;
            }
            if ("overdue".equals(notificationType) && !prefs.getEmailTypes().isInvoices()) {
                return false;
            }
            if ("convention_deadline".equals(notificationType) && !prefs.getEmailTypes().isConventions()) {
                return false;
            }
            if ("payment_confirmation".equals(notificationType) && !prefs.getEmailTypes().isPayments()) {
                return false;
            }
        }
        
        if ("SMS".equals(channel)) {
            if (!prefs.isSmsEnabled()) {
                return false;
            }
            
            // SMS uniquement pour les urgences
            if ("overdue".equals(notificationType) && !prefs.getSmsTypes().isOverdue()) {
                return false;
            }
            if ("critical".equals(notificationType) && !prefs.getSmsTypes().isUrgent()) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Récupérer les destinataires pour une notification système
     */
    public List<NotificationRecipient> getSystemNotificationRecipients(String notificationType) {
        List<NotificationRecipient> recipients = new ArrayList<>();
        
        try {
            // Les notifications système sont envoyées à tous les utilisateurs actifs
            List<User> allUsers = userRepository.findAll();
            
            for (User user : allUsers) {
                Optional<NotificationPreferences> prefsOpt = preferencesRepository.findByUserId(user.getId());
                
                NotificationRecipient recipient = new NotificationRecipient();
                recipient.setUserId(user.getId());
                recipient.setUsername(user.getUsername());
                recipient.setEmail(user.getEmail());
                recipient.setPhoneNumber(user.getPhoneNumber());
                // Extraire le premier rôle de l'utilisateur
                String role = user.getRoles().isEmpty() ? "USER" : user.getRoles().iterator().next().getName().name();
                recipient.setRole(role);
                recipient.setPrimary(false);
                recipient.setPreferences(prefsOpt.orElse(null));
                
                recipients.add(recipient);
            }
            
        } catch (Exception e) {
            System.err.println("❌ [RECIPIENTS] Erreur récupération destinataires système: " + e.getMessage());
        }
        
        return recipients;
    }
}
