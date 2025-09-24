package com.example.demo.dto.auth;

import com.example.demo.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la réponse d'authentification à deux facteurs
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorResponse {
    private boolean requireTwoFactor;
    private String token;
    private User user;
    private String twoFactorSessionId;
    
    // Constructeur pour le cas où l'authentification à deux facteurs est requise
    public TwoFactorResponse(String twoFactorSessionId) {
        this.requireTwoFactor = true;
        this.twoFactorSessionId = twoFactorSessionId;
    }
    
    // Constructeur pour le cas où l'authentification à deux facteurs n'est pas requise
    public TwoFactorResponse(String token, User user) {
        this.requireTwoFactor = false;
        this.token = token;
        this.user = user;
    }
}
