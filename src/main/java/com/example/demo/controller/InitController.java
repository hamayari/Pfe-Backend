package com.example.demo.controller;

import com.example.demo.enums.ERole;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/init")
@CrossOrigin(origins = "*")
public class InitController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Endpoint pour forcer l'initialisation des données
     * Accessible sans authentification pour bootstrap initial
     */
    @PostMapping("/bootstrap")
    public ResponseEntity<String> bootstrap() {
        try {
            StringBuilder response = new StringBuilder();
            response.append("🚀 Initialisation forcée de la base de données\n\n");

            // Créer les rôles
            response.append("📋 Création des rôles:\n");
            for (ERole roleEnum : ERole.values()) {
                if (!roleRepository.existsByName(roleEnum)) {
                    Role role = new Role();
                    role.setName(roleEnum);
                    roleRepository.save(role);
                    response.append("✅ Rôle créé: ").append(roleEnum).append("\n");
                } else {
                    response.append("ℹ️ Rôle existe déjà: ").append(roleEnum).append("\n");
                }
            }

            // SUPER ADMIN - Point de départ avec TOUS les privilèges
            response.append("\n👑 Création du SUPER ADMIN (point de départ):\n");
            if (!userRepository.existsByUsername("superadmin")) {
                User superAdmin = new User();
                superAdmin.setUsername("superadmin");
                superAdmin.setEmail("superadmin@gestionpro.com");
                superAdmin.setPassword(passwordEncoder.encode("admin123"));
                superAdmin.setName("Super Administrateur");
                superAdmin.setEnabled(true);
                
                Set<Role> superAdminRoles = new HashSet<>();
                superAdminRoles.add(roleRepository.findByName(ERole.ROLE_SUPER_ADMIN).orElseThrow());
                superAdmin.setRoles(superAdminRoles);
                
                userRepository.save(superAdmin);
                response.append("✅ Super Admin créé: superadmin / admin123\n");
            } else {
                response.append("ℹ️ Super Admin existe déjà\n");
            }
            
            response.append("\n👥 Création des utilisateurs:\n");

            // Admin
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@gestionpro.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setName("Administrateur");
                admin.setEnabled(true);
                
                Set<Role> adminRoles = new HashSet<>();
                adminRoles.add(roleRepository.findByName(ERole.ROLE_ADMIN).orElseThrow());
                admin.setRoles(adminRoles);
                
                userRepository.save(admin);
                response.append("✅ Admin créé: admin / admin123\n");
            } else {
                response.append("ℹ️ Admin existe déjà\n");
            }

            // Project Manager
            if (!userRepository.existsByUsername("projectmanager")) {
                User pm = new User();
                pm.setUsername("projectmanager");
                pm.setEmail("pm@gestionpro.com");
                pm.setPassword(passwordEncoder.encode("pm123456"));
                pm.setName("Chef de Projet");
                pm.setEnabled(true);
                
                Set<Role> pmRoles = new HashSet<>();
                pmRoles.add(roleRepository.findByName(ERole.ROLE_PROJECT_MANAGER).orElseThrow());
                pm.setRoles(pmRoles);
                
                userRepository.save(pm);
                response.append("✅ Project Manager créé: projectmanager / pm123456\n");
            } else {
                response.append("ℹ️ Project Manager existe déjà\n");
            }

            // Commercial
            if (!userRepository.existsByUsername("commercial")) {
                User commercial = new User();
                commercial.setUsername("commercial");
                commercial.setEmail("commercial@gestionpro.com");
                commercial.setPassword(passwordEncoder.encode("commercial123"));
                commercial.setName("Commercial");
                commercial.setEnabled(true);
                
                Set<Role> commercialRoles = new HashSet<>();
                commercialRoles.add(roleRepository.findByName(ERole.ROLE_COMMERCIAL).orElseThrow());
                commercial.setRoles(commercialRoles);
                
                userRepository.save(commercial);
                response.append("✅ Commercial créé: commercial / commercial123\n");
            } else {
                response.append("ℹ️ Commercial existe déjà\n");
            }

            // Decision Maker
            if (!userRepository.existsByUsername("decisionmaker")) {
                User dm = new User();
                dm.setUsername("decisionmaker");
                dm.setEmail("dm@gestionpro.com");
                dm.setPassword(passwordEncoder.encode("dm123456"));
                dm.setName("Décideur");
                dm.setEnabled(true);
                
                Set<Role> dmRoles = new HashSet<>();
                dmRoles.add(roleRepository.findByName(ERole.ROLE_DECISION_MAKER).orElseThrow());
                dm.setRoles(dmRoles);
                
                userRepository.save(dm);
                response.append("✅ Decision Maker créé: decisionmaker / dm123456\n");
            } else {
                response.append("ℹ️ Decision Maker existe déjà\n");
            }

            response.append("\n🎉 Initialisation terminée avec succès!\n");
            response.append("\n📋 Credentials disponibles:\n");
            response.append("   👤 Super Admin: superadmin / admin123\n");
            response.append("   👤 Admin: admin / admin123\n");
            response.append("   👤 Project Manager: projectmanager / pm123456\n");
            response.append("   👤 Commercial: commercial / commercial123\n");
            response.append("   👤 Decision Maker: decisionmaker / dm123456\n");

            return ResponseEntity.ok(response.toString());

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("❌ Erreur lors de l'initialisation: " + e.getMessage() + "\n" + 
                      "Stack trace: " + e.getStackTrace()[0]);
        }
    }

    /**
     * Vérifier l'état de la base de données
     */
    @GetMapping("/status")
    public ResponseEntity<String> status() {
        try {
            long userCount = userRepository.count();
            long roleCount = roleRepository.count();

            StringBuilder status = new StringBuilder();
            status.append("📊 État de la base de données:\n\n");
            status.append("👥 Utilisateurs: ").append(userCount).append("\n");
            status.append("🔐 Rôles: ").append(roleCount).append("\n");

            if (userCount == 0) {
                status.append("\n⚠️ Aucun utilisateur trouvé!\n");
                status.append("💡 Exécutez POST /api/init/bootstrap pour initialiser\n");
            } else {
                status.append("\n✅ Utilisateurs existants:\n");
                userRepository.findAll().forEach(user -> {
                    status.append("   - ").append(user.getUsername())
                          .append(" (").append(user.getEmail()).append(")\n");
                });
            }

            return ResponseEntity.ok(status.toString());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("❌ Erreur: " + e.getMessage());
        }
    }

    /**
     * Réinitialiser le mot de passe d'un utilisateur
     */
    @PostMapping("/reset-password/{username}")
    public ResponseEntity<String> resetPassword(@PathVariable String username) {
        try {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            // Mot de passe par défaut selon le rôle
            String defaultPassword = "admin123";
            if (username.equals("projectmanager")) {
                defaultPassword = "pm123456";
            } else if (username.equals("commercial")) {
                defaultPassword = "commercial123";
            } else if (username.equals("decisionmaker")) {
                defaultPassword = "dm123456";
            }

            user.setPassword(passwordEncoder.encode(defaultPassword));
            userRepository.save(user);

            return ResponseEntity.ok("✅ Mot de passe réinitialisé pour " + username + ": " + defaultPassword);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("❌ Erreur: " + e.getMessage());
        }
    }

    /**
     * Supprimer tous les utilisateurs (pour debug uniquement)
     */
    @DeleteMapping("/clear-users")
    public ResponseEntity<String> clearUsers() {
        try {
            long count = userRepository.count();
            userRepository.deleteAll();
            return ResponseEntity.ok("✅ " + count + " utilisateurs supprimés");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("❌ Erreur: " + e.getMessage());
        }
    }
}
