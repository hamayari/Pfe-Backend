package com.example.demo.security.services;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    
    private final UserRepository userRepository;
    
    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        // Input validation
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Attempted to load user with empty username");
            throw new UsernameNotFoundException("Username cannot be empty");
        }
        
        // Log the attempt to load user
        logger.debug("Loading user by username: {}", username);
        
        try {
            // Find user by username with proper error handling
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        String message = String.format("User with username '%s' not found", username);
                        logger.warn(message);
                        return new UsernameNotFoundException(message);
                    });
            
            // Check if user account is active
            if (!user.isActive()) {
                String message = String.format("User '%s' is not active", username);
                logger.warn(message);
                throw new BadCredentialsException("User account is not active");
            }
            
            logger.debug("Successfully loaded user: {}", username);
            return UserPrincipal.create(user);
            
        } catch (Exception e) {
            logger.error("Error loading user by username: " + username, e);
            if (e instanceof UsernameNotFoundException || e instanceof BadCredentialsException) {
                throw e;
            }
            throw new UsernameNotFoundException("Error loading user: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(String id) {
        if (id == null || id.trim().isEmpty()) {
            logger.warn("Attempted to load user with empty id");
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        
        logger.debug("Loading user by ID: {}", id);
        
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> {
                        String message = String.format("User with ID '%s' not found", id);
                        logger.warn(message);
                        return new UsernameNotFoundException(message);
                    });
            
            if (!user.isActive()) {
                String message = String.format("User with ID '%s' is not active", id);
                logger.warn(message);
                throw new BadCredentialsException("User account is not active");
            }
            
            logger.debug("Successfully loaded user with ID: {}", id);
            return UserPrincipal.create(user);
            
        } catch (Exception e) {
            logger.error("Error loading user by ID: " + id, e);
            if (e instanceof UsernameNotFoundException || e instanceof BadCredentialsException) {
                throw e;
            }
            throw new UsernameNotFoundException("Error loading user with ID: " + id, e);
        }
    }
}
