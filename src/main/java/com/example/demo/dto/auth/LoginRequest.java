package com.example.demo.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    private String password;
    
    // Le rôle n'est pas obligatoire, mais peut être utilisé pour la vérification
    private String role;
    
    // Indique si la connexion est pour un administrateur
    private boolean adminLogin;
}
