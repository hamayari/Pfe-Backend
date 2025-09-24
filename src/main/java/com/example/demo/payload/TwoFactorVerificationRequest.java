package com.example.demo.payload;

import jakarta.validation.constraints.NotBlank;

public class TwoFactorVerificationRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String code;

    public TwoFactorVerificationRequest() {}

    public TwoFactorVerificationRequest(String username, String code) {
        this.username = username;
        this.code = code;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}