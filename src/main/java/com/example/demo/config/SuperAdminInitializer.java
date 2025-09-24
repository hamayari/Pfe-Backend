package com.example.demo.config;

import com.example.demo.model.Role;
import com.example.demo.enums.ERole;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Set;

/**
 * Initializes a Super Admin at application startup if none exists
 * Credentials are configured in application.properties
 */
@Component
public class SuperAdminInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(SuperAdminInitializer.class);

    @Value("${superadmin.username}")
    private String superAdminUsername;
    
    @Value("${superadmin.email}")
    private String superAdminEmail;
    
    @Value("${superadmin.password}")
    private String superAdminPassword;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            logger.info("Checking SUPER_ADMIN initialization");
            
            // Create SUPER_ADMIN role if not exists
            if (!roleRepository.existsByName(ERole.ROLE_SUPER_ADMIN)) {
                logger.info("Creating SUPER_ADMIN role");
                Role superAdminRole = new Role(ERole.ROLE_SUPER_ADMIN, "Super administrateur avec tous les droits");
                roleRepository.save(superAdminRole);
            }
            
            // Create SUPER_ADMIN user if not exists
            if (!userRepository.existsByEmail(superAdminEmail)) {
                logger.info("Creating SUPER_ADMIN user");
                
                Role superAdminRole = roleRepository.findByName(ERole.ROLE_SUPER_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Role SUPER_ADMIN not found"));
                
                User superAdmin = new User(
                    superAdminUsername,
                    superAdminEmail,
                    passwordEncoder.encode(superAdminPassword)
                );
                superAdmin.setForcePasswordChange(true);
                superAdmin.setRoles(Set.of(superAdminRole));
                
                userRepository.save(superAdmin);
                logger.info("SUPER_ADMIN user created successfully");
            }
        } catch (Exception e) {
            logger.error("Error initializing SUPER_ADMIN: {}", e.getMessage());
        }
    }
}
