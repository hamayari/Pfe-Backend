package com.example.demo.service;

import com.example.demo.dto.chatbot.ChatbotResponse;
import com.example.demo.dto.chatbot.GraphiqueData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {

    @InjectMocks
    private GeminiService geminiService;

    private Map<String, Object> contextData;

    @BeforeEach
    void setUp() {
        contextData = new HashMap<>();
        contextData.put("totalConventions", 50L);
        contextData.put("activeConventions", 35L);
        contextData.put("totalFactures", 100L);
        contextData.put("facturesPayees", 75L);
        contextData.put("facturesEnRetard", 10L);
        contextData.put("montantEnRetard", 15000.0);
        contextData.put("montantTotal", 250000.0);
        contextData.put("tauxPaiement", 75.0);
        
        // Set API key to empty to force demo mode
        ReflectionTestUtils.setField(geminiService, "apiKey", "");
    }

    @Test
    void testGenerateResponse_ConventionActive() {
        String question = "Combien de conventions sont actives ?";

        ChatbotResponse response = geminiService.generateResponse(question, contextData);

        assertNotNull(response);
        assertNotNull(response.getTexte());
        assertTrue(response.getTexte().contains("conventions actives") || response.getTexte().contains("35"));
        assertNotNull(response.getKpi());
        assertNotNull(response.getGraphique());
    }

    @Test
    void testGenerateResponse_FacturesRetard() {
        String question = "Combien de factures sont en retard ?";

        ChatbotResponse response = geminiService.generateResponse(question, contextData);

        assertNotNull(response);
        assertNotNull(response.getTexte());
        assertTrue(response.getTexte().contains("retard") || response.getTexte().contains("10"));
        assertNotNull(response.getKpi());
        assertTrue(response.getKpi().containsKey("nombre") || response.getKpi().containsKey("montant"));
    }

    @Test
    void testGenerateResponse_TauxPaiement() {
        String question = "Quel est le taux de paiement des factures ?";

        ChatbotResponse response = geminiService.generateResponse(question, contextData);

        assertNotNull(response);
        assertNotNull(response.getTexte());
        assertTrue(response.getTexte().contains("taux") || response.getTexte().contains("75"));
        assertNotNull(response.getGraphique());
        assertEquals("pie", response.getGraphique().getType());
    }

    @Test
    void testGenerateResponse_RepartitionGeographique() {
        String question = "Quelle est la répartition géographique des conventions ?";

        ChatbotResponse response = geminiService.generateResponse(question, contextData);

        assertNotNull(response);
        assertNotNull(response.getTexte());
        assertTrue(response.getTexte().contains("géographique") || response.getTexte().contains("gouvernorat"));
        assertNotNull(response.getGraphique());
        assertEquals("bar", response.getGraphique().getType());
    }

    @Test
    void testGenerateResponse_ChiffreAffaires() {
        String question = "Quel est le chiffre d'affaires total ?";

        ChatbotResponse response = geminiService.generateResponse(question, contextData);

        assertNotNull(response);
        assertNotNull(response.getTexte());
        assertTrue(response.getTexte().contains("250000") || response.getTexte().contains("chiffre"));
        assertNotNull(response.getKpi());
    }

    @Test
    void testGenerateResponse_Performance() {
        String question = "Quels sont les indicateurs de performance ?";

        ChatbotResponse response = geminiService.generateResponse(question, contextData);

        assertNotNull(response);
        assertNotNull(response.getTexte());
        assertTrue(response.getTexte().contains("performance") || response.getTexte().contains("KPI"));
        assertNotNull(response.getKpi());
        assertNotNull(response.getGraphique());
    }

    @Test
    void testGenerateResponse_Alertes() {
        String question = "Quelles sont les alertes importantes ?";

        ChatbotResponse response = geminiService.generateResponse(question, contextData);

        assertNotNull(response);
        assertNotNull(response.getTexte());
        assertTrue(response.getTexte().toLowerCase().contains("alerte") || 
                   response.getTexte().toLowerCase().contains("important") ||
                   response.getTexte().toLowerCase().contains("facture"));
        assertNotNull(response.getKpi());
    }

    @Test
    void testGenerateResponse_TableauComparaison() {
        String question = "Montre-moi un tableau comparatif des gouvernorats";

        ChatbotResponse response = geminiService.generateResponse(question, contextData);

        assertNotNull(response);
        assertNotNull(response.getTexte());
        assertNotNull(response.getTableau());
        assertNotNull(response.getTableau().getColonnes());
        assertNotNull(response.getTableau().getLignes());
        assertFalse(response.getTableau().getColonnes().isEmpty());
        assertFalse(response.getTableau().getLignes().isEmpty());
    }

    @Test
    void testGenerateResponse_ConventionExpiration() {
        String question = "Combien de conventions expirent dans les 30 prochains jours ?";

        ChatbotResponse response = geminiService.generateResponse(question, contextData);

        assertNotNull(response);
        assertNotNull(response.getTexte());
        assertTrue(response.getTexte().contains("expir") || response.getTexte().contains("30"));
        assertNotNull(response.getKpi());
    }

    @Test
    void testGenerateResponse_GeneralQuestion() {
        String question = "Donne-moi un aperçu général";

        ChatbotResponse response = geminiService.generateResponse(question, contextData);

        assertNotNull(response);
        assertNotNull(response.getTexte());
        assertNotNull(response.getKpi());
    }

    @Test
    void testGenerateResponse_EmptyContext() {
        Map<String, Object> emptyContext = new HashMap<>();
        String question = "Combien de conventions actives ?";

        ChatbotResponse response = geminiService.generateResponse(question, emptyContext);

        assertNotNull(response);
        assertNotNull(response.getTexte());
    }

    @Test
    void testGenerateResponse_NullQuestion() {
        String question = "";
        ChatbotResponse response = geminiService.generateResponse(question, contextData);

        assertNotNull(response);
        assertNotNull(response.getTexte());
    }

    @Test
    void testGenerateResponse_GraphiqueData() {
        String question = "Montre-moi un graphique des conventions";

        ChatbotResponse response = geminiService.generateResponse(question, contextData);

        assertNotNull(response);
        GraphiqueData graphique = response.getGraphique();
        if (graphique != null) {
            assertNotNull(graphique.getType());
            assertNotNull(graphique.getLabels());
            assertNotNull(graphique.getValues());
            assertFalse(graphique.getLabels().isEmpty());
            assertFalse(graphique.getValues().isEmpty());
        }
    }

    @Test
    void testGenerateResponse_KpiData() {
        String question = "Quels sont les KPI principaux ?";

        ChatbotResponse response = geminiService.generateResponse(question, contextData);

        assertNotNull(response);
        assertNotNull(response.getKpi());
        assertFalse(response.getKpi().isEmpty());
    }

    @Test
    void testGenerateResponse_MultipleMetrics() {
        contextData.put("totalUsers", 25L);
        contextData.put("activeUsers", 20L);
        String question = "Analyse complète de la performance";

        ChatbotResponse response = geminiService.generateResponse(question, contextData);

        assertNotNull(response);
        assertNotNull(response.getTexte());
        assertNotNull(response.getKpi());
        assertNotNull(response.getGraphique());
    }
}
