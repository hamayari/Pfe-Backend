package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

/**
 * Service de contrôle d'accès basé sur les rôles
 * 
 * Règles:
 * - COMMERCIAL: Voit uniquement ses propres données (createdBy = son username)
 * - PROJECT_MANAGER (Chef de Projet): Voit toutes les données des commerciaux
 * - DECISION_MAKER (Décideur): Voit toutes les données (dashboard global)
 * - ADMIN/SUPER_ADMIN: Voit tout
 */
@Service
public class AccessControlService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Récupère l'utilisateur actuellement connecté
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();
        Optional<User> user = userRepository.findByUsername(username);
        
        if (user.isEmpty()) {
            user = userRepository.findByEmail(username);
        }
        
        return user.orElse(null);
    }

    /**
     * Récupère le username de l'utilisateur connecté
     */
    public String getCurrentUsername() {
        User user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * Vérifie si l'utilisateur a un rôle spécifique
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role) || 
                                 auth.getAuthority().equals(role));
    }

    /**
     * Vérifie si l'utilisateur est un commercial
     */
    public boolean isCommercial() {
        return hasRole("COMMERCIAL") || hasRole("ROLE_COMMERCIAL");
    }

    /**
     * Vérifie si l'utilisateur est un chef de projet
     */
    public boolean isProjectManager() {
        return hasRole("PROJECT_MANAGER") || hasRole("ROLE_PROJECT_MANAGER") ||
               hasRole("CHEF_PROJET") || hasRole("ROLE_CHEF_PROJET");
    }

    /**
     * Vérifie si l'utilisateur est un décideur
     */
    public boolean isDecisionMaker() {
        return hasRole("DECISION_MAKER") || hasRole("ROLE_DECISION_MAKER") ||
               hasRole("DECIDEUR") || hasRole("ROLE_DECIDEUR");
    }

    /**
     * Vérifie si l'utilisateur est admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN") || hasRole("ROLE_ADMIN") ||
               hasRole("SUPER_ADMIN") || hasRole("ROLE_SUPER_ADMIN");
    }

    /**
     * Vérifie si l'utilisateur peut voir TOUTES les données
     * (Chef de projet, Décideur, Admin)
     */
    public boolean canViewAllData() {
        return isProjectManager() || isDecisionMaker() || isAdmin();
    }

    /**
     * Vérifie si l'utilisateur peut voir uniquement SES données
     * (Commercial)
     */
    public boolean canViewOnlyOwnData() {
        return isCommercial() && !canViewAllData();
    }

    /**
     * Vérifie si l'utilisateur peut accéder à une ressource créée par un autre utilisateur
     */
    public boolean canAccessResource(String resourceCreatedBy) {
        if (canViewAllData()) {
            return true; // Chef de projet, Décideur, Admin peuvent tout voir
        }

        String currentUsername = getCurrentUsername();
        if (currentUsername == null) {
            return false;
        }

        // Commercial ne peut voir que ses propres ressources
        return currentUsername.equals(resourceCreatedBy);
    }

    /**
     * Log les informations de l'utilisateur connecté (pour debug)
     */
    public void logCurrentUserInfo() {
        User user = getCurrentUser();
        if (user != null) {
            System.out.println("========================================");
            System.out.println("👤 UTILISATEUR CONNECTÉ:");
            System.out.println("   Username: " + user.getUsername());
            System.out.println("   Email: " + user.getEmail());
            System.out.println("   Rôles: " + user.getRoles());
            System.out.println("   Commercial: " + isCommercial());
            System.out.println("   Chef de Projet: " + isProjectManager());
            System.out.println("   Décideur: " + isDecisionMaker());
            System.out.println("   Admin: " + isAdmin());
            System.out.println("   Peut voir toutes les données: " + canViewAllData());
            System.out.println("========================================");
        } else {
            System.out.println("⚠️  Aucun utilisateur connecté");
        }
    }
}
