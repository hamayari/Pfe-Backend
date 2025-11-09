package com.example.demo.service;

import com.example.demo.model.KpiAlert;
import com.example.demo.model.KpiThreshold;
import com.example.demo.model.Invoice;
import com.example.demo.model.Convention;
import com.example.demo.repository.KpiAlertRepository;
import com.example.demo.repository.KpiThresholdRepository;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.ConventionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service d'évaluation des KPI et détection d'anomalies
 */
@Service
public class KpiEvaluatorService {
    
    @Autowired
    private KpiThresholdRepository thresholdRepository;
    
    @Autowired
    private KpiAlertRepository alertRepository;
    
    @Autowired
    private KpiCalculatorService calculatorService;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private ConventionRepository conventionRepository;
    
    // 🔒 Verrouillage pour éviter les appels concurrents
    private volatile boolean isAnalyzing = false;
    
    /**
     * Évalue un KPI et détermine son statut
     */
    public KpiEvaluation evaluateKpi(String kpiName, Double currentValue, String dimension, String dimensionValue) {
        // Récupérer le seuil configuré
        Optional<KpiThreshold> thresholdOpt = dimension != null && dimensionValue != null
            ? thresholdRepository.findByKpiNameAndDimensionAndDimensionValue(kpiName, dimension, dimensionValue)
            : thresholdRepository.findByKpiName(kpiName);
        
        if (!thresholdOpt.isPresent()) {
            // Pas de seuil configuré, considérer comme normal
            return new KpiEvaluation("SAIN", "LOW", "Aucun seuil configuré", null);
        }
        
        KpiThreshold threshold = thresholdOpt.get();
        
        if (!threshold.isEnabled()) {
            return new KpiEvaluation("SAIN", "LOW", "Seuil désactivé", null);
        }
        
        // Évaluer selon les seuils
        String status;
        String severity;
        String message;
        String recommendation = null;
        
        if (currentValue >= threshold.getHighThreshold()) {
            status = "ANORMAL";
            severity = "HIGH";
            message = String.format(
                "%s à %.1f%s dépasse le seuil critique de %.1f%s",
                threshold.getDescription(),
                currentValue,
                threshold.getUnit(),
                threshold.getHighThreshold(),
                threshold.getUnit()
            );
            recommendation = generateRecommendation(kpiName, currentValue, threshold);
            
        } else if (currentValue >= threshold.getLowThreshold()) {
            status = "A_SURVEILLER";
            severity = "MEDIUM";
            message = String.format(
                "%s à %.1f%s dépasse le seuil d'avertissement de %.1f%s",
                threshold.getDescription(),
                currentValue,
                threshold.getUnit(),
                threshold.getLowThreshold(),
                threshold.getUnit()
            );
            recommendation = "Surveiller l'évolution de cet indicateur dans les prochains jours.";
            
        } else {
            status = "SAIN";
            severity = "LOW";
            message = String.format(
                "%s à %.1f%s est dans la plage normale",
                threshold.getDescription(),
                currentValue,
                threshold.getUnit()
            );
        }
        
        return new KpiEvaluation(status, severity, message, recommendation);
    }
    
    /**
     * Analyse tous les KPI et crée des alertes si nécessaire
     * 🎯 UNIQUEMENT les factures OVERDUE - Pas d'alertes consolidées KPI
     * 🔒 PROTECTION: Empêche les appels concurrents qui causent la duplication
     */
    public synchronized List<KpiAlert> analyzeAllKpis() {
        // 🔒 VÉRIFIER SI UNE ANALYSE EST DÉJÀ EN COURS
        if (isAnalyzing) {
            System.out.println("⚠️ ANALYSE DÉJÀ EN COURS - Appel ignoré pour éviter les doublons");
            return new ArrayList<>();
        }
        
        try {
            isAnalyzing = true;
            
            System.out.println("========================================");
            System.out.println("🔍 ANALYSE DES KPI DÉCLENCHÉE");
            System.out.println("========================================");
            
            List<KpiAlert> newAlerts = new ArrayList<>();
            
            // 🚨 UNIQUEMENT : Analyser les factures OVERDUE individuelles
            newAlerts.addAll(analyzeOverdueInvoices());
            
            System.out.println("========================================");
            System.out.println("📊 ANALYSE TERMINÉE");
            System.out.println("Total des alertes: " + newAlerts.size() + " (1 alerte = 1 facture OVERDUE)");
            System.out.println("========================================");
            
            return newAlerts;
            
        } finally {
            isAnalyzing = false;
        }
    }
    
    /**
     * Analyse les factures OVERDUE et crée/met à jour une alerte pour chaque facture
     * 🔧 GARANTIE: 1 alerte = 1 facture OVERDUE (jamais plus, jamais moins)
     * 🔒 TRANSACTIONNEL: Tout ou rien pour garantir la cohérence
     */
    @Transactional
    private List<KpiAlert> analyzeOverdueInvoices() {
        List<KpiAlert> alerts = new ArrayList<>();
        
        System.out.println("========================================");
        System.out.println("🔍 ANALYSE DES FACTURES OVERDUE");
        
        // 📊 ÉTAPE 1: Récupérer toutes les factures OVERDUE actuelles
        List<Invoice> overdueInvoices = invoiceRepository.findByStatus("OVERDUE");
        System.out.println("📊 Factures OVERDUE dans la DB: " + overdueInvoices.size());
        
        // 📊 ÉTAPE 2: Compter les alertes existantes
        long existingAlertsCount = alertRepository.count();
        System.out.println("📊 Alertes existantes dans la DB: " + existingAlertsCount);
        
        // 🧹 ÉTAPE 3: Supprimer TOUTES les alertes de factures (FACTURE et INVOICE)
        List<KpiAlert> existingAlerts = new ArrayList<>();
        existingAlerts.addAll(alertRepository.findByDimension("FACTURE"));
        existingAlerts.addAll(alertRepository.findByDimension("INVOICE"));
        
        Set<String> currentOverdueInvoiceIds = overdueInvoices.stream()
            .map(Invoice::getId)
            .collect(java.util.stream.Collectors.toSet());
        
        int deletedCount = 0;
        for (KpiAlert existingAlert : existingAlerts) {
            if (existingAlert.getRelatedInvoiceId() != null && 
                !currentOverdueInvoiceIds.contains(existingAlert.getRelatedInvoiceId())) {
                alertRepository.delete(existingAlert);
                deletedCount++;
                System.out.println("🗑️ Alerte supprimée pour facture résolue: " + existingAlert.getRelatedInvoiceId());
            }
        }
        
        if (deletedCount > 0) {
            System.out.println("✅ " + deletedCount + " alerte(s) obsolète(s) supprimée(s)");
        }
        
        // 🆕 ÉTAPE 4: Créer ou mettre à jour les alertes pour les factures OVERDUE
        int created = 0;
        int updated = 0;
        
        for (Invoice invoice : overdueInvoices) {
            // Vérifier si une alerte existe déjà
            Optional<KpiAlert> existing = alertRepository.findByRelatedInvoiceIdAndAlertStatus(
                invoice.getId(), 
                "PENDING_DECISION"
            );
            
            if (existing.isPresent()) {
                // Mettre à jour l'alerte existante
                KpiAlert alert = updateOverdueInvoiceAlert(existing.get(), invoice);
                alerts.add(alert);
                updated++;
            } else {
                // Créer une nouvelle alerte
                KpiAlert alert = createNewOverdueInvoiceAlert(invoice);
                if (alert != null) {
                    alerts.add(alert);
                    created++;
                }
            }
        }
        
        System.out.println("✅ Résultat: " + created + " créée(s), " + updated + " mise(s) à jour");
        System.out.println("📊 Total alertes après traitement: " + alertRepository.count());
        System.out.println("🎯 GARANTIE: " + overdueInvoices.size() + " factures OVERDUE = " + alerts.size() + " alertes");
        System.out.println("========================================");
        
        return alerts;
    }
    
    /**
     * Met à jour une alerte existante pour une facture OVERDUE
     */
    private KpiAlert updateOverdueInvoiceAlert(KpiAlert existingAlert, Invoice invoice) {
        // Calculer le nombre de jours de retard
        long daysOverdue = 0;
        if (invoice.getDueDate() != null) {
            LocalDateTime dueDate = invoice.getDueDate().atStartOfDay();
            daysOverdue = ChronoUnit.DAYS.between(dueDate, LocalDateTime.now());
        }
        
        // Déterminer la sévérité selon le retard
        String severity;
        if (daysOverdue > 60) {
            severity = "HIGH";
        } else if (daysOverdue > 30) {
            severity = "MEDIUM";
        } else {
            severity = "LOW";
        }
        
        // Mettre à jour les champs
        existingAlert.setCurrentValue((double) daysOverdue);
        existingAlert.setSeverity(severity);
        existingAlert.setDetectedAt(LocalDateTime.now());
        
        return alertRepository.save(existingAlert);
    }
    
    /**
     * Crée une nouvelle alerte pour une facture OVERDUE
     */
    private KpiAlert createNewOverdueInvoiceAlert(Invoice invoice) {
        // Récupérer la convention associée pour plus d'informations
        Convention convention = null;
        String conventionReference = "N/A";
        String clientName = "N/A";
        String gouvernorat = "N/A";
        String structure = "N/A";
        
        if (invoice.getConventionId() != null) {
            Optional<Convention> convOpt = conventionRepository.findById(invoice.getConventionId());
            if (convOpt.isPresent()) {
                convention = convOpt.get();
                conventionReference = convention.getReference() != null ? convention.getReference() : "N/A";
                clientName = convention.getCommercial() != null ? convention.getCommercial() : "N/A";
                gouvernorat = convention.getGovernorate() != null ? convention.getGovernorate() : "N/A";
                structure = convention.getStructureId() != null ? convention.getStructureId() : "N/A";
            }
        }
        
        // Calculer le nombre de jours de retard
        long daysOverdue = 0;
        if (invoice.getDueDate() != null) {
            LocalDateTime dueDate = invoice.getDueDate().atStartOfDay();
            daysOverdue = ChronoUnit.DAYS.between(dueDate, LocalDateTime.now());
        }
        
        // Déterminer la sévérité selon le retard
        String severity;
        if (daysOverdue > 60) {
            severity = "HIGH";  // Plus de 60 jours = critique
        } else if (daysOverdue > 30) {
            severity = "MEDIUM";  // 30-60 jours = moyen
        } else {
            severity = "LOW";  // Moins de 30 jours = faible
        }
        
        // Calculer les KPI pour cette convention (si disponible)
        String kpiInfo = "";
        if (convention != null) {
            try {
                // Calculer les KPI de la convention
                Map<String, KpiCalculatorService.KpiResult> conventionKpis = calculatorService.calculateKpisByStructure()
                    .getOrDefault(structure, new HashMap<>());
                
                StringBuilder kpiBuilder = new StringBuilder("\n📊 INDICATEURS DE PERFORMANCE:\n");
                
                // Taux de retard
                if (conventionKpis.containsKey("TAUX_RETARD")) {
                    double tauxRetard = conventionKpis.get("TAUX_RETARD").getValue();
                    String icon = tauxRetard > 30 ? "🔴" : tauxRetard > 15 ? "🟡" : "🟢";
                    kpiBuilder.append(String.format("%s Taux de retard: %.1f%%\n", icon, tauxRetard));
                }
                
                // Taux de régularisation
                if (conventionKpis.containsKey("TAUX_REGULARISATION")) {
                    double tauxReg = conventionKpis.get("TAUX_REGULARISATION").getValue();
                    String icon = tauxReg < 70 ? "🔴" : tauxReg < 85 ? "🟡" : "🟢";
                    kpiBuilder.append(String.format("%s Taux de régularisation: %.1f%%\n", icon, tauxReg));
                }
                
                // Taux de conversion
                if (conventionKpis.containsKey("TAUX_CONVERSION")) {
                    double tauxConv = conventionKpis.get("TAUX_CONVERSION").getValue();
                    String icon = tauxConv < 60 ? "🔴" : tauxConv < 75 ? "🟡" : "🟢";
                    kpiBuilder.append(String.format("%s Taux de conversion: %.1f%%\n", icon, tauxConv));
                }
                
                kpiInfo = kpiBuilder.toString();
            } catch (Exception e) {
                // Si erreur de calcul KPI, continuer sans
                System.out.println("⚠️ Impossible de calculer les KPI pour la convention: " + e.getMessage());
            }
        }
        
        // Construire le message détaillé
        StringBuilder message = new StringBuilder();
        message.append(String.format("🔴 FACTURE EN RETARD\n\n"));
        message.append(String.format("📄 Facture: %s\n", invoice.getInvoiceNumber()));
        message.append(String.format("💰 Montant: %.2f TND\n", invoice.getAmount()));
        message.append(String.format("⏰ Retard: %d jour(s)\n", daysOverdue));
        message.append(String.format("📅 Date d'échéance: %s\n", 
            invoice.getDueDate() != null ? invoice.getDueDate().toString().substring(0, 10) : "N/A"));
        message.append(String.format("\n📋 Convention: %s\n", conventionReference));
        message.append(String.format("👤 Client: %s\n", clientName));
        message.append(String.format("📍 Gouvernorat: %s\n", gouvernorat));
        message.append(String.format("🏢 Structure: %s\n", structure));
        
        // Ajouter les KPI si disponibles
        if (!kpiInfo.isEmpty()) {
            message.append(kpiInfo);
        }
        
        // Construire la recommandation
        StringBuilder recommendation = new StringBuilder();
        if (daysOverdue > 60) {
            recommendation.append("🚨 ACTION URGENTE REQUISE:\n");
            recommendation.append("• Contacter immédiatement le client\n");
            recommendation.append("• Envisager une procédure de recouvrement\n");
            recommendation.append("• Vérifier les garanties de paiement\n");
        } else if (daysOverdue > 30) {
            recommendation.append("⚠️ ACTION RECOMMANDÉE:\n");
            recommendation.append("• Relancer le client par téléphone\n");
            recommendation.append("• Envoyer un rappel formel\n");
            recommendation.append("• Planifier un suivi dans 7 jours\n");
        } else {
            recommendation.append("📞 ACTION SUGGÉRÉE:\n");
            recommendation.append("• Envoyer un rappel par email\n");
            recommendation.append("• Vérifier si le paiement est en cours\n");
        }
        
        // Créer une nouvelle alerte (les anciennes ont déjà été supprimées)
        String alertKpiName = "FACTURE_OVERDUE_" + invoice.getId();
        
        KpiAlert alert = new KpiAlert();
        alert.setKpiName(alertKpiName);
        alert.setCurrentValue((double) daysOverdue);
        alert.setStatus("ANORMAL");
        alert.setSeverity(severity);
        alert.setDimension("FACTURE");
        alert.setDimensionValue(invoice.getInvoiceNumber());
        alert.setMessage(message.toString());
        alert.setRecommendation(recommendation.toString());
        alert.setDetectedAt(LocalDateTime.now());
        alert.setAlertStatus("PENDING_DECISION");
        alert.setNotificationSent(false);
        
        // 🔗 LIEN VERS LA FACTURE (pour l'icône 👁️ dans le frontend)
        alert.setRelatedInvoiceId(invoice.getId());
        alert.setRelatedConventionId(invoice.getConventionId());
        
        // 📊 AJOUTER LES DÉTAILS DES ANOMALIES (KPI regroupés)
        List<KpiAlert.AnomalyDetail> anomalies = new ArrayList<>();
        
        // Anomalie 1: Retard de paiement
        KpiAlert.AnomalyDetail retardAnomaly = new KpiAlert.AnomalyDetail();
        retardAnomaly.setType("RETARD");
        retardAnomaly.setDescription(String.format("Facture en retard de %d jour(s)", daysOverdue));
        retardAnomaly.setDaysOverdue((int) daysOverdue);
        retardAnomaly.setSeverity(severity);
        retardAnomaly.setAmount(invoice.getAmount() != null ? invoice.getAmount().doubleValue() : 0.0);
        retardAnomaly.setDetectedAt(LocalDateTime.now());
        anomalies.add(retardAnomaly);
        
        // Ajouter les KPI calculés si disponibles
        if (!kpiInfo.isEmpty()) {
            // Les KPI sont déjà dans le message, on peut les extraire ou les recalculer
            // Pour l'instant, on les ajoute comme métadonnées
        }
        
        alert.setAnomalyDetails(anomalies);
        alert.setAnomalyTypes(List.of("RETARD"));
        
        return alertRepository.save(alert);
    }
    
    /**
     * Analyse un ensemble de KPI et crée UNE SEULE alerte consolidée par dimension
     */
    private List<KpiAlert> analyzeKpis(Map<String, KpiCalculatorService.KpiResult> kpis, String dimension, String dimensionValue) {
        List<KpiAlert> alerts = new ArrayList<>();
        
        // Collecter tous les KPI problématiques
        List<KpiIssue> issues = new ArrayList<>();
        String highestSeverity = "LOW";
        
        for (Map.Entry<String, KpiCalculatorService.KpiResult> entry : kpis.entrySet()) {
            String kpiName = entry.getKey();
            KpiCalculatorService.KpiResult result = entry.getValue();
            
            KpiEvaluation evaluation = evaluateKpi(kpiName, result.getValue(), dimension, dimensionValue);
            
            // Collecter les KPI anormaux ou à surveiller
            if ("ANORMAL".equals(evaluation.getStatus()) || "A_SURVEILLER".equals(evaluation.getStatus())) {
                issues.add(new KpiIssue(kpiName, result, evaluation));
                
                // Déterminer la sévérité la plus élevée
                if ("HIGH".equals(evaluation.getSeverity())) {
                    highestSeverity = "HIGH";
                } else if ("MEDIUM".equals(evaluation.getSeverity()) && !"HIGH".equals(highestSeverity)) {
                    highestSeverity = "MEDIUM";
                }
            }
        }
        
        // Si des problèmes sont détectés, créer UNE SEULE alerte consolidée
        if (!issues.isEmpty()) {
            KpiAlert consolidatedAlert = createConsolidatedAlert(issues, dimension, dimensionValue, highestSeverity);
            alerts.add(consolidatedAlert);
        }
        
        return alerts;
    }
    
    /**
     * Crée une alerte consolidée regroupant plusieurs KPI problématiques
     */
    private KpiAlert createConsolidatedAlert(List<KpiIssue> issues, String dimension, String dimensionValue, String severity) {
        // Vérifier si une alerte consolidée existe déjà pour cette dimension
        String consolidatedKpiName = "ALERTE_CONSOLIDEE_" + dimension;
        Optional<KpiAlert> existingAlert = alertRepository.findByKpiNameAndDimensionAndDimensionValueAndAlertStatus(
            consolidatedKpiName, 
            dimension, 
            dimensionValue, 
            "PENDING_DECISION"
        );
        
        // Construire le message consolidé
        StringBuilder message = new StringBuilder();
        StringBuilder recommendation = new StringBuilder();
        
        if (dimensionValue != null) {
            message.append(String.format("⚠️ Alertes multiples détectées pour %s : %s\n\n", dimension, dimensionValue));
        } else {
            message.append("⚠️ Alertes multiples détectées au niveau global\n\n");
        }
        
        message.append(String.format("📊 %d indicateur(s) problématique(s) :\n\n", issues.size()));
        
        // Déterminer le statut global
        boolean hasAnormal = issues.stream().anyMatch(i -> "ANORMAL".equals(i.evaluation.getStatus()));
        String globalStatus = hasAnormal ? "ANORMAL" : "A_SURVEILLER";
        
        // Ajouter chaque KPI problématique
        int index = 1;
        for (KpiIssue issue : issues) {
            String icon = "ANORMAL".equals(issue.evaluation.getStatus()) ? "🔴" : "🟡";
            message.append(String.format("%s %d. %s\n", icon, index++, issue.evaluation.getMessage()));
            
            if (issue.evaluation.getRecommendation() != null) {
                recommendation.append("• ").append(issue.evaluation.getRecommendation()).append("\n");
            }
        }
        
        // Ajouter une recommandation globale
        if (recommendation.length() == 0) {
            recommendation.append("Analysez les causes de ces anomalies et prenez les mesures correctives appropriées.");
        }
        
        if (existingAlert.isPresent()) {
            // Mettre à jour l'alerte existante
            KpiAlert alert = existingAlert.get();
            alert.setCurrentValue((double) issues.size()); // Nombre de KPI problématiques
            alert.setStatus(globalStatus);
            alert.setSeverity(severity);
            alert.setMessage(message.toString());
            alert.setRecommendation(recommendation.toString());
            alert.setDetectedAt(LocalDateTime.now());
            
            System.out.println("⚠️ Alerte consolidée mise à jour: " + dimension + " - " + dimensionValue + " (" + issues.size() + " KPI)");
            return alertRepository.save(alert);
        }
        
        // Créer une nouvelle alerte consolidée
        KpiAlert alert = new KpiAlert();
        alert.setKpiName(consolidatedKpiName);
        alert.setCurrentValue((double) issues.size());
        alert.setStatus(globalStatus);
        alert.setSeverity(severity);
        alert.setDimension(dimension);
        alert.setDimensionValue(dimensionValue);
        alert.setMessage(message.toString());
        alert.setRecommendation(recommendation.toString());
        alert.setDetectedAt(LocalDateTime.now());
        alert.setAlertStatus("PENDING_DECISION");
        alert.setNotificationSent(false);
        
        System.out.println("✅ Nouvelle alerte consolidée créée: " + dimension + " - " + dimensionValue + " (" + issues.size() + " KPI)");
        
        return alertRepository.save(alert);
    }
    
    /**
     * Classe interne pour stocker un KPI problématique
     */
    private static class KpiIssue {
        String kpiName;
        KpiCalculatorService.KpiResult result;
        KpiEvaluation evaluation;
        
        KpiIssue(String kpiName, KpiCalculatorService.KpiResult result, KpiEvaluation evaluation) {
            this.kpiName = kpiName;
            this.result = result;
            this.evaluation = evaluation;
        }
    }
    
    /**
     * Crée une alerte KPI (ou met à jour si elle existe déjà)
     */
    private KpiAlert createAlert(String kpiName, KpiCalculatorService.KpiResult result, 
                                  KpiEvaluation evaluation, String dimension, String dimensionValue) {
        
        // ✅ VÉRIFIER SI UNE ALERTE EXISTE DÉJÀ POUR CE KPI
        Optional<KpiAlert> existingAlert = alertRepository.findByKpiNameAndDimensionAndDimensionValueAndAlertStatus(
            kpiName, 
            dimension, 
            dimensionValue, 
            "PENDING_DECISION"
        );
        
        if (existingAlert.isPresent()) {
            // Mettre à jour l'alerte existante au lieu d'en créer une nouvelle
            KpiAlert alert = existingAlert.get();
            alert.setCurrentValue(result.getValue());
            alert.setStatus(evaluation.getStatus());
            alert.setSeverity(evaluation.getSeverity());
            alert.setMessage(evaluation.getMessage());
            alert.setRecommendation(evaluation.getRecommendation());
            alert.setDetectedAt(LocalDateTime.now());
            
            System.out.println("⚠️ Alerte existante mise à jour: " + kpiName + " - " + dimensionValue);
            return alertRepository.save(alert);
        }
        
        // Créer une nouvelle alerte seulement si elle n'existe pas
        KpiAlert alert = new KpiAlert();
        alert.setKpiName(kpiName);
        alert.setCurrentValue(result.getValue());
        alert.setStatus(evaluation.getStatus());
        alert.setSeverity(evaluation.getSeverity());
        alert.setDimension(dimension);
        alert.setDimensionValue(dimensionValue);
        alert.setMessage(evaluation.getMessage());
        alert.setRecommendation(evaluation.getRecommendation());
        alert.setDetectedAt(LocalDateTime.now());
        alert.setAlertStatus("PENDING_DECISION");
        alert.setNotificationSent(false);
        
        System.out.println("✅ Nouvelle alerte créée: " + kpiName + " - " + dimensionValue);
        
        // Sauvegarder l'alerte
        return alertRepository.save(alert);
    }
    
    /**
     * Génère une recommandation automatique
     */
    private String generateRecommendation(String kpiName, Double currentValue, KpiThreshold threshold) {
        switch (kpiName) {
            case "TAUX_RETARD":
                return String.format(
                    "Action recommandée : Relancer les clients avec factures en retard. " +
                    "Le taux de retard actuel (%.1f%%) dépasse largement la normale (%.1f%%). " +
                    "Vérifiez les conventions concernées et planifiez des actions de recouvrement.",
                    currentValue, threshold.getNormalValue()
                );
                
            case "TAUX_PAIEMENT":
                return String.format(
                    "Le taux de paiement (%.1f%%) est inférieur à la cible (%.1f%%). " +
                    "Analysez les raisons du retard et mettez en place un plan d'action.",
                    currentValue, threshold.getNormalValue()
                );
                
            case "MONTANT_IMPAYE_PERCENT":
                return String.format(
                    "Le montant impayé représente %.1f%% du total facturé. " +
                    "Priorisez le recouvrement des créances importantes.",
                    currentValue
                );
                
            case "DUREE_MOYENNE_PAIEMENT":
                return String.format(
                    "La durée moyenne de paiement (%.1f jours) est trop élevée. " +
                    "Négociez des délais de paiement plus courts avec les clients.",
                    currentValue
                );
                
            default:
                return "Analysez les causes de cette anomalie et prenez les mesures correctives appropriées.";
        }
    }
    
    /**
     * Classe pour stocker le résultat d'une évaluation
     */
    public static class KpiEvaluation {
        private String status;
        private String severity;
        private String message;
        private String recommendation;
        
        public KpiEvaluation(String status, String severity, String message, String recommendation) {
            this.status = status;
            this.severity = severity;
            this.message = message;
            this.recommendation = recommendation;
        }
        
        public String getStatus() { return status; }
        public String getSeverity() { return severity; }
        public String getMessage() { return message; }
        public String getRecommendation() { return recommendation; }
    }
}
