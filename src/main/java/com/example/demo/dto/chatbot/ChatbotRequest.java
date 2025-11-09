package com.example.demo.dto.chatbot;

public class ChatbotRequest {
    private String question;
    
    public ChatbotRequest() {}
    
    public ChatbotRequest(String question) {
        this.question = question;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
}
