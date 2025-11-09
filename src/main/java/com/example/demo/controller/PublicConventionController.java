package com.example.demo.controller;

import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.service.ConventionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Contrôleur PUBLIC pour n8n (sans authentification)
 */
@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*")
public class PublicConventionController {

    @Autowired
    private ConventionService conventionService;
    
    @Autowired
    private ConventionRepository conventionRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;

    /**
     * GET /api/public/conventions - Récupérer TOUTES les conventions (pour n8n)
     */
    @GetMapping("/conventions")
    public ResponseEntity<Map<String, Object>> getAllConventions() {
        System.out.println("========================================");
        System.out.println("🌐 [PUBLIC] GET /api/public/conventions");
        System.out.println("🔓 Accès public (n8n)");
        
        try {
            List<Convention> conventions = conventionService.getAllConventions();
            System.out.println("✅ " + conventions.size() + " conventions trouvées");
            System.out.println("========================================");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ " + conventions.size() + " conventions trouvées");
            response.put("data", conventions);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            errorResponse.put("data", new java.util.ArrayList<>());
            
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * POST /api/public/conventions - Créer une convention (pour n8n/chatbot)
     */
    @PostMapping("/conventions")
    public ResponseEntity<Map<String, Object>> createConvention(@RequestBody Map<String, Object> conventionData) {
        System.out.println("========================================");
        System.out.println("🌐 [PUBLIC] POST /api/public/conventions");
        System.out.println("📝 Données reçues: " + conventionData);
        
        try {
            // Extraire le client
            String clientName = (String) conventionData.getOrDefault("client", "Client Test");
            String title = (String) conventionData.getOrDefault("title", "Convention pour " + clientName);
            
            // Vérifier si une convention existe déjà pour ce client
            List<Convention> existingConventions = conventionRepository.findByClient(clientName);
            if (!existingConventions.isEmpty()) {
                System.out.println("⚠️ Convention existe déjà pour le client: " + clientName);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "⚠️ Une convention existe déjà pour le client \"" + clientName + "\".\n" +
                                       "📋 Référence: " + existingConventions.get(0).getReference() + "\n" +
                                       "💰 Montant: " + existingConventions.get(0).getAmount() + " DT\n" +
                                       "📅 Créée le: " + existingConventions.get(0).getCreatedAt());
                response.put("data", existingConventions.get(0));
                
                return ResponseEntity.ok(response);
            }
            
            // Créer une convention simplifiée
            Convention convention = new Convention();
            
            // Données obligatoires
            convention.setTitle(title);
            convention.setReference(generateReference());
            
            // Montant
            BigDecimal amount = BigDecimal.ZERO;
            Object amountObj = conventionData.get("amount");
            if (amountObj != null) {
                if (amountObj instanceof Number) {
                    amount = BigDecimal.valueOf(((Number) amountObj).doubleValue());
                } else if (amountObj instanceof String) {
                    try {
                        amount = new BigDecimal((String) amountObj);
                    } catch (NumberFormatException e) {
                        amount = BigDecimal.ZERO;
                    }
                }
            }
            convention.setAmount(amount);
            
            // Structure et zone
            convention.setStructureId((String) conventionData.getOrDefault("structureId", "STRUCT-001"));
            convention.setZoneGeographiqueId((String) conventionData.getOrDefault("zoneGeographiqueId", "Tunis"));
            convention.setGovernorate((String) conventionData.getOrDefault("governorate", "Tunis"));
            
            // Dates
            convention.setStartDate(java.time.LocalDate.now());
            convention.setEndDate(java.time.LocalDate.now().plusMonths(1));
            convention.setDueDate(java.time.LocalDate.now().plusMonths(1));
            
            // Statut et autres
            convention.setStatus("DRAFT");
            convention.setPaymentStatus("PENDING");
            convention.setClient(clientName);
            convention.setDescription((String) conventionData.getOrDefault("description", ""));
            convention.setCreatedBy((String) conventionData.getOrDefault("username", "chatbot"));
            convention.setCreatedAt(java.time.LocalDate.now());
            
            // Sauvegarder la convention
            Convention saved = conventionRepository.save(convention);
            
            System.out.println("✅ Convention créée: " + saved.getId());
            
            // Créer automatiquement une facture
            Invoice invoice = new Invoice();
            invoice.setInvoiceNumber(generateInvoiceNumber());
            invoice.setConventionId(saved.getId());
            invoice.setAmount(saved.getAmount());
            invoice.setStatus("PENDING");
            invoice.setDueDate(saved.getDueDate());
            invoice.setIssueDate(java.time.LocalDate.now());
            invoice.setCreatedBy(saved.getCreatedBy());
            invoice.setCreatedAt(java.time.LocalDate.now());
            
            Invoice savedInvoice = invoiceRepository.save(invoice);
            
            System.out.println("✅ Facture créée automatiquement: " + savedInvoice.getInvoiceNumber());
            System.out.println("========================================");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Convention créée avec succès !\n" +
                                   "📋 Référence: " + saved.getReference() + "\n" +
                                   "💰 Montant: " + saved.getAmount() + " DT\n" +
                                   "🧾 Facture générée: " + savedInvoice.getInvoiceNumber());
            response.put("data", saved);
            response.put("invoice", savedInvoice);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur création: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * PUT /api/public/conventions/{id} - Modifier une convention (pour n8n/chatbot)
     */
    @PutMapping("/conventions/{id}")
    public ResponseEntity<Map<String, Object>> updateConvention(
            @PathVariable String id,
            @RequestBody Map<String, Object> conventionData) {
        System.out.println("========================================");
        System.out.println("🌐 [PUBLIC] PUT /api/public/conventions/" + id);
        System.out.println("📝 Données reçues: " + conventionData);
        
        try {
            // Récupérer la convention existante
            Convention convention = conventionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Convention non trouvée avec l'ID: " + id));
            
            System.out.println("📋 Convention trouvée: " + convention.getReference());
            
            // Mettre à jour les champs si présents
            if (conventionData.containsKey("amount")) {
                Object amountObj = conventionData.get("amount");
                if (amountObj instanceof Number) {
                    convention.setAmount(BigDecimal.valueOf(((Number) amountObj).doubleValue()));
                } else if (amountObj instanceof String) {
                    try {
                        convention.setAmount(new BigDecimal((String) amountObj));
                    } catch (NumberFormatException e) {
                        // Ignorer si invalide
                    }
                }
                System.out.println("💰 Montant mis à jour: " + convention.getAmount());
            }
            
            if (conventionData.containsKey("status")) {
                String status = (String) conventionData.get("status");
                convention.setStatus(status.toUpperCase());
                System.out.println("📊 Statut mis à jour: " + convention.getStatus());
            }
            
            if (conventionData.containsKey("paymentStatus")) {
                String paymentStatus = (String) conventionData.get("paymentStatus");
                convention.setPaymentStatus(paymentStatus.toUpperCase());
                System.out.println("💳 Statut paiement mis à jour: " + convention.getPaymentStatus());
            }
            
            if (conventionData.containsKey("title")) {
                convention.setTitle((String) conventionData.get("title"));
            }
            
            if (conventionData.containsKey("description")) {
                convention.setDescription((String) conventionData.get("description"));
            }
            
            if (conventionData.containsKey("client")) {
                convention.setClient((String) conventionData.get("client"));
            }
            
            // Mettre à jour la date de modification
            convention.setUpdatedAt(java.time.LocalDate.now());
            convention.setLastModifiedBy((String) conventionData.getOrDefault("username", "chatbot"));
            
            // Sauvegarder
            Convention updated = conventionRepository.save(convention);
            
            System.out.println("✅ Convention modifiée avec succès");
            System.out.println("========================================");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Convention modifiée avec succès !\n" +
                                   "📋 Référence: " + updated.getReference() + "\n" +
                                   "💰 Montant: " + updated.getAmount() + " DT\n" +
                                   "📊 Statut: " + updated.getStatus());
            response.put("data", updated);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur modification: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * DELETE /api/public/conventions/{id} - Supprimer une convention (pour n8n/chatbot)
     */
    @DeleteMapping("/conventions/{id}")
    public ResponseEntity<Map<String, Object>> deleteConvention(@PathVariable String id) {
        System.out.println("========================================");
        System.out.println("🌐 [PUBLIC] DELETE /api/public/conventions/" + id);
        
        try {
            // Vérifier que la convention existe
            Convention convention = conventionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Convention non trouvée avec l'ID: " + id));
            
            String reference = convention.getReference();
            
            // Supprimer
            conventionRepository.deleteById(id);
            
            System.out.println("✅ Convention supprimée: " + reference);
            System.out.println("========================================");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Convention supprimée avec succès !\n📋 Référence: " + reference);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur suppression: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * POST /api/public/conventions/execute - Endpoint universel pour toutes les opérations
     * Alternative pour n8n qui ne supporte que POST
     */
    @PostMapping("/conventions/execute")
    public ResponseEntity<Map<String, Object>> executeOperation(@RequestBody Map<String, Object> request) {
        System.out.println("========================================");
        System.out.println("🌐 [PUBLIC] POST /api/public/conventions/execute");
        System.out.println("📝 Requête: " + request);
        
        try {
            String operation = (String) request.get("operation");
            String id = (String) request.get("id");
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getOrDefault("data", new HashMap<>());
            
            System.out.println("🔧 Opération: " + operation);
            System.out.println("🆔 ID: " + id);
            
            switch (operation != null ? operation.toLowerCase() : "") {
                case "create":
                    return createConvention(data);
                    
                case "read":
                    return getAllConventions();
                    
                case "update":
                    if (id == null || id.isEmpty()) {
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("success", false);
                        errorResponse.put("message", "❌ ID requis pour l'opération UPDATE");
                        return ResponseEntity.ok(errorResponse);
                    }
                    return updateConvention(id, data);
                    
                case "delete":
                    if (id == null || id.isEmpty()) {
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("success", false);
                        errorResponse.put("message", "❌ ID requis pour l'opération DELETE");
                        return ResponseEntity.ok(errorResponse);
                    }
                    return deleteConvention(id);
                    
                default:
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "❌ Opération inconnue: " + operation);
                    return ResponseEntity.ok(errorResponse);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur executeOperation: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * Génère une référence unique pour la convention
     */
    private String generateReference() {
        return "CONV-" + System.currentTimeMillis();
    }
    
    /**
     * Génère un numéro de facture unique
     */
    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis();
    }
}
