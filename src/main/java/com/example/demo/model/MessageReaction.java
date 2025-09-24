package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "message_reactions")
public class MessageReaction {
    @Id
    private String id;
    private String messageId;
    private String userId;
    private String reaction;
    private String emoji; // Alias for reaction
    private String userName;
    private LocalDateTime createdAt;
    
    // Constructor needed by MessageService
    public MessageReaction(String emoji, String userId, String userName) {
        this.emoji = emoji;
        this.reaction = emoji; // Set both for compatibility
        this.userId = userId;
        this.userName = userName;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getter for compatibility
    public String getEmoji() {
        return emoji != null ? emoji : reaction;
    }
    
    public void setEmoji(String emoji) {
        this.emoji = emoji;
        this.reaction = emoji;
    }
}
