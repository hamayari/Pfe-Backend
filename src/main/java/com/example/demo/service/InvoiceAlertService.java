package com.example.demo.service;

import com.example.demo.model.Invoice;
import com.example.demo.model.KpiAlert;
import com.example.demo.model.User;
import com.example.demo.enums.ERole;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service pour créer des alertes individuelles par facture impayée
 * 1 alerte = 1 facture PENDING ou OVERDUE
 * ✅ Vérification automatique toutes les heures
 */
@Service
public class InvoiceAlertService {
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private KpiAlertManagementService alertManagementService;
    
    @Autowired
    private com.example.demo.repository.KpiAlertRepository kpiAlertRepository;
    
    /**
     * ✅ Vérifier les factures PENDING et créer des alertes individuelles
     * Chaque facture PENDING génère une alerte pour le Décideur
     * 🔄 Exécution automatique toutes les heures
     */
    @Scheduled(fixedRate = 3600000) // Toutes les heures (3600000 ms)
    public List<KpiAlert> checkPendingInvoices() {
        System.out.println("========================================");
        System.out.println("🔍 [INVOICE ALERT] Vérification des factures PENDING");
        System.out.println("========================================");
        
        List<KpiAlert> createdAlerts = new ArrayList<>();
        
        try {
            // Récupérer toutes les factures PENDING
            List<Invoice> pendingInvoices = invoiceRepository.findByStatus("PENDING");
            System.out.println("📊 Factures PENDING trouvées: " + pendingInvoices.size());
            
            if (pendingInvoices.isEmpty()) {
                System.out.println("✅ Aucune facture PENDING");
                System.out.println("========================================");
                return createdAlerts;
            }
            
            // Récupérer les décideurs
            List<User> decisionMakers = userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER);
            
            // Créer une alerte pour chaque facture PENDING
            for (Invoice invoice : pendingInvoices) {
                KpiAlert alert = createAlertForPendingInvoice(invoice, decisionMakers);
                if (alert != null) {
                    createdAlerts.add(alert);
                }
            }
            
            System.out.println("✅ " + createdAlerts.size() + " alertes créées");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification: " + e.getMessage());
            e.printStackTrace();
        }
        
        return createdAlerts;
    }
    
    /**
     * ❌ MÉTHODE OBSOLÈTE - NE PLUS UTILISER
     * Utiliser checkPendingInvoices() à la place
     * @deprecated Remplacé par checkPendingInvoices()
     */
    @Deprecated
    public List<KpiAlert> checkOverdueInvoices() {
        System.out.println("========================================");
        System.out.println("🔍 [INVOICE ALERT] Vérification des factures en retard");
        System.out.println("========================================");
        
        List<KpiAlert> createdAlerts = new ArrayList<>();
        
        try {
            // Récupérer toutes les factures OVERDUE
            List<Invoice> overdueInvoices = invoiceRepository.findByStatus("OVERDUE");
            System.out.println("📊 Factures en retard trouvées: " + overdueInvoices.size());
            
            if (overdueInvoices.isEmpty()) {
                System.out.println("✅ Aucune facture en retard");
                System.out.println("========================================");
                return createdAlerts;
            }
            
            // Récupérer les décideurs
            List<User> decisionMakers = userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER);
            
            // Créer une alerte pour chaque facture en retard
            for (Invoice invoice : overdueInvoices) {
                KpiAlert alert = createAlertForInvoice(invoice, decisionMakers);
                if (alert != null) {
                    createdAlerts.add(alert);
                }
            }
            
            System.out.println("✅ " + createdAlerts.size() + " alertes créées");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification: " + e.getMessage());
            e.printStackTrace();
        }
        
        return createdAlerts;
    }
    
    /**
     * Créer une alerte pour une facture PENDING
     */
    private KpiAlert createAlertForPendingInvoice(Invoice invoice, List<User> decisionMakers) {
        try {
            // ✅ VÉRIFIER SI UNE ALERTE EXISTE DÉJÀ POUR CETTE FACTURE
            List<KpiAlert> existingAlerts = kpiAlertRepository
                .findByRelatedInvoiceId(invoice.getId());
            
            // Filtrer les alertes non envoyées au PM
            List<KpiAlert> pendingAlerts = existingAlerts.stream()
                .filter(a -> "PENDING_DECISION".equals(a.getAlertStatus()))
                .collect(java.util.stream.Collectors.toList());
            
            if (!pendingAlerts.isEmpty()) {
                System.out.println("⚠️ Alerte déjà existante pour la facture " + invoice.getReference() + " - Mise à jour");
                KpiAlert alert = pendingAlerts.get(0);
                
                // Supprimer les doublons
                if (pendingAlerts.size() > 1) {
                    System.out.println("🗑️ Suppression de " + (pendingAlerts.size() - 1) + " doublon(s)");
                    for (int i = 1; i < pendingAlerts.size(); i++) {
                        kpiAlertRepository.delete(pendingAlerts.get(i));
                    }
                }
                
                updateExistingPendingAlert(alert, invoice, decisionMakers);
                return kpiAlertRepository.save(alert);
            }
            
            System.out.println("✅ Création d'une NOUVELLE alerte pour la facture PENDING " + invoice.getReference());
            
            // Créer l'alerte
            KpiAlert alert = new KpiAlert();
            alert.setKpiName("FACTURE_PENDING");
            
            // Informations de la facture
            alert.setDimension("INVOICE");
            alert.setDimensionValue(invoice.getReference());
            
            // Valeurs
            double amount = parseAmount(invoice.getAmount());
            alert.setCurrentValue(amount);
            alert.setThresholdValue(0.0);
            
            // Sévérité basée sur le montant et l'ancienneté
            LocalDate createdAt = invoice.getCreatedAt();
            long daysOld = createdAt != null ? ChronoUnit.DAYS.between(createdAt, LocalDate.now()) : 0;
            
            String severity;
            String priority;
            if (amount > 50000 || daysOld > 30) {
                severity = "HIGH";
                priority = "HIGH";
            } else if (amount > 20000 || daysOld > 14) {
                severity = "MEDIUM";
                priority = "NORMAL";
            } else {
                severity = "LOW";
                priority = "LOW";
            }
            alert.setSeverity(severity);
            alert.setPriority(priority);
            
            // Message détaillé avec toutes les infos de la facture
            String message = String.format(
                "📄 Facture PENDING: %s\n" +
                "💰 Montant: %.2f TND\n" +
                "👤 Client: %s\n" +
                "📧 Email: %s\n" +
                "📅 Date création: %s\n" +
                "📅 Date échéance: %s\n" +
                "⏳ Ancienneté: %d jours",
                invoice.getReference(),
                amount,
                invoice.getClientId() != null ? invoice.getClientId() : "N/A",
                invoice.getClientEmail() != null ? invoice.getClientEmail() : "N/A",
                invoice.getIssueDate() != null ? invoice.getIssueDate().toString() : "N/A",
                invoice.getDueDate() != null ? invoice.getDueDate().toString() : "N/A",
                daysOld
            );
            alert.setMessage(message);
            
            // Recommandation
            String recommendation = String.format(
                "Actions recommandées:\n" +
                "1. Vérifier le statut de la facture avec le commercial\n" +
                "2. Contacter le client pour confirmer la réception\n" +
                "3. Relancer si nécessaire\n" +
                "4. Déléguer au Chef de Projet pour suivi"
            );
            alert.setRecommendation(recommendation);
            
            // Statut initial
            alert.setAlertStatus("PENDING_DECISION");
            alert.setStatus("🟡 EN ATTENTE");
            
            // Destinataires (Décideurs uniquement)
            List<String> recipients = new ArrayList<>();
            for (User dm : decisionMakers) {
                recipients.add(dm.getId());
            }
            alert.setRecipients(recipients);
            
            // Lien direct vers la facture
            alert.setRelatedInvoiceId(invoice.getId());
            
            // Métadonnées complètes de la facture
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("invoiceId", invoice.getId());
            metadata.put("invoiceNumber", invoice.getInvoiceNumber());
            metadata.put("reference", invoice.getReference());
            metadata.put("clientId", invoice.getClientId());
            metadata.put("clientEmail", invoice.getClientEmail());
            metadata.put("amount", amount);
            metadata.put("dueDate", invoice.getDueDate() != null ? invoice.getDueDate().toString() : null);
            metadata.put("issueDate", invoice.getIssueDate() != null ? invoice.getIssueDate().toString() : null);
            metadata.put("createdBy", invoice.getCreatedBy());
            metadata.put("status", invoice.getStatus());
            metadata.put("paymentMethod", invoice.getPaymentMethod());
            metadata.put("daysOld", daysOld);
            alert.setMetadata(metadata);
            
            // Sauvegarder l'alerte
            KpiAlert savedAlert = alertManagementService.createAlert(alert, "system");
            
            System.out.println(String.format(
                "✅ Alerte PENDING créée: %s - %.2f TND - %d jours",
                invoice.getReference(),
                amount,
                daysOld
            ));
            
            return savedAlert;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur création alerte PENDING pour facture " + invoice.getReference() + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Créer une alerte pour une facture spécifique (OVERDUE)
     */
    private KpiAlert createAlertForInvoice(Invoice invoice, List<User> decisionMakers) {
        try {
            // ✅ VÉRIFIER SI UNE ALERTE EXISTE DÉJÀ POUR CETTE FACTURE
            List<KpiAlert> existingAlerts = kpiAlertRepository
                .findByRelatedInvoiceId(invoice.getId());
            
            // Filtrer les alertes non envoyées au PM
            List<KpiAlert> pendingAlerts = existingAlerts.stream()
                .filter(a -> "PENDING_DECISION".equals(a.getAlertStatus()))
                .collect(java.util.stream.Collectors.toList());
            
            if (!pendingAlerts.isEmpty()) {
                System.out.println("⚠️ Alerte déjà existante pour la facture " + invoice.getReference() + " - Mise à jour");
                KpiAlert alert = pendingAlerts.get(0); // Prendre la première
                
                // Supprimer les doublons s'il y en a
                if (pendingAlerts.size() > 1) {
                    System.out.println("🗑️ Suppression de " + (pendingAlerts.size() - 1) + " doublon(s)");
                    for (int i = 1; i < pendingAlerts.size(); i++) {
                        kpiAlertRepository.delete(pendingAlerts.get(i));
                    }
                }
                
                // Mettre à jour l'alerte existante au lieu d'en créer une nouvelle
                updateExistingAlert(alert, invoice, decisionMakers);
                return kpiAlertRepository.save(alert);
            }
            
            // Calculer le nombre de jours de retard
            LocalDate dueDate = invoice.getDueDate();
            LocalDate today = LocalDate.now();
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, today);
            
            System.out.println("✅ Création d'une NOUVELLE alerte pour la facture " + invoice.getReference());
            
            // Créer l'alerte
            KpiAlert alert = new KpiAlert();
            alert.setKpiName("FACTURE_IMPAYEE");
            
            // Informations de la facture
            alert.setDimension("INVOICE");
            alert.setDimensionValue(invoice.getReference());
            
            // Valeurs
            double amount = parseAmount(invoice.getAmount());
            alert.setCurrentValue(amount);
            alert.setThresholdValue(0.0); // Seuil = 0 jours de retard
            
            // Sévérité basée sur le nombre de jours de retard
            String severity;
            String priority;
            if (daysOverdue > 60) {
                severity = "CRITICAL";
                priority = "URGENT";
            } else if (daysOverdue > 30) {
                severity = "HIGH";
                priority = "HIGH";
            } else {
                severity = "MEDIUM";
                priority = "NORMAL";
            }
            alert.setSeverity(severity);
            alert.setPriority(priority);
            
            // Message détaillé
            String message = String.format(
                "Facture %s en retard de %d jours - Montant: %.2f TND - Client: %s",
                invoice.getReference(),
                daysOverdue,
                amount,
                invoice.getClientId() != null ? invoice.getClientId() : "N/A"
            );
            alert.setMessage(message);
            
            // Recommandation
            String recommendation = generateRecommendation(invoice, daysOverdue);
            alert.setRecommendation(recommendation);
            
            // Statut initial
            alert.setAlertStatus("PENDING_DECISION");
            alert.setStatus("🔴 EN RETARD");
            
            // Destinataires (Décideurs)
            List<String> recipients = new ArrayList<>();
            for (User dm : decisionMakers) {
                recipients.add(dm.getId());
            }
            alert.setRecipients(recipients);
            
            // Lien direct vers la facture
            alert.setRelatedInvoiceId(invoice.getId());
            
            // Métadonnées de la facture (pour affichage détaillé)
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("invoiceId", invoice.getId());
            metadata.put("invoiceNumber", invoice.getInvoiceNumber());
            metadata.put("reference", invoice.getReference());
            metadata.put("clientId", invoice.getClientId());
            metadata.put("clientEmail", invoice.getClientEmail());
            metadata.put("amount", amount);
            metadata.put("dueDate", dueDate.toString());
            metadata.put("daysOverdue", daysOverdue);
            metadata.put("issueDate", invoice.getIssueDate() != null ? invoice.getIssueDate().toString() : null);
            metadata.put("createdBy", invoice.getCreatedBy());
            metadata.put("status", invoice.getStatus());
            metadata.put("paymentMethod", invoice.getPaymentMethod());
            alert.setMetadata(metadata);
            
            // Sauvegarder l'alerte
            KpiAlert savedAlert = alertManagementService.createAlert(alert, "system");
            
            System.out.println(String.format(
                "✅ Alerte créée: %s - %d jours de retard - %.2f TND",
                invoice.getReference(),
                daysOverdue,
                amount
            ));
            
            return savedAlert;
            
        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
            // Doublons détectés - nettoyer et réessayer
            System.err.println("⚠️ Doublons détectés pour facture " + invoice.getReference() + " - Nettoyage...");
            List<KpiAlert> allAlerts = kpiAlertRepository.findByRelatedInvoiceId(invoice.getId());
            if (allAlerts.size() > 1) {
                // Garder la plus récente, supprimer les autres
                allAlerts.sort((a, b) -> b.getDetectedAt().compareTo(a.getDetectedAt()));
                for (int i = 1; i < allAlerts.size(); i++) {
                    kpiAlertRepository.delete(allAlerts.get(i));
                }
                System.out.println("✅ " + (allAlerts.size() - 1) + " doublon(s) supprimé(s)");
                // Mettre à jour l'alerte restante
                updateExistingAlert(allAlerts.get(0), invoice, decisionMakers);
                return kpiAlertRepository.save(allAlerts.get(0));
            }
            return null;
        } catch (Exception e) {
            System.err.println("❌ Erreur création alerte pour facture " + invoice.getReference() + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Générer une recommandation basée sur le retard
     */
    private String generateRecommendation(Invoice invoice, long daysOverdue) {
        if (daysOverdue > 60) {
            return String.format(
                "URGENT: Facture en retard de %d jours. Actions recommandées:\n" +
                "1. Contact immédiat du client\n" +
                "2. Mise en demeure si nécessaire\n" +
                "3. Envisager une procédure de recouvrement\n" +
                "4. Bloquer les nouvelles commandes",
                daysOverdue
            );
        } else if (daysOverdue > 30) {
            return String.format(
                "Facture en retard de %d jours. Actions recommandées:\n" +
                "1. Relancer le client par téléphone\n" +
                "2. Envoyer un rappel formel par email\n" +
                "3. Proposer un échéancier de paiement\n" +
                "4. Suivre quotidiennement",
                daysOverdue
            );
        } else {
            return String.format(
                "Facture en retard de %d jours. Actions recommandées:\n" +
                "1. Envoyer un rappel amical au client\n" +
                "2. Vérifier si le paiement est en cours\n" +
                "3. Confirmer les coordonnées bancaires\n" +
                "4. Planifier un suivi dans 7 jours",
                daysOverdue
            );
        }
    }
    
    /**
     * Mettre à jour une alerte existante pour facture PENDING
     */
    private void updateExistingPendingAlert(KpiAlert alert, Invoice invoice, List<User> decisionMakers) {
        // Recalculer l'ancienneté
        LocalDate createdAt = invoice.getCreatedAt();
        long daysOld = createdAt != null ? ChronoUnit.DAYS.between(createdAt, LocalDate.now()) : 0;
        
        // Mettre à jour la sévérité
        double amount = parseAmount(invoice.getAmount());
        String severity;
        if (amount > 50000 || daysOld > 30) {
            severity = "HIGH";
        } else if (amount > 20000 || daysOld > 14) {
            severity = "MEDIUM";
        } else {
            severity = "LOW";
        }
        alert.setSeverity(severity);
        
        // Mettre à jour le message
        String message = String.format(
            "📄 Facture PENDING: %s\n" +
            "💰 Montant: %.2f TND\n" +
            "👤 Client: %s\n" +
            "📧 Email: %s\n" +
            "📅 Date création: %s\n" +
            "📅 Date échéance: %s\n" +
            "⏳ Ancienneté: %d jours",
            invoice.getReference(),
            amount,
            invoice.getClientId() != null ? invoice.getClientId() : "N/A",
            invoice.getClientEmail() != null ? invoice.getClientEmail() : "N/A",
            invoice.getIssueDate() != null ? invoice.getIssueDate().toString() : "N/A",
            invoice.getDueDate() != null ? invoice.getDueDate().toString() : "N/A",
            daysOld
        );
        alert.setMessage(message);
        
        System.out.println("✅ Alerte PENDING mise à jour pour facture " + invoice.getReference());
    }
    
    /**
     * Mettre à jour une alerte existante
     */
    private void updateExistingAlert(KpiAlert alert, Invoice invoice, List<User> decisionMakers) {
        // Recalculer les jours de retard
        LocalDate dueDate = invoice.getDueDate();
        LocalDate today = LocalDate.now();
        long daysOverdue = ChronoUnit.DAYS.between(dueDate, today);
        
        // Mettre à jour la sévérité
        String severity;
        if (daysOverdue > 60) {
            severity = "CRITICAL";
        } else if (daysOverdue > 30) {
            severity = "HIGH";
        } else {
            severity = "MEDIUM";
        }
        alert.setSeverity(severity);
        
        // Mettre à jour le message
        double amount = parseAmount(invoice.getAmount());
        String message = String.format(
            "Facture %s en retard de %d jours - Montant: %.2f TND - Client: %s",
            invoice.getReference(),
            daysOverdue,
            amount,
            invoice.getClientId() != null ? invoice.getClientId() : "N/A"
        );
        alert.setMessage(message);
        
        System.out.println("✅ Alerte mise à jour pour facture " + invoice.getReference());
    }
    
    /**
     * Parser le montant (String ou Double)
     */
    private double parseAmount(Object amount) {
        if (amount == null) return 0.0;
        if (amount instanceof Number) {
            return ((Number) amount).doubleValue();
        }
        try {
            return Double.parseDouble(amount.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    /**
     * Compter le nombre de factures OVERDUE
     */
    public long countOverdueInvoices() {
        return invoiceRepository.findByStatus("OVERDUE").size();
    }
}
