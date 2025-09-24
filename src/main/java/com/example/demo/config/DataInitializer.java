package com.example.demo.config;

import com.example.demo.enums.ERole;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Créer les rôles s'ils n'existent pas
        createRolesIfNotExist();
        
        // Créer les utilisateurs de test s'ils n'existent pas
        createTestUsersIfNotExist();
    }

    private void createRolesIfNotExist() {
        for (ERole role : ERole.values()) {
            if (!roleRepository.existsByName(role)) {
                Role newRole = new Role();
                newRole.setName(role);
                roleRepository.save(newRole);
                System.out.println("✅ Rôle créé: " + role);
            }
        }
    }

    private void createTestUsersIfNotExist() {
        // Admin utilisateur
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@gestionpro.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setName("Administrateur Système");
            adminUser.setEnabled(true);
            
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(roleRepository.findByName(ERole.ROLE_ADMIN).orElseThrow());
            adminUser.setRoles(adminRoles);
            
            userRepository.save(adminUser);
            System.out.println("✅ Utilisateur Admin créé: admin / admin123");
        }

        // Commercial utilisateur
        if (!userRepository.existsByUsername("commercial")) {
            User commercialUser = new User();
            commercialUser.setUsername("commercial");
            commercialUser.setEmail("commercial@gestionpro.com");
            commercialUser.setPassword(passwordEncoder.encode("commercial123"));
            commercialUser.setName("Commercial Test");
            commercialUser.setEnabled(true);
            
            Set<Role> commercialRoles = new HashSet<>();
            commercialRoles.add(roleRepository.findByName(ERole.ROLE_COMMERCIAL).orElseThrow());
            commercialUser.setRoles(commercialRoles);
            
            userRepository.save(commercialUser);
            System.out.println("✅ Utilisateur Commercial créé: commercial / commercial123");
        }

        // Project Manager utilisateur
        if (!userRepository.existsByUsername("projectmanager")) {
            User pmUser = new User();
            pmUser.setUsername("projectmanager");
            pmUser.setEmail("pm@gestionpro.com");
            pmUser.setPassword(passwordEncoder.encode("pm123"));
            pmUser.setName("Project Manager");
            pmUser.setEnabled(true);
            
            Set<Role> pmRoles = new HashSet<>();
            pmRoles.add(roleRepository.findByName(ERole.ROLE_PROJECT_MANAGER).orElseThrow());
            pmUser.setRoles(pmRoles);
            
            userRepository.save(pmUser);
            System.out.println("✅ Utilisateur Project Manager créé: projectmanager / pm123");
        }

        // Decision Maker utilisateur
        if (!userRepository.existsByUsername("decisionmaker")) {
            User dmUser = new User();
            dmUser.setUsername("decisionmaker");
            dmUser.setEmail("dm@gestionpro.com");
            dmUser.setPassword(passwordEncoder.encode("dm123"));
            dmUser.setName("Decision Maker");
            dmUser.setEnabled(true);
            
            Set<Role> dmRoles = new HashSet<>();
            dmRoles.add(roleRepository.findByName(ERole.ROLE_DECISION_MAKER).orElseThrow());
            dmUser.setRoles(dmRoles);
            
            userRepository.save(dmUser);
            System.out.println("✅ Utilisateur Decision Maker créé: decisionmaker / dm123");
        }

        System.out.println("🎉 Initialisation des données terminée !");
        System.out.println("📋 Credentials de test disponibles :");
        System.out.println("   👤 Admin: admin / admin123");
        System.out.println("   👤 Commercial: commercial / commercial123");
        System.out.println("   👤 Project Manager: projectmanager / pm123");
        System.out.println("   👤 Decision Maker: decisionmaker / dm123");
    }
}
