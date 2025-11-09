package com.example.demo.service;

import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service de chatbot intelligent avec Gemini 1.5 Flash
 * Analyse les données de la base et génère des réponses contextuelles
 */
@Service
public class ChatbotService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);
    
    @Value("${gemini.api.key:AIzaSyCC1ObcAPXgaKgJZIDjlX3qSJe-iuBOLAI}")
    private String geminiApiKey;
    
    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent}")
    private String geminiApiUrl;
    
    private final ConventionRepository conventionRepository;
    private final InvoiceRepository invoiceRepository;
    private final RestTemplate restTemplate;
    
    public ChatbotService(ConventionRepository conventionRepository, InvoiceRepository invoiceRepository) {
        this.conventionRepository = conventionRepository;
        this.invoiceRepository = invoiceRepository;
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Traiter un message utilisateur et générer une réponse intelligente
     */
    public String processMessage(String userMessage) {
        logger.info("🤖 Traitement du message : {}", userMessage);
        
        try {
            // ÉTAPE 1 : Détecter l'intention
            String intention = detectIntention(userMessage);
            logger.info("✅ Intention détectée : {}", intention);
            
            // ÉTAPE 2 : Vérifier si c'est une conversation sociale (pas de données nécessaires)
            if (isSocialConversation(intention)) {
                logger.info("💬 Conversation sociale - Pas de données MongoDB nécessaires");
                String prompt = buildSocialPrompt(userMessage, intention);
                String response = callGeminiAPI(prompt);
                logger.info("✅ Réponse sociale générée");
                return response;
            }
            
            // ÉTAPE 3 : Question métier - Récupérer les données de la base
            Map<String, Object> databaseData = extractDatabaseData(intention);
            logger.info("📊 Données métier extraites : {}", databaseData);
            
            // ÉTAPE 4 : Construire le prompt avec données métier
            String prompt = buildBusinessPrompt(userMessage, databaseData, intention);
            
            // ÉTAPE 5 : Appeler Gemini pour générer la réponse
            String response = callGeminiAPI(prompt);
            
            logger.info("✅ Réponse métier générée avec succès");
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Erreur lors du traitement : {}", e.getMessage());
            return getFallbackResponse(userMessage);
        }
    }
    
    /**
     * Vérifier si c'est une conversation sociale (salutations, remerciements, etc.)
     */
    private boolean isSocialConversation(String intention) {
        return intention.equals("SALUTATION") || 
               intention.equals("REMERCIEMENT") || 
               intention.equals("AU_REVOIR") ||
               intention.equals("AIDE");
    }
    
    /**
     * Détecter l'intention de l'utilisateur
     */
    private String detectIntention(String message) {
        String lower = message.toLowerCase();
        
        // Salutations
        if (lower.matches("^(bonjour|bonsoir|salut|hello|hi|hey|coucou).*")) {
            return "SALUTATION";
        }
        
        // Remerciements
        if (lower.matches(".*(merci|thanks|thank you).*")) {
            return "REMERCIEMENT";
        }
        
        // Au revoir
        if (lower.matches(".*(au revoir|bye|à bientôt|adieu|à plus).*")) {
            return "AU_REVOIR";
        }
        
        // Aide
        if (lower.contains("aide") || lower.contains("help") || lower.contains("peux-tu") || lower.contains("comment")) {
            return "AIDE";
        }
        
        // Conventions
        if (lower.contains("convention")) {
            return "CONVENTIONS";
        }
        
        // Factures
        if (lower.contains("facture") || lower.contains("paiement")) {
            return "FACTURES";
        }
        
        // Performance / Analyse
        if (lower.contains("performance") || lower.contains("analyse") || lower.contains("kpi")) {
            return "ANALYSE";
        }
        
        // Général
        return "GENERAL";
    }
    
    /**
     * Extraire les données pertinentes de la base MongoDB avec détails
     */
    private Map<String, Object> extractDatabaseData(String intention) {
        Map<String, Object> data = new HashMap<>();
        
        try {
            // Compter les conventions et factures
            long totalConventions = conventionRepository.count();
            long totalInvoices = invoiceRepository.count();
            
            data.put("totalConventions", totalConventions);
            data.put("totalInvoices", totalInvoices);
            
            // Récupérer les conventions avec détails
            if (intention.equals("CONVENTIONS") || intention.equals("ANALYSE") || intention.equals("GENERAL")) {
                List<Map<String, Object>> conventionsData = conventionRepository.findAll().stream()
                    .limit(20)
                    .map(conv -> {
                        Map<String, Object> convMap = new HashMap<>();
                        convMap.put("nom", conv.getTitle() != null ? conv.getTitle() : "Sans nom");
                        convMap.put("statut", conv.getStatus() != null ? conv.getStatus() : "Inconnu");
                        convMap.put("montant", conv.getAmount() != null ? conv.getAmount() : BigDecimal.ZERO);
                        convMap.put("dateDebut", conv.getStartDate());
                        convMap.put("dateFin", conv.getEndDate());
                        return convMap;
                    })
                    .toList();
                data.put("conventions", conventionsData);
            }
            
            // Récupérer les factures avec détails
            if (intention.equals("FACTURES") || intention.equals("ANALYSE") || intention.equals("GENERAL")) {
                List<Map<String, Object>> invoicesData = invoiceRepository.findAll().stream()
                    .limit(20)
                    .map(inv -> {
                        Map<String, Object> invMap = new HashMap<>();
                        invMap.put("numero", inv.getInvoiceNumber() != null ? inv.getInvoiceNumber() : "N/A");
                        invMap.put("statut", inv.getStatus() != null ? inv.getStatus() : "INCONNU");
                        invMap.put("montant", inv.getAmount() != null ? inv.getAmount() : BigDecimal.ZERO);
                        invMap.put("dateEmission", inv.getIssueDate());
                        invMap.put("dateEcheance", inv.getDueDate());
                        return invMap;
                    })
                    .toList();
                data.put("invoices", invoicesData);
            }
            
            logger.info("📊 Données extraites : {} conventions, {} factures", totalConventions, totalInvoices);
            
        } catch (Exception e) {
            logger.error("❌ Erreur extraction données : {}", e.getMessage());
        }
        
        return data;
    }
    
    /**
     * Construire un prompt pour conversation sociale (sans données métier)
     */
    private String buildSocialPrompt(String userMessage, String intention) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Tu es un assistant décisionnel professionnel et convivial.\n\n");
        
        prompt.append("CONTEXTE :\n");
        prompt.append("L'utilisateur engage une conversation sociale avec toi.\n");
        prompt.append("Tu dois répondre de manière naturelle, professionnelle et chaleureuse.\n\n");
        
        prompt.append(String.format("Message de l'utilisateur : \"%s\"\n\n", userMessage));
        
        prompt.append("INSTRUCTIONS :\n");
        
        switch (intention) {
            case "SALUTATION":
                prompt.append("- Réponds avec une salutation professionnelle et chaleureuse\n");
                prompt.append("- Propose ton aide pour analyser les conventions et factures\n");
                prompt.append("- Sois bref (2-3 phrases maximum)\n");
                break;
                
            case "REMERCIEMENT":
                prompt.append("- Réponds poliment au remerciement\n");
                prompt.append("- Indique que tu es là pour aider\n");
                prompt.append("- Sois bref (1-2 phrases)\n");
                break;
                
            case "AU_REVOIR":
                prompt.append("- Dis au revoir de manière professionnelle\n");
                prompt.append("- Souhaite une bonne journée\n");
                prompt.append("- Sois bref (1-2 phrases)\n");
                break;
                
            case "AIDE":
                prompt.append("- Explique ce que tu peux faire (analyser conventions, factures, KPIs)\n");
                prompt.append("- Donne 2-3 exemples de questions\n");
                prompt.append("- Sois encourageant\n");
                break;
        }
        
        prompt.append("\nRéponds en français de manière naturelle et professionnelle.");
        
        return prompt.toString();
    }
    
    /**
     * Construire un prompt métier avec données MongoDB détaillées
     */
    private String buildBusinessPrompt(String userMessage, Map<String, Object> data, String intention) {
        StringBuilder prompt = new StringBuilder();
        
        // Rôle système
        prompt.append("Tu es un assistant décisionnel intelligent pour un système de gestion de conventions et factures.\n\n");
        prompt.append("RÔLE :\n");
        prompt.append("- Tu analyses les données réelles extraites de MongoDB\n");
        prompt.append("- Tu donnes des réponses précises avec des chiffres concrets\n");
        prompt.append("- Tu identifies les tendances et fais des recommandations\n");
        prompt.append("- Tu es professionnel et orienté prise de décision\n\n");
        
        // Contexte des données DÉTAILLÉES
        prompt.append("📊 DONNÉES RÉELLES DE LA BASE MONGODB :\n\n");
        
        // Conventions
        prompt.append(String.format("📋 CONVENTIONS : %s au total\n", data.get("totalConventions")));
        if (data.containsKey("conventions") && data.get("conventions") != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> conventions = (List<Map<String, Object>>) data.get("conventions");
            if (!conventions.isEmpty()) {
                prompt.append("Détails des conventions :\n");
                for (int i = 0; i < Math.min(conventions.size(), 10); i++) {
                    Map<String, Object> conv = conventions.get(i);
                    prompt.append(String.format("  • %s (Statut: %s, Montant: %s DT)\n", 
                        conv.get("nom"), 
                        conv.get("statut"),
                        conv.get("montant")));
                }
            }
        }
        prompt.append("\n");
        
        // Factures
        prompt.append(String.format("💰 FACTURES : %s au total\n", data.get("totalInvoices")));
        if (data.containsKey("invoices") && data.get("invoices") != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> invoices = (List<Map<String, Object>>) data.get("invoices");
            if (!invoices.isEmpty()) {
                // Calculer statistiques
                long payees = invoices.stream().filter(inv -> "PAYEE".equals(inv.get("statut"))).count();
                long impayees = invoices.stream().filter(inv -> "IMPAYEE".equals(inv.get("statut"))).count();
                long enRetard = invoices.stream().filter(inv -> "EN_RETARD".equals(inv.get("statut"))).count();
                
                prompt.append(String.format("  • Payées : %d\n", payees));
                prompt.append(String.format("  • Impayées : %d\n", impayees));
                prompt.append(String.format("  • En retard : %d\n", enRetard));
                
                // Détails des factures impayées ou en retard
                if (impayees > 0 || enRetard > 0) {
                    prompt.append("\nFactures nécessitant attention :\n");
                    invoices.stream()
                        .filter(inv -> "IMPAYEE".equals(inv.get("statut")) || "EN_RETARD".equals(inv.get("statut")))
                        .limit(5)
                        .forEach(inv -> prompt.append(String.format("  • Facture #%s : %s DT (Statut: %s)\n",
                            inv.get("numero"), inv.get("montant"), inv.get("statut"))));
                }
            }
        }
        prompt.append("\n");
        
        // Question utilisateur
        prompt.append(String.format("❓ QUESTION : \"%s\"\n\n", userMessage));
        
        // Instructions de réponse
        prompt.append("📝 INSTRUCTIONS :\n");
        prompt.append("1. Analyse les données ci-dessus pour répondre à la question\n");
        prompt.append("2. Utilise UNIQUEMENT les chiffres réels fournis (ne jamais inventer)\n");
        prompt.append("3. Structure ta réponse avec des emojis et du formatage markdown\n");
        prompt.append("4. Si pertinent, identifie des tendances ou problèmes\n");
        prompt.append("5. Donne des recommandations concrètes si nécessaire\n");
        prompt.append("6. Sois concis (200-300 mots maximum)\n\n");
        
        prompt.append("Réponds en français de manière professionnelle et orientée décision.");
        
        return prompt.toString();
    }
    
    /**
     * Appeler l'API Gemini pour générer la réponse
     */
    private String callGeminiAPI(String prompt) {
        try {
            logger.info("🚀 Appel Gemini API...");
            logger.info("📝 Prompt (premiers 200 chars): {}", prompt.substring(0, Math.min(200, prompt.length())));
            
            String url = geminiApiUrl + "?key=" + geminiApiKey;
            
            // Construire le body de la requête
            Map<String, Object> requestBody = new HashMap<>();
            
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));
            
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 1024);
            requestBody.put("generationConfig", generationConfig);
            
            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            logger.info("📡 Envoi requête à Gemini...");
            
            // Appel API
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                (Class<Map<String, Object>>)(Class<?>)Map.class
            );
            
            logger.info("✅ Réponse Gemini reçue (status: {})", response.getStatusCode());
            
            // Extraire la réponse
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> responseContent = (Map<String, Object>) candidate.get("content");
                    @SuppressWarnings("unchecked")
                    List<Map<String, String>> parts = (List<Map<String, String>>) responseContent.get("parts");
                    
                    if (parts != null && !parts.isEmpty()) {
                        String responseText = parts.get(0).get("text");
                        logger.info("✅ Réponse Gemini (premiers 100 chars): {}", 
                            responseText.substring(0, Math.min(100, responseText.length())));
                        return responseText;
                    }
                }
            }
            
            logger.warn("⚠️ Réponse Gemini vide ou invalide");
            return "Désolé, je n'ai pas pu générer une réponse.";
            
        } catch (Exception e) {
            logger.error("❌ Erreur appel Gemini API : {}", e.getMessage());
            logger.error("❌ Type d'erreur : {}", e.getClass().getName());
            if (e.getCause() != null) {
                logger.error("❌ Cause : {}", e.getCause().getMessage());
            }
            throw new RuntimeException("Erreur lors de l'appel à Gemini", e);
        }
    }
    
    /**
     * Réponse de secours en cas d'erreur
     */
    private String getFallbackResponse(String userMessage) {
        String lower = userMessage.toLowerCase();
        
        if (lower.matches("^(bonjour|bonsoir|salut|hello|hi).*")) {
            return "👋 Bonjour ! Je suis votre assistant décisionnel. Comment puis-je vous aider ?";
        }
        
        long totalConventions = conventionRepository.count();
        long totalInvoices = invoiceRepository.count();
        
        return String.format(
            "🤖 **Aperçu de vos données**\n\n" +
            "📊 Conventions : %d\n" +
            "📈 Factures : %d\n\n" +
            "Posez-moi une question spécifique pour plus de détails !",
            totalConventions, totalInvoices
        );
    }
}
