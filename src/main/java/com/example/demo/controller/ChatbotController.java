package com.example.demo.controller;

import com.example.demo.dto.chatbot.ChatbotRequest;
import com.example.demo.dto.chatbot.ChatbotResponse;
import com.example.demo.dto.chatbot.ActionRequest;
import com.example.demo.dto.chatbot.ActionResponse;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.ChatbotContextService;
import com.example.demo.service.GeminiService;
import com.example.demo.service.ChatbotActionService;
import com.example.demo.service.ChatbotNLPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur pour le chatbot décisionnel
 * Accessible uniquement aux utilisateurs avec le rôle DECISION_MAKER
 */
@RestController
@RequestMapping("/api/decideur")
@CrossOrigin(origins = "*")
public class ChatbotController {

    @Autowired
    private GeminiService geminiService;
    
    @Autowired
    private ChatbotContextService contextService;
    
    @Autowired
    private com.example.demo.service.ChatbotService chatbotService;
    
    @Autowired
    private ChatbotActionService actionService;
    
    @Autowired
    private ChatbotNLPService nlpService;
    
    @Value("${gemini.api.key:}")
    private String geminiApiKey;
    
    /**
     * Endpoint principal du chatbot
     * POST /api/decideur/ask
     * 
     * Reçoit une question du décideur et retourne une analyse complète
     */
    @PostMapping("/ask")
    @PreAuthorize("hasAnyRole('DECISION_MAKER', 'DECIDEUR', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ChatbotResponse> ask(@RequestBody ChatbotRequest request) {
        System.out.println("========================================");
        System.out.println("🤖 [CHATBOT] Question reçue du décideur");
        System.out.println("📝 Question: " + request.getQuestion());
        
        try {
            // 1. Préparer le contexte de données
            System.out.println("📊 Préparation du contexte de données...");
            Map<String, Object> contextData = contextService.prepareContext(request.getQuestion());
            System.out.println("✅ Contexte préparé avec " + contextData.size() + " éléments");
            
            // 2. Générer la réponse avec Gemini
            System.out.println("🤖 Appel à Gemini Flash 1.5...");
            ChatbotResponse response = geminiService.generateResponse(
                request.getQuestion(), 
                contextData
            );
            
            System.out.println("✅ Réponse générée avec succès");
            System.out.println("📝 Texte: " + response.getTexte().substring(0, Math.min(100, response.getTexte().length())) + "...");
            System.out.println("📊 KPI: " + response.getKpi().size() + " indicateurs");
            System.out.println("📈 Graphique: " + (response.getGraphique() != null ? response.getGraphique().getType() : "aucun"));
            System.out.println("========================================");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du traitement de la question: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================");
            
            return ResponseEntity.status(500).body(
                new ChatbotResponse(
                    "Désolé, une erreur s'est produite lors du traitement de votre question. Veuillez réessayer.",
                    Map.of("status", "error"),
                    null
                )
            );
        }
    }
    
    /**
     * Endpoint simple pour le chatbot conversationnel
     * POST /api/decideur/chat
     * Body: { "message": "votre question" }
     * Accessible sans authentification pour les tests
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        System.out.println("========================================");
        System.out.println("💬 [CHATBOT] Message reçu");
        System.out.println("📝 Message: " + request.get("message"));
        
        try {
            String userMessage = request.get("message");
            if (userMessage == null || userMessage.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Le message ne peut pas être vide"
                ));
            }
            
            // Traiter le message avec le service chatbot
            String response = chatbotService.processMessage(userMessage);
            
            System.out.println("✅ Réponse générée");
            System.out.println("========================================");
            
            return ResponseEntity.ok(Map.of(
                "response", response,
                "status", "success"
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================");
            
            return ResponseEntity.status(500).body(Map.of(
                "error", "Erreur lors du traitement du message",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Endpoint de test pour vérifier que le chatbot est accessible
     * Accessible sans authentification pour les tests
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        System.out.println("🏥 [CHATBOT HEALTH] Vérification de santé du service");
        boolean isConfigured = geminiApiKey != null && !geminiApiKey.isEmpty();
        System.out.println("🔑 [CHATBOT HEALTH] Clé API Gemini configurée: " + isConfigured);
        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "service", "Chatbot Décisionnel",
            "version", "1.0",
            "gemini_configured", isConfigured ? "true" : "false"
        ));
    }
    
    /**
     * Endpoint de test pour vérifier la connexion Gemini
     * GET /api/decideur/test-gemini
     */
    @GetMapping("/test-gemini")
    public ResponseEntity<Map<String, Object>> testGemini() {
        System.out.println("========================================");
        System.out.println("🧪 [TEST GEMINI] Test de connexion à Gemini...");
        System.out.println("🔑 Clé API configurée: " + (geminiApiKey != null && !geminiApiKey.isEmpty() ? "Oui (masquée)" : "Non"));
        System.out.println("📍 URL API: " + (geminiApiKey != null ? "Configurée" : "Non configurée"));
        
        try {
            // Test simple avec une question basique
            System.out.println("📤 Envoi de la requête de test...");
            String testResponse = chatbotService.processMessage("Dis bonjour en français");
            
            System.out.println("✅ [TEST GEMINI] Succès !");
            System.out.println("📝 Réponse reçue (premiers 100 chars): " + 
                testResponse.substring(0, Math.min(100, testResponse.length())));
            System.out.println("========================================");
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "✅ Gemini fonctionne correctement !",
                "response", testResponse,
                "gemini_configured", true,
                "api_key_present", geminiApiKey != null && !geminiApiKey.isEmpty(),
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            System.err.println("❌ [TEST GEMINI] Erreur: " + e.getMessage());
            System.err.println("❌ Type d'erreur: " + e.getClass().getName());
            if (e.getCause() != null) {
                System.err.println("❌ Cause: " + e.getCause().getMessage());
            }
            System.out.println("========================================");
            
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "❌ Erreur lors du test Gemini",
                "error", e.getMessage(),
                "error_type", e.getClass().getSimpleName(),
                "gemini_configured", geminiApiKey != null && !geminiApiKey.isEmpty(),
                "timestamp", java.time.LocalDateTime.now().toString(),
                "troubleshooting", "Vérifiez que l'API Gemini est activée dans Google Cloud Console"
            ));
        }
    }
    
    /**
     * Endpoint pour exécuter des actions opérationnelles
     * POST /api/decideur/action
     * Body: { "action": "create_convention", "parameters": {...} }
     */
    @PostMapping("/action")
    @PreAuthorize("hasAnyRole('DECISION_MAKER', 'DECIDEUR', 'ADMIN', 'SUPER_ADMIN', 'COMMERCIAL')")
    public ResponseEntity<ActionResponse> executeAction(@RequestBody ActionRequest request) {
        System.out.println("========================================");
        System.out.println("⚙️ [ACTION] Demande d'exécution d'action");
        System.out.println("📝 Action: " + request.getAction());
        System.out.println("📊 Paramètres: " + request.getParameters());
        
        try {
            ActionResponse response = actionService.executeAction(request);
            
            System.out.println(response.isSuccess() ? "✅ [ACTION] Succès" : "❌ [ACTION] Échec");
            System.out.println("📝 Message: " + response.getMessage());
            System.out.println("========================================");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ [ACTION] Erreur: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================");
            
            return ResponseEntity.status(500).body(
                new ActionResponse(false, "Erreur lors de l'exécution de l'action: " + e.getMessage())
            );
        }
    }
    
    /**
     * Endpoint CRUD via prompts
     * POST /api/decideur/prompt
     * 
     * Permet d'exécuter des actions CRUD en langage naturel
     */
    @PostMapping("/prompt")
    @PreAuthorize("hasAnyRole('DECISION_MAKER', 'DECIDEUR', 'ADMIN', 'SUPER_ADMIN', 'COMMERCIAL')")
    public ResponseEntity<ChatbotResponse> processPrompt(
            @RequestBody ChatbotRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        System.out.println("========================================");
        System.out.println("💬 [PROMPT] Requête reçue");
        System.out.println("📝 Prompt: " + request.getQuestion());
        System.out.println("👤 Utilisateur: " + (userPrincipal != null ? userPrincipal.getUsername() : "null"));
        System.out.println("🎭 Rôles: " + (userPrincipal != null ? userPrincipal.getAuthorities() : "null"));
        
        try {
            // Passer l'utilisateur au service NLP pour le contrôle d'accès
            ChatbotResponse response = nlpService.processPromptWithUser(request, userPrincipal);
            
            System.out.println("✅ [PROMPT] Traité avec succès");
            System.out.println("📝 Réponse: " + response.getTexte());
            System.out.println("========================================");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ [PROMPT] Erreur: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================");
            
            ChatbotResponse errorResponse = new ChatbotResponse(
                "❌ Erreur lors du traitement: " + e.getMessage(),
                null,
                null
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
