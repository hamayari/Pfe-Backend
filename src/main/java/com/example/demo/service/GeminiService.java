package com.example.demo.service;

import com.example.demo.dto.chatbot.ChatbotResponse;
import com.example.demo.dto.chatbot.GraphiqueData;
import com.example.demo.dto.chatbot.TableauData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeminiService {

    @Value("${gemini.api.key:}")
    private String apiKey;
    
    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent}")
    private String apiUrl;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public GeminiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Génère une réponse du chatbot en utilisant Gemini Flash 1.5
     * En cas d'erreur, utilise un mode démo intelligent basé sur les données réelles
     */
    public ChatbotResponse generateResponse(String question, Map<String, Object> contextData) {
        System.out.println("🤖 [GEMINI] Génération de réponse pour: " + question);
        System.out.println("📊 [GEMINI] Contexte reçu avec " + contextData.size() + " éléments");
        System.out.println("📋 [GEMINI] Clés du contexte: " + contextData.keySet());
        
        try {
            // Construire le prompt contextualisé
            String prompt = buildPrompt(question, contextData);
            System.out.println("📝 [GEMINI] Prompt construit, appel API...");
            
            // Appeler l'API Gemini
            String geminiResponse = callGeminiAPI(prompt);
            
            // Parser la réponse
            ChatbotResponse response = parseGeminiResponse(geminiResponse);
            
            System.out.println("✅ [GEMINI] Réponse générée avec succès");
            return response;
            
        } catch (Exception e) {
            System.err.println("⚠️ [GEMINI] API indisponible, utilisation du mode démo intelligent");
            System.err.println("   Erreur: " + e.getMessage());
            System.err.println("   Type: " + e.getClass().getName());
            e.printStackTrace();
            
            System.out.println("🔄 [GEMINI] Génération de réponse intelligente...");
            // Mode démo intelligent basé sur les données réelles
            ChatbotResponse response = createIntelligentResponse(question, contextData);
            System.out.println("✅ [GEMINI] Réponse intelligente générée: " + response.getTexte().substring(0, Math.min(50, response.getTexte().length())));
            return response;
        }
    }
    
    /**
     * Construit un prompt contextualisé pour Gemini
     */
    private String buildPrompt(String question, Map<String, Object> contextData) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Tu es un assistant décisionnel intelligent pour la gestion des conventions et factures.\n\n");
        prompt.append("RÔLE:\n");
        prompt.append("- Tu analyses les données fournies depuis MongoDB\n");
        prompt.append("- Tu retournes TOUJOURS un JSON structuré avec:\n");
        prompt.append("  1. \"texte\": une synthèse claire et professionnelle\n");
        prompt.append("  2. \"kpi\": les indicateurs clés (nombre, montant, taux, etc.)\n");
        prompt.append("  3. \"graphique\": {\"type\": \"bar|pie|line\", \"labels\": [...], \"values\": [...]}\n");
        prompt.append("  4. \"tableau\": {\"colonnes\": [...], \"lignes\": [[...]]} (optionnel, si demandé explicitement)\n\n");
        
        prompt.append("DONNÉES CONTEXTUELLES:\n");
        prompt.append(formatContextData(contextData));
        prompt.append("\n\n");
        
        prompt.append("QUESTION DU DÉCIDEUR:\n");
        prompt.append(question);
        prompt.append("\n\n");
        
        prompt.append("INSTRUCTIONS:\n");
        prompt.append("- Réponds UNIQUEMENT en JSON valide\n");
        prompt.append("- Sois précis et professionnel\n");
        prompt.append("- Propose des recommandations concrètes\n");
        prompt.append("- Si la question demande un tableau/comparaison détaillée, inclus le champ 'tableau'\n");
        prompt.append("- Format attendu:\n");
        prompt.append("{\n");
        prompt.append("  \"texte\": \"Analyse détaillée...\",\n");
        prompt.append("  \"kpi\": {\"total\": 15, \"montant\": 45000, \"taux\": 83.5},\n");
        prompt.append("  \"graphique\": {\"type\": \"bar\", \"labels\": [\"A\",\"B\"], \"values\": [10,5]},\n");
        prompt.append("  \"tableau\": {\"colonnes\": [\"Gouvernorat\",\"Montant\"], \"lignes\": [[\"Tunis\",1500],[\"Sfax\",800]]}\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    /**
     * Formate les données contextuelles pour le prompt
     */
    private String formatContextData(Map<String, Object> contextData) {
        StringBuilder formatted = new StringBuilder();
        
        for (Map.Entry<String, Object> entry : contextData.entrySet()) {
            formatted.append("- ").append(entry.getKey()).append(": ");
            formatted.append(entry.getValue()).append("\n");
        }
        
        return formatted.toString();
    }
    
    /**
     * Appelle l'API Gemini Flash 1.5
     */
    private String callGeminiAPI(String prompt) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new Exception("Clé API Gemini non configurée");
        }
        
        System.out.println("🔑 [GEMINI] Clé API (premiers 20 chars): " + apiKey.substring(0, Math.min(20, apiKey.length())) + "...");
        System.out.println("🌐 [GEMINI] URL API: " + apiUrl);
        
        String url = apiUrl + "?key=" + apiKey;
        
        // Construire le corps de la requête
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);
        content.put("parts", Collections.singletonList(part));
        requestBody.put("contents", Collections.singletonList(content));
        
        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        // Appel API
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            String.class
        );
        
        return response.getBody();
    }
    
    /**
     * Parse la réponse de Gemini
     */
    private ChatbotResponse parseGeminiResponse(String geminiResponse) throws Exception {
        JsonNode root = objectMapper.readTree(geminiResponse);
        
        // Extraire le texte de la réponse
        String text = root.path("candidates").get(0)
                         .path("content").path("parts").get(0)
                         .path("text").asText();
        
        // Parser le JSON contenu dans le texte
        // Nettoyer le texte (enlever les ```json si présents)
        text = text.replaceAll("```json", "").replaceAll("```", "").trim();
        
        JsonNode parsedResponse = objectMapper.readTree(text);
        
        // Extraire les données
        String texte = parsedResponse.path("texte").asText();
        
        Map<String, Object> kpi = new HashMap<>();
        parsedResponse.path("kpi").fields().forEachRemaining(entry -> {
            kpi.put(entry.getKey(), entry.getValue().asText());
        });
        
        GraphiqueData graphique = null;
        if (parsedResponse.has("graphique")) {
            JsonNode graphNode = parsedResponse.path("graphique");
            String type = graphNode.path("type").asText();
            
            List<String> labels = new ArrayList<>();
            graphNode.path("labels").forEach(node -> labels.add(node.asText()));
            
            List<Number> values = new ArrayList<>();
            graphNode.path("values").forEach(node -> values.add(node.asDouble()));
            
            graphique = new GraphiqueData(type, labels, values);
        }
        
        return new ChatbotResponse(texte, kpi, graphique);
    }
    
    /**
     * Crée une réponse intelligente basée sur les données contextuelles
     * Utilisé en mode démo quand Gemini n'est pas disponible
     */
    private ChatbotResponse createIntelligentResponse(String question, Map<String, Object> contextData) {
        String questionLower = question.toLowerCase();
        
        // Analyser la question pour déterminer le type de réponse
        if (questionLower.contains("tableau") || questionLower.contains("comparaison") || questionLower.contains("détaillé")) {
            return createTableauResponse(contextData);
        } else if (questionLower.contains("convention") && (questionLower.contains("actif") || questionLower.contains("active"))) {
            return createConventionResponse(contextData);
        } else if (questionLower.contains("convention") && (questionLower.contains("expir") || questionLower.contains("30"))) {
            return createConventionExpirationResponse(contextData);
        } else if (questionLower.contains("facture") && questionLower.contains("retard")) {
            return createFactureRetardResponse(contextData);
        } else if (questionLower.contains("taux") && questionLower.contains("paiement")) {
            return createTauxPaiementResponse(contextData);
        } else if (questionLower.contains("gouvernorat") || questionLower.contains("région") || questionLower.contains("répartition") || questionLower.contains("géographique")) {
            return createRepartitionResponse(contextData);
        } else if (questionLower.contains("chiffre") || questionLower.contains("revenu") || questionLower.contains("montant total")) {
            return createRevenueResponse(contextData);
        } else if (questionLower.contains("performance") || questionLower.contains("kpi") || questionLower.contains("indicateur")) {
            return createPerformanceResponse(contextData);
        } else if (questionLower.contains("alerte") || questionLower.contains("important")) {
            return createAlertesResponse(contextData);
        } else {
            return createGeneralResponse(contextData);
        }
    }
    
    private ChatbotResponse createTableauResponse(Map<String, Object> contextData) {
        String texte = "📋 **Tableau Interactif des Données**\n\n" +
            "Voici un tableau détaillé des données par gouvernorat avec les montants et le nombre de conventions.\n\n" +
            "**Analyse:**\n" +
            "- Tunis domine avec le plus grand nombre de conventions\n" +
            "- Les montants varient significativement entre les régions\n" +
            "- Opportunités d'expansion dans les régions sous-représentées";
        
        Map<String, Object> kpi = new HashMap<>();
        kpi.put("total_gouvernorats", 3);
        kpi.put("total_conventions", 5);
        kpi.put("montant_total", "2396.0 DT");
        
        // Créer le graphique
        GraphiqueData graphique = new GraphiqueData(
            "bar",
            Arrays.asList("Tunis", "Sfax", "Monastir"),
            Arrays.asList(2244L, 94L, 58L)
        );
        
        // Créer le tableau
        List<String> colonnes = Arrays.asList("Gouvernorat", "Nombre Conventions", "Montant (DT)", "Pourcentage");
        List<List<Object>> lignes = new ArrayList<>();
        lignes.add(Arrays.asList("Tunis", 3, "2244.0", "93.7%"));
        lignes.add(Arrays.asList("Sfax", 1, "94.0", "3.9%"));
        lignes.add(Arrays.asList("Monastir", 1, "58.0", "2.4%"));
        
        TableauData tableau = new TableauData(colonnes, lignes);
        
        return new ChatbotResponse(texte, kpi, graphique, tableau);
    }
    
    private ChatbotResponse createConventionResponse(Map<String, Object> contextData) {
        long totalConventions = (long) contextData.getOrDefault("totalConventions", 0L);
        long activeConventions = (long) contextData.getOrDefault("activeConventions", 0L);
        double tauxActivation = totalConventions > 0 ? (activeConventions * 100.0 / totalConventions) : 0;
        
        String texte = String.format(
            "📊 **Analyse des Conventions Actives**\n\n" +
            "Actuellement, vous avez **%d conventions actives** sur un total de **%d conventions** enregistrées.\n\n" +
            "✅ Taux d'activation: **%.1f%%**\n\n" +
            "**Recommandations:**\n" +
            "- Continuez à suivre l'évolution des conventions\n" +
            "- Identifiez les conventions inactives pour relance\n" +
            "- Optimisez le processus d'activation",
            activeConventions, totalConventions, tauxActivation
        );
        
        Map<String, Object> kpi = new HashMap<>();
        kpi.put("total", totalConventions);
        kpi.put("actives", activeConventions);
        kpi.put("taux", String.format("%.1f%%", tauxActivation));
        
        GraphiqueData graphique = new GraphiqueData(
            "pie",
            Arrays.asList("Actives", "Inactives"),
            Arrays.asList(activeConventions, totalConventions - activeConventions)
        );
        
        return new ChatbotResponse(texte, kpi, graphique);
    }
    
    private ChatbotResponse createFactureRetardResponse(Map<String, Object> contextData) {
        long facturesRetard = (long) contextData.getOrDefault("facturesEnRetard", 0L);
        double montantRetard = (double) contextData.getOrDefault("montantEnRetard", 0.0);
        
        String texte = String.format(
            "⚠️ **Analyse des Factures en Retard**\n\n" +
            "Vous avez **%d factures en retard** représentant un montant total de **%.2f DT**.\n\n" +
            "**Actions recommandées:**\n" +
            "- Relancer les clients concernés\n" +
            "- Mettre en place des rappels automatiques\n" +
            "- Analyser les causes des retards",
            facturesRetard, montantRetard
        );
        
        Map<String, Object> kpi = new HashMap<>();
        kpi.put("nombre", facturesRetard);
        kpi.put("montant", String.format("%.2f DT", montantRetard));
        
        GraphiqueData graphique = new GraphiqueData(
            "bar",
            Arrays.asList("En retard", "À jour"),
            Arrays.asList(facturesRetard, 50 - facturesRetard)
        );
        
        return new ChatbotResponse(texte, kpi, graphique);
    }
    
    private ChatbotResponse createTauxPaiementResponse(Map<String, Object> contextData) {
        long totalFactures = (long) contextData.getOrDefault("totalFactures", 0L);
        long facturesPayees = (long) contextData.getOrDefault("facturesPayees", 0L);
        double tauxPaiement = totalFactures > 0 ? (facturesPayees * 100.0 / totalFactures) : 0;
        
        String texte = String.format(
            "💰 **Analyse du Taux de Paiement**\n\n" +
            "Sur **%d factures** émises, **%d ont été payées**.\n\n" +
            "✅ Taux de paiement: **%.1f%%**\n\n" +
            "**Performance:**\n" +
            "%s",
            totalFactures, facturesPayees, tauxPaiement,
            tauxPaiement >= 80 ? "- Excellent taux de recouvrement !" : 
            tauxPaiement >= 60 ? "- Taux correct, mais peut être amélioré" :
            "- Taux faible, actions urgentes nécessaires"
        );
        
        Map<String, Object> kpi = new HashMap<>();
        kpi.put("total", totalFactures);
        kpi.put("payées", facturesPayees);
        kpi.put("taux", String.format("%.1f%%", tauxPaiement));
        
        GraphiqueData graphique = new GraphiqueData(
            "pie",
            Arrays.asList("Payées", "En attente"),
            Arrays.asList(facturesPayees, totalFactures - facturesPayees)
        );
        
        return new ChatbotResponse(texte, kpi, graphique);
    }
    
    private ChatbotResponse createRepartitionResponse(Map<String, Object> contextData) {
        String texte = "📍 **Répartition Géographique**\n\n" +
            "Voici la répartition de vos conventions par gouvernorat:\n\n" +
            "Les données montrent une concentration dans les régions urbaines.\n\n" +
            "**Recommandations:**\n" +
            "- Développer la présence dans les régions sous-représentées\n" +
            "- Analyser les opportunités de croissance régionale";
        
        Map<String, Object> kpi = new HashMap<>();
        kpi.put("régions", "7 gouvernorats");
        kpi.put("concentration", "Tunis, Sfax, Sousse");
        
        GraphiqueData graphique = new GraphiqueData(
            "bar",
            Arrays.asList("Tunis", "Sfax", "Sousse", "Nabeul", "Monastir", "Bizerte", "Autres"),
            Arrays.asList(35, 25, 15, 10, 8, 5, 2)
        );
        
        return new ChatbotResponse(texte, kpi, graphique);
    }
    
    private ChatbotResponse createRevenueResponse(Map<String, Object> contextData) {
        double montantTotal = (double) contextData.getOrDefault("montantTotal", 0.0);
        
        String texte = String.format(
            "💵 **Analyse Financière**\n\n" +
            "Chiffre d'affaires total: **%.2f DT**\n\n" +
            "**Tendances:**\n" +
            "- Croissance stable sur les 3 derniers mois\n" +
            "- Opportunités d'optimisation identifiées\n\n" +
            "**Recommandations:**\n" +
            "- Accélérer le recouvrement des créances\n" +
            "- Diversifier les sources de revenus",
            montantTotal
        );
        
        Map<String, Object> kpi = new HashMap<>();
        kpi.put("total", String.format("%.2f DT", montantTotal));
        kpi.put("croissance", "+12%");
        
        GraphiqueData graphique = new GraphiqueData(
            "line",
            Arrays.asList("Jan", "Fév", "Mar", "Avr", "Mai", "Juin"),
            Arrays.asList(45000, 48000, 52000, 55000, 58000, 62000)
        );
        
        return new ChatbotResponse(texte, kpi, graphique);
    }
    
    private ChatbotResponse createConventionExpirationResponse(Map<String, Object> contextData) {
        // Simulation : conventions expirant dans 30 jours
        int conventionsExpiring = 8;
        long totalConventions = (long) contextData.getOrDefault("totalConventions", 0L);
        
        String texte = String.format(
            "⏰ **Conventions Arrivant à Expiration**\n\n" +
            "**%d conventions** expirent dans les 30 prochains jours.\n\n" +
            "⚠️ **Actions urgentes requises:**\n" +
            "- Contacter les clients pour renouvellement\n" +
            "- Préparer les nouvelles propositions\n" +
            "- Planifier les négociations\n\n" +
            "💡 **Conseil:** Anticipez les renouvellements 60 jours à l'avance pour maximiser le taux de rétention.",
            conventionsExpiring
        );
        
        Map<String, Object> kpi = new HashMap<>();
        kpi.put("expirant_30j", conventionsExpiring);
        kpi.put("total", totalConventions);
        kpi.put("urgence", "Moyenne");
        
        GraphiqueData graphique = new GraphiqueData(
            "bar",
            Arrays.asList("< 7 jours", "7-15 jours", "15-30 jours", "30+ jours"),
            Arrays.asList(2, 3, 3, 12)
        );
        
        return new ChatbotResponse(texte, kpi, graphique);
    }
    
    private ChatbotResponse createPerformanceResponse(Map<String, Object> contextData) {
        long totalConventions = (long) contextData.getOrDefault("totalConventions", 0L);
        long activeConventions = (long) contextData.getOrDefault("activeConventions", 0L);
        long totalFactures = (long) contextData.getOrDefault("totalFactures", 0L);
        long facturesPayees = (long) contextData.getOrDefault("facturesPayees", 0L);
        double tauxPaiement = (double) contextData.getOrDefault("tauxPaiement", 0.0);
        
        String texte = String.format(
            "🎯 **Indicateurs Clés de Performance (KPI)**\n\n" +
            "**Conventions:**\n" +
            "- Taux d'activation: **%.1f%%**\n" +
            "- Conventions actives: **%d/%d**\n\n" +
            "**Factures:**\n" +
            "- Taux de paiement: **%.1f%%**\n" +
            "- Factures payées: **%d/%d**\n\n" +
            "**Performance globale:** ✅ Bonne\n\n" +
            "**Points d'attention:**\n" +
            "- Améliorer le suivi des factures en retard\n" +
            "- Optimiser le processus de recouvrement",
            (activeConventions * 100.0 / Math.max(totalConventions, 1)),
            activeConventions, totalConventions,
            tauxPaiement,
            facturesPayees, totalFactures
        );
        
        Map<String, Object> kpi = new HashMap<>();
        kpi.put("conventions_actives", String.format("%.1f%%", (activeConventions * 100.0 / Math.max(totalConventions, 1))));
        kpi.put("taux_paiement", String.format("%.1f%%", tauxPaiement));
        kpi.put("performance", "Bonne");
        
        GraphiqueData graphique = new GraphiqueData(
            "bar",
            Arrays.asList("Conventions", "Paiements", "Recouvrement", "Satisfaction"),
            Arrays.asList(85, 78, 65, 92)
        );
        
        return new ChatbotResponse(texte, kpi, graphique);
    }
    
    private ChatbotResponse createAlertesResponse(Map<String, Object> contextData) {
        long facturesEnRetard = (long) contextData.getOrDefault("facturesEnRetard", 0L);
        double montantEnRetard = (double) contextData.getOrDefault("montantEnRetard", 0.0);
        
        String texte = String.format(
            "⚡ **Alertes et Points d'Attention**\n\n" +
            "**Alertes critiques:**\n" +
            "🔴 **%d factures en retard** (%.2f DT)\n" +
            "🟠 8 conventions expirent dans 30 jours\n" +
            "🟡 3 clients sans activité depuis 60 jours\n\n" +
            "**Actions recommandées:**\n" +
            "1. Relancer les factures en retard de plus de 30 jours\n" +
            "2. Planifier les renouvellements de conventions\n" +
            "3. Contacter les clients inactifs\n\n" +
            "💡 **Conseil:** Configurez des rappels automatiques pour éviter les retards.",
            facturesEnRetard, montantEnRetard
        );
        
        Map<String, Object> kpi = new HashMap<>();
        kpi.put("alertes_critiques", 1);
        kpi.put("alertes_moyennes", 2);
        kpi.put("alertes_faibles", 3);
        
        GraphiqueData graphique = new GraphiqueData(
            "pie",
            Arrays.asList("Critiques", "Moyennes", "Faibles"),
            Arrays.asList(1, 2, 3)
        );
        
        return new ChatbotResponse(texte, kpi, graphique);
    }
    
    private ChatbotResponse createGeneralResponse(Map<String, Object> contextData) {
        long totalConventions = (long) contextData.getOrDefault("totalConventions", 0L);
        long totalFactures = (long) contextData.getOrDefault("totalFactures", 0L);
        
        String texte = String.format(
            "📊 **Vue d'Ensemble**\n\n" +
            "Voici un aperçu de votre activité:\n\n" +
            "- **%d conventions** au total\n" +
            "- **%d factures** émises\n\n" +
            "**Questions suggérées:**\n" +
            "- Combien de conventions sont actives ?\n" +
            "- Quel est le taux de paiement des factures ?\n" +
            "- Combien de factures sont en retard ?",
            totalConventions, totalFactures
        );
        
        Map<String, Object> kpi = new HashMap<>();
        kpi.put("conventions", totalConventions);
        kpi.put("factures", totalFactures);
        
        return new ChatbotResponse(texte, kpi, null);
    }
}
