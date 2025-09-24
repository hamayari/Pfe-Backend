package com.example.demo.controller;

import com.example.demo.model.Convention;
import com.example.demo.service.ConventionService;
import com.example.demo.dto.convention.ConventionRequest;
import com.example.demo.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.Map;

@RestController
@RequestMapping("/api/conventions")
public class ConventionController {

    @Autowired
    private ConventionService conventionService;

    @PostMapping
    public ResponseEntity<Convention> createConvention(
            @Valid @RequestBody ConventionRequest request) {
        System.out.println("🚀 ConventionController.createConvention() appelé");
        System.out.println("📋 Référence: " + request.getReference());
        System.out.println("📋 Titre: " + request.getTitle());
        
        try {
            Convention result = conventionService.createConvention(request, "commercial");
            System.out.println("✅ Convention créée avec succès: " + result.getId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("❌ Erreur lors de la création de convention: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Convention> updateConvention(
            @PathVariable String id,
            @Valid @RequestBody ConventionRequest request) {
        return ResponseEntity.ok(conventionService.updateConvention(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<Convention> getConvention(@PathVariable String id) {
        return ResponseEntity.ok(conventionService.getConventionById(id));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getConventionPDF(@PathVariable String id) {
        byte[] pdfBytes = conventionService.generateConventionPDF(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "convention-" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping
    public ResponseEntity<List<Convention>> getAllConventions(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            System.out.println("🔍 getAllConventions appelé - UserPrincipal: " + (userPrincipal != null ? userPrincipal.getUsername() : "null"));
            if (userPrincipal != null) {
                System.out.println("🔍 Rôles de l'utilisateur: " + userPrincipal.getAuthorities());
                System.out.println("🔍 ID utilisateur: " + userPrincipal.getId());
            }
            
            // Pour les commerciaux et super admin, afficher TOUTES les conventions
            if (userPrincipal != null && userPrincipal.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_COMMERCIAL") || auth.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
                List<Convention> conventions = conventionService.getAllConventions();
                System.out.println("✅ " + conventions.size() + " conventions trouvées pour le commercial");
                return ResponseEntity.ok(conventions);
            } else {
                // Pour les autres rôles, garder le filtrage par utilisateur
                System.out.println("⚠️ Utilisateur n'est pas commercial, filtrage par utilisateur");
                if (userPrincipal != null) {
                    List<Convention> userConventions = conventionService.getAllConventionsByUser(userPrincipal.getId());
                    System.out.println("✅ " + userConventions.size() + " conventions trouvées pour l'utilisateur " + userPrincipal.getId());
                    return ResponseEntity.ok(userConventions);
                } else {
                    System.out.println("❌ UserPrincipal est null");
                    return ResponseEntity.ok(new java.util.ArrayList<>());
                }
            }
        } catch (Exception e) {
            // Retourner une liste vide en cas d'erreur pour éviter les erreurs 500
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }
    }

    @GetMapping("/structure/{structure}")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<List<Convention>> getConventionsByStructure(@PathVariable String structure) {
        return ResponseEntity.ok(conventionService.getConventionsByStructure(structure));
    }

    @GetMapping("/zone/{zone}")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<List<Convention>> getConventionsByZone(@PathVariable String zone) {
        return ResponseEntity.ok(conventionService.getConventionsByGeographicZone(zone));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<List<Convention>> searchConventions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String governorate,
            @RequestParam(required = false) String structureId,
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin,
            @RequestParam(required = false) List<String> tags) {
        return ResponseEntity.ok(conventionService.searchConventions(status, governorate, structureId, dateDebut, dateFin, tags));
    }

    // Endpoint de test temporaire sans authentification
    @GetMapping("/test")
    public ResponseEntity<List<Convention>> testGetAllConventions() {
        try {
            System.out.println("🧪 Test endpoint appelé - récupération de toutes les conventions");
            List<Convention> conventions = conventionService.getAllConventions();
            System.out.println("✅ " + conventions.size() + " conventions trouvées (test)");
            for (Convention c : conventions) {
                System.out.println("  - " + c.getReference() + " (ID: " + c.getId() + ", Créée par: " + c.getCreatedBy() + ")");
            }
            return ResponseEntity.ok(conventions);
        } catch (Exception e) {
            System.out.println("❌ Erreur dans test endpoint: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new java.util.ArrayList<>());
        }
    }

    // Endpoint de test pour vérifier la base de données
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugConventions() {
        System.out.println("🔍 Debug endpoint appelé");
        List<Convention> allConventions = conventionService.getAllConventions();
        System.out.println("📊 Total conventions en base: " + allConventions.size());
        
        // Log détaillé de chaque convention
        for (int i = 0; i < allConventions.size(); i++) {
            Convention c = allConventions.get(i);
            System.out.println("📋 Convention " + (i+1) + ":");
            System.out.println("  - ID: " + c.getId());
            System.out.println("  - Référence: " + c.getReference());
            System.out.println("  - Titre: " + c.getTitle());
            System.out.println("  - Créée par: " + c.getCreatedBy());
            System.out.println("  - Date création: " + c.getCreatedAt());
            System.out.println("  - Statut: " + c.getStatus());
        }
        
        Map<String, Object> debugInfo = new java.util.HashMap<>();
        debugInfo.put("totalConventions", allConventions.size());
        debugInfo.put("conventions", allConventions);
        debugInfo.put("timestamp", java.time.LocalDateTime.now());
        debugInfo.put("database", "demo_db");
        debugInfo.put("collection", "conventions");
        
        return ResponseEntity.ok(debugInfo);
    }

    @PostMapping("/{id}/tags")
    public ResponseEntity<Convention> addTag(@PathVariable String id, @RequestBody Map<String, String> body) {
        String tag = body.get("tag");
        return ResponseEntity.ok(conventionService.addTag(id, tag));
    }

    @DeleteMapping("/{id}/tags")
    public ResponseEntity<Convention> removeTag(@PathVariable String id, @RequestParam String tag) {
        return ResponseEntity.ok(conventionService.removeTag(id, tag));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteConvention(@PathVariable String id) {
        conventionService.deleteConvention(id);
        return ResponseEntity.ok().build();
    }
}
