package com.example.demo.repository;

import com.example.demo.model.User;
import com.example.demo.enums.ERole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByCreatedBy(String createdBy);
    List<User> findByRoles_Name(ERole name);
    Page<User> findByUsernameContainingOrEmailContaining(String username, String email, Pageable pageable);
    List<User> findTop20ByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);
    Optional<User> findByResetToken(String token);
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    // Méthodes pour Slack-like features
    List<User> findByStatus(String status);
    List<User> findByStatusIn(List<String> statuses);
    
    // Méthode manquante pour MessagingService
    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);
    
    // Méthodes pour AdminDashboardService
    long countByEnabledTrue();
    long countByEnabledFalse();
    long countByCreatedAtAfter(java.time.LocalDateTime date);
    long countByCreatedAtBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // Méthodes pour le système de notifications
    List<User> findByRoles_NameIn(List<ERole> roles);
}