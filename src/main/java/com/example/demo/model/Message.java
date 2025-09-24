package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "messages")
public class Message {

    @Id
    private String id;

    @Indexed
    private String senderId;

    @Indexed
    private String senderName;

    @Indexed
    private List<String> recipientIds;

    private List<String> recipientNames;

    private String subject;

    private String content;

    @Indexed
    private String messageType; // DIRECT, GROUP, SYSTEM, ANNOUNCEMENT

    @Indexed
    private String priority; // LOW, MEDIUM, HIGH, URGENT

    @Indexed
    private String status; // SENT, DELIVERED, READ, ARCHIVED

    private List<String> attachments;

    private String relatedEntityId; // ID de la convention/facture liée

    private String relatedEntityType; // CONVENTION, INVOICE, etc.

    @Indexed
    private LocalDateTime sentAt;

    private LocalDateTime deliveredAt;
    
    // Champs pour le real-time messaging
    @Indexed
    private String conversationId;
    
    private String type; // text, file, image, etc.
    
    private boolean read;

    private LocalDateTime readAt;

    private LocalDateTime updatedAt;
    
    // Nouvelles fonctionnalités
    private String parentMessageId; // Pour les threads/réponses
    private List<MessageReaction> reactions;
    private List<String> mentions; // IDs des utilisateurs mentionnés
    private boolean edited;
    private LocalDateTime editedAt;
    private boolean deleted;
    private LocalDateTime deletedAt;
    private String senderAvatar;

    // Pinning
    private boolean pinned;
    private LocalDateTime pinnedAt;
    private String pinnedByUserId;

    // Constructeurs
    public Message() {
        this.sentAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "SENT";
    }

    public Message(String senderId, String senderName, List<String> recipientIds, String subject, String content) {
        this();
        this.senderId = senderId;
        this.senderName = senderName;
        this.recipientIds = recipientIds;
        this.subject = subject;
        this.content = content;
        this.messageType = "DIRECT";
        this.priority = "MEDIUM";
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    // Getters et Setters pour les nouvelles propriétés
    public String getParentMessageId() {
        return parentMessageId;
    }

    public void setParentMessageId(String parentMessageId) {
        this.parentMessageId = parentMessageId;
    }

    public List<MessageReaction> getReactions() {
        return reactions;
    }

    public void setReactions(List<MessageReaction> reactions) {
        this.reactions = reactions;
    }

    public List<String> getMentions() {
        return mentions;
    }

    public void setMentions(List<String> mentions) {
        this.mentions = mentions;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public LocalDateTime getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(LocalDateTime editedAt) {
        this.editedAt = editedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public LocalDateTime getPinnedAt() {
        return pinnedAt;
    }

    public void setPinnedAt(LocalDateTime pinnedAt) {
        this.pinnedAt = pinnedAt;
    }

    public String getPinnedByUserId() {
        return pinnedByUserId;
    }

    public void setPinnedByUserId(String pinnedByUserId) {
        this.pinnedByUserId = pinnedByUserId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public List<String> getRecipientIds() {
        return recipientIds;
    }

    public void setRecipientIds(List<String> recipientIds) {
        this.recipientIds = recipientIds;
    }

    public List<String> getRecipientNames() {
        return recipientNames;
    }

    public void setRecipientNames(List<String> recipientNames) {
        this.recipientNames = recipientNames;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public String getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(String relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Getters/Setters pour les nouveaux champs
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", senderId='" + senderId + '\'' +
                ", senderName='" + senderName + '\'' +
                ", recipientIds=" + recipientIds +
                ", subject='" + subject + '\'' +
                ", content='" + content + '\'' +
                ", messageType='" + messageType + '\'' +
                ", priority='" + priority + '\'' +
                ", status='" + status + '\'' +
                ", sentAt=" + sentAt +
                '}';
    }
} 