package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EscalationItem {
    private String id;
    private String title;
    private String description;
    private String level;
    private String assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime dueDate;
    private String status;
    private String conventionId;
    private String conventionReference;
}































