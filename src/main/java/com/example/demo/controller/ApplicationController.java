package com.example.demo.controller;

import com.example.demo.model.Application;
import com.example.demo.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    @Autowired
    private ApplicationRepository applicationRepository;

    // Récupérer toutes les applications
    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        try {
            List<Application> applications = applicationRepository.findAll();
            System.out.println("✅ [GET APPLICATIONS] " + applications.size() + " applications récupérées");
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            System.err.println("❌ [GET APPLICATIONS] Erreur: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Récupérer une application par ID
    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable String id) {
        try {
            Optional<Application> application = applicationRepository.findById(id);
            return application.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Créer une nouvelle application
    @PostMapping
    public ResponseEntity<?> createApplication(@RequestBody Application application) {
        try {
            // Vérifier que les champs obligatoires sont présents
            if (application.getCode() == null || application.getCode().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Le code est obligatoire");
            }
            if (application.getLibelle() == null || application.getLibelle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Le libellé est obligatoire");
            }
            
            // Vérifier l'unicité du code
            if (applicationRepository.findByCode(application.getCode()).isPresent()) {
                return ResponseEntity.badRequest().body("Une application avec ce code existe déjà");
            }
            
            Application savedApplication = applicationRepository.save(application);
            return ResponseEntity.ok(savedApplication);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de l'application: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erreur interne du serveur: " + e.getMessage());
        }
    }

    // Mettre à jour une application
    @PutMapping("/{id}")
    public ResponseEntity<Application> updateApplication(@PathVariable String id, @RequestBody Application application) {
        try {
            if (!applicationRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            application.setId(id);
            Application updatedApplication = applicationRepository.save(application);
            return ResponseEntity.ok(updatedApplication);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Supprimer une application
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable String id) {
        try {
            if (!applicationRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            applicationRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Désactiver une application (au lieu de la supprimer)
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Application> deactivateApplication(@PathVariable String id) {
        try {
            Optional<Application> applicationOpt = applicationRepository.findById(id);
            if (applicationOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Application application = applicationOpt.get();
            application.setActif(false);
            Application updatedApplication = applicationRepository.save(application);
            return ResponseEntity.ok(updatedApplication);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

