package com.example.demo.repository;

import com.example.demo.model.MonitoringThresholds;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MonitoringThresholdsRepository extends MongoRepository<MonitoringThresholds, String> {
    
    Optional<MonitoringThresholds> findByMetricName(String metricName);
    
    boolean existsByMetricName(String metricName);
} 