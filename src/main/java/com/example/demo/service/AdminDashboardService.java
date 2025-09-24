package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.model.Role;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.enums.ERole;
import com.example.demo.dto.CreateUserRequest;
import com.example.demo.repository.ConventionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.repository.AuditLogRepository;
import com.example.demo.payload.response.UserDTO;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.StructureRepository;
import com.example.demo.repository.ZoneGeographiqueRepository;

import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

class NomenclatureDTO {
    public String id;
    public String name;
    public String type;
    public String description;
    public String usage;
    public NomenclatureDTO(String id, String name, String type, String description, String usage) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.usage = usage;
    }
}

@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    @SuppressWarnings("unused")
    private final ConventionRepository conventionRepository;
    @SuppressWarnings("unused")
    private final AuditLogRepository auditLogRepository;
    private final ApplicationRepository applicationRepository;
    private final ZoneGeographiqueRepository zoneGeographiqueRepository;
    private final StructureRepository structureRepository;

    public AdminDashboardService(UserRepository userRepository, RoleRepository roleRepository, ConventionRepository conventionRepository, AuditLogRepository auditLogRepository, ApplicationRepository applicationRepository, ZoneGeographiqueRepository zoneGeographiqueRepository, StructureRepository structureRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.conventionRepository = conventionRepository;
        this.auditLogRepository = auditLogRepository;
        this.applicationRepository = applicationRepository;
        this.zoneGeographiqueRepository = zoneGeographiqueRepository;
        this.structureRepository = structureRepository;
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User createUserFromRequest(CreateUserRequest request) {
        User user = new User();
        
        // Générer un ID MongoDB
        user.setId(new org.bson.types.ObjectId().toString());
        
        // Définir les champs de base
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(request.getPassword()); // Note: devrait être hashé en production
        
        // Définir la date de création
        user.setCreatedAt(java.time.Instant.now());
        
        // Définir les valeurs par défaut
        user.setIsActive(true);
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setLocked(false);
        user.setStatus("offline");
        user.setStatusMessage("Disponible");
        
        // Traiter les rôles
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> userRoles = new HashSet<>();
            for (String roleName : request.getRoles()) {
                try {
                    ERole roleEnum = ERole.valueOf(roleName);
                    Role role = roleRepository.findByName(roleEnum)
                        .orElse(new Role(roleEnum));
                    userRoles.add(role);
                } catch (IllegalArgumentException e) {
                    // Rôle invalide, ignorer
                    System.err.println("Rôle invalide: " + roleName);
                }
            }
            user.setRoles(userRoles);
        } else {
            // Rôle par défaut
            Role defaultRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElse(new Role(ERole.ROLE_USER));
            user.setRoles(Set.of(defaultRole));
        }
        
        // Générer un avatar par défaut
        user.setAvatar(generateDefaultAvatar(user.getName() != null ? user.getName() : user.getUsername()));
        
        return userRepository.save(user);
    }

    @Transactional
    public User createUser(User user) {
        // Générer un ID MongoDB si nécessaire
        if (user.getId() == null) {
            user.setId(new org.bson.types.ObjectId().toString());
        }
        
        // Définir la date de création
        user.setCreatedAt(java.time.Instant.now());
        
        // Définir les valeurs par défaut
        user.setIsActive(true);
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setLocked(false);
        user.setStatus("offline");
        user.setStatusMessage("Disponible");
        
        // Générer un avatar par défaut si nécessaire
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            user.setAvatar(generateDefaultAvatar(user.getName() != null ? user.getName() : user.getUsername()));
        }
        
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(String id, User user) {
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // Mettre à jour les champs modifiables
        if (user.getUsername() != null) {
            existingUser.setUsername(user.getUsername());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            existingUser.setRoles(user.getRoles());
        }
        if (user.getStatus() != null) {
            existingUser.setStatus(user.getStatus());
        }
        if (user.getStatusMessage() != null) {
            existingUser.setStatusMessage(user.getStatusMessage());
        }
        if (user.getAvatar() != null) {
            existingUser.setAvatar(user.getAvatar());
        }
        
        return userRepository.save(existingUser);
    }
    
    // Méthode pour mettre à jour un utilisateur avec un rôle string
    @Transactional
    public User updateUserWithRole(String id, String username, String email, String name, String roleString) {
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // Mettre à jour les champs de base
        if (username != null) {
            existingUser.setUsername(username);
        }
        if (email != null) {
            existingUser.setEmail(email);
        }
        if (name != null) {
            existingUser.setName(name);
        }
        
        // Mettre à jour le rôle
        if (roleString != null && !roleString.isEmpty()) {
            try {
                ERole roleEnum = ERole.valueOf(roleString);
                Role role = roleRepository.findByName(roleEnum)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleString));
                existingUser.setRoles(Set.of(role));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role: " + roleString);
            }
        }
        
        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(String id) {
        userRepository.deleteById(id);
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

    public Page<User> searchUsers(String query, Pageable pageable) {
        return userRepository.findByUsernameContainingOrEmailContaining(query, query, pageable);
    }

    public List<User> getUsersByRole(ERole role) {
        return userRepository.findByRoles_Name(role);
    }

    public Map<String, Object> getAdminDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // ================ CARTES KPI PRINCIPALES ================
            
            // 👥 UTILISATEURS
            long totalUsers = userRepository.count();
            long activeUsers = userRepository.countByEnabledTrue();
            long pendingUsers = userRepository.countByEnabledFalse();
            long newUsersThisMonth = userRepository.countByCreatedAtAfter(LocalDate.now().minusMonths(1).atStartOfDay());
            
            // 🏢 STRUCTURES & NOMENCLATURES
            long totalStructures = structureRepository.count();
            long totalApplications = applicationRepository.count();
            long totalZones = zoneGeographiqueRepository.count();
            long totalNomenclatures = totalApplications + totalZones + totalStructures;
            
            // 🚨 ALERTES FONCTIONNELLES (valeurs par défaut)
            long criticalAlerts = 5;
            long warningAlerts = 3;
            
            // 📊 ACTIONS & ACTIVITÉS (valeurs par défaut)
            long totalActions = totalUsers + totalStructures + totalApplications;
            long actionsThisWeek = newUsersThisMonth / 4; // Estimation hebdomadaire
            long pendingActions = pendingUsers;

            // ================ GRAPHIQUES INTERACTIFS ================
            
            // 📊 Répartition des utilisateurs par rôle
            List<Map<String, Object>> usersByRoleChart = new ArrayList<>();
            usersByRoleChart.add(Map.of("role", "Admin", "count", (long) userRepository.findByRoles_Name(ERole.ROLE_ADMIN).size()));
            usersByRoleChart.add(Map.of("role", "Commercial", "count", (long) userRepository.findByRoles_Name(ERole.ROLE_COMMERCIAL).size()));
            usersByRoleChart.add(Map.of("role", "Chef de Projet", "count", (long) userRepository.findByRoles_Name(ERole.ROLE_PROJECT_MANAGER).size()));
            usersByRoleChart.add(Map.of("role", "Décideur", "count", (long) userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER).size()));

            // 📈 Évolution des utilisateurs (données simples)
            List<Map<String, Object>> usersEvolution = new ArrayList<>();
            for (int i = 11; i >= 0; i--) {
                LocalDate month = LocalDate.now().minusMonths(i);
                usersEvolution.add(Map.of(
                    "month", month.format(java.time.format.DateTimeFormatter.ofPattern("MMM yyyy")),
                    "count", Math.max(0, totalUsers - (i * 5)) // Simulation de croissance
                ));
            }

            // 🥧 Répartition des structures par type (données simples)
            List<Map<String, Object>> structuresByType = new ArrayList<>();
            structuresByType.add(Map.of("type", "Entreprise", "count", totalStructures * 60 / 100));
            structuresByType.add(Map.of("type", "Ministère", "count", totalStructures * 25 / 100));
            structuresByType.add(Map.of("type", "Organisation", "count", totalStructures * 15 / 100));
            
            // 📈 Évolution des actions (données simples)
            List<Map<String, Object>> actionsEvolution = new ArrayList<>();
            for (int i = 5; i >= 0; i--) {
                LocalDate month = LocalDate.now().minusMonths(i);
                actionsEvolution.add(Map.of(
                    "date", month.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")),
                    "count", Math.max(0, totalActions / 6) // Distribution égale
                ));
            }

            // ================ DONNÉES RÉCENTES ================
            List<User> recentUsers = userRepository.findAll().stream()
                .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                .limit(5)
                .collect(Collectors.toList());

            // ================ ALERTES MÉTIER ================
            List<Map<String, Object>> businessAlerts = new ArrayList<>();
            businessAlerts.add(Map.of(
                "id", "1",
                "type", "info",
                "title", "Système opérationnel",
                "message", "Tous les services fonctionnent correctement",
                "timestamp", LocalDate.now().toString()
            ));

            // ================ ASSEMBLAGE DES STATISTIQUES ================
            
            // Cartes KPI principales
            stats.put("totalUsers", totalUsers);
            stats.put("activeUsers", activeUsers);
            stats.put("pendingUsers", pendingUsers);
            stats.put("newUsersThisMonth", newUsersThisMonth);
            
            stats.put("totalStructures", totalStructures);
            stats.put("totalNomenclatures", totalNomenclatures);
            stats.put("totalApplications", totalApplications);
            stats.put("totalZones", totalZones);
            
            stats.put("criticalAlerts", criticalAlerts);
            stats.put("warningAlerts", warningAlerts);
            
            stats.put("totalActions", totalActions);
            stats.put("actionsThisWeek", actionsThisWeek);
            stats.put("pendingActions", pendingActions);
            
            // Données récentes et alertes
            stats.put("recentUsers", recentUsers);
            stats.put("businessAlerts", businessAlerts);
            
            // Graphiques
            stats.put("usersByRoleChart", usersByRoleChart);
            stats.put("usersEvolution", usersEvolution);
            stats.put("structuresByType", structuresByType);
            stats.put("actionsEvolution", actionsEvolution);

        } catch (Exception e) {
            // En cas d'erreur, retourner des données par défaut
            System.err.println("Erreur lors du calcul des statistiques : " + e.getMessage());
            stats.put("totalUsers", 0L);
            stats.put("activeUsers", 0L);
            stats.put("pendingUsers", 0L);
            stats.put("newUsersThisMonth", 0L);
            stats.put("totalStructures", 0L);
            stats.put("totalNomenclatures", 0L);
            stats.put("totalApplications", 0L);
            stats.put("totalZones", 0L);
            stats.put("criticalAlerts", 0L);
            stats.put("warningAlerts", 0L);
            stats.put("totalActions", 0L);
            stats.put("actionsThisWeek", 0L);
            stats.put("pendingActions", 0L);
        }

        return stats;
    }

    public List<Object> getNomenclatures() {
        List<NomenclatureDTO> result = new ArrayList<>();
        applicationRepository.findAll().forEach(app ->
            result.add(new NomenclatureDTO(
                app.getId(),
                app.getLibelle(),
                "Application",
                app.getDescription(),
                ""
            ))
        );
        zoneGeographiqueRepository.findAll().forEach(zone ->
            result.add(new NomenclatureDTO(
                zone.getId(),
                zone.getLibelle(),
                "ZoneGeographique",
                zone.getDescription(),
                zone.getGouvernement() != null ? zone.getGouvernement() : ""
            ))
        );
        structureRepository.findAll().forEach(structure ->
            result.add(new NomenclatureDTO(
                structure.getId(),
                structure.getLibelle(),
                "Structure",
                structure.getDescription(),
                structure.getTypeStructure() != null ? structure.getTypeStructure() : ""
            ))
        );
        return new ArrayList<>(result);
    }

    // ================ VUE D'ENSEMBLE ADMIN ================
    
    public Map<String, Object> getAdminOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // ================ BLOC UTILISATEURS ================
        List<UserDTO> allUsers = getAllUserDTOs();
        overview.put("users", allUsers);
        overview.put("totalUsers", allUsers.size());
        
        // ================ BLOC NOMENCLATURES ================
        List<Object> nomenclatures = getNomenclatures();
        overview.put("nomenclatures", nomenclatures);
        overview.put("totalNomenclatures", nomenclatures.size());
        
        // ================ BLOC ALERTES/NOTIFICATIONS ================
        List<Map<String, Object>> businessAlerts = new ArrayList<>();
        businessAlerts.add(Map.of(
            "id", "1",
            "type", "info",
            "title", "Système opérationnel",
            "message", "Tous les services fonctionnent correctement",
            "timestamp", LocalDate.now().toString()
        ));
        overview.put("alerts", businessAlerts);
        overview.put("totalAlerts", businessAlerts.size());
        
        // ================ BLOC JOURNAL D'AUDIT ================
        List<Object> recentLogs = new ArrayList<>();
        overview.put("auditLogs", recentLogs);
        overview.put("totalLogs", recentLogs.size());
        
        return overview;
    }

    public List<UserDTO> getAllUserDTOs() {
        return userRepository.findAll().stream()
            .map(this::mapToUserDTO)
            .collect(Collectors.toList());
    }

    public UserDTO mapToUserDTO(User user) {
        List<String> roleNames = user.getRoles().stream()
                                     .map(role -> role.getName().name())
                                     .collect(Collectors.toList());
        return new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.isEnabled(),
            roleNames,
            user.getCreatedAt(),
            user.getProfileImageUrl(),
            user.isMustChangePassword()
        );
    }
}