package com.example.demo.dto.chatbot;

import java.util.Map;

public class ChatbotResponse {
    private String texte;
    private Map<String, Object> kpi;
    private GraphiqueData graphique;
    private TableauData tableau;
    
    public ChatbotResponse() {}
    
    public ChatbotResponse(String texte, Map<String, Object> kpi, GraphiqueData graphique) {
        this.texte = texte;
        this.kpi = kpi;
        this.graphique = graphique;
        this.tableau = null;
    }
    
    public ChatbotResponse(String texte, Map<String, Object> kpi, GraphiqueData graphique, TableauData tableau) {
        this.texte = texte;
        this.kpi = kpi;
        this.graphique = graphique;
        this.tableau = tableau;
    }
    
    public String getTexte() {
        return texte;
    }
    
    public void setTexte(String texte) {
        this.texte = texte;
    }
    
    public Map<String, Object> getKpi() {
        return kpi;
    }
    
    public void setKpi(Map<String, Object> kpi) {
        this.kpi = kpi;
    }
    
    public GraphiqueData getGraphique() {
        return graphique;
    }
    
    public void setGraphique(GraphiqueData graphique) {
        this.graphique = graphique;
    }
    
    public TableauData getTableau() {
        return tableau;
    }
    
    public void setTableau(TableauData tableau) {
        this.tableau = tableau;
    }
}
