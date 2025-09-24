package com.example.demo.payload.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorResponse {
    private String message;
    private boolean requiresTwoFactor;
    private String qrCode;
    private String secretKey;
    
    public TwoFactorResponse(String message, boolean requiresTwoFactor) {
        this.message = message;
        this.requiresTwoFactor = requiresTwoFactor;
    }
    
    public TwoFactorResponse(String message, boolean requiresTwoFactor, String qrCode) {
        this.message = message;
        this.requiresTwoFactor = requiresTwoFactor;
        this.qrCode = qrCode;
    }
}
