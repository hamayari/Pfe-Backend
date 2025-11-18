package com.example.demo.config;

import com.example.demo.model.KpiThreshold;
import com.example.demo.repository.KpiThresholdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Initialise les seuils KPI par défaut au démarrage
 */
@Component
@Profile("!test")
public class KpiThresholdInitializer implements CommandLineRunner {
    
    @Autowired
    private KpiThresholdRepository thresholdRepository;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("========================================");
        System.out.println("🔧 [KPI INIT] Initialisation des seuils KPI");
        System.out.println("========================================");
        
        // Vérifier si les seuils existent déjà
        if (thresholdRepository.count() > 0) {
            System.out.println("✅ [KPI INIT] Seuils déjà initialisés (" + thresholdRepository.count() + " seuils)");
            return;
        }
        
        // 1. Taux de retard
        createThreshold(
            "TAUX_RETARD",
            "Taux de factures en retard",
            5.0,   // Seuil bas (🟡)
            10.0,  // Seuil haut (🔴)
            3.0,   // Valeur normale
            10.0,  // Tolérance ±10%
            "%",
            "GLOBAL",
            null,
            "HIGH"
        );
        
        // 2. Taux de paiement
        createThreshold(
            "TAUX_PAIEMENT",
            "Taux de factures payées",
            85.0,  // Seuil bas (🟡)
            75.0,  // Seuil haut (🔴)
            90.0,  // Valeur normale
            10.0,  // Tolérance ±10%
            "%",
            "GLOBAL",
            null,
            "HIGH"
        );
        
        // 3. Montant impayé
        createThreshold(
            "MONTANT_IMPAYE_PERCENT",
            "Pourcentage du montant impayé",
            15.0,  // Seuil bas (🟡)
            25.0,  // Seuil haut (🔴)
            10.0,  // Valeur normale
            10.0,  // Tolérance ±10%
            "%",
            "GLOBAL",
            null,
            "HIGH"
        );
        
        // 4. Durée moyenne de paiement
        createThreshold(
            "DUREE_MOYENNE_PAIEMENT",
            "Durée moyenne de paiement",
            30.0,  // Seuil bas (🟡)
            45.0,  // Seuil haut (🔴)
            20.0,  // Valeur normale
            15.0,  // Tolérance ±15%
            "jours",
            "GLOBAL",
            null,
            "MEDIUM"
        );
        
        // 5. Taux de conversion
        createThreshold(
            "TAUX_CONVERSION",
            "Taux de conventions actives",
            60.0,  // Seuil bas (🟡)
            50.0,  // Seuil haut (🔴)
            70.0,  // Valeur normale
            10.0,  // Tolérance ±10%
            "%",
            "GLOBAL",
            null,
            "MEDIUM"
        );
        
        System.out.println("✅ [KPI INIT] " + thresholdRepository.count() + " seuils créés avec succès");
        System.out.println("========================================");
    }
    
    private void createThreshold(String kpiName, String description, Double lowThreshold, 
                                  Double highThreshold, Double normalValue, Double tolerancePercent,
                                  String unit, String dimension, String dimensionValue, String priority) {
        KpiThreshold threshold = new KpiThreshold();
        threshold.setKpiName(kpiName);
        threshold.setDescription(description);
        threshold.setLowThreshold(lowThreshold);
        threshold.setHighThreshold(highThreshold);
        threshold.setNormalValue(normalValue);
        threshold.setTolerancePercent(tolerancePercent);
        threshold.setUnit(unit);
        threshold.setDimension(dimension);
        threshold.setDimensionValue(dimensionValue);
        threshold.setEnabled(true);
        threshold.setPriority(priority);
        threshold.setCreatedBy("SYSTEM");
        threshold.setCreatedAt(LocalDateTime.now());
        threshold.setUpdatedAt(LocalDateTime.now());
        
        thresholdRepository.save(threshold);
        System.out.println("   ✓ Seuil créé: " + kpiName + " (" + description + ")");
    }
}
