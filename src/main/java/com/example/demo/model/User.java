package com.example.demo.model;

import com.example.demo.enums.ERole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.stream.Collectors;
import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User implements UserDetails {
    @Id
    private String id;

    @NotBlank
    @Size(max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(max = 120)
    @JsonIgnore
    private String password;

    private Set<Role> roles = new HashSet<>();

    // Photo de profil pour Slack-like experience
    private String profilePhoto;
    private String avatar; // URL de l'avatar
    private String status; // Statut utilisateur (online, away, offline, busy)
    private String statusMessage; // Message de statut personnalisé

    private boolean needsPasswordChange = false;
    private Instant lastLoginAt;
    private String lastLoginIp;
    
    private boolean forcePasswordChange;
    private boolean enabled = true;
    private boolean isActive = true;
    private boolean mustChangePassword = false;
    
    private String resetToken;
    private Instant resetTokenExpiry;
    
    private String createdBy;
    private String name;
    
    private NotificationPreference notificationPreference = new NotificationPreference();
    
    private String profileImageUrl;
    
    private String phoneNumber;
    
    // Audit fields
    private Instant createdAt = Instant.now();
    
    private boolean emailVerified = false;
    private boolean locked = false;
    
    // Blocage utilisateur
    private String blockReason;
    private Instant blockedAt;
    
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Manual getters and setters to ensure they exist
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Getters/Setters pour les champs Slack-like
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public boolean isNeedsPasswordChange() {
        return needsPasswordChange;
    }

    public void setNeedsPasswordChange(boolean needsPasswordChange) {
        this.needsPasswordChange = needsPasswordChange;
    }

    public boolean isForcePasswordChange() {
        return forcePasswordChange;
    }

    public void setForcePasswordChange(boolean forcePasswordChange) {
        this.forcePasswordChange = forcePasswordChange;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public Instant getResetTokenExpiry() {
        return resetTokenExpiry;
    }

    public void setResetTokenExpiry(Instant resetTokenExpiry) {
        this.resetTokenExpiry = resetTokenExpiry;
    }

    public boolean hasRole(ERole role) {
        return roles.stream().anyMatch(r -> role == r.getName());
    }

    public ERole getHighestRole() {
        return roles.stream()
            .map(Role::getName)
            .filter(java.util.Objects::nonNull)
            .max(java.util.Comparator.comparingInt(ERole::ordinal))
            .orElse(ERole.ROLE_USER);
    }

    public boolean isSuperAdmin() {
        return hasRole(ERole.ROLE_SUPER_ADMIN);
    }

    public boolean isAdmin() {
        return hasRole(ERole.ROLE_ADMIN);
    }

    public boolean canManage(User other) {
        ERole thisHighestRole = this.getHighestRole();
        ERole otherHighestRole = other.getHighestRole();
        return thisHighestRole.ordinal() > otherHighestRole.ordinal();
    }

    public boolean canCreateRole(ERole targetRole) {
        return getHighestRole().ordinal() >= targetRole.ordinal();
    }

    @Override
    public String getUsername() {
        return username != null ? username : email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public boolean isActive() {
        return isActive;
    }
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
    public String getBlockReason() {
        return blockReason;
    }
    public void setBlockReason(String blockReason) {
        this.blockReason = blockReason;
    }
    public Instant getBlockedAt() {
        return blockedAt;
    }
    public void setBlockedAt(Instant blockedAt) {
        this.blockedAt = blockedAt;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
