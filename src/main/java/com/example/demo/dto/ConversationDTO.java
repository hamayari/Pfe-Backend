package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ConversationDTO {
    private String id;
    private String name;
    private String description;
    private String type;
    private List<String> participantIds;
    private List<String> participantNames;
    private String createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastMessageAt;
    private String lastMessageId;
    private String lastMessageContent;
    private String lastMessageSenderId;
    private String lastMessageSenderName;
    private boolean active;
    private String avatar;
    private Boolean isPublic;
}














































