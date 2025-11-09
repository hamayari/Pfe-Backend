package com.example.demo.dto.chatbot;

import java.util.List;

public class GraphiqueData {
    private String type; // "bar", "pie", "line"
    private List<String> labels;
    private List<Number> values;
    
    public GraphiqueData() {}
    
    public GraphiqueData(String type, List<String> labels, List<Number> values) {
        this.type = type;
        this.labels = labels;
        this.values = values;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public List<String> getLabels() {
        return labels;
    }
    
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
    
    public List<Number> getValues() {
        return values;
    }
    
    public void setValues(List<Number> values) {
        this.values = values;
    }
}
