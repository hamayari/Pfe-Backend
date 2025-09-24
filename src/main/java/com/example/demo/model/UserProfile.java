package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Document(collection = "user_profiles")
public class UserProfile {
    @Id
    private String id;
    private String userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String position;
    private String department;
    private String profileImageUrl;
    private LocalDateTime lastUpdated;
    private String bio;
    private String email;
    
    // Additional professional information
    private String skills;
    private String certifications;
    private int yearsOfExperience;
    
    public UserProfile() {
        this.lastUpdated = LocalDateTime.now();
    }
}
