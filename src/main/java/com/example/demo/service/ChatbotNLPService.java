package com.example.demo.service;

import com.example.demo.dto.chatbot.ChatbotRequest;
import com.example.demo.dto.chatbot.ChatbotResponse;
import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatbotNLPService {

    @Autowired
    private ConventionRepository conventionRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private N8nService n8nService;

    public ChatbotResponse processPrompt(ChatbotRequest request) {
        String prompt = request.getQuestion().toLowerCase().trim();
        
        System.out.println("🧠 [NLP] Analyse: " + prompt);
        
        CRUDIntent intent = detectCRUDIntent(prompt);
        EntityType entityType = detectEntityType(prompt);
        Map<String, Object> data = extractEntities(prompt);
        
        return executeCRUD(intent, entityType, data, prompt);
    }
    
    /**
     * Traite un prompt avec contrôle d'accès basé sur le rôle de l'utilisateur
     */
    public ChatbotResponse processPromptWithUser(ChatbotRequest request, UserPrincipal userPrincipal) {
        String prompt = request.getQuestion().toLowerCase().trim();
        
        System.out.println("🧠 [NLP] Analyse avec utilisateur: " + prompt);
        
        // Vérifier les permissions
        if (userPrincipal == null) {
            return new ChatbotResponse("❌ Vous devez être authentifié pour utiliser le chatbot", null, null);
        }
        
        CRUDIntent intent = detectCRUDIntent(prompt);
        EntityType entityType = detectEntityType(prompt);
        Map<String, Object> data = extractEntities(prompt);
        
        // Ajouter les informations utilisateur aux données
        data.put("userId", userPrincipal.getId());
        data.put("username", userPrincipal.getUsername());
        data.put("roles", userPrincipal.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toList());
        
        // Vérifier les permissions selon le rôle avec message détaillé
        String permissionError = checkPermissionWithMessage(userPrincipal, intent, entityType);
        if (permissionError != null) {
            return new ChatbotResponse(permissionError, null, null);
        }
        
        return executeCRUD(intent, entityType, data, prompt);
    }
    
    /**
     * Vérifie si l'utilisateur a la permission d'effectuer l'action
     * Retourne un message d'erreur si pas de permission, null sinon
     */
    private String checkPermissionWithMessage(UserPrincipal user, CRUDIntent intent, EntityType entityType) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || 
                                 auth.getAuthority().equals("ROLE_SUPER_ADMIN"));
        
        boolean isDecideur = user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_DECISION_MAKER") || 
                                 auth.getAuthority().equals("ROLE_DECIDEUR"));
        
        boolean isCommercial = user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_COMMERCIAL"));
        
        boolean isChefProjet = user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_PROJECT_MANAGER"));
        
        String roleName = isAdmin ? "ADMIN" : 
                         isDecideur ? "DECIDEUR" : 
                         isCommercial ? "COMMERCIAL" : 
                         isChefProjet ? "CHEF DE PROJET" : "USER";
        
        // ADMIN et SUPER_ADMIN : Tout autorisé
        if (isAdmin) {
            return null; // Pas d'erreur, permission accordée
        }
        
        // DECIDEUR : Lecture de tout, création de conventions uniquement
        if (isDecideur) {
            if (intent == CRUDIntent.READ) {
                return null; // Peut lire toutes les entités
            }
            if (intent == CRUDIntent.CREATE && entityType == EntityType.CONVENTION) {
                return null; // Peut créer des conventions
            }
            
            // Messages d'erreur spécifiques
            if (intent == CRUDIntent.UPDATE) {
                return "❌ **Permission refusée** : En tant que DECIDEUR, vous ne pouvez pas modifier les " + entityType.name().toLowerCase() + "s.\n\n" +
                       "✅ **Actions autorisées** :\n" +
                       "• Consulter toutes les données (conventions, factures, etc.)\n" +
                       "• Créer de nouvelles conventions\n" +
                       "• Générer des rapports et analyses";
            }
            if (intent == CRUDIntent.DELETE) {
                return "❌ **Permission refusée** : En tant que DECIDEUR, vous ne pouvez pas supprimer de données.\n\n" +
                       "✅ **Actions autorisées** :\n" +
                       "• Consulter toutes les données\n" +
                       "• Créer de nouvelles conventions\n" +
                       "• Générer des rapports";
            }
            if (intent == CRUDIntent.CREATE && entityType != EntityType.CONVENTION) {
                return "❌ **Permission refusée** : En tant que DECIDEUR, vous ne pouvez créer que des conventions.\n\n" +
                       "✅ **Essayez plutôt** :\n" +
                       "• \"Crée une convention pour le client X\"\n" +
                       "• \"Montre toutes les " + entityType.name().toLowerCase() + "s\"";
            }
        }
        
        // CHEF DE PROJET : Lecture de tout, gestion conventions/factures
        if (isChefProjet) {
            if (intent == CRUDIntent.READ) {
                return null; // Peut lire toutes les entités
            }
            if (entityType == EntityType.CONVENTION || entityType == EntityType.INVOICE) {
                if (intent != CRUDIntent.DELETE) {
                    return null; // Peut créer/modifier conventions et factures
                }
            }
            
            if (intent == CRUDIntent.DELETE) {
                return "❌ **Permission refusée** : En tant que CHEF DE PROJET, vous ne pouvez pas supprimer de données.\n\n" +
                       "✅ **Actions autorisées** :\n" +
                       "• Consulter toutes les données\n" +
                       "• Créer et modifier des conventions\n" +
                       "• Créer et modifier des factures";
            }
            
            return "❌ **Permission refusée** : En tant que CHEF DE PROJET, vous ne pouvez gérer que les conventions et factures.\n\n" +
                   "✅ **Essayez plutôt** :\n" +
                   "• \"Montre toutes les conventions\"\n" +
                   "• \"Crée une facture pour la convention X\"";
        }
        
        // COMMERCIAL : CRUD sur ses propres conventions et factures
        if (isCommercial) {
            if (entityType == EntityType.CONVENTION) {
                return null; // Peut faire CRUD sur ses conventions
            }
            if (entityType == EntityType.INVOICE) {
                if (intent == CRUDIntent.READ || intent == CRUDIntent.CREATE) {
                    return null; // Peut lire et créer des factures
                }
                return "❌ **Permission refusée** : En tant que COMMERCIAL, vous ne pouvez pas " + 
                       (intent == CRUDIntent.UPDATE ? "modifier" : "supprimer") + " les factures.\n\n" +
                       "✅ **Actions autorisées** :\n" +
                       "• Consulter vos factures\n" +
                       "• Créer de nouvelles factures\n" +
                       "• Gérer vos conventions (CRUD complet)";
            }
            
            return "❌ **Permission refusée** : En tant que COMMERCIAL, vous ne pouvez gérer que vos conventions et factures.\n\n" +
                   "✅ **Actions autorisées** :\n" +
                   "• \"Montre mes conventions\"\n" +
                   "• \"Crée une convention pour le client X\"\n" +
                   "• \"Modifie la convention CONV-123\"\n" +
                   "• \"Consulte mes factures\"";
        }
        
        // Aucun rôle reconnu
        return "❌ **Permission refusée** : Votre rôle (" + roleName + ") ne vous permet pas d'effectuer cette action.\n\n" +
               "Contactez un administrateur pour plus d'informations.";
    }
    
    /**
     * Version simplifiée pour compatibilité
     */
    private boolean checkPermission(UserPrincipal user, CRUDIntent intent, EntityType entityType) {
        return checkPermissionWithMessage(user, intent, entityType) == null;
    }

    private CRUDIntent detectCRUDIntent(String prompt) {
        if (prompt.matches(".*(cr[ée]e|ajoute|nouveau).*")) return CRUDIntent.CREATE;
        if (prompt.matches(".*(met[s]? [àa] jour|modifie|change).*")) return CRUDIntent.UPDATE;
        if (prompt.matches(".*(supprime|efface|delete).*")) return CRUDIntent.DELETE;
        if (prompt.matches(".*(montre|affiche|liste|voir).*")) return CRUDIntent.READ;
        return CRUDIntent.UNKNOWN;
    }

    private EntityType detectEntityType(String prompt) {
        // SMS (priorité haute)
        if (prompt.matches(".*(sms|envoie|envoyer|message).*")) {
            System.out.println("🏷️ Entité détectée: SMS");
            return EntityType.NOTIFICATION; // Utiliser NOTIFICATION pour SMS
        }
        
        // Recherche (priorité haute)
        if (prompt.matches(".*(recherche|cherche|trouve|par structure|par zone|par statut).*")) {
            System.out.println("🏷️ Entité détectée: SEARCH");
            return EntityType.SEARCH;
        }
        
        // Factures (priorité haute car souvent mentionnées avec conventions)
        if (prompt.matches(".*(facture|invoice|bill|inv-).*")) {
            System.out.println("🏷️ Entité détectée: FACTURE");
            return EntityType.INVOICE;
        }
        
        // Zones géographiques
        if (prompt.matches(".*(zone|zone géographique|gouvernorat).*")) {
            System.out.println("🏷️ Entité détectée: ZONE");
            return EntityType.STRUCTURE; // Utiliser STRUCTURE pour zones
        }
        
        // Conventions
        if (prompt.matches(".*(convention|contrat|accord|conv-).*")) {
            System.out.println("🏷️ Entité détectée: CONVENTION");
            return EntityType.CONVENTION;
        }
        
        // Utilisateurs
        if (prompt.matches(".*(utilisateur|user|compte|commercial|decideur).*")) {
            System.out.println("🏷️ Entité détectée: USER");
            return EntityType.USER;
        }
        
        // Structures
        if (prompt.matches(".*(structure|organisation).*")) {
            System.out.println("🏷️ Entité détectée: STRUCTURE");
            return EntityType.STRUCTURE;
        }
        
        // Applications
        if (prompt.matches(".*(application|candidature|demande|app).*")) {
            System.out.println("🏷️ Entité détectée: APPLICATION");
            return EntityType.APPLICATION;
        }
        
        // Paiements
        if (prompt.matches(".*(paiement|payment|règlement).*")) {
            System.out.println("🏷️ Entité détectée: PAYMENT");
            return EntityType.PAYMENT;
        }
        
        // Si aucune entité détectée mais qu'il y a un ID MongoDB, deviner selon le contexte
        if (prompt.matches(".*[a-f0-9]{24}.*")) {
            // Par défaut, supposer que c'est une convention
            System.out.println("⚠️ Entité non détectée, supposant CONVENTION par défaut");
            return EntityType.CONVENTION;
        }
        
        System.out.println("⚠️ Aucune entité détectée");
        return EntityType.UNKNOWN;
    }

    private Map<String, Object> extractEntities(String prompt) {
        Map<String, Object> data = new HashMap<>();
        
        // ID - Support pour références et IDs MongoDB
        Pattern refPattern = Pattern.compile("(CONV-\\d+|C-\\d+|INV-\\d+|F-\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher refMatcher = refPattern.matcher(prompt);
        if (refMatcher.find()) {
            data.put("id", refMatcher.group(1).toUpperCase());
            System.out.println("🔍 Référence extraite: " + refMatcher.group(1).toUpperCase());
        }
        
        // ID MongoDB (24 caractères hexadécimaux)
        Pattern mongoIdPattern = Pattern.compile("\\b([a-f0-9]{24})\\b", Pattern.CASE_INSENSITIVE);
        Matcher mongoIdMatcher = mongoIdPattern.matcher(prompt);
        if (mongoIdMatcher.find()) {
            data.put("id", mongoIdMatcher.group(1));
            System.out.println("🔍 ID MongoDB extrait: " + mongoIdMatcher.group(1));
        }
        
        // Client - Extraction améliorée
        Pattern clientPattern = Pattern.compile("(?:client|pour le client|pour)\\s+([A-Za-z0-9\\s]+?)(?:\\s+montant|\\s+de|$)", Pattern.CASE_INSENSITIVE);
        Matcher clientMatcher = clientPattern.matcher(prompt);
        if (clientMatcher.find()) {
            String clientName = clientMatcher.group(1).trim();
            data.put("client", clientName);
            System.out.println("🔍 Client extrait: " + clientName);
        }
        
        // Montant
        Pattern amountPattern = Pattern.compile("montant[:\\s]*(\\d+)|([\\d]+)\\s*(?:dt|dinar)", Pattern.CASE_INSENSITIVE);
        Matcher amountMatcher = amountPattern.matcher(prompt);
        if (amountMatcher.find()) {
            String amount = amountMatcher.group(1) != null ? amountMatcher.group(1) : amountMatcher.group(2);
            data.put("amount", new BigDecimal(amount));
            System.out.println("💰 Montant extrait: " + amount);
        }
        
        // Référence - Extraction
        Pattern refSearchPattern = Pattern.compile("(?:référence|reference)\\s+([A-Z0-9\\-]+)", Pattern.CASE_INSENSITIVE);
        Matcher refSearchMatcher = refSearchPattern.matcher(prompt);
        if (refSearchMatcher.find()) {
            String ref = refSearchMatcher.group(1).trim();
            data.put("reference", ref);
            System.out.println("📋 Référence recherchée: " + ref);
        }
        
        // Numéro de facture - Extraction
        Pattern invoiceNumPattern = Pattern.compile("(?:numéro|numero|facture numéro|facture numero)\\s+([A-Z0-9\\-]+)", Pattern.CASE_INSENSITIVE);
        Matcher invoiceNumMatcher = invoiceNumPattern.matcher(prompt);
        if (invoiceNumMatcher.find()) {
            String invoiceNum = invoiceNumMatcher.group(1).trim();
            data.put("invoiceNumber", invoiceNum);
            System.out.println("🧾 Numéro de facture recherché: " + invoiceNum);
        }
        
        // Titre - Extraction
        Pattern titlePattern = Pattern.compile("(?:titre|title)\\s+([A-Za-zÀ-ÿ\\s]+?)(?:\\s+(?:zone|structure|statut|montant)|$)", Pattern.CASE_INSENSITIVE);
        Matcher titleMatcher = titlePattern.matcher(prompt);
        if (titleMatcher.find()) {
            String title = titleMatcher.group(1).trim();
            data.put("title", title);
            System.out.println("📝 Titre recherché: " + title);
        }
        
        // Tag - Extraction
        Pattern tagPattern = Pattern.compile("(?:tag|étiquette)\\s+([A-Za-zÀ-ÿ]+)", Pattern.CASE_INSENSITIVE);
        Matcher tagMatcher = tagPattern.matcher(prompt);
        if (tagMatcher.find()) {
            String tag = tagMatcher.group(1).trim();
            data.put("tag", tag);
            System.out.println("🏷️ Tag recherché: " + tag);
        }
        
        // Zone géographique - Extraction améliorée
        Pattern zonePattern = Pattern.compile("(?:zone|gouvernorat|par zone)\\s+([A-Za-zÀ-ÿ\\s]+?)(?:\\s+(?:structure|statut|montant)|$)", Pattern.CASE_INSENSITIVE);
        Matcher zoneMatcher = zonePattern.matcher(prompt);
        if (zoneMatcher.find()) {
            String zone = zoneMatcher.group(1).trim();
            data.put("zone", zone);
            System.out.println("🌍 Zone extraite: " + zone);
        }
        
        // Structure - Extraction améliorée
        Pattern structPattern = Pattern.compile("(?:structure|par structure)\\s+([A-Z0-9\\-]+)", Pattern.CASE_INSENSITIVE);
        Matcher structMatcher = structPattern.matcher(prompt);
        if (structMatcher.find()) {
            String struct = structMatcher.group(1).trim();
            data.put("structure", struct);
            System.out.println("🏢 Structure extraite: " + struct);
        }
        
        // Montant minimum - Extraction
        Pattern amountMinPattern = Pattern.compile("(?:montant min|montant minimum|plus de|supérieur à)\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher amountMinMatcher = amountMinPattern.matcher(prompt);
        if (amountMinMatcher.find()) {
            String amountMin = amountMinMatcher.group(1);
            data.put("amountMin", new BigDecimal(amountMin));
            System.out.println("💰 Montant min extrait: " + amountMin);
        }
        
        // Montant maximum - Extraction
        Pattern amountMaxPattern = Pattern.compile("(?:montant max|montant maximum|moins de|inférieur à)\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher amountMaxMatcher = amountMaxPattern.matcher(prompt);
        if (amountMaxMatcher.find()) {
            String amountMax = amountMaxMatcher.group(1);
            data.put("amountMax", new BigDecimal(amountMax));
            System.out.println("💰 Montant max extrait: " + amountMax);
        }
        
        // Rôle utilisateur - Extraction
        Pattern rolePattern = Pattern.compile("(?:rôle|role)\\s+(COMMERCIAL|ADMIN|SUPER_ADMIN|DECIDEUR|PROJECT_MANAGER|DECISION_MAKER)", Pattern.CASE_INSENSITIVE);
        Matcher roleMatcher = rolePattern.matcher(prompt);
        if (roleMatcher.find()) {
            String role = roleMatcher.group(1).trim().toUpperCase();
            // Normaliser les noms de rôles
            if (role.equals("DECISION_MAKER")) role = "DECIDEUR";
            data.put("role", role);
            System.out.println("👔 Rôle extrait: " + role);
        }
        
        // Statut - Extraction améliorée avec priorité pour "non payées"
        if (prompt.contains("non payée") || prompt.contains("non payé") || 
            prompt.contains("non payées") || prompt.contains("non payés") ||
            prompt.contains("impayée") || prompt.contains("impayé") ||
            prompt.contains("impayées") || prompt.contains("impayés") ||
            prompt.contains("unpaid")) {
            data.put("status", "PENDING");
            System.out.println("📊 Statut extrait: PENDING (non payées)");
        } else if (prompt.contains("payée") || prompt.contains("payé") || prompt.contains("paid")) {
            data.put("status", "PAID");
            System.out.println("📊 Statut extrait: PAID");
        } else if (prompt.contains("active") || prompt.contains("actif")) {
            data.put("status", "ACTIVE");
            System.out.println("📊 Statut extrait: ACTIVE");
        } else if (prompt.contains("draft") || prompt.contains("brouillon")) {
            data.put("status", "DRAFT");
            System.out.println("📊 Statut extrait: DRAFT");
        } else if (prompt.contains("pending") || prompt.contains("en attente")) {
            data.put("status", "PENDING");
            System.out.println("📊 Statut extrait: PENDING");
        } else if (prompt.contains("en retard") || prompt.contains("retard") || prompt.contains("overdue")) {
            data.put("status", "OVERDUE");
            System.out.println("📊 Statut extrait: OVERDUE");
        } else if (prompt.contains("expired") || prompt.contains("expiré")) {
            data.put("status", "EXPIRED");
            System.out.println("📊 Statut extrait: EXPIRED");
        } else if (prompt.contains("online") || prompt.contains("en ligne")) {
            data.put("status", "online");
            System.out.println("📊 Statut extrait: online");
        } else if (prompt.contains("offline") || prompt.contains("hors ligne")) {
            data.put("status", "offline");
            System.out.println("📊 Statut extrait: offline");
        }
        
        return data;
    }

    private ChatbotResponse executeCRUD(CRUDIntent intent, EntityType entityType, Map<String, Object> data, String prompt) {
        try {
            // Gérer la recherche spécialement
            if (entityType == EntityType.SEARCH) {
                System.out.println("🔍 [NLP] Traitement de recherche détecté");
                // Forcer l'intent à READ et détecter l'entité cible
                intent = CRUDIntent.READ;
                
                // Détecter l'entité cible de la recherche
                if (prompt.contains("convention")) {
                    entityType = EntityType.CONVENTION;
                } else if (prompt.contains("facture") || prompt.contains("invoice")) {
                    entityType = EntityType.INVOICE;
                } else if (prompt.contains("utilisateur") || prompt.contains("user")) {
                    entityType = EntityType.USER;
                } else {
                    // Par défaut, rechercher des conventions
                    entityType = EntityType.CONVENTION;
                }
                
                System.out.println("🔍 [NLP] Recherche convertie en READ de " + entityType);
            }
            
            // Vérifier si un ID est requis pour UPDATE ou DELETE
            if ((intent == CRUDIntent.UPDATE || intent == CRUDIntent.DELETE) && !data.containsKey("id")) {
                String entityName = entityType.name().toLowerCase();
                return new ChatbotResponse(
                    "❌ **ID manquant** : Pour modifier ou supprimer une " + entityName + ", vous devez fournir son ID.\n\n" +
                    "✅ **Exemples corrects** :\n" +
                    "• \"Modifie la " + entityName + " 68f855fc64c2eb49fedecb7b montant 7000\"\n" +
                    "• \"Supprime la " + entityName + " 68f855fc64c2eb49fedecb7b\"\n\n" +
                    "💡 **Astuce** : Utilisez d'abord \"Montre toutes les " + entityName + "s\" pour obtenir les IDs.",
                    null,
                    null
                );
            }
            
            // Vérifier si n8n est disponible
            boolean useN8n = n8nService.isN8nAvailable();
            
            if (useN8n) {
                System.out.println("🔗 [NLP] Utilisation de n8n pour le traitement");
                return executeCRUDViaN8n(intent, entityType, data, prompt);
            } else {
                System.out.println("⚠️ [NLP] n8n non disponible, traitement direct");
                return executeCRUDDirect(intent, entityType, data);
            }
        } catch (Exception e) {
            return new ChatbotResponse("❌ Erreur: " + e.getMessage(), null, null);
        }
    }
    
    /**
     * Exécute CRUD via n8n
     */
    private ChatbotResponse executeCRUDViaN8n(CRUDIntent intent, EntityType entityType, Map<String, Object> data, String prompt) {
        Map<String, Object> n8nResponse;
        
        // Utiliser executeOperation au lieu des méthodes spécifiques
        n8nResponse = n8nService.executeOperation(intent.name().toLowerCase(), entityType.name(), data);
        
        // Extraire les données de la réponse n8n
        // Structure: {data: [{json: {data: [...], success: true}}], message: "..."}
        Object responseData = n8nResponse.get("data");
        String message = (String) n8nResponse.getOrDefault("message", "Action exécutée via n8n");
        
        // Extraire les vraies données (imbriquées dans data[0].json.data)
        List<?> items = null;
        String successMessage = null;
        
        if (responseData instanceof List && !((List<?>) responseData).isEmpty()) {
            Object firstItem = ((List<?>) responseData).get(0);
            if (firstItem instanceof Map) {
                Object jsonObj = ((Map<?, ?>) firstItem).get("json");
                if (jsonObj instanceof Map) {
                    Map<?, ?> jsonMap = (Map<?, ?>) jsonObj;
                    
                    // Extraire le message de succès
                    successMessage = (String) jsonMap.get("message");
                    
                    // Extraire les données
                    Object actualData = jsonMap.get("data");
                    if (actualData instanceof List) {
                        items = (List<?>) actualData;
                    } else if (actualData instanceof Map) {
                        // Pour CREATE/UPDATE, data est un objet unique
                        items = List.of(actualData);
                    }
                }
            }
        }
        
        // Pour CREATE, UPDATE, DELETE : utiliser le message de succès
        if (intent != CRUDIntent.READ && successMessage != null) {
            return new ChatbotResponse(successMessage, null, null);
        }
        
        // Si c'est une opération READ, formater les données
        if (intent == CRUDIntent.READ && items != null) {
            if (true) { // Toujours vrai, juste pour garder la structure
                
                if (items.isEmpty()) {
                    message = "❌ Aucune " + entityType.name().toLowerCase() + " trouvée";
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("✅ ").append(items.size()).append(" ").append(entityType.name().toLowerCase()).append("(s) trouvée(s) :\n\n");
                    
                    for (int i = 0; i < Math.min(items.size(), 10); i++) {
                        Object item = items.get(i);
                        if (item instanceof Map) {
                            Map<String, Object> itemMap = (Map<String, Object>) item;
                            sb.append("📋 ").append(itemMap.getOrDefault("title", itemMap.getOrDefault("reference", "Item " + (i+1)))).append("\n");
                            sb.append("   ID: ").append(itemMap.getOrDefault("id", "N/A")).append("\n");
                            sb.append("   Statut: ").append(itemMap.getOrDefault("status", "N/A")).append("\n");
                            if (itemMap.containsKey("amount")) {
                                sb.append("   Montant: ").append(itemMap.get("amount")).append(" DT\n");
                            }
                            sb.append("\n");
                        }
                    }
                    
                    if (items.size() > 10) {
                        sb.append("... et ").append(items.size() - 10).append(" autre(s)\n");
                    }
                    
                    message = sb.toString();
                }
            }
        }
        
        return new ChatbotResponse(message, null, null);
    }
    
    /**
     * Exécute CRUD directement (fallback)
     */
    private ChatbotResponse executeCRUDDirect(CRUDIntent intent, EntityType entityType, Map<String, Object> data) {
        switch (intent) {
            case CREATE:
                return handleCreate(entityType, data);
            case READ:
                return handleRead(entityType, data);
            case UPDATE:
                return handleUpdate(entityType, data);
            case DELETE:
                return handleDelete(entityType, data);
            default:
                return new ChatbotResponse("❌ Je n'ai pas compris votre demande.", null, null);
        }
    }

    private ChatbotResponse handleCreate(EntityType entityType, Map<String, Object> data) {
        if (entityType == EntityType.CONVENTION) {
            Convention conv = new Convention();
            conv.setTitle((String) data.getOrDefault("structure", "Nouvelle convention"));
            conv.setStructureId((String) data.get("structure"));
            conv.setAmount((BigDecimal) data.get("amount"));
            conv.setStatus("DRAFT");
            conv.setCreatedAt(LocalDate.now());
            Convention saved = conventionRepository.save(conv);
            return new ChatbotResponse("✅ Convention " + saved.getId() + " créée avec succès !", null, null);
        }
        
        if (entityType == EntityType.INVOICE) {
            Invoice inv = new Invoice();
            inv.setAmount((BigDecimal) data.get("amount"));
            inv.setStatus("PENDING");
            inv.setCreatedAt(LocalDate.now());
            Invoice saved = invoiceRepository.save(inv);
            return new ChatbotResponse("✅ Facture " + saved.getId() + " créée avec succès !", null, null);
        }
        
        return new ChatbotResponse("❌ Type d'entité non reconnu", null, null);
    }

    private ChatbotResponse handleRead(EntityType entityType, Map<String, Object> data) {
        if (entityType == EntityType.CONVENTION) {
            List<Convention> conventions = conventionRepository.findAll();
            
            // Filtrer par statut si spécifié
            String statusFilter = (String) data.get("status");
            if (statusFilter != null) {
                conventions = conventions.stream()
                    .filter(c -> c.getStatus().equalsIgnoreCase(statusFilter))
                    .toList();
            }
            
            String result = "📄 **Conventions trouvées:** " + conventions.size() + "\n\n";
            for (Convention c : conventions) {
                result += "• " + c.getId() + " - " + c.getTitle() + " (" + c.getStatus() + ")\n";
            }
            return new ChatbotResponse(result, null, null);
        }
        
        if (entityType == EntityType.INVOICE) {
            List<Invoice> invoices = invoiceRepository.findAll();
            
            // Filtrer par statut si spécifié
            String statusFilter = (String) data.get("status");
            if (statusFilter != null) {
                invoices = invoices.stream()
                    .filter(i -> i.getStatus().equalsIgnoreCase(statusFilter))
                    .toList();
            }
            
            String result = "📄 **Factures trouvées:** " + invoices.size() + "\n\n";
            for (Invoice i : invoices) {
                result += "• " + i.getId() + " - " + i.getAmount() + " DT (" + i.getStatus() + ")\n";
            }
            return new ChatbotResponse(result, null, null);
        }
        
        return new ChatbotResponse("❌ Type d'entité non reconnu", null, null);
    }

    private ChatbotResponse handleUpdate(EntityType entityType, Map<String, Object> data) {
        String id = (String) data.get("id");
        if (id == null) return new ChatbotResponse("❌ ID manquant", null, null);
        
        if (entityType == EntityType.INVOICE) {
            Optional<Invoice> invOpt = invoiceRepository.findById(id);
            if (invOpt.isEmpty()) return new ChatbotResponse("❌ Facture non trouvée", null, null);
            
            Invoice inv = invOpt.get();
            if (data.containsKey("status")) inv.setStatus((String) data.get("status"));
            if (data.containsKey("amount")) inv.setAmount((BigDecimal) data.get("amount"));
            invoiceRepository.save(inv);
            
            return new ChatbotResponse("✅ Facture " + id + " mise à jour !", null, null);
        }
        
        return new ChatbotResponse("❌ Type d'entité non reconnu", null, null);
    }

    private ChatbotResponse handleDelete(EntityType entityType, Map<String, Object> data) {
        String id = (String) data.get("id");
        if (id == null) return new ChatbotResponse("❌ ID manquant", null, null);
        
        if (entityType == EntityType.CONVENTION) {
            conventionRepository.deleteById(id);
            return new ChatbotResponse("✅ Convention " + id + " supprimée !", null, null);
        }
        
        if (entityType == EntityType.INVOICE) {
            invoiceRepository.deleteById(id);
            return new ChatbotResponse("✅ Facture " + id + " supprimée !", null, null);
        }
        
        return new ChatbotResponse("❌ Type d'entité non reconnu", null, null);
    }

    enum CRUDIntent { CREATE, READ, UPDATE, DELETE, UNKNOWN }
    enum EntityType { 
        CONVENTION, 
        INVOICE, 
        USER, 
        STRUCTURE, 
        APPLICATION, 
        NOTIFICATION, 
        PAYMENT, 
        REMINDER, 
        SEARCH,
        UNKNOWN 
    }
}
