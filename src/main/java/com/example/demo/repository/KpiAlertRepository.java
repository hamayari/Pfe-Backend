package com.example.demo.repository;

import com.example.demo.model.KpiAlert;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface KpiAlertRepository extends MongoRepository<KpiAlert, String> {
    
    // Trouver les alertes actives
    List<KpiAlert> findByAlertStatus(String alertStatus);
    
    // Trouver par KPI
    List<KpiAlert> findByKpiName(String kpiName);
    
    // Trouver par dimension
    List<KpiAlert> findByDimensionAndDimensionValue(String dimension, String dimensionValue);
    
    // Trouver par sévérité
    List<KpiAlert> findBySeverity(String severity);
    
    // Trouver les alertes récentes
    List<KpiAlert> findByDetectedAtAfter(LocalDateTime date);
    
    // Trouver les alertes actives par sévérité
    List<KpiAlert> findByAlertStatusAndSeverityOrderByDetectedAtDesc(String alertStatus, String severity);
    
    // Trouver les alertes non notifiées
    List<KpiAlert> findByNotificationSentFalse();
    
    // Compter les alertes actives
    long countByAlertStatus(String alertStatus);
    
    // Compter les alertes par sévérité
    long countBySeverity(String severity);
    
    // Trouver les alertes actives (NEW ou IN_PROGRESS)
    List<KpiAlert> findByAlertStatusIn(List<String> statuses);
    
    // Trouver les alertes résolues récemment
    List<KpiAlert> findByAlertStatusAndResolvedAtAfter(String alertStatus, LocalDateTime date);
    
    // Trouver les alertes archivées
    List<KpiAlert> findByAlertStatusOrderByArchivedAtDesc(String alertStatus);
    
    // Trouver les alertes par destinataire
    List<KpiAlert> findByRecipientsContaining(String userId);
    
    // Trouver les alertes actives par destinataire
    List<KpiAlert> findByRecipientsContainingAndAlertStatusIn(String userId, List<String> statuses);
    
    // Trouver par facture et statut d'envoi au chef de projet
    java.util.Optional<KpiAlert> findByRelatedInvoiceIdAndSentToProjectManager(String invoiceId, boolean sent);
    
    // Trouver toutes les alertes envoyées/non envoyées au chef de projet
    List<KpiAlert> findBySentToProjectManager(boolean sent);
    
    // Trouver les alertes par facture
    List<KpiAlert> findByRelatedInvoiceId(String invoiceId);
    
    // Trouver une alerte par facture et statut (pour UPSERT)
    java.util.Optional<KpiAlert> findByRelatedInvoiceIdAndAlertStatus(String invoiceId, String alertStatus);
    
    // Trouver une alerte existante par KPI, dimension et statut (pour éviter les doublons)
    java.util.Optional<KpiAlert> findByKpiNameAndDimensionAndDimensionValueAndAlertStatus(
        String kpiName, 
        String dimension, 
        String dimensionValue, 
        String alertStatus
    );
    
    // Trouver une alerte par KPI et statut (pour les factures individuelles)
    java.util.Optional<KpiAlert> findByKpiNameAndAlertStatus(String kpiName, String alertStatus);
    
    // Trouver toutes les alertes par dimension (pour nettoyage automatique)
    List<KpiAlert> findByDimension(String dimension);
}
