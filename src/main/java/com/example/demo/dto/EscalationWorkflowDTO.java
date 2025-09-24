package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EscalationWorkflowDTO {
    private String id;
    private String title;
    private String description;
    private String level;
    private String status;
    private String assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime dueDate;
    private LocalDateTime resolvedAt;
    private String conventionId;
    private String conventionReference;
    private List<EscalationStep> steps;
    private String resolution;
    private String resolvedBy;
}
