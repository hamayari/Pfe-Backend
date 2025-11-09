package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
public class NotificationInitializationService implements CommandLineRunner {

    @Autowired
    private NotificationTemplateService templateService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 [INIT] Initialisation du système de notifications...");
        
        try {
            // Initialiser les templates par défaut
            templateService.initializeDefaultTemplates();
            
            System.out.println("✅ [INIT] Système de notifications initialisé avec succès");
            
        } catch (Exception e) {
            System.err.println("❌ [INIT] Erreur lors de l'initialisation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}











