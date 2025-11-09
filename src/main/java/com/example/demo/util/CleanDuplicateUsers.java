package com.example.demo.util;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

// @Component - Temporairement désactivé
@Order(1) // Exécuter AVANT DataInitializer
public class CleanDuplicateUsers implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🧹 Vérification des doublons d'utilisateurs...");
        
        List<User> allUsers = userRepository.findAll();
        
        // Grouper par username
        Map<String, List<User>> usersByUsername = allUsers.stream()
            .collect(Collectors.groupingBy(User::getUsername));
        
        int duplicatesRemoved = 0;
        
        for (Map.Entry<String, List<User>> entry : usersByUsername.entrySet()) {
            String username = entry.getKey();
            List<User> users = entry.getValue();
            
            if (users.size() > 1) {
                System.out.println("⚠️ Doublon détecté pour: " + username + " (" + users.size() + " entrées)");
                
                // Garder le premier, supprimer les autres
                User toKeep = users.get(0);
                List<User> toDelete = users.subList(1, users.size());
                
                for (User user : toDelete) {
                    System.out.println("   🗑️ Suppression de l'ID: " + user.getId());
                    userRepository.delete(user);
                    duplicatesRemoved++;
                }
                
                System.out.println("   ✅ Conservé l'ID: " + toKeep.getId());
            }
        }
        
        if (duplicatesRemoved > 0) {
            System.out.println("✅ " + duplicatesRemoved + " doublons supprimés");
        } else {
            System.out.println("✅ Aucun doublon trouvé");
        }
    }
}
