package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import com.example.demo.model.MessageReaction;

@Data
public class MessageDTO {
    private String id;
    private String conversationId;
    private String senderId;
    private String senderName;
    private String content;
    private String messageType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean edited;
    private List<String> attachments;
    private int reactionCount;
    private boolean isRead;
    
    // Additional fields needed by MessageService
    private List<String> recipientIds;
    private List<String> recipientNames;
    private String subject;
    private String priority;
    private String status;
    private String relatedEntityId;
    private String relatedEntityType;
    private List<String> mentions;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
    private String type;
    private String senderAvatar;
    private boolean pinned;
    private LocalDateTime pinnedAt;
    private String pinnedByUserId;
    private boolean deleted;
    private LocalDateTime deletedAt;
    private String parentMessageId;
    private LocalDateTime editedAt;
    private List<MessageReaction> reactions;
}
