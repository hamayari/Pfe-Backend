package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeamMember {
    private String id;
    private String username;
    private String name;
    private String role;
    private String status;
    private String avatar;
    private LocalDateTime lastActive;
    private int assignedConventions;
    private int completedTasks;
}































