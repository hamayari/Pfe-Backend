package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration pour activer les tâches planifiées (Schedulers)
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
    // Spring Boot activera automatiquement tous les @Scheduled
}
