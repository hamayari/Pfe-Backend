package com.example.demo.controller;

import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Récupérer les informations de l'utilisateur connecté
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getCurrentUserProfile() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "Utilisateur non authentifié");
                return ResponseEntity.status(401).body(response);
            }
            
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String username = userPrincipal.getUsername();
            
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Utilisateur non trouvé");
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
            // Récupérer le premier rôle (ou rôle principal)
            String userRole = user.getRoles().isEmpty() ? "USER" : 
                             user.getRoles().iterator().next().getName().name();
            
            // Construire la réponse avec les informations de l'utilisateur
            response.put("success", true);
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("role", userRole);
            response.put("name", user.getName());
            response.put("firstName", user.getName()); // Utiliser name comme firstName
            response.put("lastName", ""); // Pas de lastName dans le modèle
            response.put("phoneNumber", user.getPhoneNumber());
            
            System.out.println("✅ [PROFILE] Profil récupéré pour " + username + " (role: " + userRole + ")");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ [PROFILE] Erreur récupération profil: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération du profil: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Mettre à jour le numéro de téléphone d'un utilisateur (endpoint public pour configuration initiale)
     */
    @PutMapping("/{username}/phone")
    @PreAuthorize("permitAll()")
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
    
    /**
     * Changer le mot de passe de l'utilisateur connecté
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody ChangePasswordRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "Utilisateur non authentifié");
                return ResponseEntity.status(401).body(response);
            }
            
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String username = userPrincipal.getUsername();
            
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Utilisateur non trouvé");
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
            // Vérifier que les champs ne sont pas vides
            if (request.getOldPassword() == null || request.getOldPassword().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "L'ancien mot de passe est requis");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Le nouveau mot de passe est requis");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "La confirmation du mot de passe est requise");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Vérifier que l'ancien mot de passe est correct
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                response.put("success", false);
                response.put("message", "L'ancien mot de passe est incorrect");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Vérifier que le nouveau mot de passe et la confirmation correspondent
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                response.put("success", false);
                response.put("message", "Le nouveau mot de passe et la confirmation ne correspondent pas");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Vérifier que le nouveau mot de passe est différent de l'ancien
            if (request.getOldPassword().equals(request.getNewPassword())) {
                response.put("success", false);
                response.put("message", "Le nouveau mot de passe doit être différent de l'ancien");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Vérifier la longueur minimale du mot de passe
            if (request.getNewPassword().length() < 6) {
                response.put("success", false);
                response.put("message", "Le mot de passe doit contenir au moins 6 caractères");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Mettre à jour le mot de passe
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            
            System.out.println("🔐 [PROFILE] Mot de passe changé avec succès pour " + username);
            
            response.put("success", true);
            response.put("message", "Mot de passe changé avec succès");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ [PROFILE] Erreur changement mot de passe: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur lors du changement de mot de passe: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}