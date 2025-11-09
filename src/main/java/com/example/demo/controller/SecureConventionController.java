package com.example.demo.controller;

import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.service.ConventionService;
import com.example.demo.service.impl.InvoiceServiceImpl;
import com.example.demo.service.AccessControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Contrôleur sécurisé pour les conventions et factures
 * 
 * Applique le filtrage automatique selon le rôle:
 * - COMMERCIAL: Voit uniquement ses propres données
 * - PROJECT_MANAGER: Voit toutes les données
 * - DECISION_MAKER: Voit toutes les données
 * - ADMIN: Voit toutes les données
 */
@RestController
@RequestMapping("/api/secure")
@CrossOrigin(origins = "*")
public class SecureConventionController {

    @Autowired
    private ConventionService conventionService;

    @Autowired
    private InvoiceServiceImpl invoiceService;

    @Autowired
    private AccessControlService accessControlService;

    /**
     * Récupère les informations de l'utilisateur connecté
     * 
     * @return Informations complètes de l'utilisateur (nom, email, rôle, etc.)
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        System.out.println("\n🔒 [SECURE] GET /api/secure/me");
        
        com.example.demo.model.User user = accessControlService.getCurrentUser();
        
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Non authentifié",
                "message", "Aucun utilisateur connecté"
            ));
        }
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("name", user.getName());
        userInfo.put("email", user.getEmail());
        userInfo.put("phoneNumber", user.getPhoneNumber());
        userInfo.put("country", user.getCountry());
        
        // Extraire les rôles
        List<String> roles = new java.util.ArrayList<>();
        if (user.getRoles() != null) {
            user.getRoles().forEach(role -> roles.add(role.getName().name()));
        }
        userInfo.put("roles", roles);
        
        // Informations de rôle simplifiées
        userInfo.put("isCommercial", accessControlService.isCommercial());
        userInfo.put("isProjectManager", accessControlService.isProjectManager());
        userInfo.put("isDecisionMaker", accessControlService.isDecisionMaker());
        userInfo.put("isAdmin", accessControlService.isAdmin());
        userInfo.put("canViewAllData", accessControlService.canViewAllData());
        
        System.out.println("✅ Utilisateur: " + user.getName() + " (" + user.getUsername() + ")");
        
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Récupère les conventions selon le rôle de l'utilisateur
     * 
     * @return Liste des conventions filtrées selon le rôle
     */
    @GetMapping("/conventions")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'PROJECT_MANAGER', 'DECISION_MAKER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<Convention>> getConventions() {
        System.out.println("\n🔒 [SECURE] GET /api/secure/conventions");
        
        List<Convention> conventions = conventionService.getConventionsForCurrentUser();
        
        return ResponseEntity.ok(conventions);
    }

    /**
     * Récupère les factures selon le rôle de l'utilisateur
     * 
     * @return Liste des factures filtrées selon le rôle
     */
    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'PROJECT_MANAGER', 'DECISION_MAKER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<Invoice>> getInvoices() {
        System.out.println("\n🔒 [SECURE] GET /api/secure/invoices");
        
        List<Invoice> invoices = invoiceService.getInvoicesForCurrentUser();
        
        return ResponseEntity.ok(invoices);
    }

    /**
     * Récupère les statistiques selon le rôle de l'utilisateur
     * 
     * @return Statistiques filtrées selon le rôle
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'PROJECT_MANAGER', 'DECISION_MAKER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        System.out.println("\n🔒 [SECURE] GET /api/secure/stats");
        
        List<Convention> conventions = conventionService.getConventionsForCurrentUser();
        List<Invoice> invoices = invoiceService.getInvoicesForCurrentUser();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConventions", conventions.size());
        stats.put("totalInvoices", invoices.size());
        stats.put("userRole", accessControlService.isCommercial() ? "COMMERCIAL" :
                             accessControlService.isProjectManager() ? "PROJECT_MANAGER" :
                             accessControlService.isDecisionMaker() ? "DECISION_MAKER" : "ADMIN");
        stats.put("canViewAllData", accessControlService.canViewAllData());
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Vérifie si l'utilisateur peut accéder à une convention spécifique
     * 
     * @param id ID de la convention
     * @return Convention si autorisé, 403 sinon
     */
    @GetMapping("/conventions/{id}")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'PROJECT_MANAGER', 'DECISION_MAKER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getConventionById(@PathVariable String id) {
        System.out.println("\n🔒 [SECURE] GET /api/secure/conventions/" + id);
        
        Convention convention = conventionService.getConventionById(id);
        
        if (convention == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Vérifier si l'utilisateur peut accéder à cette convention
        if (!accessControlService.canAccessResource(convention.getCreatedBy())) {
            System.out.println("❌ Accès refusé - Convention créée par: " + convention.getCreatedBy());
            return ResponseEntity.status(403).body(Map.of(
                "error", "Accès refusé",
                "message", "Vous n'avez pas l'autorisation d'accéder à cette convention"
            ));
        }
        
        return ResponseEntity.ok(convention);
    }

    /**
     * Vérifie si l'utilisateur peut accéder à une facture spécifique
     * 
     * @param id ID de la facture
     * @return Facture si autorisé, 403 sinon
     */
    @GetMapping("/invoices/{id}")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'PROJECT_MANAGER', 'DECISION_MAKER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getInvoiceById(@PathVariable String id) {
        System.out.println("\n🔒 [SECURE] GET /api/secure/invoices/" + id);
        
        Invoice invoice = invoiceService.getInvoiceById(id);
        
        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Vérifier si l'utilisateur peut accéder à cette facture
        if (!accessControlService.canAccessResource(invoice.getCreatedBy())) {
            System.out.println("❌ Accès refusé - Facture créée par: " + invoice.getCreatedBy());
            return ResponseEntity.status(403).body(Map.of(
                "error", "Accès refusé",
                "message", "Vous n'avez pas l'autorisation d'accéder à cette facture"
            ));
        }
        
        return ResponseEntity.ok(invoice);
    }
}
