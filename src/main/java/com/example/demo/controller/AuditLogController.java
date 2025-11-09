package com.example.demo.controller;

import com.example.demo.model.AuditLog;
import com.example.demo.service.AuditLogService;
import com.example.demo.service.AccessControlService;
import com.example.demo.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
public class AuditLogController {
    @Autowired
    private AuditLogService auditLogService;
    
    @Autowired
    private AccessControlService accessControlService;

    /**
     * GET /api/audit
     * Récupérer les logs d'audit avec filtres et pagination
     * SÉCURISÉ : Les commerciaux voient uniquement leur propre historique
     */
    @GetMapping
    public Page<AuditLog> getAuditLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        System.out.println("========================================");
        System.out.println("📜 [GET AUDIT LOGS] Récupération de l'historique");
        
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime start = null;
        LocalDateTime end = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        
        try {
            if (startDate != null && !startDate.isEmpty()) {
                start = LocalDateTime.parse(startDate, formatter);
            }
            if (endDate != null && !endDate.isEmpty()) {
                end = LocalDateTime.parse(endDate, formatter);
            }
        } catch (Exception ignored) {}
        
        // FILTRAGE PAR RÔLE
        String filteredUsername = username;
        
        if (userPrincipal != null) {
            System.out.println("👤 Utilisateur: " + userPrincipal.getUsername());
            
            if (accessControlService.canViewOnlyOwnData()) {
                // COMMERCIAL: Voir UNIQUEMENT son propre historique
                filteredUsername = userPrincipal.getUsername();
                System.out.println("⚠️  COMMERCIAL - Filtrage forcé par username: " + filteredUsername);
            } else if (accessControlService.canViewAllData()) {
                // Chef de projet, Décideur, Admin: Voir tout l'historique
                System.out.println("✅ Utilisateur autorisé à voir TOUT l'historique");
                // Garder le username du filtre (peut être null pour tout voir)
            }
        }
        
        Page<AuditLog> logs = auditLogService.searchLogs(action, entityType, filteredUsername, start, end, pageable);
        System.out.println("📊 Nombre de logs retournés: " + logs.getTotalElements());
        System.out.println("========================================");
        
        return logs;
    }

    /**
     * GET /api/audit/user/{username}
     * Récupérer les logs d'un utilisateur spécifique
     * SÉCURISÉ : Les commerciaux peuvent uniquement voir leur propre historique
     */
    @GetMapping("/user/{username}")
    public Page<AuditLog> getUserAuditLogs(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        System.out.println("========================================");
        System.out.println("📜 [GET USER AUDIT] Historique de l'utilisateur: " + username);
        
        Pageable pageable = PageRequest.of(page, size);
        
        // SÉCURITÉ : Vérifier si l'utilisateur peut accéder à cet historique
        if (userPrincipal != null && accessControlService.canViewOnlyOwnData()) {
            // COMMERCIAL: Peut uniquement voir SON PROPRE historique
            String currentUsername = userPrincipal.getUsername();
            if (!currentUsername.equals(username)) {
                System.out.println("❌ ACCÈS REFUSÉ - Commercial " + currentUsername + 
                                 " tente d'accéder à l'historique de " + username);
                // Retourner une page vide au lieu d'une erreur
                return Page.empty(pageable);
            }
            System.out.println("✅ Accès autorisé - Commercial accède à son propre historique");
        } else {
            System.out.println("✅ Accès autorisé - Utilisateur peut voir tout l'historique");
        }
        
        Page<AuditLog> logs = auditLogService.searchLogs(null, null, username, null, null, pageable);
        System.out.println("📊 Nombre de logs retournés: " + logs.getTotalElements());
        System.out.println("========================================");
        
        return logs;
    }

    /**
     * POST /api/audit
     * Créer une nouvelle entrée d'audit
     */
    @PostMapping
    public AuditLog createAuditLog(@RequestBody AuditLog auditLog) {
        if (auditLog.getTimestamp() == null) {
            auditLog.setTimestamp(LocalDateTime.now());
        }
        return auditLogService.save(auditLog);
    }
}
