package com.example.demo.config;

import com.example.demo.enums.ERole;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class UserRoleAssigner implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("🔧 Attribution des rôles aux utilisateurs existants...");
        
        // Assigner le rôle ADMIN à l'utilisateur admin
        assignRoleToUser("admin", ERole.ROLE_ADMIN);
        
        // Assigner le rôle SUPER_ADMIN à l'utilisateur superadmin
        assignRoleToUser("superadmin", ERole.ROLE_SUPER_ADMIN);
        
        // Assigner le rôle COMMERCIAL à l'utilisateur commercial
        assignRoleToUser("commercial", ERole.ROLE_COMMERCIAL);
        
        // Assigner le rôle PROJECT_MANAGER à l'utilisateur projectmanager
        assignRoleToUser("projectmanager", ERole.ROLE_PROJECT_MANAGER);
        
        // Assigner le rôle DECISION_MAKER à l'utilisateur decisionmaker
        assignRoleToUser("decisionmaker", ERole.ROLE_DECISION_MAKER);
        
        // Assigner le rôle USER par défaut aux autres utilisateurs
        assignDefaultRoleToOtherUsers();
        
        System.out.println("✅ Attribution des rôles terminée !");
    }
    
    private void assignRoleToUser(String username, ERole role) {
        userRepository.findByUsername(username).ifPresent(user -> {
            if (user.getRoles().isEmpty()) {
                Role roleEntity = roleRepository.findByName(role)
                    .orElseThrow(() -> new RuntimeException("Role " + role + " not found"));
                
                Set<Role> roles = new HashSet<>();
                roles.add(roleEntity);
                user.setRoles(roles);
                
                userRepository.save(user);
                System.out.println("✅ Rôle " + role + " assigné à " + username);
            } else {
                System.out.println("ℹ️ " + username + " a déjà des rôles assignés");
            }
        });
    }
    
    private void assignDefaultRoleToOtherUsers() {
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
            .orElseThrow(() -> new RuntimeException("Role USER not found"));
        
        userRepository.findAll().forEach(user -> {
            if (user.getRoles().isEmpty()) {
                Set<Role> roles = new HashSet<>();
                roles.add(userRole);
                user.setRoles(roles);
                
                userRepository.save(user);
                System.out.println("✅ Rôle USER assigné à " + user.getUsername());
            }
        });
    }
}










