package com.example.demo.controller;

import com.example.demo.model.NotificationTemplate;
import com.example.demo.service.NotificationTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notification-templates")
public class NotificationTemplateController {

    @Autowired
    private NotificationTemplateService templateService;

    /**
     * Récupérer tous les templates actifs
     */
    @GetMapping
    public ResponseEntity<List<NotificationTemplate>> getAllActiveTemplates() {
        try {
            List<NotificationTemplate> templates = templateService.getAllActiveTemplates();
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            System.err.println("❌ [TEMPLATE_CONTROLLER] Erreur récupération templates: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupérer les templates par type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<NotificationTemplate>> getTemplatesByType(@PathVariable String type) {
        try {
            List<NotificationTemplate> templates = templateService.getTemplatesByType(type);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            System.err.println("❌ [TEMPLATE_CONTROLLER] Erreur récupération templates par type: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupérer un template par ID
     */
    @GetMapping("/{templateId}")
    public ResponseEntity<NotificationTemplate> getTemplate(@PathVariable String templateId) {
        try {
            Optional<NotificationTemplate> template = templateService.getTemplate(templateId);
            if (template.isPresent()) {
                return ResponseEntity.ok(template.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("❌ [TEMPLATE_CONTROLLER] Erreur récupération template: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Créer ou mettre à jour un template
     */
    @PostMapping
    public ResponseEntity<NotificationTemplate> saveTemplate(@RequestBody NotificationTemplate template) {
        try {
            // Valider le template
            if (!templateService.validateTemplate(template)) {
                return ResponseEntity.badRequest().build();
            }
            
            NotificationTemplate savedTemplate = templateService.saveTemplate(template);
            return ResponseEntity.ok(savedTemplate);
        } catch (Exception e) {
            System.err.println("❌ [TEMPLATE_CONTROLLER] Erreur sauvegarde template: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Supprimer un template
     */
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String templateId) {
        try {
            templateService.deleteTemplate(templateId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("❌ [TEMPLATE_CONTROLLER] Erreur suppression template: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Désactiver un template
     */
    @PutMapping("/{templateId}/deactivate")
    public ResponseEntity<Void> deactivateTemplate(@PathVariable String templateId) {
        try {
            templateService.deactivateTemplate(templateId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("❌ [TEMPLATE_CONTROLLER] Erreur désactivation template: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Générer un message à partir d'un template
     */
    @PostMapping("/{templateId}/generate")
    public ResponseEntity<Map<String, String>> generateMessage(
            @PathVariable String templateId,
            @RequestBody Map<String, String> variables) {
        try {
            String subject = templateService.generateSubject(templateId, variables);
            String content = templateService.generateMessage(templateId, variables);
            
            Map<String, String> result = Map.of(
                "subject", subject,
                "content", content
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ [TEMPLATE_CONTROLLER] Erreur génération message: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Extraire les variables d'un template
     */
    @GetMapping("/{templateId}/variables")
    public ResponseEntity<List<String>> getTemplateVariables(@PathVariable String templateId) {
        try {
            List<String> variables = templateService.extractVariables(templateId);
            return ResponseEntity.ok(variables);
        } catch (Exception e) {
            System.err.println("❌ [TEMPLATE_CONTROLLER] Erreur extraction variables: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Réinitialiser les templates par défaut
     */
    @PostMapping("/initialize-defaults")
    public ResponseEntity<Void> initializeDefaultTemplates() {
        try {
            templateService.initializeDefaultTemplates();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("❌ [TEMPLATE_CONTROLLER] Erreur initialisation templates: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}




