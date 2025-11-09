package com.example.demo.controller;

import com.example.demo.model.KpiAlert;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.KpiAlertManagementService;
import com.example.demo.enums.ERole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Contrôleur de test pour créer des alertes KPI de démonstration
 */
@RestController
@RequestMapping("/api/test/kpi-alerts")
@CrossOrigin(origins = "*")
public class KpiAlertTestController {
    
    @Autowired
    private KpiAlertManagementService alertService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Créer des alertes de test
     */
    @PostMapping("/create-demo-alerts")
    public ResponseEntity<Map<String, Object>> createDemoAlerts() {
        try {
            // Trouver les chefs de projet
            List<User> projectManagers = userRepository.findByRoles_Name(ERole.ROLE_PROJECT_MANAGER);
            List<String> recipients = new ArrayList<>();
            for (User pm : projectManagers) {
                recipients.add(pm.getId());
            }
            
            List<KpiAlert> createdAlerts = new ArrayList<>();
            
            // 1. Alerte Taux de retard - CRITIQUE
            KpiAlert alert1 = new KpiAlert();
            alert1.setKpiName("TAUX_RETARD");
            alert1.setCurrentValue(58.3);
            alert1.setThresholdValue(15.0);
            alert1.setSeverity("HIGH");
            alert1.setStatus("🔴 ANORMAL");
            alert1.setAlertStatus("PENDING_DECISION");
            alert1.setDimension("GOUVERNORAT");
            alert1.setDimensionValue("Sfax");
            alert1.setMessage("Taux de factures en retard à 58.3%, seuil critique dépassé (15.0%)");
            alert1.setRecommendation("Contacter immédiatement les clients en retard de la région de Sfax. Mettre en place un plan de recouvrement urgent avec relances téléphoniques et visites sur site.");
            alert1.setPriority("CRITICAL");
            alert1.setRecipients(recipients);
            
            KpiAlert saved1 = alertService.createAlert(alert1, "system");
            createdAlerts.add(saved1);
            
            // 2. Alerte Montant impayé - CRITIQUE
            KpiAlert alert2 = new KpiAlert();
            alert2.setKpiName("MONTANT_IMPAYE");
            alert2.setCurrentValue(45000.0);
            alert2.setThresholdValue(30000.0);
            alert2.setSeverity("HIGH");
            alert2.setStatus("🔴 ANORMAL");
            alert2.setAlertStatus("PENDING_DECISION");
            alert2.setDimension("GOUVERNORAT");
            alert2.setDimensionValue("Tunis");
            alert2.setMessage("Montant total impayé à 45000.0 TND, seuil critique dépassé (30000.0 TND)");
            alert2.setRecommendation("Analyser les factures impayées de Tunis. Prioriser les montants les plus élevés. Négocier des plans de paiement échelonnés si nécessaire.");
            alert2.setPriority("CRITICAL");
            alert2.setRecipients(recipients);
            
            KpiAlert saved2 = alertService.createAlert(alert2, "system");
            createdAlerts.add(saved2);
            
            // 3. Alerte Taux de régularisation - MOYEN
            KpiAlert alert3 = new KpiAlert();
            alert3.setKpiName("TAUX_REGULARISATION");
            alert3.setCurrentValue(65.0);
            alert3.setThresholdValue(70.0);
            alert3.setSeverity("MEDIUM");
            alert3.setStatus("🟡 A_SURVEILLER");
            alert3.setAlertStatus("PENDING_DECISION");
            alert3.setDimension("GOUVERNORAT");
            alert3.setDimensionValue("Sousse");
            alert3.setMessage("Taux de régularisation à 65.0%, en dessous du seuil (70.0%)");
            alert3.setRecommendation("Suivre de près l'évolution du taux de régularisation à Sousse. Identifier les causes du ralentissement et mettre en place des actions correctives.");
            alert3.setPriority("HIGH");
            alert3.setRecipients(recipients);
            
            KpiAlert saved3 = alertService.createAlert(alert3, "system");
            createdAlerts.add(saved3);
            
            // 4. Alerte Délai de paiement - CRITIQUE
            KpiAlert alert4 = new KpiAlert();
            alert4.setKpiName("DELAI_PAIEMENT");
            alert4.setCurrentValue(52.0);
            alert4.setThresholdValue(45.0);
            alert4.setSeverity("HIGH");
            alert4.setStatus("🔴 ANORMAL");
            alert4.setAlertStatus("PENDING_DECISION");
            alert4.setDimension("GOUVERNORAT");
            alert4.setDimensionValue("Ariana");
            alert4.setMessage("Délai moyen de paiement à 52.0 jours, seuil critique dépassé (45.0 jours)");
            alert4.setRecommendation("Analyser les causes des retards de paiement à Ariana. Revoir les conditions de paiement avec les clients. Mettre en place des rappels automatiques avant échéance.");
            alert4.setPriority("CRITICAL");
            alert4.setRecipients(recipients);
            
            KpiAlert saved4 = alertService.createAlert(alert4, "system");
            createdAlerts.add(saved4);
            
            // 5. Alerte Taux de conversion - MOYEN
            KpiAlert alert5 = new KpiAlert();
            alert5.setKpiName("TAUX_CONVERSION");
            alert5.setCurrentValue(10.5);
            alert5.setThresholdValue(12.0);
            alert5.setSeverity("MEDIUM");
            alert5.setStatus("🟡 A_SURVEILLER");
            alert5.setAlertStatus("PENDING_DECISION");
            alert5.setDimension("GOUVERNORAT");
            alert5.setDimensionValue("Monastir");
            alert5.setMessage("Taux de conversion à 10.5%, en dessous du seuil critique (12.0%)");
            alert5.setRecommendation("Analyser les raisons de la baisse du taux de conversion à Monastir. Revoir la stratégie commerciale et améliorer le suivi des prospects.");
            alert5.setPriority("HIGH");
            alert5.setRecipients(recipients);
            
            KpiAlert saved5 = alertService.createAlert(alert5, "system");
            createdAlerts.add(saved5);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "5 alertes de démonstration créées avec succès");
            response.put("count", createdAlerts.size());
            response.put("alerts", createdAlerts);
            
            System.out.println("✅ " + createdAlerts.size() + " alertes de test créées");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur création alertes test: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(error);
        }
    }
}
