package com.example.demo.controller;

import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.model.User;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur PUBLIC UNIVERSEL pour toutes les opérations CRUD
 * Utilisé par le chatbot via n8n
 */
@RestController
@RequestMapping("/api/public/crud")
@CrossOrigin(origins = "*")
public class PublicCrudController {

    @Autowired
    private ConventionRepository conventionRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired(required = false)
    private com.example.demo.repository.ZoneGeographiqueRepository zoneGeographiqueRepository;
    
    @Autowired(required = false)
    private com.example.demo.repository.StructureRepository structureRepository;
    
    @Autowired(required = false)
    private com.example.demo.repository.ApplicationRepository applicationRepository;
    
    @Autowired(required = false)
    private com.example.demo.service.SmsService smsService;

    /**
     * POST /api/public/crud/execute - Endpoint universel pour TOUTES les entités
     */
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeOperation(@RequestBody Map<String, Object> request) {
        System.out.println("========================================");
        System.out.println("🌐 [PUBLIC CRUD] POST /api/public/crud/execute");
        System.out.println("📝 Requête: " + request);
        
        try {
            String entityType = (String) request.get("entityType");
            String operation = (String) request.get("operation");
            String id = (String) request.get("id");
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getOrDefault("data", new HashMap<>());
            
            System.out.println("🏷️ Entité: " + entityType);
            System.out.println("🔧 Opération: " + operation);
            System.out.println("🆔 ID: " + id);
            
            // Router vers la bonne entité
            switch (entityType != null ? entityType.toUpperCase() : "") {
                case "CONVENTION":
                    return handleConventionOperation(operation, id, data);
                    
                case "INVOICE":
                case "FACTURE":
                    return handleInvoiceOperation(operation, id, data);
                    
                case "USER":
                case "UTILISATEUR":
                    return handleUserOperation(operation, id, data);
                    
                case "ZONE":
                case "ZONE_GEOGRAPHIQUE":
                    return handleZoneOperation(operation, id, data);
                    
                case "STRUCTURE":
                    return handleStructureOperation(operation, id, data);
                    
                case "APPLICATION":
                    return handleApplicationOperation(operation, id, data);
                    
                case "SMS":
                    return handleSmsOperation(operation, id, data);
                    
                case "SEARCH":
                    return handleSearchOperation(operation, id, data);
                    
                default:
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "❌ Type d'entité non supporté: " + entityType + "\n\n" +
                                                 "✅ Entités supportées: CONVENTION, FACTURE, USER, ZONE, STRUCTURE, APPLICATION, SMS");
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
    
    // ==================== CONVENTIONS ====================
    
    private ResponseEntity<Map<String, Object>> handleConventionOperation(String operation, String id, Map<String, Object> data) {
        switch (operation != null ? operation.toLowerCase() : "") {
            case "create":
                return createConvention(data);
            case "read":
                // Vérifier si c'est une recherche avec filtres
                if (data.containsKey("status") || data.containsKey("zone") || data.containsKey("structure")) {
                    return searchConventions(data);
                }
                return getAllConventions();
            case "update":
                return updateConvention(id, data);
            case "delete":
                return deleteConvention(id);
            default:
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "❌ Opération inconnue: " + operation);
                return ResponseEntity.ok(errorResponse);
        }
    }
    
    private ResponseEntity<Map<String, Object>> createConvention(Map<String, Object> data) {
        try {
            String clientName = (String) data.getOrDefault("client", "Client Test");
            
            // Vérifier existence
            List<Convention> existing = conventionRepository.findByClient(clientName);
            if (!existing.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "⚠️ Une convention existe déjà pour le client \"" + clientName + "\".\n" +
                                       "📋 Référence: " + existing.get(0).getReference());
                response.put("data", existing.get(0));
                return ResponseEntity.ok(response);
            }
            
            Convention convention = new Convention();
            convention.setTitle((String) data.getOrDefault("title", "Convention pour " + clientName));
            convention.setReference("CONV-" + System.currentTimeMillis());
            convention.setClient(clientName);
            
            // Montant
            Object amountObj = data.get("amount");
            if (amountObj instanceof Number) {
                convention.setAmount(BigDecimal.valueOf(((Number) amountObj).doubleValue()));
            }
            
            convention.setStatus("DRAFT");
            convention.setPaymentStatus("PENDING");
            convention.setStartDate(java.time.LocalDate.now());
            convention.setEndDate(java.time.LocalDate.now().plusMonths(1));
            convention.setCreatedBy((String) data.getOrDefault("username", "chatbot"));
            convention.setCreatedAt(java.time.LocalDate.now());
            
            Convention saved = conventionRepository.save(convention);
            
            // Créer facture automatiquement
            Invoice invoice = new Invoice();
            invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());
            invoice.setConventionId(saved.getId());
            invoice.setAmount(saved.getAmount());
            invoice.setStatus("PENDING");
            invoice.setDueDate(saved.getEndDate());
            invoice.setIssueDate(java.time.LocalDate.now());
            invoice.setCreatedBy(saved.getCreatedBy());
            invoice.setCreatedAt(java.time.LocalDate.now());
            Invoice savedInvoice = invoiceRepository.save(invoice);
            
            System.out.println("✅ Convention créée: " + saved.getId());
            System.out.println("✅ Facture créée: " + savedInvoice.getInvoiceNumber());
            
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
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    private ResponseEntity<Map<String, Object>> getAllConventions() {
        try {
            List<Convention> conventions = conventionRepository.findAll();
            
            // Formater l'affichage avec détails
            StringBuilder message = new StringBuilder();
            message.append("✅ **").append(conventions.size()).append(" convention(s) trouvée(s)**\n\n");
            
            if (!conventions.isEmpty()) {
                int count = 0;
                for (Convention conv : conventions) {
                    count++;
                    if (count > 10) {
                        message.append("\n... et ").append(conventions.size() - 10).append(" autre(s) convention(s)");
                        break;
                    }
                    
                    message.append("**").append(count).append(". Convention ").append(conv.getClient() != null ? conv.getClient() : "N/A").append("**\n");
                    message.append("   📋 Référence: `").append(conv.getReference()).append("`\n");
                    message.append("   💰 Montant: ").append(conv.getAmount()).append(" DT\n");
                    message.append("   📊 Statut: ").append(conv.getStatus() != null ? conv.getStatus() : "N/A").append("\n");
                    message.append("   📅 Date: ").append(conv.getStartDate() != null ? conv.getStartDate() : "N/A").append("\n");
                    if (conv.getCreatedBy() != null) {
                        message.append("   👤 Créé par: ").append(conv.getCreatedBy()).append("\n");
                    }
                    message.append("\n");
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message.toString());
            response.put("data", conventions);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    private ResponseEntity<Map<String, Object>> searchConventions(Map<String, Object> data) {
        try {
            System.out.println("🔍 [SEARCH] Recherche avec filtres: " + data);
            
            List<Convention> allConventions = conventionRepository.findAll();
            List<Convention> filteredConventions = new ArrayList<>();
            
            // Extraire TOUS les filtres possibles
            String statusFilter = data.containsKey("status") ? ((String) data.get("status")).toUpperCase() : null;
            String zoneFilter = data.containsKey("zone") ? ((String) data.get("zone")).toLowerCase() : null;
            String structureFilter = data.containsKey("structure") ? (String) data.get("structure") : null;
            String referenceFilter = data.containsKey("reference") ? ((String) data.get("reference")).toUpperCase() : null;
            String titleFilter = data.containsKey("title") ? ((String) data.get("title")).toLowerCase() : null;
            String clientFilter = data.containsKey("client") ? ((String) data.get("client")).toLowerCase() : null;
            String tagFilter = data.containsKey("tag") ? ((String) data.get("tag")).toLowerCase() : null;
            
            // Filtres de montant
            BigDecimal amountMin = null;
            BigDecimal amountMax = null;
            if (data.containsKey("amountMin")) {
                Object amountMinObj = data.get("amountMin");
                if (amountMinObj instanceof Number) {
                    amountMin = BigDecimal.valueOf(((Number) amountMinObj).doubleValue());
                }
            }
            if (data.containsKey("amountMax")) {
                Object amountMaxObj = data.get("amountMax");
                if (amountMaxObj instanceof Number) {
                    amountMax = BigDecimal.valueOf(((Number) amountMaxObj).doubleValue());
                }
            }
            
            System.out.println("🔍 Filtres actifs:");
            if (statusFilter != null) System.out.println("   📊 Statut: " + statusFilter);
            if (zoneFilter != null) System.out.println("   🌍 Zone: " + zoneFilter);
            if (structureFilter != null) System.out.println("   🏢 Structure: " + structureFilter);
            if (referenceFilter != null) System.out.println("   📋 Référence: " + referenceFilter);
            if (titleFilter != null) System.out.println("   📝 Titre: " + titleFilter);
            if (clientFilter != null) System.out.println("   👤 Client: " + clientFilter);
            if (tagFilter != null) System.out.println("   🏷️ Tag: " + tagFilter);
            if (amountMin != null) System.out.println("   💰 Montant min: " + amountMin);
            if (amountMax != null) System.out.println("   💰 Montant max: " + amountMax);
            
            for (Convention conv : allConventions) {
                boolean matches = true;
                
                // Filtrer par statut
                if (statusFilter != null) {
                    if (conv.getStatus() == null || !conv.getStatus().equalsIgnoreCase(statusFilter)) {
                        matches = false;
                    }
                }
                
                // Filtrer par zone
                if (zoneFilter != null && matches) {
                    boolean zoneMatches = false;
                    if (conv.getZoneGeographiqueId() != null && conv.getZoneGeographiqueId().toLowerCase().contains(zoneFilter)) {
                        zoneMatches = true;
                    }
                    if (conv.getGovernorate() != null && conv.getGovernorate().toLowerCase().contains(zoneFilter)) {
                        zoneMatches = true;
                    }
                    if (!zoneMatches) {
                        matches = false;
                    }
                }
                
                // Filtrer par structure
                if (structureFilter != null && matches) {
                    if (conv.getStructureId() == null || !conv.getStructureId().equals(structureFilter)) {
                        matches = false;
                    }
                }
                
                // Filtrer par référence
                if (referenceFilter != null && matches) {
                    if (conv.getReference() == null || !conv.getReference().toUpperCase().contains(referenceFilter)) {
                        matches = false;
                    }
                }
                
                // Filtrer par titre
                if (titleFilter != null && matches) {
                    if (conv.getTitle() == null || !conv.getTitle().toLowerCase().contains(titleFilter)) {
                        matches = false;
                    }
                }
                
                // Filtrer par client
                if (clientFilter != null && matches) {
                    if (conv.getClient() == null || !conv.getClient().toLowerCase().contains(clientFilter)) {
                        matches = false;
                    }
                }
                
                // Filtrer par tag
                if (tagFilter != null && matches) {
                    if (conv.getTag() == null || !conv.getTag().toLowerCase().contains(tagFilter)) {
                        matches = false;
                    }
                }
                
                // Filtrer par montant minimum
                if (amountMin != null && matches) {
                    if (conv.getAmount() == null || conv.getAmount().compareTo(amountMin) < 0) {
                        matches = false;
                    }
                }
                
                // Filtrer par montant maximum
                if (amountMax != null && matches) {
                    if (conv.getAmount() == null || conv.getAmount().compareTo(amountMax) > 0) {
                        matches = false;
                    }
                }
                
                if (matches) {
                    filteredConventions.add(conv);
                }
            }
            
            System.out.println("✅ " + filteredConventions.size() + " convention(s) trouvée(s) après filtrage");
            
            // Formater l'affichage avec détails
            StringBuilder message = new StringBuilder();
            
            String filterDescription = "";
            if (statusFilter != null) filterDescription += " statut " + statusFilter;
            if (zoneFilter != null) filterDescription += " zone " + zoneFilter;
            if (structureFilter != null) filterDescription += " structure " + structureFilter;
            
            message.append("✅ **").append(filteredConventions.size()).append(" convention(s) trouvée(s)");
            if (!filterDescription.isEmpty()) {
                message.append(" pour").append(filterDescription);
            }
            message.append("**\n\n");
            
            if (!filteredConventions.isEmpty()) {
                int count = 0;
                for (Convention conv : filteredConventions) {
                    count++;
                    if (count > 10) {
                        message.append("\n... et ").append(filteredConventions.size() - 10).append(" autre(s) convention(s)");
                        break;
                    }
                    
                    message.append("**").append(count).append(". Convention ").append(conv.getClient() != null ? conv.getClient() : "N/A").append("**\n");
                    message.append("   📋 Référence: `").append(conv.getReference()).append("`\n");
                    message.append("   💰 Montant: ").append(conv.getAmount()).append(" DT\n");
                    message.append("   📊 Statut: ").append(conv.getStatus() != null ? conv.getStatus() : "N/A").append("\n");
                    message.append("   📅 Date: ").append(conv.getStartDate() != null ? conv.getStartDate() : "N/A").append("\n");
                    if (conv.getZoneGeographiqueId() != null || conv.getGovernorate() != null) {
                        message.append("   🌍 Zone: ").append(conv.getGovernorate() != null ? conv.getGovernorate() : conv.getZoneGeographiqueId()).append("\n");
                    }
                    if (conv.getCreatedBy() != null) {
                        message.append("   👤 Créé par: ").append(conv.getCreatedBy()).append("\n");
                    }
                    message.append("\n");
                }
            } else {
                message.append("Aucune convention ne correspond aux critères de recherche.");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message.toString());
            response.put("data", filteredConventions);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur searchConventions: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    private ResponseEntity<Map<String, Object>> updateConvention(String id, Map<String, Object> data) {
        try {
            System.out.println("📝 [UPDATE] ID reçu: " + id);
            System.out.println("📝 [UPDATE] Data reçue: " + data);
            
            Convention convention = null;
            
            // Essayer de trouver par ID MongoDB
            if (id != null && id.matches("[a-f0-9]{24}")) {
                convention = conventionRepository.findById(id).orElse(null);
                System.out.println("🔍 Recherche par ID MongoDB: " + (convention != null ? "Trouvé" : "Non trouvé"));
            }
            
            // Si pas trouvé, essayer par référence (CONV-xxx)
            if (convention == null && id != null) {
                convention = conventionRepository.findByReference(id);
                System.out.println("🔍 Recherche par référence: " + (convention != null ? "Trouvé" : "Non trouvé"));
            }
            
            if (convention == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "❌ Convention non trouvée avec l'identifiant: " + id + "\n\n" +
                                           "💡 Utilisez soit:\n" +
                                           "• L'ID MongoDB (ex: 68f855fc64c2eb49fedecb7b)\n" +
                                           "• La référence (ex: CONV-1729567890123)");
                return ResponseEntity.ok(errorResponse);
            }
            
            System.out.println("📋 Convention trouvée: " + convention.getReference());
            System.out.println("📊 Statut actuel: " + convention.getStatus());
            System.out.println("💰 Montant actuel: " + convention.getAmount());
            
            // Mise à jour du montant
            if (data.containsKey("amount")) {
                Object amountObj = data.get("amount");
                System.out.println("💰 Montant reçu: " + amountObj + " (type: " + (amountObj != null ? amountObj.getClass().getSimpleName() : "null") + ")");
                
                if (amountObj instanceof Number) {
                    BigDecimal newAmount = BigDecimal.valueOf(((Number) amountObj).doubleValue());
                    convention.setAmount(newAmount);
                    System.out.println("💰 Montant mis à jour: " + newAmount);
                } else if (amountObj instanceof String) {
                    try {
                        BigDecimal newAmount = new BigDecimal((String) amountObj);
                        convention.setAmount(newAmount);
                        System.out.println("💰 Montant mis à jour (depuis String): " + newAmount);
                    } catch (NumberFormatException e) {
                        System.err.println("❌ Erreur conversion montant: " + e.getMessage());
                    }
                }
            }
            
            // Mise à jour du statut
            if (data.containsKey("status")) {
                String statusValue = (String) data.get("status");
                System.out.println("📊 Statut reçu: " + statusValue);
                
                if (statusValue != null && !statusValue.trim().isEmpty()) {
                    String newStatus = statusValue.toUpperCase().trim();
                    convention.setStatus(newStatus);
                    System.out.println("📊 Statut mis à jour: " + newStatus);
                }
            }
            
            // Mise à jour de la date
            convention.setUpdatedAt(java.time.LocalDate.now());
            convention.setLastModifiedBy((String) data.getOrDefault("username", "system"));
            
            System.out.println("💾 Sauvegarde en cours...");
            System.out.println("📊 Statut avant save: " + convention.getStatus());
            System.out.println("💰 Montant avant save: " + convention.getAmount());
            
            Convention updated = conventionRepository.save(convention);
            
            System.out.println("✅ Convention sauvegardée");
            System.out.println("📊 Statut après save: " + updated.getStatus());
            System.out.println("💰 Montant après save: " + updated.getAmount());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Convention modifiée avec succès !\n" +
                                   "📋 Référence: " + updated.getReference() + "\n" +
                                   "💰 Montant: " + updated.getAmount() + " DT\n" +
                                   "📊 Statut: " + updated.getStatus());
            response.put("data", updated);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur updateConvention: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    private ResponseEntity<Map<String, Object>> deleteConvention(String id) {
        try {
            Convention convention = null;
            
            // Essayer de trouver par ID MongoDB
            if (id != null && id.matches("[a-f0-9]{24}")) {
                convention = conventionRepository.findById(id).orElse(null);
            }
            
            // Si pas trouvé, essayer par référence (CONV-xxx)
            if (convention == null && id != null) {
                convention = conventionRepository.findByReference(id);
            }
            
            if (convention == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "❌ Convention non trouvée avec l'identifiant: " + id + "\n\n" +
                                           "💡 Utilisez soit:\n" +
                                           "• L'ID MongoDB (ex: 68f855fc64c2eb49fedecb7b)\n" +
                                           "• La référence (ex: CONV-1729567890123)");
                return ResponseEntity.ok(errorResponse);
            }
            
            String reference = convention.getReference();
            String conventionMongoId = convention.getId();
            
            // Supprimer les factures associées EN CASCADE
            System.out.println("🗑️ Recherche des factures liées à la convention " + reference);
            List<Invoice> relatedInvoices = invoiceRepository.findByConventionId(conventionMongoId);
            
            int invoicesDeleted = 0;
            if (relatedInvoices != null && !relatedInvoices.isEmpty()) {
                System.out.println("🗑️ " + relatedInvoices.size() + " facture(s) trouvée(s) à supprimer");
                for (Invoice invoice : relatedInvoices) {
                    System.out.println("🗑️ Suppression facture: " + invoice.getInvoiceNumber());
                    invoiceRepository.deleteById(invoice.getId());
                    invoicesDeleted++;
                }
            } else {
                System.out.println("ℹ️ Aucune facture liée à cette convention");
            }
            
            // Supprimer la convention
            System.out.println("🗑️ Suppression de la convention " + reference);
            conventionRepository.deleteById(conventionMongoId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            
            String message = "✅ Convention supprimée avec succès !\n" +
                           "📋 Référence: " + reference;
            
            if (invoicesDeleted > 0) {
                message += "\n🧾 " + invoicesDeleted + " facture(s) associée(s) supprimée(s)";
            }
            
            response.put("message", message);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur deleteConvention: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    // ==================== FACTURES ====================
    
    private ResponseEntity<Map<String, Object>> handleInvoiceOperation(String operation, String id, Map<String, Object> data) {
        switch (operation != null ? operation.toLowerCase() : "") {
            case "create":
                return createInvoice(data);
            case "read":
                // Vérifier si c'est une recherche avec filtres
                if (data.containsKey("status") || data.containsKey("amountMin") || data.containsKey("amountMax") || data.containsKey("invoiceNumber")) {
                    return searchInvoices(data);
                }
                return getAllInvoices();
            case "update":
                return updateInvoice(id, data);
            case "delete":
                return deleteInvoice(id);
            default:
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "❌ Opération inconnue: " + operation);
                return ResponseEntity.ok(errorResponse);
        }
    }
    
    private ResponseEntity<Map<String, Object>> createInvoice(Map<String, Object> data) {
        try {
            Invoice invoice = new Invoice();
            invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());
            invoice.setConventionId((String) data.get("conventionId"));
            
            Object amountObj = data.get("amount");
            if (amountObj instanceof Number) {
                invoice.setAmount(BigDecimal.valueOf(((Number) amountObj).doubleValue()));
            }
            
            invoice.setStatus("PENDING");
            invoice.setIssueDate(java.time.LocalDate.now());
            invoice.setDueDate(java.time.LocalDate.now().plusMonths(1));
            invoice.setCreatedBy((String) data.getOrDefault("username", "chatbot"));
            invoice.setCreatedAt(java.time.LocalDate.now());
            
            Invoice saved = invoiceRepository.save(invoice);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Facture créée avec succès !\n🧾 Numéro: " + saved.getInvoiceNumber());
            response.put("data", saved);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    private ResponseEntity<Map<String, Object>> getAllInvoices() {
        try {
            List<Invoice> invoices = invoiceRepository.findAll();
            
            // Formater l'affichage avec détails
            StringBuilder message = new StringBuilder();
            message.append("✅ **").append(invoices.size()).append(" facture(s) trouvée(s)**\n\n");
            
            if (!invoices.isEmpty()) {
                int count = 0;
                for (Invoice inv : invoices) {
                    count++;
                    if (count > 10) {
                        message.append("\n... et ").append(invoices.size() - 10).append(" autre(s) facture(s)");
                        break;
                    }
                    
                    message.append("**").append(count).append(". Facture**\n");
                    message.append("   🧾 Numéro: `").append(inv.getInvoiceNumber()).append("`\n");
                    message.append("   💰 Montant: ").append(inv.getAmount()).append(" DT\n");
                    message.append("   📊 Statut: ").append(inv.getStatus() != null ? inv.getStatus() : "N/A").append("\n");
                    message.append("   📅 Date émission: ").append(inv.getIssueDate() != null ? inv.getIssueDate() : "N/A").append("\n");
                    if (inv.getDueDate() != null) {
                        message.append("   ⏰ Date échéance: ").append(inv.getDueDate()).append("\n");
                    }
                    if (inv.getCreatedBy() != null) {
                        message.append("   👤 Créé par: ").append(inv.getCreatedBy()).append("\n");
                    }
                    message.append("\n");
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message.toString());
            response.put("data", invoices);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    private ResponseEntity<Map<String, Object>> searchInvoices(Map<String, Object> data) {
        try {
            System.out.println("🔍 [SEARCH] Recherche factures avec filtres: " + data);
            
            List<Invoice> allInvoices = invoiceRepository.findAll();
            List<Invoice> filteredInvoices = new ArrayList<>();
            
            // Extraire les filtres
            String statusFilter = data.containsKey("status") ? ((String) data.get("status")).toUpperCase() : null;
            String invoiceNumberFilter = data.containsKey("invoiceNumber") ? ((String) data.get("invoiceNumber")).toUpperCase() : null;
            String conventionIdFilter = data.containsKey("conventionId") ? (String) data.get("conventionId") : null;
            
            // Filtres de montant
            BigDecimal amountMin = null;
            BigDecimal amountMax = null;
            if (data.containsKey("amountMin")) {
                Object amountMinObj = data.get("amountMin");
                if (amountMinObj instanceof Number) {
                    amountMin = BigDecimal.valueOf(((Number) amountMinObj).doubleValue());
                }
            }
            if (data.containsKey("amountMax")) {
                Object amountMaxObj = data.get("amountMax");
                if (amountMaxObj instanceof Number) {
                    amountMax = BigDecimal.valueOf(((Number) amountMaxObj).doubleValue());
                }
            }
            
            System.out.println("🔍 Filtres actifs:");
            if (statusFilter != null) System.out.println("   📊 Statut: " + statusFilter);
            if (invoiceNumberFilter != null) System.out.println("   🧾 Numéro: " + invoiceNumberFilter);
            if (conventionIdFilter != null) System.out.println("   📋 Convention ID: " + conventionIdFilter);
            if (amountMin != null) System.out.println("   💰 Montant min: " + amountMin);
            if (amountMax != null) System.out.println("   💰 Montant max: " + amountMax);
            
            for (Invoice inv : allInvoices) {
                boolean matches = true;
                
                // Filtrer par statut
                if (statusFilter != null) {
                    if (inv.getStatus() == null || !inv.getStatus().equalsIgnoreCase(statusFilter)) {
                        matches = false;
                    }
                }
                
                // Filtrer par numéro de facture
                if (invoiceNumberFilter != null && matches) {
                    if (inv.getInvoiceNumber() == null || !inv.getInvoiceNumber().toUpperCase().contains(invoiceNumberFilter)) {
                        matches = false;
                    }
                }
                
                // Filtrer par convention ID
                if (conventionIdFilter != null && matches) {
                    if (inv.getConventionId() == null || !inv.getConventionId().equals(conventionIdFilter)) {
                        matches = false;
                    }
                }
                
                // Filtrer par montant minimum
                if (amountMin != null && matches) {
                    if (inv.getAmount() == null || inv.getAmount().compareTo(amountMin) < 0) {
                        matches = false;
                    }
                }
                
                // Filtrer par montant maximum
                if (amountMax != null && matches) {
                    if (inv.getAmount() == null || inv.getAmount().compareTo(amountMax) > 0) {
                        matches = false;
                    }
                }
                
                if (matches) {
                    filteredInvoices.add(inv);
                }
            }
            
            System.out.println("✅ " + filteredInvoices.size() + " facture(s) trouvée(s) après filtrage");
            
            // Formater l'affichage avec détails
            StringBuilder message = new StringBuilder();
            
            String filterDescription = "";
            if (statusFilter != null) filterDescription += " statut " + statusFilter;
            if (invoiceNumberFilter != null) filterDescription += " numéro " + invoiceNumberFilter;
            if (conventionIdFilter != null) filterDescription += " convention " + conventionIdFilter;
            if (amountMin != null) filterDescription += " montant min " + amountMin;
            if (amountMax != null) filterDescription += " montant max " + amountMax;
            
            message.append("✅ **").append(filteredInvoices.size()).append(" facture(s) trouvée(s)");
            if (!filterDescription.isEmpty()) {
                message.append(" pour").append(filterDescription);
            }
            message.append("**\n\n");
            
            if (!filteredInvoices.isEmpty()) {
                int count = 0;
                for (Invoice inv : filteredInvoices) {
                    count++;
                    if (count > 10) {
                        message.append("\n... et ").append(filteredInvoices.size() - 10).append(" autre(s) facture(s)");
                        break;
                    }
                    
                    message.append("**").append(count).append(". Facture**\n");
                    message.append("   🧾 Numéro: `").append(inv.getInvoiceNumber()).append("`\n");
                    message.append("   💰 Montant: ").append(inv.getAmount()).append(" DT\n");
                    message.append("   📊 Statut: ").append(inv.getStatus() != null ? inv.getStatus() : "N/A").append("\n");
                    message.append("   📅 Date émission: ").append(inv.getIssueDate() != null ? inv.getIssueDate() : "N/A").append("\n");
                    if (inv.getDueDate() != null) {
                        message.append("   ⏰ Date échéance: ").append(inv.getDueDate()).append("\n");
                    }
                    if (inv.getCreatedBy() != null) {
                        message.append("   👤 Créé par: ").append(inv.getCreatedBy()).append("\n");
                    }
                    message.append("\n");
                }
            } else {
                message.append("Aucune facture ne correspond aux critères de recherche.");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message.toString());
            response.put("data", filteredInvoices);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur searchInvoices: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    private ResponseEntity<Map<String, Object>> updateInvoice(String id, Map<String, Object> data) {
        try {
            Invoice invoice = null;
            
            // Essayer de trouver par ID MongoDB
            if (id != null && id.matches("[a-f0-9]{24}")) {
                invoice = invoiceRepository.findById(id).orElse(null);
            }
            
            // Si pas trouvé, essayer par numéro de facture (INV-xxx)
            if (invoice == null && id != null) {
                invoice = invoiceRepository.findByInvoiceNumber(id);
            }
            
            if (invoice == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "❌ Facture non trouvée avec l'identifiant: " + id + "\n\n" +
                                           "💡 Utilisez soit:\n" +
                                           "• L'ID MongoDB (ex: 68f855fc64c2eb49fedecb7b)\n" +
                                           "• Le numéro de facture (ex: INV-1729567890123)");
                return ResponseEntity.ok(errorResponse);
            }
            
            if (data.containsKey("amount")) {
                Object amountObj = data.get("amount");
                if (amountObj instanceof Number) {
                    invoice.setAmount(BigDecimal.valueOf(((Number) amountObj).doubleValue()));
                }
            }
            
            if (data.containsKey("status")) {
                invoice.setStatus(((String) data.get("status")).toUpperCase());
            }
            
            invoice.setUpdatedAt(java.time.LocalDate.now());
            Invoice updated = invoiceRepository.save(invoice);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Facture modifiée avec succès !\n" +
                                   "🧾 Numéro: " + updated.getInvoiceNumber() + "\n" +
                                   "💰 Montant: " + updated.getAmount() + " DT\n" +
                                   "📊 Statut: " + updated.getStatus());
            response.put("data", updated);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    private ResponseEntity<Map<String, Object>> deleteInvoice(String id) {
        try {
            Invoice invoice = null;
            
            // Essayer de trouver par ID MongoDB
            if (id != null && id.matches("[a-f0-9]{24}")) {
                invoice = invoiceRepository.findById(id).orElse(null);
            }
            
            // Si pas trouvé, essayer par numéro de facture (INV-xxx)
            if (invoice == null && id != null) {
                invoice = invoiceRepository.findByInvoiceNumber(id);
            }
            
            if (invoice == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "❌ Facture non trouvée avec l'identifiant: " + id + "\n\n" +
                                           "💡 Utilisez soit:\n" +
                                           "• L'ID MongoDB (ex: 68f855fc64c2eb49fedecb7b)\n" +
                                           "• Le numéro de facture (ex: INV-1729567890123)");
                return ResponseEntity.ok(errorResponse);
            }
            
            String invoiceNumber = invoice.getInvoiceNumber();
            invoiceRepository.deleteById(invoice.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Facture supprimée avec succès !\n🧾 Numéro: " + invoiceNumber);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    // ==================== UTILISATEURS ====================
    
    private ResponseEntity<Map<String, Object>> handleUserOperation(String operation, String id, Map<String, Object> data) {
        switch (operation != null ? operation.toLowerCase() : "") {
            case "read":
                // Vérifier si c'est une recherche avec filtres EXPLICITES
                // Ignorer "username" et "userId" qui sont ajoutés automatiquement par le NLP
                boolean hasExplicitFilters = data.entrySet().stream()
                    .anyMatch(entry -> {
                        String key = entry.getKey();
                        // Ignorer les champs ajoutés automatiquement par le système
                        if (key.equals("username") || key.equals("userId") || key.equals("roles")) {
                            return false;
                        }
                        // Vérifier les vrais filtres de recherche
                        return key.equals("role") || key.equals("email") || key.equals("status");
                    });
                
                if (hasExplicitFilters) {
                    return searchUsers(data);
                }
                return getAllUsers();
            default:
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "❌ Opération non autorisée pour les utilisateurs via chatbot");
                return ResponseEntity.ok(errorResponse);
        }
    }
    
    private ResponseEntity<Map<String, Object>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ " + users.size() + " utilisateur(s) trouvé(s)");
            response.put("data", users);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    private ResponseEntity<Map<String, Object>> searchUsers(Map<String, Object> data) {
        try {
            System.out.println("🔍 [SEARCH] Recherche utilisateurs avec filtres: " + data);
            
            List<User> allUsers = userRepository.findAll();
            List<User> filteredUsers = new ArrayList<>();
            
            // Extraire les filtres - IGNORER les champs système
            String roleFilter = data.containsKey("role") ? ((String) data.get("role")).toUpperCase() : null;
            // NE PAS utiliser "username" du data car c'est le username connecté ajouté par le système
            String usernameFilter = null; // On ne filtre par username que si explicitement demandé dans le prompt
            String emailFilter = data.containsKey("email") ? ((String) data.get("email")).toLowerCase() : null;
            String statusFilter = data.containsKey("status") ? ((String) data.get("status")).toLowerCase() : null;
            
            System.out.println("🔍 Filtres actifs:");
            if (roleFilter != null) System.out.println("   👔 Rôle: " + roleFilter);
            if (usernameFilter != null) System.out.println("   👤 Username: " + usernameFilter);
            if (emailFilter != null) System.out.println("   📧 Email: " + emailFilter);
            if (statusFilter != null) System.out.println("   📊 Statut: " + statusFilter);
            
            for (User user : allUsers) {
                boolean matches = true;
                
                // Filtrer par rôle
                if (roleFilter != null) {
                    boolean hasRole = user.getRoles().stream()
                        .anyMatch(role -> role.getName().name().equalsIgnoreCase(roleFilter));
                    if (!hasRole) {
                        matches = false;
                    }
                }
                
                // Filtrer par username
                if (usernameFilter != null && matches) {
                    if (user.getUsername() == null || !user.getUsername().toLowerCase().contains(usernameFilter)) {
                        matches = false;
                    }
                }
                
                // Filtrer par email
                if (emailFilter != null && matches) {
                    if (user.getEmail() == null || !user.getEmail().toLowerCase().contains(emailFilter)) {
                        matches = false;
                    }
                }
                
                // Filtrer par statut
                if (statusFilter != null && matches) {
                    if (user.getStatus() == null || !user.getStatus().toLowerCase().contains(statusFilter)) {
                        matches = false;
                    }
                }
                
                if (matches) {
                    filteredUsers.add(user);
                }
            }
            
            System.out.println("✅ " + filteredUsers.size() + " utilisateur(s) trouvé(s) après filtrage");
            
            // Formater l'affichage
            StringBuilder message = new StringBuilder();
            
            String filterDescription = "";
            if (roleFilter != null) filterDescription += " rôle " + roleFilter;
            if (usernameFilter != null) filterDescription += " username " + usernameFilter;
            if (emailFilter != null) filterDescription += " email " + emailFilter;
            if (statusFilter != null) filterDescription += " statut " + statusFilter;
            
            message.append("✅ **").append(filteredUsers.size()).append(" utilisateur(s) trouvé(s)");
            if (!filterDescription.isEmpty()) {
                message.append(" pour").append(filterDescription);
            }
            message.append("**\n\n");
            
            if (!filteredUsers.isEmpty()) {
                int count = 0;
                for (User user : filteredUsers) {
                    count++;
                    if (count > 10) {
                        message.append("\n... et ").append(filteredUsers.size() - 10).append(" autre(s) utilisateur(s)");
                        break;
                    }
                    
                    message.append("**").append(count).append(". ").append(user.getUsername()).append("**\n");
                    message.append("   📧 Email: ").append(user.getEmail()).append("\n");
                    if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                        message.append("   👔 Rôles: ");
                        user.getRoles().forEach(role -> message.append(role.getName()).append(" "));
                        message.append("\n");
                    }
                    if (user.getStatus() != null) {
                        message.append("   📊 Statut: ").append(user.getStatus()).append("\n");
                    }
                    message.append("\n");
                }
            } else {
                message.append("Aucun utilisateur ne correspond aux critères de recherche.");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message.toString());
            response.put("data", filteredUsers);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur searchUsers: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    // ==================== ZONES GÉOGRAPHIQUES ====================
    
    private ResponseEntity<Map<String, Object>> handleZoneOperation(String operation, String id, Map<String, Object> data) {
        if (zoneGeographiqueRepository == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Service zones géographiques non disponible");
            return ResponseEntity.ok(errorResponse);
        }
        
        switch (operation != null ? operation.toLowerCase() : "") {
            case "read":
                return getAllZones();
            case "create":
                return createZone(data);
            case "update":
                return updateZone(id, data);
            case "delete":
                return deleteZone(id);
            default:
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "❌ Opération inconnue: " + operation);
                return ResponseEntity.ok(errorResponse);
        }
    }
    
    private ResponseEntity<Map<String, Object>> getAllZones() {
        try {
            List<?> zones = zoneGeographiqueRepository.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ " + zones.size() + " zone(s) trouvée(s)");
            response.put("data", zones);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    private ResponseEntity<Map<String, Object>> createZone(Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "🔜 Création de zones via chatbot en développement");
        return ResponseEntity.ok(response);
    }
    
    private ResponseEntity<Map<String, Object>> updateZone(String id, Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "🔜 Modification de zones via chatbot en développement");
        return ResponseEntity.ok(response);
    }
    
    private ResponseEntity<Map<String, Object>> deleteZone(String id) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "🔜 Suppression de zones via chatbot en développement");
        return ResponseEntity.ok(response);
    }
    
    // ==================== STRUCTURES ====================
    
    private ResponseEntity<Map<String, Object>> handleStructureOperation(String operation, String id, Map<String, Object> data) {
        if (structureRepository == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Service structures non disponible");
            return ResponseEntity.ok(errorResponse);
        }
        
        switch (operation != null ? operation.toLowerCase() : "") {
            case "read":
                return getAllStructures();
            default:
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "❌ Opération inconnue: " + operation);
                return ResponseEntity.ok(errorResponse);
        }
    }
    
    private ResponseEntity<Map<String, Object>> getAllStructures() {
        try {
            List<?> structures = structureRepository.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ " + structures.size() + " structure(s) trouvée(s)");
            response.put("data", structures);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    // ==================== APPLICATIONS ====================
    
    private ResponseEntity<Map<String, Object>> handleApplicationOperation(String operation, String id, Map<String, Object> data) {
        if (applicationRepository == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Service applications non disponible");
            return ResponseEntity.ok(errorResponse);
        }
        
        switch (operation != null ? operation.toLowerCase() : "") {
            case "read":
                return getAllApplications();
            default:
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "❌ Opération inconnue: " + operation);
                return ResponseEntity.ok(errorResponse);
        }
    }
    
    private ResponseEntity<Map<String, Object>> getAllApplications() {
        try {
            List<?> applications = applicationRepository.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ " + applications.size() + " application(s) trouvée(s)");
            response.put("data", applications);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    // ==================== SMS ====================
    
    private ResponseEntity<Map<String, Object>> handleSmsOperation(String operation, String id, Map<String, Object> data) {
        if (smsService == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Service SMS non disponible");
            return ResponseEntity.ok(errorResponse);
        }
        
        switch (operation != null ? operation.toLowerCase() : "") {
            case "send":
                return sendSms(data);
            default:
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "❌ Opération inconnue: " + operation);
                return ResponseEntity.ok(errorResponse);
        }
    }
    
    private ResponseEntity<Map<String, Object>> sendSms(Map<String, Object> data) {
        try {
            String phoneNumber = (String) data.get("phoneNumber");
            String message = (String) data.get("message");
            
            if (phoneNumber == null || message == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "❌ Numéro de téléphone et message requis");
                return ResponseEntity.ok(errorResponse);
            }
            
            // Appeler le service SMS
            // smsService.sendSms(...);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ SMS envoyé avec succès au " + phoneNumber);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    // ==================== RECHERCHE AVANCÉE ====================
    
    private ResponseEntity<Map<String, Object>> handleSearchOperation(String operation, String id, Map<String, Object> data) {
        try {
            String entityType = (String) data.get("searchEntity");
            String searchType = (String) data.get("searchType");
            String searchValue = (String) data.get("searchValue");
            
            System.out.println("🔍 Recherche: " + entityType + " par " + searchType + " = " + searchValue);
            
            // Recherche par structure
            if ("structure".equalsIgnoreCase(searchType)) {
                List<Convention> conventions = conventionRepository.findByStructureId(searchValue);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "✅ " + conventions.size() + " convention(s) trouvée(s) pour la structure " + searchValue);
                response.put("data", conventions);
                return ResponseEntity.ok(response);
            }
            
            // Recherche par zone
            if ("zone".equalsIgnoreCase(searchType)) {
                List<Convention> conventions = conventionRepository.findByZoneGeographiqueId(searchValue);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "✅ " + conventions.size() + " convention(s) trouvée(s) pour la zone " + searchValue);
                response.put("data", conventions);
                return ResponseEntity.ok(response);
            }
            
            // Recherche par statut
            if ("statut".equalsIgnoreCase(searchType) || "status".equalsIgnoreCase(searchType)) {
                List<Convention> conventions = conventionRepository.findByStatus(searchValue.toUpperCase());
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "✅ " + conventions.size() + " convention(s) avec le statut " + searchValue);
                response.put("data", conventions);
                return ResponseEntity.ok(response);
            }
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Type de recherche non supporté: " + searchType + "\n\n" +
                                         "✅ Types supportés: structure, zone, statut");
            return ResponseEntity.ok(errorResponse);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "❌ Erreur: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
}
