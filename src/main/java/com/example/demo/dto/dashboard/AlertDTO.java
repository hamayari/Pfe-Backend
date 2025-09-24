package com.example.demo.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertDTO {
    private String id;
    private String title;
    private String description;
    private AlertSeverity severity;
    private LocalDateTime timestamp;
    private String source;
    private boolean acknowledged;
    private String actionUrl;
    private String category;
    private Map<String, Object> metadata;
}
