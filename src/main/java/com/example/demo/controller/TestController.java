package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import com.example.demo.model.Convention;
import com.example.demo.repository.ConventionRepository;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private ConventionRepository conventionRepository;

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Backend is running");
        response.put("timestamp", java.time.LocalDateTime.now());
        return response;
    }

    @GetMapping("/conventions")
    public Map<String, Object> testConventions() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Convention> conventions = conventionRepository.findAll();
            response.put("totalConventions", conventions.size());
            response.put("conventions", conventions);
            response.put("message", "Conventions récupérées de la base");
            response.put("timestamp", java.time.LocalDateTime.now());
            
            // Log détaillé
            System.out.println("🔍 TEST ENDPOINT - " + conventions.size() + " conventions trouvées");
            for (Convention c : conventions) {
                System.out.println("  - " + c.getReference() + " (ID: " + c.getId() + ")");
            }
            
        } catch (Exception e) {
            response.put("error", "Erreur: " + e.getMessage());
            response.put("totalConventions", 0);
            System.out.println("❌ ERREUR dans test endpoint: " + e.getMessage());
            e.printStackTrace();
        }
        
        return response;
    }
}