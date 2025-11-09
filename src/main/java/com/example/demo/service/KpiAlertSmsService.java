package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service d'envoi de SMS pour les alertes KPI
 * Utilise SmsService existant pour la cohérence
 */
@Service
@Slf4j
public class KpiAlertSmsService {

    private final SmsService smsService;

    @Value("${sms.simulation.mode:false}")
    private boolean simulationMode;

    @Autowired
    public KpiAlertSmsService(SmsService smsService) {
        this.smsService = smsService;
    }

    /**
     * Envoyer un SMS d'alerte KPI
     */
    public void sendKpiAlertSms(
            String toPhoneNumber,
            String recipientName,
            String kpiName,
            String severity,
            String priority,
            Double currentValue) {
        
        if (toPhoneNumber == null || toPhoneNumber.isEmpty()) {
            log.warn("⚠️ Numéro de téléphone manquant pour: {}", recipientName);
            return;
        }

        try {
            log.info("📱 Envoi SMS alerte KPI à: {}", toPhoneNumber);

            // Préparer les variables pour le template
            Map<String, String> variables = new HashMap<>();
            variables.put("kpiName", kpiName);
            variables.put("severity", severity);
            variables.put("priority", priority);
            variables.put("currentValue", String.format("%.1f", currentValue));

            // Utiliser SmsService existant avec template
            smsService.sendSmsWithTemplate(toPhoneNumber, "kpi_alert", variables);
            
            log.info("✅ SMS alerte KPI envoyé à: {}", toPhoneNumber);

        } catch (Exception e) {
            log.error("❌ Erreur envoi SMS alerte KPI à {}: {}", toPhoneNumber, e.getMessage(), e);
        }
    }

    /**
     * Envoyer un SMS de délégation au Chef de Projet
     */
    public void sendDelegationSms(
            String toPhoneNumber,
            String projectManagerName,
            String kpiName,
            String priority) {
        
        if (toPhoneNumber == null || toPhoneNumber.isEmpty()) {
            log.warn("⚠️ Numéro de téléphone manquant pour: {}", projectManagerName);
            return;
        }

        try {
            log.info("📱 Envoi SMS délégation à: {}", toPhoneNumber);

            // Préparer les variables pour le template
            Map<String, String> variables = new HashMap<>();
            variables.put("kpiName", kpiName);
            variables.put("priority", priority);

            // Utiliser SmsService existant avec template
            smsService.sendSmsWithTemplate(toPhoneNumber, "kpi_delegation", variables);
            
            log.info("✅ SMS délégation envoyé à: {}", toPhoneNumber);

        } catch (Exception e) {
            log.error("❌ Erreur envoi SMS délégation à {}: {}", toPhoneNumber, e.getMessage(), e);
        }
    }

    /**
     * Vérifier si le service SMS est configuré
     */
    public boolean isConfigured() {
        return !simulationMode;
    }
}
