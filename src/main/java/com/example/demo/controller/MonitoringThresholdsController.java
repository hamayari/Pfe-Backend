package com.example.demo.controller;

import com.example.demo.model.MonitoringThresholds;
import com.example.demo.repository.MonitoringThresholdsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/monitoring/thresholds")
public class MonitoringThresholdsController {

    @Autowired
    private MonitoringThresholdsRepository thresholdsRepository;

    @GetMapping
    public List<MonitoringThresholds> getAllThresholds() {
        return thresholdsRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonitoringThresholds> getThresholdById(@PathVariable String id) {
        Optional<MonitoringThresholds> threshold = thresholdsRepository.findById(id);
        return threshold.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/metric/{metricName}")
    public ResponseEntity<MonitoringThresholds> getThresholdByMetric(@PathVariable String metricName) {
        Optional<MonitoringThresholds> threshold = thresholdsRepository.findByMetricName(metricName);
        return threshold.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public MonitoringThresholds createThreshold(@RequestBody MonitoringThresholds threshold) {
        threshold.setCreatedAt(LocalDateTime.now());
        threshold.setUpdatedAt(LocalDateTime.now());
        return thresholdsRepository.save(threshold);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MonitoringThresholds> updateThreshold(@PathVariable String id, @RequestBody MonitoringThresholds thresholdDetails) {
        Optional<MonitoringThresholds> threshold = thresholdsRepository.findById(id);
        
        if (threshold.isPresent()) {
            MonitoringThresholds existingThreshold = threshold.get();
            existingThreshold.setMetricName(thresholdDetails.getMetricName());
            existingThreshold.setWarningThreshold(thresholdDetails.getWarningThreshold());
            existingThreshold.setCriticalThreshold(thresholdDetails.getCriticalThreshold());
            existingThreshold.setEnabled(thresholdDetails.isEnabled());
            existingThreshold.setDescription(thresholdDetails.getDescription());
            existingThreshold.setUpdatedAt(LocalDateTime.now());
            
            MonitoringThresholds updatedThreshold = thresholdsRepository.save(existingThreshold);
            return ResponseEntity.ok(updatedThreshold);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteThreshold(@PathVariable String id) {
        if (thresholdsRepository.existsById(id)) {
            thresholdsRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/initialize-defaults")
    public ResponseEntity<String> initializeDefaultThresholds() {
        // Créer les seuils par défaut s'ils n'existent pas
        createDefaultThresholdIfNotExists("CPU", 70.0, 85.0, "Utilisation du processeur");
        createDefaultThresholdIfNotExists("RAM", 75.0, 90.0, "Utilisation de la mémoire RAM");
        createDefaultThresholdIfNotExists("DISK", 80.0, 95.0, "Utilisation de l'espace disque");
        
        return ResponseEntity.ok("Seuils par défaut initialisés avec succès");
    }
    
    private void createDefaultThresholdIfNotExists(String metricName, double warning, double critical, String description) {
        if (!thresholdsRepository.existsByMetricName(metricName)) {
            MonitoringThresholds threshold = new MonitoringThresholds(metricName, warning, critical, description);
            thresholdsRepository.save(threshold);
        }
    }
} 