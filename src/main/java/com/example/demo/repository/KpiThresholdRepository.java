package com.example.demo.repository;

import com.example.demo.model.KpiThreshold;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface KpiThresholdRepository extends MongoRepository<KpiThreshold, String> {
    
    // Trouver par nom de KPI
    Optional<KpiThreshold> findByKpiName(String kpiName);
    
    // Trouver par dimension
    List<KpiThreshold> findByDimension(String dimension);
    
    // Trouver par KPI et dimension
    Optional<KpiThreshold> findByKpiNameAndDimensionAndDimensionValue(
        String kpiName, String dimension, String dimensionValue);
    
    // Trouver les seuils actifs
    List<KpiThreshold> findByEnabledTrue();
    
    // Trouver par priorité
    List<KpiThreshold> findByPriority(String priority);
}
