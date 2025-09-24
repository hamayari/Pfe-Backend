package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

/**
 * Configuration pour l'optimisation de la génération de documents
 * Évite le stockage en base et optimise les performances
 */
@Configuration
@EnableAsync
public class DocumentConfig {

    /**
     * Executor pour la génération asynchrone de documents
     * Évite de bloquer le thread principal
     */
    @Bean(name = "documentExecutor")
    public Executor documentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("DocumentGen-");
        executor.initialize();
        return executor;
    }

    /**
     * Configuration pour les limites de taille des documents
     */
    @Bean
    public DocumentLimits documentLimits() {
        return new DocumentLimits();
    }

    /**
     * Classe pour définir les limites de génération de documents
     */
    public static class DocumentLimits {
        private final int maxDocumentsPerRequest = 50;
        private final int maxFileSizeMB = 10;
        private final int cacheExpirationMinutes = 30;
        private final int maxConcurrentGenerations = 5;

        public int getMaxDocumentsPerRequest() {
            return maxDocumentsPerRequest;
        }

        public int getMaxFileSizeMB() {
            return maxFileSizeMB;
        }

        public int getCacheExpirationMinutes() {
            return cacheExpirationMinutes;
        }

        public int getMaxConcurrentGenerations() {
            return maxConcurrentGenerations;
        }
    }
} 