package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EscalationStep {
    private String id;
    private String stepName;
    private String description;
    private String assignedTo;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private String status;
    private String comments;
}































