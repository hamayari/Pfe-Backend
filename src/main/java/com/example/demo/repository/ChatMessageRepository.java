package com.example.demo.repository;

import com.example.demo.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    
    List<ChatMessage> findByConversationIdOrderByTimestampAsc(String conversationId);
    
    List<ChatMessage> findBySenderId(String senderId);
    
    List<ChatMessage> findByType(ChatMessage.MessageType type);
}


