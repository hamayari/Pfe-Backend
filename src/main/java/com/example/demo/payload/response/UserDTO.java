package com.example.demo.payload.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.Set;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String structure;
    private boolean enabled;
    private boolean emailVerified;
    private boolean locked;
    private String blockReason;
    private LocalDateTime blockedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<String> roles;
    private List<String> rolesList; // Pour la compatibilité
    private String profileImageUrl;
    private boolean mustChangePassword;
    private boolean isActive;
    private Instant createdAtInstant; // Pour la compatibilité
    
    // Constructeur pour AdminDashboardService
    public UserDTO(String id, String username, String email, boolean enabled, 
                   List<String> roles, Instant createdAt, String profileImageUrl, 
                   boolean mustChangePassword) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.enabled = enabled;
        this.rolesList = roles;
        this.createdAtInstant = createdAt;
        this.profileImageUrl = profileImageUrl;
        this.mustChangePassword = mustChangePassword;
    }
}
