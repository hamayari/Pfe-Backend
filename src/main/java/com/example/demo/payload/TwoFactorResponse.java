package com.example.demo.payload;

public class TwoFactorResponse {
    private String sessionToken;
    private boolean success;
    
    public TwoFactorResponse(String sessionToken) {
        this.sessionToken = sessionToken;
        this.success = true;
    }
    
    public String getSessionToken() {
        return sessionToken;
    }
    
    public boolean isSuccess() {
        return success;
    }
}
