package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
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
    private String profileImageUrl;
    private boolean mustChangePassword;
    private boolean isActive;
}







































