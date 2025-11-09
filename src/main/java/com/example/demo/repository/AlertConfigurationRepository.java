package com.example.demo.repository;

import com.example.demo.model.AlertConfiguration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlertConfigurationRepository extends MongoRepository<AlertConfiguration, String> {
    
    /**
     * Récupère la configuration active
     */
    Optional<AlertConfiguration> findFirstByActiveTrue();
}
