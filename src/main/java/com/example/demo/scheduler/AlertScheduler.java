package com.example.demo.scheduler;

import com.example.demo.service.InvoiceAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler pour générer automatiquement les alertes pour les factures OVERDUE
 * Exécuté toutes les 5 minutes
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlertScheduler {

    private final InvoiceAlertService invoiceAlertService;

    /**
     * Génère automatiquement les alertes pour les factures PENDING
     * Exécuté toutes les 5 minutes + au démarrage (après 30 secondes)
     */
    @Scheduled(fixedRate = 300000, initialDelay = 30000) // 5 min, démarrage après 30s
    public void generateOverdueInvoiceAlerts() {
        log.info("🔔 [SCHEDULER] Génération automatique des alertes - {}", java.time.LocalDateTime.now());
        
        try {
            // Vérifier les factures PENDING (en attente de paiement)
            var alerts = invoiceAlertService.checkPendingInvoices();
            log.info("✅ [SCHEDULER] {} alerte(s) générée(s)/mise(s) à jour", alerts.size());
            
        } catch (Exception e) {
            log.error("❌ [SCHEDULER] Erreur lors de la génération des alertes: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Méthode manuelle pour forcer la génération (utile pour les tests)
     */
    public void forceGeneration() {
        log.info("🔧 [MANUAL] Génération manuelle forcée des alertes");
        generateOverdueInvoiceAlerts();
    }
}
