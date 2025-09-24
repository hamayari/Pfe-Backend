package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationHistoryDTO {
    private String id;
    private String recipientId;
    private String recipientName;
    private String title;
    private String message;
    private String type;
    private String status;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private String channel;
    private boolean successful;
    private String errorMessage;
}







































