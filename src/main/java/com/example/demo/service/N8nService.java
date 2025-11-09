package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service pour communiquer avec n8n
 */
@Service
public class N8nService {

    @Value("${n8n.webhook.url:http://localhost:5678/webhook}")
    private String n8nWebhookUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Autowired(required = false)
    private com.example.demo.controller.PublicCrudController publicCrudController;

    /**
     * Envoie un prompt à n8n pour traitement
     */
    public Map<String, Object> sendToN8n(String prompt, String intent, String entityType, Map<String, Object> entities) {
        try {
            // Préparer le payload pour n8n
            Map<String, Object> payload = new HashMap<>();
            payload.put("prompt", prompt);
            payload.put("intent", intent);
            payload.put("entityType", entityType);
            payload.put("entities", entities);
            payload.put("timestamp", System.currentTimeMillis());
            
            System.out.println("🔗 [n8n] Envoi vers n8n: " + n8nWebhookUrl);
            System.out.println("📦 [n8n] Payload: " + payload);
            
            // Configurer les headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            // Envoyer à n8n
            ResponseEntity<Map> response = restTemplate.exchange(
                n8nWebhookUrl + "/chatbot-crud",
                HttpMethod.POST,
                request,
                Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();
            System.out.println("✅ [n8n] Réponse reçue: " + responseBody);
            
            // Vérifier que la réponse n'est pas null
            if (responseBody == null) {
                System.err.println("⚠️ [n8n] Réponse null reçue de n8n");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "n8n a retourné une réponse vide");
                return errorResponse;
            }
            
            return responseBody;
            
        } catch (Exception e) {
            System.err.println("❌ [n8n] Erreur: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback : retourner une erreur
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur de communication avec n8n: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Webhook universel pour toutes les opérations CRUD
     * Appel DIRECT au contrôleur (pas d'appel HTTP)
     */
    public Map<String, Object> executeOperation(String operation, String entityType, Map<String, Object> data) {
        try {
            // Préparer le payload pour le contrôleur
            Map<String, Object> request = new HashMap<>();
            request.put("operation", operation.toLowerCase());
            request.put("id", data.get("id"));
            request.put("data", data);
            request.put("entityType", entityType);
            
            System.out.println("🔗 [n8n] Appel DIRECT au PublicCrudController");
            System.out.println("📦 [n8n] Opération: " + operation);
            System.out.println("🏷️ [n8n] Entité: " + entityType);
            System.out.println("📦 [n8n] Payload: " + request);
            
            // Vérifier que le contrôleur est disponible
            if (publicCrudController == null) {
                System.err.println("⚠️ [n8n] PublicCrudController non disponible");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Service CRUD non disponible");
                return errorResponse;
            }
            
            // Appeler DIRECTEMENT le contrôleur (pas d'appel HTTP)
            ResponseEntity<Map<String, Object>> response = publicCrudController.executeOperation(request);
            
            Map<String, Object> responseBody = response.getBody();
            System.out.println("✅ [n8n] Réponse reçue: " + responseBody);
            
            if (responseBody == null) {
                System.err.println("⚠️ [n8n] Réponse null");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Réponse vide");
                return errorResponse;
            }
            
            return responseBody;
            
        } catch (Exception e) {
            System.err.println("❌ [n8n] Erreur: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur: " + e.getMessage());
            return errorResponse;
        }
    }

    // ==================== CONVENTIONS ====================
    
    public Map<String, Object> createConvention(Map<String, Object> data) {
        return executeOperation("create", "CONVENTION", data);
    }
    
    public Map<String, Object> getConventions(Map<String, Object> filters) {
        return executeOperation("read", "CONVENTION", filters);
    }
    
    public Map<String, Object> updateConvention(String id, Map<String, Object> data) {
        data.put("id", id);
        return executeOperation("update", "CONVENTION", data);
    }
    
    public Map<String, Object> deleteConvention(String id) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return executeOperation("delete", "CONVENTION", data);
    }

    // ==================== FACTURES ====================
    
    public Map<String, Object> createInvoice(Map<String, Object> data) {
        return executeOperation("create", "INVOICE", data);
    }
    
    public Map<String, Object> getInvoices(Map<String, Object> filters) {
        return executeOperation("read", "INVOICE", filters);
    }
    
    public Map<String, Object> updateInvoice(String id, Map<String, Object> data) {
        data.put("id", id);
        return executeOperation("update", "INVOICE", data);
    }
    
    public Map<String, Object> deleteInvoice(String id) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return executeOperation("delete", "INVOICE", data);
    }

    // ==================== UTILISATEURS ====================
    
    public Map<String, Object> createUser(Map<String, Object> data) {
        return executeOperation("create", "USER", data);
    }
    
    public Map<String, Object> getUsers(Map<String, Object> filters) {
        return executeOperation("read", "USER", filters);
    }
    
    public Map<String, Object> updateUser(String id, Map<String, Object> data) {
        data.put("id", id);
        return executeOperation("update", "USER", data);
    }
    
    public Map<String, Object> deleteUser(String id) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return executeOperation("delete", "USER", data);
    }

    // ==================== STRUCTURES ====================
    
    public Map<String, Object> createStructure(Map<String, Object> data) {
        return executeOperation("create", "STRUCTURE", data);
    }
    
    public Map<String, Object> getStructures(Map<String, Object> filters) {
        return executeOperation("read", "STRUCTURE", filters);
    }
    
    public Map<String, Object> updateStructure(String id, Map<String, Object> data) {
        data.put("id", id);
        return executeOperation("update", "STRUCTURE", data);
    }
    
    public Map<String, Object> deleteStructure(String id) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return executeOperation("delete", "STRUCTURE", data);
    }

    // ==================== APPLICATIONS ====================
    
    public Map<String, Object> createApplication(Map<String, Object> data) {
        return executeOperation("create", "APPLICATION", data);
    }
    
    public Map<String, Object> getApplications(Map<String, Object> filters) {
        return executeOperation("read", "APPLICATION", filters);
    }
    
    public Map<String, Object> updateApplication(String id, Map<String, Object> data) {
        data.put("id", id);
        return executeOperation("update", "APPLICATION", data);
    }
    
    public Map<String, Object> deleteApplication(String id) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return executeOperation("delete", "APPLICATION", data);
    }

    // ==================== NOTIFICATIONS ====================
    
    public Map<String, Object> sendNotification(Map<String, Object> data) {
        return executeOperation("send", "NOTIFICATION", data);
    }
    
    public Map<String, Object> getNotifications(Map<String, Object> filters) {
        return executeOperation("read", "NOTIFICATION", filters);
    }

    // ==================== PAIEMENTS ====================
    
    public Map<String, Object> createPayment(Map<String, Object> data) {
        return executeOperation("create", "PAYMENT", data);
    }
    
    public Map<String, Object> getPayments(Map<String, Object> filters) {
        return executeOperation("read", "PAYMENT", filters);
    }
    
    public Map<String, Object> validatePayment(String id, Map<String, Object> data) {
        data.put("id", id);
        return executeOperation("validate", "PAYMENT", data);
    }

    /**
     * Vérifie si n8n est accessible
     */
    public boolean isN8nAvailable() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                n8nWebhookUrl.replace("/webhook", "") + "/healthz",
                String.class
            );
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            System.err.println("⚠️ [n8n] n8n non accessible: " + e.getMessage());
            return false;
        }
    }
}
