package com.example.demo.controller;

import com.example.demo.model.KpiAlert;
import com.example.demo.model.User;
import com.example.demo.repository.KpiAlertRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.InAppNotificationService;
import com.example.demo.service.KpiAlertEmailService;
import com.example.demo.service.KpiAlertSmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur pour la délégation des alertes KPI du Décideur vers le Chef de Projet
 */
@RestController
@RequestMapping("/api/kpi-alerts")
@Slf4j
@CrossOrigin(origins = "*")
public class KpiAlertDelegationController {

    private final KpiAlertRepository kpiAlertRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final InAppNotificationService notificationService;
    private final KpiAlertEmailService emailService;
    private final KpiAlertSmsService smsService;

    @Autowired
    public KpiAlertDelegationController(
            KpiAlertRepository kpiAlertRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate,
            InAppNotificationService notificationService,
            KpiAlertEmailService emailService,
            KpiAlertSmsService smsService) {
        this.kpiAlertRepository = kpiAlertRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    /**
     * Déléguer une alerte KPI au Chef de Projet
     */
    @PostMapping("/{alertId}/delegate")
    @PreAuthorize("hasRole('DECISION_MAKER')")
    public ResponseEntity<Map<String, Object>> delegateAlert(
            @PathVariable String alertId,
            @RequestBody DelegationRequest request,
            Authentication authentication) {
        
        log.info("🔄 Délégation de l'alerte {} au Chef de Projet {}", alertId, request.getProjectManagerId());
        
        try {
            // 1. Récupérer l'alerte
            Optional<KpiAlert> alertOpt = kpiAlertRepository.findById(alertId);
            if (alertOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            KpiAlert alert = alertOpt.get();
            
            // 2. Vérifier que l'alerte est en attente de décision
            if (!"PENDING_DECISION".equals(alert.getAlertStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Cette alerte a déjà été traitée"
                ));
            }
            
            // 3. Récupérer le Chef de Projet
            Optional<User> pmOpt = userRepository.findById(request.getProjectManagerId());
            if (pmOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Chef de Projet introuvable"
                ));
            }
            
            User projectManager = pmOpt.get();
            
            // 4. Récupérer le Décideur
            String decideurId = authentication.getName();
            User decideur = userRepository.findByUsername(decideurId)
                .orElse(userRepository.findById(decideurId).orElse(null));
            
            // 5. Mettre à jour l'alerte
            alert.setAlertStatus("DELEGATED");
            alert.setSentToProjectManager(true);
            alert.setSentToProjectManagerAt(LocalDateTime.now());
            alert.setProjectManagerId(request.getProjectManagerId());
            
            // Ajouter à l'historique
            KpiAlert.AlertAction action = new KpiAlert.AlertAction();
            action.setActionType("DELEGATED");
            action.setPerformedBy(decideur != null ? decideur.getId() : decideurId);
            action.setPerformedByName(decideur != null ? decideur.getName() : "Décideur");
            action.setPerformedAt(LocalDateTime.now());
            action.setComment(request.getComment() != null ? request.getComment() : "Délégué au Chef de Projet");
            
            alert.getActionHistory().add(action);
            
            // Mettre à jour la priorité si spécifiée
            if (request.getPriority() != null) {
                alert.setPriority(request.getPriority());
            }
            
            // 6. Sauvegarder
            KpiAlert savedAlert = kpiAlertRepository.save(alert);
            log.info("✅ Alerte {} déléguée avec succès", alertId);
            
            // 7. Notifier le Chef de Projet par WebSocket
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "KPI_ALERT_DELEGATED");
            notification.put("alertId", savedAlert.getId());
            notification.put("kpiName", savedAlert.getKpiName());
            notification.put("severity", savedAlert.getSeverity());
            notification.put("priority", savedAlert.getPriority());
            notification.put("message", savedAlert.getMessage());
            notification.put("from", decideur != null ? decideur.getName() : "Décideur");
            notification.put("comment", request.getComment());
            notification.put("timestamp", LocalDateTime.now().toString());
            
            messagingTemplate.convertAndSendToUser(
                projectManager.getUsername(),
                "/queue/kpi-alerts",
                notification
            );
            
            log.info("📨 Notification WebSocket envoyée au Chef de Projet: {}", projectManager.getUsername());
            
            // 8. Créer notification in-app
            if (notificationService != null) {
                try {
                    notificationService.createNotification(
                        projectManager.getId(),
                        "KPI_ALERT_DELEGATED",
                        "🚨 Alerte KPI Déléguée: " + savedAlert.getKpiName(),
                        String.format("Le Décideur %s vous a délégué une alerte KPI. Priorité: %s. %s",
                            decideur != null ? decideur.getName() : "Décideur",
                            savedAlert.getPriority(),
                            request.getComment() != null ? request.getComment() : ""),
                        savedAlert.getPriority(),
                        "KPI_ALERT"
                    );
                    log.info("✅ Notification in-app créée pour le Chef de Projet");
                } catch (Exception e) {
                    log.error("❌ Erreur création notification in-app: {}", e.getMessage());
                }
            }
            
            // 9. Envoyer Email de délégation
            if (emailService != null && projectManager.getEmail() != null) {
                try {
                    emailService.sendDelegationEmail(
                        projectManager.getEmail(),
                        projectManager.getName(),
                        decideur != null ? decideur.getName() : "Décideur",
                        savedAlert.getKpiName(),
                        savedAlert.getMessage(),
                        request.getComment(),
                        savedAlert.getPriority(),
                        savedAlert.getId()
                    );
                    log.info("✅ Email de délégation envoyé à: {}", projectManager.getEmail());
                } catch (Exception e) {
                    log.error("❌ Erreur envoi email délégation: {}", e.getMessage());
                }
            }
            
            // 10. Envoyer SMS si priorité urgente/critique
            if (smsService != null && projectManager.getPhoneNumber() != null && 
                ("URGENT".equals(savedAlert.getPriority()) || "CRITICAL".equals(savedAlert.getPriority()))) {
                try {
                    smsService.sendDelegationSms(
                        projectManager.getPhoneNumber(),
                        projectManager.getName(),
                        savedAlert.getKpiName(),
                        savedAlert.getPriority()
                    );
                    log.info("✅ SMS de délégation envoyé à: {}", projectManager.getPhoneNumber());
                } catch (Exception e) {
                    log.error("❌ Erreur envoi SMS délégation: {}", e.getMessage());
                }
            }
            
            // 9. Réponse
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Alerte déléguée avec succès au Chef de Projet");
            response.put("alert", savedAlert);
            response.put("projectManager", Map.of(
                "id", projectManager.getId(),
                "name", projectManager.getName(),
                "email", projectManager.getEmail()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de la délégation: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Erreur lors de la délégation: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Lister les Chefs de Projet disponibles pour délégation
     */
    @GetMapping("/available-project-managers")
    @PreAuthorize("hasRole('DECISION_MAKER')")
    public ResponseEntity<Map<String, Object>> getAvailableProjectManagers() {
        try {
            var projectManagers = userRepository.findByRoles_Name(
                com.example.demo.enums.ERole.ROLE_PROJECT_MANAGER
            );
            
            var pmList = projectManagers.stream()
                .map(pm -> Map.of(
                    "id", pm.getId(),
                    "name", pm.getName(),
                    "email", pm.getEmail(),
                    "username", pm.getUsername()
                ))
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "projectManagers", pmList,
                "count", pmList.size()
            ));
            
        } catch (Exception e) {
            log.error("❌ Erreur récupération Chefs de Projet: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * DTO pour la requête de délégation
     */
    public static class DelegationRequest {
        private String projectManagerId;
        private String comment;
        private String priority;
        
        public String getProjectManagerId() { return projectManagerId; }
        public void setProjectManagerId(String projectManagerId) { this.projectManagerId = projectManagerId; }
        
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }
}
