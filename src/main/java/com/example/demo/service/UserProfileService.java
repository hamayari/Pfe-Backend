package com.example.demo.service;

import com.example.demo.model.UserProfile;
import com.example.demo.repository.UserProfileRepository;
import com.example.demo.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserProfileService {
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    public UserProfile getCurrentUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        return userProfileRepository.findByEmail(userPrincipal.getEmail())
            .orElseThrow(() -> new AccessDeniedException("Profile not found for current user"));
    }
    
    public UserProfile updateProfile(UserProfile profile) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        UserProfile existingProfile = userProfileRepository.findByEmail(userPrincipal.getEmail())
            .orElseThrow(() -> new AccessDeniedException("Profile not found for current user"));

        // Only allow updating specific fields
        existingProfile.setFirstName(profile.getFirstName());
        existingProfile.setLastName(profile.getLastName());
        existingProfile.setPhoneNumber(profile.getPhoneNumber());
        existingProfile.setLastUpdated(LocalDateTime.now());

        return userProfileRepository.save(existingProfile);
    }
}
