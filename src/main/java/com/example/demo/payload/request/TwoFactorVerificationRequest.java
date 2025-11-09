package com.example.demo.payload.request;

public class TwoFactorVerificationRequest {
    private String sessionId;
    private String verificationCode;
    private String username;

    public TwoFactorVerificationRequest() {}

    public TwoFactorVerificationRequest(String sessionId, String verificationCode) {
        this.sessionId = sessionId;
        this.verificationCode = verificationCode;
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}























































