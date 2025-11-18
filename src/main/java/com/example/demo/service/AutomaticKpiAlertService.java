package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.model.Invoice;
import com.example.demo.model.Convention;
import com.example.demo.model.KpiAlert;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.KpiAlertRepository;
import com.example.demo.enums.ERole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service de détection automatique des anomalies KPI
 * ⚠️ DÉSACTIVÉ pour les taux - Utiliser InvoiceAlertService pour les factures PENDING
 */
@Service
public class AutomaticKpiAlertService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private ConventionRepository conventionRepository;
    
    @Autowired(required = false)
    private InAppNotificationService notificationService;
    
    @Autowired
    private KpiAlertRepository kpiAlertRepository;
    
    @Autowired
    private KpiAlertManagementService kpiAlertManagementService;
    
    @Autowired(required = false)
    private KpiAlertEmailService emailService;
    
    @Autowired(required = false)
    private KpiAlertSmsService smsService;
    
    // Seuils de référence pour chaque KPI
    private static final Map<String, KpiThreshold> KPI_THRESHOLDS = new HashMap<>();
    
    static {
        // ✅ ALERTES SUR LES TAUX RÉACTIVÉES
        // Ces alertes coexistent avec les alertes individuelles sur les factures PENDING
        
        // Taux de retard
        KPI_THRESHOLDS.put("TAUX_RETARD", new KpiThreshold(10.0, 15.0, "Taux de factures en retard"));
        
        // Montant impayé
        KPI_THRESHOLDS.put("MONTANT_IMPAYE", new KpiThreshold(20000.0, 30000.0, "Montant total impayé"));
        
        // Taux de régularisation
        KPI_THRESHOLDS.put("TAUX_REGULARISATION", new KpiThreshold(70.0, 60.0, "Taux de régularisation"));
        
        // Délai moyen de paiement
        KPI_THRESHOLDS.put("DELAI_PAIEMENT", new KpiThreshold(30.0, 45.0, "Délai moyen de paiement"));
        
        // Taux de conversion
        KPI_THRESHOLDS.put("TAUX_CONVERSION", new KpiThreshold(15.0, 12.0, "Taux de conversion"));
    }
    
    
    /**
     * ❌ DÉSACTIVÉ - Ne plus vérifier les anomalies de taux automatiquement
     * Utiliser InvoiceAlertService.checkPendingInvoices() à la place
     */
    @Deprecated
    public void checkKpiAnomalies() {
        System.out.println("========================================");
        System.out.println("🔍 [AUTO KPI] Vérification automatique des KPI");
        System.out.println("⏰ Heure: " + LocalDateTime.now());
        System.out.println("========================================");
        
        try {
            // Simuler la récupération des KPI actuels
            Map<String, Double> currentKpis = getCurrentKpiValues();
            
            List<KpiAnomaly> anomalies = new ArrayList<>();
            
            // Vérifier chaque KPI
            for (Map.Entry<String, Double> entry : currentKpis.entrySet()) {
                String kpiName = entry.getKey();
                Double currentValue = entry.getValue();
                KpiThreshold threshold = KPI_THRESHOLDS.get(kpiName);
                
                if (threshold != null) {
                    KpiAnomaly anomaly = checkThreshold(kpiName, currentValue, threshold);
                    if (anomaly != null) {
                        anomalies.add(anomaly);
                        System.out.println("🚨 Anomalie détectée: " + anomaly.kpiName + " = " + currentValue);
                    }
                }
            }
            
            // Si des anomalies sont détectées, notifier le Chef de Projet
            if (!anomalies.isEmpty()) {
                System.out.println("📢 [AUTO KPI] " + anomalies.size() + " anomalie(s) détectée(s)");
                notifyProjectManager(anomalies);
            } else {
                System.out.println("✅ [AUTO KPI] Tous les KPI sont normaux");
            }
            
        } catch (Exception e) {
            System.err.println("❌ [AUTO KPI] Erreur: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("========================================");
    }
    
    /**
     * Récupérer les valeurs actuelles des KPI depuis la base de données
     */
    private Map<String, Double> getCurrentKpiValues() {
        Map<String, Double> kpis = new HashMap<>();
        
        try {
            // 1. TAUX DE RETARD - Calculer depuis les factures
            long totalInvoices = invoiceRepository.count();
            List<Invoice> overdueInvoicesList = invoiceRepository.findByStatus("OVERDUE");
            long overdueInvoices = overdueInvoicesList.size();
            double tauxRetard = totalInvoices > 0 ? (overdueInvoices * 100.0) / totalInvoices : 0.0;
            kpis.put("TAUX_RETARD", tauxRetard);
            System.out.println("📊 Taux de retard calculé: " + String.format("%.1f%%", tauxRetard) + 
                             " (" + overdueInvoices + "/" + totalInvoices + ")");
            
            // 2. MONTANT IMPAYÉ - Somme des factures non payées
            List<Invoice> pendingInvoices = invoiceRepository.findByStatus("PENDING");
            List<Invoice> sentInvoices = invoiceRepository.findByStatus("SENT");
            List<Invoice> draftInvoices = invoiceRepository.findByStatus("DRAFT");
            
            double montantImpaye = 0.0;
            for (Invoice inv : pendingInvoices) {
                montantImpaye += parseAmount(inv.getAmount());
            }
            for (Invoice inv : sentInvoices) {
                montantImpaye += parseAmount(inv.getAmount());
            }
            for (Invoice inv : overdueInvoicesList) {
                montantImpaye += parseAmount(inv.getAmount());
            }
            for (Invoice inv : draftInvoices) {
                montantImpaye += parseAmount(inv.getAmount());
            }
            
            kpis.put("MONTANT_IMPAYE", montantImpaye);
            System.out.println("💰 Montant impayé calculé: " + String.format("%.2f TND", montantImpaye));
            
            // 3. TAUX DE RÉGULARISATION - Factures payées / total
            List<Invoice> paidInvoicesList = invoiceRepository.findByStatus("PAID");
            long paidInvoices = paidInvoicesList.size();
            double tauxRegul = totalInvoices > 0 ? (paidInvoices * 100.0) / totalInvoices : 0.0;
            kpis.put("TAUX_REGULARISATION", tauxRegul);
            System.out.println("✅ Taux de régularisation calculé: " + String.format("%.1f%%", tauxRegul));
            
            // 4. DÉLAI MOYEN DE PAIEMENT - Calculer depuis les factures payées
            double avgDelai = paidInvoicesList.stream()
                .filter(inv -> inv.getCreatedAt() != null && inv.getUpdatedAt() != null)
                .mapToLong(inv -> ChronoUnit.DAYS.between(inv.getCreatedAt(), inv.getUpdatedAt()))
                .average()
                .orElse(0.0);
            kpis.put("DELAI_PAIEMENT", avgDelai);
            System.out.println("⏱️ Délai moyen de paiement calculé: " + String.format("%.1f jours", avgDelai));
            
            // 5. TAUX DE CONVERSION - Conventions signées / total
            long totalConventions = conventionRepository.count();
            long signedConventions = conventionRepository.countByStatus("SIGNED");
            double tauxConversion = totalConventions > 0 ? (signedConventions * 100.0) / totalConventions : 0.0;
            kpis.put("TAUX_CONVERSION", tauxConversion);
            System.out.println("📈 Taux de conversion calculé: " + String.format("%.1f%%", tauxConversion));
            
        } catch (Exception e) {
            System.err.println("❌ Erreur calcul KPI: " + e.getMessage());
            e.printStackTrace();
            // En cas d'erreur, retourner des valeurs par défaut
            kpis.put("TAUX_RETARD", 0.0);
            kpis.put("MONTANT_IMPAYE", 0.0);
            kpis.put("TAUX_REGULARISATION", 100.0);
            kpis.put("DELAI_PAIEMENT", 0.0);
            kpis.put("TAUX_CONVERSION", 0.0);
        }
        
        return kpis;
    }
    
    /**
     * Vérifier si un KPI dépasse son seuil
     */
    private KpiAnomaly checkThreshold(String kpiName, Double currentValue, KpiThreshold threshold) {
        String severity = null;
        String message = null;
        
        // Vérifier dépassement seuil critique (HIGH)
        if (threshold.isHigherBetter) {
            if (currentValue < threshold.criticalThreshold) {
                severity = "HIGH";
                message = String.format("%s est tombé à %.1f%%, en dessous du seuil critique de %.1f%%", 
                    threshold.displayName, currentValue, threshold.criticalThreshold);
            } else if (currentValue < threshold.warningThreshold) {
                severity = "MEDIUM";
                message = String.format("%s est à %.1f%%, en dessous du seuil de %.1f%%", 
                    threshold.displayName, currentValue, threshold.warningThreshold);
            }
        } else {
            if (currentValue > threshold.criticalThreshold) {
                severity = "HIGH";
                message = String.format("%s a atteint %.1f%%, au-dessus du seuil critique de %.1f%%", 
                    threshold.displayName, currentValue, threshold.criticalThreshold);
            } else if (currentValue > threshold.warningThreshold) {
                severity = "MEDIUM";
                message = String.format("%s est à %.1f%%, au-dessus du seuil de %.1f%%", 
                    threshold.displayName, currentValue, threshold.warningThreshold);
            }
        }
        
        if (severity != null) {
            return new KpiAnomaly(kpiName, threshold.displayName, currentValue, severity, message);
        }
        
        return null;
    }
    
    /**
     * Notifier automatiquement le Décideur (pas le Chef de Projet)
     * Le Décideur pourra ensuite déléguer au Chef de Projet si nécessaire
     */
    private void notifyProjectManager(List<KpiAnomaly> anomalies) {
        System.out.println("========================================");
        System.out.println("📨 [AUTO NOTIFICATION] Envoi au Décideur (en attente de décision)");
        
        // Trouver tous les Chefs de Projet
        List<User> projectManagers = userRepository.findByRoles_Name(ERole.ROLE_PROJECT_MANAGER);
        
        if (projectManagers.isEmpty()) {
            System.out.println("⚠️ Aucun Chef de Projet trouvé");
            return;
        }
        
        for (KpiAnomaly anomaly : anomalies) {
            // 1. SAUVEGARDER L'ALERTE DANS MONGODB
            KpiAlert alert = new KpiAlert();
            alert.setKpiName(anomaly.kpiName);
            alert.setCurrentValue(anomaly.currentValue);
            alert.setSeverity(anomaly.severity);
            alert.setStatus(anomaly.severity.equals("HIGH") ? "🔴 ANORMAL" : "🟡 A_SURVEILLER");
            alert.setAlertStatus("PENDING_DECISION"); // Statut initial: en attente de décision
            alert.setDimension("GLOBAL");
            alert.setDimensionValue("all");
            alert.setMessage(anomaly.message);
            alert.setRecommendation(generateRecommendation(anomaly));
            alert.setPriority(anomaly.severity.equals("HIGH") ? "CRITICAL" : "HIGH");
            
            // ⚠️ IMPORTANT: Les alertes vont UNIQUEMENT au Décideur d'abord
            // Le Décideur pourra ensuite les déléguer au Chef de Projet
            List<String> recipients = new ArrayList<>();
            
            // Ajouter UNIQUEMENT les décideurs
            List<User> decisionMakers = userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER);
            for (User dm : decisionMakers) {
                recipients.add(dm.getId());
            }
            
            alert.setRecipients(recipients);
            
            System.out.println("📋 Destinataires: " + recipients.size() + " Décideur(s) uniquement");
            
            // Sauvegarder avec traçabilité
            KpiAlert savedAlert = kpiAlertManagementService.createAlert(alert, "system");
            System.out.println("💾 Alerte sauvegardée dans MongoDB: " + savedAlert.getId());
            
            // 2. Créer la notification WebSocket
            Map<String, Object> notification = new HashMap<>();
            notification.put("id", savedAlert.getId()); // Utiliser l'ID de MongoDB
            notification.put("type", "KPI_ALERT");
            notification.put("kpiName", anomaly.displayName);
            notification.put("status", "CRITICAL");
            notification.put("severity", anomaly.severity);
            notification.put("message", anomaly.message);
            notification.put("recommendation", generateRecommendation(anomaly));
            notification.put("dimension", "global");
            notification.put("dimensionValue", "all");
            notification.put("timestamp", LocalDateTime.now().toString());
            notification.put("currentValue", anomaly.currentValue);
            notification.put("autoDetected", true);
            notification.put("alertId", savedAlert.getId()); // Lien vers l'alerte MongoDB
            
            // 3. Envoyer au topic général
            messagingTemplate.convertAndSend("/topic/kpi-alerts", notification);
            System.out.println("✅ Notification envoyée au topic général");
            
            // ⚠️ IMPORTANT: Envoyer UNIQUEMENT aux Décideurs (pas aux Chefs de Projet)
            // Les Chefs de Projet recevront les alertes SEULEMENT si le Décideur les délègue
            // Réutiliser la variable decisionMakers déjà déclarée plus haut
            
            for (User dm : decisionMakers) {
                // Envoyer notification WebSocket personnelle
                messagingTemplate.convertAndSendToUser(
                    dm.getUsername(),
                    "/queue/kpi-alerts",
                    notification
                );
                System.out.println("✅ Notification personnelle envoyée au Décideur: " + dm.getUsername());
                
                // Créer aussi une notification in-app persistante
                if (notificationService != null) {
                    try {
                        notificationService.createNotification(
                            dm.getId(),
                            anomaly.severity.equals("HIGH") ? "KPI_ALERT" : "KPI_WARNING",
                            "🚨 Alerte KPI Automatique: " + anomaly.displayName,
                            anomaly.message,
                            anomaly.severity.equals("HIGH") ? "HIGH" : "MEDIUM",
                            "KPI_ALERT"
                        );
                        System.out.println("✅ Notification in-app créée pour le Décideur");
                    } catch (Exception e) {
                        System.err.println("❌ Erreur notification interne: " + e.getMessage());
                    }
                }
                
                // Envoyer Email si service disponible
                if (emailService != null && dm.getEmail() != null) {
                    try {
                        emailService.sendKpiAlertEmail(
                            dm.getEmail(),
                            dm.getName(),
                            anomaly.displayName,
                            anomaly.message,
                            generateRecommendation(anomaly),
                            anomaly.severity,
                            alert.getPriority(),
                            savedAlert.getId()
                        );
                        System.out.println("✅ Email envoyé à: " + dm.getEmail());
                    } catch (Exception e) {
                        System.err.println("❌ Erreur envoi email: " + e.getMessage());
                    }
                }
                
                // Envoyer SMS si service disponible et alerte critique
                if (smsService != null && dm.getPhoneNumber() != null && anomaly.severity.equals("HIGH")) {
                    try {
                        smsService.sendKpiAlertSms(
                            dm.getPhoneNumber(),
                            dm.getName(),
                            anomaly.displayName,
                            anomaly.severity,
                            alert.getPriority(),
                            anomaly.currentValue
                        );
                        System.out.println("✅ SMS envoyé à: " + dm.getPhoneNumber());
                    } catch (Exception e) {
                        System.err.println("❌ Erreur envoi SMS: " + e.getMessage());
                    }
                }
            }
            
            System.out.println("📊 Alerte envoyée à " + decisionMakers.size() + " Décideur(s)");
        }
        
        System.out.println("========================================");
    }
    
    /**
     * Générer une recommandation selon le type d'anomalie
     */
    private String generateRecommendation(KpiAnomaly anomaly) {
        switch (anomaly.kpiName) {
            case "TAUX_RETARD":
                return "Contacter immédiatement les clients avec factures en retard. Analyser les causes du retard.";
            case "MONTANT_IMPAYE":
                return "Prioriser le recouvrement des créances. Envoyer des rappels de paiement.";
            case "TAUX_REGULARISATION":
                return "Accélérer le processus de régularisation. Identifier les blocages.";
            case "DELAI_PAIEMENT":
                return "Négocier des délais de paiement plus courts avec les clients.";
            case "TAUX_CONVERSION":
                return "Analyser les causes de la baisse. Former l'équipe commerciale.";
            default:
                return "Analyser la situation et mettre en place un plan d'action correctif.";
        }
    }
    
    /**
     * Classe interne pour les seuils KPI
     */
    private static class KpiThreshold {
        double warningThreshold;
        double criticalThreshold;
        String displayName;
        boolean isHigherBetter;
        
        KpiThreshold(double warningThreshold, double criticalThreshold, String displayName) {
            this.warningThreshold = warningThreshold;
            this.criticalThreshold = criticalThreshold;
            this.displayName = displayName;
            // Si criticalThreshold < warningThreshold, alors plus c'est haut, mieux c'est
            this.isHigherBetter = criticalThreshold < warningThreshold;
        }
    }
    
    /**
     * Parser le montant d'une facture (BigDecimal vers double)
     */
    private double parseAmount(java.math.BigDecimal amount) {
        if (amount == null) {
            return 0.0;
        }
        try {
            return amount.doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    /**
     * Classe interne pour les anomalies détectées
     */
    private static class KpiAnomaly {
        String kpiName;
        String displayName;
        Double currentValue;
        String severity;
        String message;
        
        KpiAnomaly(String kpiName, String displayName, Double currentValue, String severity, String message) {
            this.kpiName = kpiName;
            this.displayName = displayName;
            this.currentValue = currentValue;
            this.severity = severity;
            this.message = message;
        }
    }
    
    /**
     * Compter le nombre total d'alertes
     */
    public long countAlerts() {
        return kpiAlertRepository.count();
    }
    
    /**
     * Compter les alertes par dimension
     */
    public long countAlertsByDimension(String dimension) {
        return kpiAlertRepository.findByDimension(dimension).size();
    }
    
    /**
     * Déléguer une alerte du Décideur au Chef de Projet
     */
    public boolean delegateAlertToProjectManager(String alertId) {
        try {
            // Récupérer l'alerte
            Optional<KpiAlert> alertOpt = kpiAlertRepository.findById(alertId);
            if (alertOpt.isEmpty()) {
                System.err.println("❌ Alerte non trouvée: " + alertId);
                return false;
            }
            
            KpiAlert alert = alertOpt.get();
            
            System.out.println("📤 Délégation alerte: " + alert.getKpiName());
            System.out.println("   De: DECISION_MAKER → À: PROJECT_MANAGER");
            
            // Garder le statut PENDING_DECISION pour que le Chef de Projet puisse la voir
            // Mais ajouter une note dans le message
            String originalMessage = alert.getMessage();
            alert.setMessage("🔄 [Délégué par le Décideur] " + originalMessage);
            
            // Mettre à jour les destinataires (seulement les Chefs de Projet)
            List<User> projectManagers = userRepository.findByRoles_Name(ERole.ROLE_PROJECT_MANAGER);
            List<String> pmIds = new ArrayList<>();
            for (User pm : projectManagers) {
                pmIds.add(pm.getId());
            }
            alert.setRecipients(pmIds);
            
            // Sauvegarder
            kpiAlertRepository.save(alert);
            
            // Créer une notification in-app pour chaque Chef de Projet
            if (notificationService != null) {
                for (User pm : projectManagers) {
                    try {
                        // Créer une notification persistante dans MongoDB
                        notificationService.createNotification(
                            pm.getId(),
                            "ALERT_DELEGATED",
                            "🔄 Alerte KPI Déléguée",
                            "Le Décideur vous a délégué une alerte: " + alert.getKpiName() + " - " + originalMessage,
                            alert.getSeverity().equals("HIGH") ? "HIGH" : "MEDIUM",
                            "KPI_ALERT"
                        );
                        
                        System.out.println("✅ Notification in-app créée pour: " + pm.getUsername());
                    } catch (Exception e) {
                        System.err.println("❌ Erreur création notification in-app: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            
            // Envoyer notification WebSocket
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "ALERT_DELEGATED");
            notification.put("alertId", alertId);
            notification.put("kpiName", alert.getKpiName());
            notification.put("message", "Nouvelle alerte déléguée par le Décideur");
            notification.put("timestamp", LocalDateTime.now().toString());
            
            messagingTemplate.convertAndSend("/topic/kpi-alerts", notification);
            
            System.out.println("✅ Alerte déléguée avec succès");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur délégation alerte: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
