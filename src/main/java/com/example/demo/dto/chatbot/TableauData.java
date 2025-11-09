package com.example.demo.dto.chatbot;

import java.util.List;

public class TableauData {
    private List<String> colonnes;
    private List<List<Object>> lignes;
    
    public TableauData() {}
    
    public TableauData(List<String> colonnes, List<List<Object>> lignes) {
        this.colonnes = colonnes;
        this.lignes = lignes;
    }
    
    public List<String> getColonnes() {
        return colonnes;
    }
    
    public void setColonnes(List<String> colonnes) {
        this.colonnes = colonnes;
    }
    
    public List<List<Object>> getLignes() {
        return lignes;
    }
    
    public void setLignes(List<List<Object>> lignes) {
        this.lignes = lignes;
    }
}
