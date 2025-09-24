package com.example.demo.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la vérification du code d'authentification à deux facteurs
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorVerificationRequest {
    
    @NotBlank(message = "L'email est requis")
    @Email(message = "Format d'email invalide")
    private String email;
    
    @NotBlank(message = "Le code de vérification est requis")
    private String code;
    
    @NotBlank(message = "L'identifiant de session 2FA est requis")
    private String twoFactorSessionId;
}
