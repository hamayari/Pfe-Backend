package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.model.Role;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.dto.UserDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.enums.ERole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.LocalDateTime;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(String id) {
        return userRepository.findById(id.toString())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Transactional
    public User createUser(UserDTO userDTO, String password) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber()); // Ajout du numéro de téléphone
        user.setPassword(passwordEncoder.encode(password));
        
        // Gestion des rôles avec rôle par défaut
        Set<Role> roles;
        if (userDTO.getRoles() == null || userDTO.getRoles().isEmpty()) {
            // Assigner le rôle COMMERCIAL par défaut si aucun rôle n'est spécifié
            System.out.println("⚠️ Aucun rôle spécifié, assignation du rôle COMMERCIAL par défaut");
            Role defaultRole = roleRepository.findByName(ERole.ROLE_COMMERCIAL)
                    .orElseThrow(() -> new ResourceNotFoundException("Default role COMMERCIAL not found"));
            roles = new HashSet<>();
            roles.add(defaultRole);
        } else {
            roles = userDTO.getRoles().stream()
                    .map(role -> roleRepository.findByName(ERole.valueOf(role))
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + role)))
                    .collect(Collectors.toSet());
        }
        user.setRoles(roles);
        
        user.setCreatedAt(Instant.now());
        // Initialisation pour utilisateur actif
        user.setIsActive(true);
        user.setEmailVerified(true);
        user.setLocked(false);
        
        System.out.println("✅ Utilisateur créé: " + user.getUsername() + " avec rôles: " + 
                          user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.joining(", ")));
        
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(String id, UserDTO userDTO, String newPassword) {
        User user = getUserById(id);

        if (userDTO.getUsername() != null) {
            user.setUsername(userDTO.getUsername());
        }
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (newPassword != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        if (userDTO.getRoles() != null) {
            user.setRoles(userDTO.getRoles().stream()
                    .map(role -> roleRepository.findByName(ERole.valueOf(role))
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + role)))
                    .collect(Collectors.toSet()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String id) {
        if (!userRepository.existsById(id.toString())) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id.toString());
    }

    @Transactional
    public User blockUser(String userId, String reason) {
        User user = getUserById(userId);
        user.setIsActive(false);
        user.setBlockReason(reason);
        user.setBlockedAt(Instant.now());
        return userRepository.save(user);
    }

    @Transactional
    public User unblockUser(String userId) {
        User user = getUserById(userId);
        user.setIsActive(true);
        user.setBlockReason(null);
        user.setBlockedAt(null);
        return userRepository.save(user);
    }

    public UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId().toString());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet()));
        dto.setCreatedAt(user.getCreatedAt() != null ? 
            LocalDateTime.ofInstant(user.getCreatedAt(), java.time.ZoneId.systemDefault()) : null);
        dto.setActive(user.isActive());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setLocked(user.isLocked());
        return dto;
    }

    // ================ SLACK-LIKE PROFILE PHOTO MANAGEMENT ================
    
    private static final String UPLOAD_DIR = "uploads/profile-photos/";
    
    public String saveProfilePhoto(String userId, MultipartFile photo) throws IOException {
        // Créer le dossier s'il n'existe pas
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom de fichier unique
        String originalFilename = photo.getOriginalFilename();
        String fileExtension = originalFilename != null ? 
            originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = userId + "_" + UUID.randomUUID().toString() + fileExtension;
        
        // Sauvegarder le fichier
        Path filePath = uploadPath.resolve(filename);
        Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Mettre à jour l'utilisateur
        User user = getUserById(userId);
        String photoUrl = "/api/files/profile-photos/" + filename;
        user.setProfilePhoto(photoUrl);
        user.setAvatar(photoUrl); // Aussi mettre à jour l'avatar
        userRepository.save(user);
        
        return photoUrl;
    }
    
    public void generateDefaultAvatars() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
                String defaultAvatar = generateDefaultAvatar(user.getUsername());
                user.setAvatar(defaultAvatar);
                userRepository.save(user);
            }
        }
    }
    
    private String generateDefaultAvatar(String username) {
        // Générer un avatar par défaut basé sur les initiales
        String initials = username.length() >= 2 ? 
            username.substring(0, 2).toUpperCase() : 
            username.toUpperCase();
        
        // Couleurs pour les avatars
        String[] colors = {"#e91e63", "#9c27b0", "#673ab7", "#3f51b5", "#2196f3", 
                          "#00bcd4", "#009688", "#4caf50", "#8bc34a", "#cddc39"};
        String color = colors[Math.abs(username.hashCode()) % colors.length];
        
        // Créer un SVG simple
        String svg = String.format(
            "<svg width='40' height='40' xmlns='http://www.w3.org/2000/svg'>" +
            "<rect width='40' height='40' fill='%s' rx='6'/>" +
            "<text x='20' y='26' text-anchor='middle' fill='white' font-family='Arial' font-size='14' font-weight='600'>%s</text>" +
            "</svg>", color, initials
        );
        
        return "data:image/svg+xml;base64," + java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }

    // ================ MÉTHODES POUR WEBSOCKET ================

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public void setUserOnlineStatus(String userId, boolean isOnline) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setStatus(isOnline ? "online" : "offline");
                userRepository.save(user);
                System.out.println("🔌 Statut connexion: " + user.getUsername() + " -> " + (isOnline ? "en ligne" : "hors ligne"));
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur statut connexion: " + e.getMessage());
        }
    }
}
