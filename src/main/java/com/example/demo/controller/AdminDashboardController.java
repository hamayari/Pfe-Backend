package com.example.demo.controller;


import com.example.demo.model.User;
import com.example.demo.service.AdminDashboardService;
import com.example.demo.dto.CreateUserRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.AuditLogRepository;
import com.example.demo.model.Convention;
import com.example.demo.model.AuditLog;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.StructureRepository;
import com.example.demo.repository.ZoneGeographiqueRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true", maxAge = 3600)
// @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')") // Temporairement désactivé pour le développement
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ConventionRepository conventionRepository;
    private final AuditLogRepository auditLogRepository;
    private final ApplicationRepository applicationRepository;
    private final StructureRepository structureRepository;
    private final ZoneGeographiqueRepository zoneGeographiqueRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminDashboardController(UserRepository userRepository, RoleRepository roleRepository, ConventionRepository conventionRepository, AuditLogRepository auditLogRepository, ApplicationRepository applicationRepository, StructureRepository structureRepository, ZoneGeographiqueRepository zoneGeographiqueRepository, PasswordEncoder passwordEncoder) {
        this.adminDashboardService = new AdminDashboardService(userRepository, roleRepository, conventionRepository, auditLogRepository, applicationRepository, zoneGeographiqueRepository, structureRepository, passwordEncoder);
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.conventionRepository = conventionRepository;
        this.auditLogRepository = auditLogRepository;
        this.applicationRepository = applicationRepository;
        this.structureRepository = structureRepository;
        this.zoneGeographiqueRepository = zoneGeographiqueRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsersManagement() {
        List<User> users = adminDashboardService.getAllUsers();
        
        System.out.println("========================================");
        System.out.println("📋 [GET USERS] Retour de " + users.size() + " utilisateurs");
        
        // Logger les détails du premier utilisateur pour debug
        if (!users.isEmpty()) {
            User firstUser = users.get(0);
            System.out.println("👤 Exemple utilisateur:");
            System.out.println("   Username: " + firstUser.getUsername());
            System.out.println("   Email: " + firstUser.getEmail());
            System.out.println("   Phone: " + firstUser.getPhoneNumber());
            System.out.println("   Roles: " + firstUser.getRoles());
        }
        System.out.println("========================================");
        
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserDetails(@PathVariable String id) {
        return adminDashboardService.getUserById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        try {
            User createdUser = adminDashboardService.createUserFromRequest(request);
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody Map<String, Object> userData) {
        System.out.println("========================================");
        System.out.println("✏️ [UPDATE USER] Modification de l'utilisateur ID: " + id);
        System.out.println("📋 Données reçues: " + userData);
        
        try {
            String username = (String) userData.get("username");
            String email = (String) userData.get("email");
            String name = (String) userData.get("name");
            String phoneNumber = (String) userData.get("phoneNumber");
            String country = (String) userData.get("country");
            
            // Gérer les rôles (peut être un tableau)
            Object rolesObj = userData.get("roles");
            String role = null;
            if (rolesObj instanceof List) {
                List<?> rolesList = (List<?>) rolesObj;
                if (!rolesList.isEmpty()) {
                    role = rolesList.get(0).toString();
                }
            } else if (rolesObj instanceof String) {
                role = (String) rolesObj;
            }
            
            System.out.println("   Username: " + username);
            System.out.println("   Email: " + email);
            System.out.println("   Phone: " + phoneNumber);
            System.out.println("   Role: " + role);
            
            User updatedUser = adminDashboardService.updateUserComplete(id, username, email, name, phoneNumber, country, role);
            
            System.out.println("✅ [UPDATE USER] Utilisateur modifié avec succès");
            System.out.println("========================================");
            
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            System.err.println("❌ [UPDATE USER] Erreur: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la modification: " + e.getMessage());
            
            return ResponseEntity.status(400).body(errorResponse);
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String id) {
        System.out.println("🗑️ [DELETE USER] Suppression de l'utilisateur ID: " + id);
        
        try {
            adminDashboardService.deleteUser(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Utilisateur supprimé avec succès");
            
            System.out.println("✅ [DELETE USER] Utilisateur supprimé avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ [DELETE USER] Erreur: " + e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de la suppression: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/users/search")
    public ResponseEntity<Page<User>> searchUsers(
            @RequestParam String query,
            Pageable pageable) {
        return ResponseEntity.ok(adminDashboardService.searchUsers(query, pageable));
    }

    // Méthodes de gestion des rôles temporairement désactivées
    @GetMapping("/roles")
    public List<Object> getRolesManagement() {
        return new ArrayList<>();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        Map<String, Object> stats = adminDashboardService.getAdminDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/nomenclatures")
    public ResponseEntity<List<Object>> getNomenclatures() {
        List<Object> nomenclatures = adminDashboardService.getNomenclatures();
        return ResponseEntity.ok(nomenclatures);
    }

    @PostMapping("/nomenclatures")
    public ResponseEntity<?> createNomenclature(@RequestBody Map<String, Object> body) {
        String type = (String) body.get("type");
        if (type == null) return ResponseEntity.badRequest().body("Type manquant");
        switch (type) {
            case "Application":
                // Mapper les champs nécessaires
                com.example.demo.model.Application app = new com.example.demo.model.Application();
                app.setLibelle((String) body.get("name"));
                app.setDescription((String) body.get("description"));
                app.setActif(true);
                applicationRepository.save(app);
                break;
            case "Structure":
                com.example.demo.model.Structure structure = new com.example.demo.model.Structure();
                structure.setLibelle((String) body.get("name"));
                structure.setDescription((String) body.get("description"));
                structure.setTypeStructure((String) body.get("usage"));
                structure.setActif(true);
                structureRepository.save(structure);
                break;
            default:
                return ResponseEntity.badRequest().body("Type de nomenclature inconnu");
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/nomenclatures/{id}")
    public ResponseEntity<?> updateNomenclature(@PathVariable String id, @RequestBody Map<String, Object> body) {
        String type = (String) body.get("type");
        if (type == null) return ResponseEntity.badRequest().body("Type manquant");
        switch (type) {
            case "Application":
                var appOpt = applicationRepository.findById(id);
                if (appOpt.isPresent()) {
                    var app = appOpt.get();
                    app.setLibelle((String) body.get("name"));
                    app.setDescription((String) body.get("description"));
                    applicationRepository.save(app);
                }
                break;
            case "Structure":
                var structOpt = structureRepository.findById(id);
                if (structOpt.isPresent()) {
                    var structure = structOpt.get();
                    structure.setLibelle((String) body.get("name"));
                    structure.setDescription((String) body.get("description"));
                    structure.setTypeStructure((String) body.get("usage"));
                    structureRepository.save(structure);
                }
                break;
            default:
                return ResponseEntity.badRequest().body("Type de nomenclature inconnu");
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/nomenclatures/{id}")
    public ResponseEntity<?> deleteNomenclature(@PathVariable String id, @RequestParam String type) {
        switch (type) {
            case "Application":
                applicationRepository.deleteById(id);
                break;
            case "Structure":
                structureRepository.deleteById(id);
                break;
            default:
                return ResponseEntity.badRequest().body("Type de nomenclature inconnu");
        }
        return ResponseEntity.ok().build();
    }

    // @GetMapping("/logs")
    // public ResponseEntity<List<Object>> getRecentLogs() {
    //     List<Object> logs = adminDashboardService.getRecentLogs();
    //     return ResponseEntity.ok(logs);
    // }

    @GetMapping("/database-info")
    public ResponseEntity<Map<String, Object>> getDatabaseInfo() {
        Map<String, Object> dbInfo = new HashMap<>();
        
        // Users collection
        List<User> users = userRepository.findAll();
        dbInfo.put("users", users);
        dbInfo.put("usersCount", users.size());
        
        // Conventions collection
        List<Convention> conventions = conventionRepository.findAll();
        dbInfo.put("conventions", conventions);
        dbInfo.put("conventionsCount", conventions.size());
        
        // Audit logs collection
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        dbInfo.put("auditLogs", auditLogs);
        dbInfo.put("auditLogsCount", auditLogs.size());
        
        return ResponseEntity.ok(dbInfo);
    }

    // We will need to implement services for these
    // @GetMapping("/nomenclatures")
    // public ResponseEntity<?> getNomenclatures() {
    //     // ... implementation
    //     return ResponseEntity.ok().build();
    // }

    // @GetMapping("/logs")
    // public ResponseEntity<?> getRecentLogs() {
    //     // ... implementation
    //     return ResponseEntity.ok().build();
    // }
}