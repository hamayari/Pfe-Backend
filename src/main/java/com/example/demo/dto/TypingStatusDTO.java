package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TypingStatusDTO {
    private String userId;
    private String username;
    private String conversationId;
    private boolean isTyping;
    private LocalDateTime timestamp;
}
