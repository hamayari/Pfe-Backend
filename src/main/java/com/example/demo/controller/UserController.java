package com.example.demo.controller;

import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.demo.dto.UserDTO;
import com.example.demo.service.UserService;
import com.example.demo.payload.request.UserCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import com.example.demo.model.User;
import com.example.demo.model.NotificationPreference;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create a new user", description = "Creates a new user with specified roles. Only admins can create users.")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(request.getUsername());
        userDTO.setEmail(request.getEmail());
        userDTO.setPhoneNumber(request.getPhoneNumber()); // Ajout du numéro de téléphone
        userDTO.setRoles(request.getRoles().stream()
                .map(role -> role.name())
                .collect(Collectors.toSet()));
        return ResponseEntity.ok(userService.convertToDTO(userService.createUser(userDTO, request.getPassword())));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get all users", description = "Returns a list of all users.")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            System.out.println("Nombre d'utilisateurs trouvés: " + users.size());
            
            List<UserDTO> userDTOs = users.stream()
                .map(user -> {
                    try {
                        System.out.println("Conversion de l'utilisateur: " + user.getUsername());
                        return userService.convertToDTO(user);
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la conversion de l'utilisateur " + user.getUsername() + ": " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            System.err.println("Erreur dans getAllUsers: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update a user", description = "Updates an existing user's information. Only the creator admin or super admin can update users.")
    public ResponseEntity<UserDTO> updateUser(@PathVariable String userId, @Valid @RequestBody UserCreateRequest request) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(request.getUsername());
        userDTO.setEmail(request.getEmail());
        userDTO.setRoles(request.getRoles().stream()
                .map(role -> role.name())
                .collect(Collectors.toSet()));
        // Utilise newPassword si renseigné, sinon password (pour compatibilité)
        String passwordToSet = (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) ? request.getNewPassword() : null;
        return ResponseEntity.ok(userService.convertToDTO(userService.updateUser(userId, userDTO, passwordToSet)));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete a user", description = "Deletes a user. Only the creator admin or super admin can delete users.")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/block")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Block a user", description = "Blocks a user with a given reason. Only admins can block users.")
    public ResponseEntity<UserDTO> blockUser(@PathVariable String userId, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(userService.convertToDTO(userService.blockUser(userId, reason)));
    }

    @PostMapping("/{userId}/unblock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Unblock a user", description = "Unblocks a user. Only admins can unblock users.")
    public ResponseEntity<UserDTO> unblockUser(@PathVariable String userId) {
        return ResponseEntity.ok(userService.convertToDTO(userService.unblockUser(userId)));
    }

    @GetMapping("/{id}/notification-preference")
    public ResponseEntity<NotificationPreference> getNotificationPreference(@PathVariable String id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(user.getNotificationPreference());
    }

    @PutMapping("/{id}/notification-preference")
    public ResponseEntity<?> updateNotificationPreference(@PathVariable String id, @RequestBody NotificationPreference pref) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        user.setNotificationPreference(pref);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    // ================ SLACK-LIKE PROFILE FEATURES ================
    
    @PostMapping("/{userId}/profile-photo")
    @Operation(summary = "Upload profile photo", description = "Upload a profile photo for Slack-like experience")
    public ResponseEntity<?> uploadProfilePhoto(@PathVariable String userId, 
                                               @RequestParam("photo") MultipartFile photo) {
        try {
            if (photo.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Aucun fichier fourni"));
            }

            // Vérifier le type de fichier
            String contentType = photo.getContentType();
            if (!contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le fichier doit être une image"));
            }

            // Sauvegarder la photo
            String photoUrl = userService.saveProfilePhoto(userId, photo);
            
            return ResponseEntity.ok(Map.of(
                "message", "Photo de profil mise à jour avec succès",
                "photoUrl", photoUrl,
                "userId", userId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de l'upload: " + e.getMessage()));
        }
    }

    @PutMapping("/{userId}/status")
    @Operation(summary = "Update user status", description = "Update user status for Slack-like presence")
    public ResponseEntity<?> updateUserStatus(@PathVariable String userId, 
                                             @RequestBody Map<String, String> statusData) {
        try {
            String status = statusData.get("status"); // online, away, busy, offline
            String statusMessage = statusData.get("statusMessage");

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            user.setStatus(status);
            user.setStatusMessage(statusMessage);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                "message", "Statut mis à jour",
                "status", status,
                "statusMessage", statusMessage,
                "userId", userId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la mise à jour du statut: " + e.getMessage()));
        }
    }

    // Endpoint attendu par le frontend: GET /api/users/presence
    @GetMapping("/presence")
    @Operation(summary = "Get online users", description = "Get list of currently online users for Slack-like presence")
    public ResponseEntity<List<Map<String, Object>>> getOnlineUsers() {
        try {
            List<User> onlineUsers = userRepository.findByStatus("online");
            
            List<Map<String, Object>> result = onlineUsers.stream()
                .map(user -> {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", user.getId());
                    userInfo.put("username", user.getUsername());
                    userInfo.put("email", user.getEmail());
                    userInfo.put("avatar", user.getAvatar());
                    userInfo.put("profilePhoto", user.getProfilePhoto());
                    userInfo.put("status", user.getStatus());
                    userInfo.put("statusMessage", user.getStatusMessage());
                    userInfo.put("roles", user.getRoles().stream()
                        .map(role -> role.getName().toString())
                        .collect(Collectors.toList()));
                    return userInfo;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Ne pas remonter 500 au frontend, retourner liste vide pour robustesse
            return ResponseEntity.ok().body(List.of());
        }
    }

    // Recherche utilisateur (pour inviter à une conversation)
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> searchUsers(@RequestParam String q) {
        try {
            List<User> users = userRepository.findTop20ByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q);
            List<Map<String, Object>> result = users.stream().map(u -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", u.getId());
                m.put("username", u.getUsername());
                m.put("email", u.getEmail());
                m.put("avatar", u.getAvatar());
                m.put("status", u.getStatus());
                return m;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }
    // Endpoint utilitaire: forcer les autres utilisateurs hors ligne (tests)
    @PostMapping("/presence/force-offline")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> forceOthersOffline(@RequestParam String keepUsername) {
        List<User> all = userRepository.findAll();
        for (User u : all) {
            if (!u.getUsername().equalsIgnoreCase(keepUsername)) {
                u.setStatus("offline");
                userRepository.save(u);
            } else {
                u.setStatus("online");
                userRepository.save(u);
            }
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/profile-info")
    @Operation(summary = "Get user profile info", description = "Get complete user profile info for Slack-like display")
    public ResponseEntity<Map<String, Object>> getUserProfileInfo(@PathVariable String userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> profileInfo = new HashMap<>();
            profileInfo.put("id", user.getId());
            profileInfo.put("username", user.getUsername());
            profileInfo.put("email", user.getEmail());
            profileInfo.put("avatar", user.getAvatar());
            profileInfo.put("profilePhoto", user.getProfilePhoto());
            profileInfo.put("status", user.getStatus());
            profileInfo.put("statusMessage", user.getStatusMessage());
            profileInfo.put("lastLoginAt", user.getLastLoginAt());
            profileInfo.put("roles", user.getRoles().stream()
                .map(role -> role.getName().toString())
                .collect(Collectors.toList()));

            return ResponseEntity.ok(profileInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
