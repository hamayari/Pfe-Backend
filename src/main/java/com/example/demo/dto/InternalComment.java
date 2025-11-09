package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InternalComment {
    private String id;
    private String authorId;
    private String authorName;
    private String content;
    private LocalDateTime timestamp;
    private String conventionId;
    private String conventionReference;
    private boolean isUrgent;
}






































