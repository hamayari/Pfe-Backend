package com.example.demo.controller;

import com.example.demo.model.Client;
import com.example.demo.service.ClientService;
import com.example.demo.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@RestController
@RequestMapping("/api/client/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ClientAuthController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> clientLogin(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email et mot de passe requis"));
        }

        Optional<Client> clientOpt = clientService.authenticateClient(email, password);
        
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            
            // Générer un token JWT pour le client
            String token = jwtUtils.generateClientToken(client.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("client", Map.of(
                "id", client.getId(),
                "email", client.getEmail(),
                "name", client.getName(),
                "forcePasswordChange", client.isForcePasswordChange()
            ));
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Identifiants invalides"));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (email == null || oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tous les champs sont requis"));
        }

        boolean success = clientService.changeClientPassword(email, oldPassword, newPassword);
        
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Mot de passe changé avec succès"));
        } else {
            return ResponseEntity.status(400).body(Map.of("error", "Ancien mot de passe incorrect"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email requis"));
        }

        boolean success = clientService.resetClientPassword(email);
        
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Nouveau mot de passe envoyé par email"));
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "Client non trouvé"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getClientProfile(@RequestHeader("Authorization") String token) {
        try {
            // Extraire le token du header Authorization
            String jwt = token.replace("Bearer ", "");
            
            // Vérifier le token et extraire l'email
            if (jwtUtils.validateJwtToken(jwt)) {
                String email = jwtUtils.getUserNameFromJwtToken(jwt);
                Optional<Client> clientOpt = clientService.getClientByEmail(email);
                
                if (clientOpt.isPresent()) {
                    Client client = clientOpt.get();
                    Map<String, Object> profile = new HashMap<>();
                    profile.put("id", client.getId());
                    profile.put("email", client.getEmail());
                    profile.put("name", client.getName());
                    profile.put("company", client.getCompany());
                    profile.put("phoneNumber", client.getPhoneNumber());
                    profile.put("active", client.isActive());
                    profile.put("forcePasswordChange", client.isForcePasswordChange());
                    profile.put("lastLoginAt", client.getLastLoginAt());
                    
                    return ResponseEntity.ok(profile);
                }
            }
            
            return ResponseEntity.status(401).body(Map.of("error", "Token invalide"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token invalide"));
        }
    }
} 