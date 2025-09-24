package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller pour la gestion des profils utilisateur
 */
@RestController
@RequestMapping("/api/user-profile")
@CrossOrigin(origins = "*")
public class UserProfileController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Mettre à jour le numéro de téléphone d'un utilisateur (test sans auth)
     */
    @PutMapping("/{username}/phone")
    public ResponseEntity<Map<String, Object>> updatePhoneNumber(
            @PathVariable String username,
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String phoneNumber = request.get("phoneNumber");
            
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Le numéro de téléphone est requis");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Valider le format du numéro (format international)
            if (!phoneNumber.startsWith("+")) {
                response.put("success", false);
                response.put("message", "Le numéro doit être au format international (+XX...)");
                return ResponseEntity.badRequest().body(response);
            }
            
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Utilisateur non trouvé");
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            user.setPhoneNumber(phoneNumber);
            userRepository.save(user);
            
            System.out.println("📱 [PROFILE] Numéro de téléphone mis à jour pour " + username + ": " + phoneNumber);
            
            response.put("success", true);
            response.put("message", "Numéro de téléphone mis à jour avec succès");
            response.put("phoneNumber", phoneNumber);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ [PROFILE] Erreur mise à jour numéro: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Erreur lors de la mise à jour: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Obtenir le profil d'un utilisateur (test sans auth)
     */
    @GetMapping("/{username}")
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Utilisateur non trouvé");
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
            response.put("success", true);
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("phoneNumber", user.getPhoneNumber());
            response.put("name", user.getName());
            response.put("status", user.getStatus());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ [PROFILE] Erreur récupération profil: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}