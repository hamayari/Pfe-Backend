package com.example.demo.service;

import com.example.demo.dto.MessageDTO;
import com.example.demo.model.Message;
import com.example.demo.model.Conversation;
import com.example.demo.model.User;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class MessagingService {

    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    @Autowired
    private UserRepository userRepository;

    public List<MessageDTO> getAllMessages() {
        return messageRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Method needed by MessagingController
    public List<MessageDTO> getMessages(String conversationId) {
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public MessageDTO sendMessage(MessageDTO messageDTO) {
        Message message = convertToEntity(messageDTO);
        message.setSentAt(LocalDateTime.now());
        Message savedMessage = messageRepository.save(message);
        return convertToDTO(savedMessage);
    }
    
    // Method needed by MessagingController
    public void deleteMessage(String messageId) {
        messageRepository.deleteById(messageId);
    }
    
    // Method needed by MessagingController
    public MessageDTO editMessage(String messageId, String newContent) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setContent(newContent);
        message.setEdited(true);
        message.setEditedAt(LocalDateTime.now());
        return convertToDTO(messageRepository.save(message));
    }
    
    // Method needed by MessagingController
    public List<Conversation> getConversations() {
        return conversationRepository.findAll();
    }
    
    // Method needed by MessagingController
    public Conversation createConversation(Conversation conversation) {
        conversation.setCreatedAt(LocalDateTime.now());
        return conversationRepository.save(conversation);
    }
    
    // Method needed by MessagingController
    public Conversation updateConversation(String id, Conversation conversation) {
        conversation.setId(id);
        conversation.setUpdatedAt(LocalDateTime.now());
        return conversationRepository.save(conversation);
    }
    
    // Method needed by MessagingController
    public void markConversationRead(String conversationId) {
        List<Message> unreadMessages = messageRepository.findByConversationIdAndReadFalse(conversationId);
        unreadMessages.forEach(message -> {
            message.setRead(true);
            message.setReadAt(LocalDateTime.now());
            messageRepository.save(message);
        });
    }
    
    // Method needed by MessagingController
    public List<User> getUsers() {
        return userRepository.findAll();
    }
    
    // Method needed by MessagingController
    public List<User> searchUsers(String query) {
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }
    
    // Method needed by MessagingController
    public List<User> getUsersPresence() {
        return userRepository.findAll(); // Simplified implementation
    }
    
    // Method needed by MessagingController
    public Map<String, Object> getMessageStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMessages", messageRepository.count());
        stats.put("totalConversations", conversationRepository.count());
        return stats;
    }
    
    // Method needed by MessagingController
    public Map<String, Object> getUnreadCounts() {
        Map<String, Object> counts = new HashMap<>();
        counts.put("unreadCount", messageRepository.countByReadFalse());
        return counts;
    }
    
    // Method needed by MessagingController
    public MessageDTO addReaction(String messageId, String emoji) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        // Simplified reaction implementation
        return convertToDTO(message);
    }
    
    // Method needed by MessagingController
    public MessageDTO removeReaction(String messageId, String emoji) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        // Simplified reaction removal implementation
        return convertToDTO(message);
    }

    private MessageDTO convertToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversationId());
        dto.setSenderId(message.getSenderId());
        dto.setSenderName(message.getSenderName());
        dto.setContent(message.getContent());
        dto.setMessageType(message.getType());
        dto.setCreatedAt(message.getSentAt());
        dto.setUpdatedAt(message.getUpdatedAt());
        dto.setEdited(message.isEdited());
        dto.setAttachments(message.getAttachments());
        dto.setRead(message.isRead());
        return dto;
    }

    private Message convertToEntity(MessageDTO dto) {
        Message message = new Message();
        message.setId(dto.getId());
        message.setConversationId(dto.getConversationId());
        message.setSenderId(dto.getSenderId());
        message.setSenderName(dto.getSenderName());
        message.setContent(dto.getContent());
        message.setType(dto.getMessageType());
        return message;
    }
}
