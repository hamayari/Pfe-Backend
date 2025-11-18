package com.example.demo.service;

import com.example.demo.model.Conversation;
import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleMessagingServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SimpleMessagingService messagingService;

    private Conversation testConversation;
    private Message testMessage;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("USER-001");
        testUser.setUsername("testuser");

        testConversation = new Conversation();
        testConversation.setId("CONV-001");
        testConversation.setName("Test Channel");
        testConversation.setType("GROUP");
        testConversation.setParticipantIds(Arrays.asList("USER-001", "USER-002"));
        testConversation.setIsPublic(true);

        testMessage = new Message();
        testMessage.setId("MSG-001");
        testMessage.setConversationId("CONV-001");
        testMessage.setSenderId("USER-001");
        testMessage.setContent("Test message");
        testMessage.setSentAt(LocalDateTime.now());
    }

    @Test
    void testGetChannelsForUser() {
        when(conversationRepository.findByParticipantIdsContainingAndType("USER-001", "GROUP"))
            .thenReturn(Arrays.asList(testConversation));

        List<Conversation> result = messagingService.getChannelsForUser("USER-001");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(conversationRepository).findByParticipantIdsContainingAndType("USER-001", "GROUP");
    }

    @Test
    void testGetChannelsForUser_Empty() {
        when(conversationRepository.findByParticipantIdsContainingAndType(anyString(), anyString()))
            .thenReturn(new ArrayList<>())
            .thenReturn(new ArrayList<>())
            .thenReturn(Arrays.asList(testConversation));
        when(conversationRepository.findByNameAndType(anyString(), anyString()))
            .thenReturn(new ArrayList<>());
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);

        List<Conversation> result = messagingService.getChannelsForUser("USER-001");

        assertNotNull(result);
    }

    @Test
    void testCreateChannel() {
        when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);

        Conversation result = messagingService.createChannel("New Channel", "Description", true, "USER-001");

        assertNotNull(result);
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void testGetDirectMessagesForUser() {
        testConversation.setType("DIRECT");
        when(conversationRepository.findByParticipantIdsContainingAndType("USER-001", "DIRECT"))
            .thenReturn(Arrays.asList(testConversation));
        when(userRepository.findById("USER-002")).thenReturn(Optional.of(testUser));

        List<Conversation> result = messagingService.getDirectMessagesForUser("USER-001");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testCreateOrGetDirectMessage_Existing() {
        testConversation.setType("DIRECT");
        testConversation.setParticipantIds(Arrays.asList("USER-001", "USER-002"));
        
        when(conversationRepository.findByParticipantIdsContainingAndType("USER-001", "DIRECT"))
            .thenReturn(Arrays.asList(testConversation));

        Conversation result = messagingService.createOrGetDirectMessage("USER-001", "USER-002");

        assertNotNull(result);
        verify(conversationRepository, never()).save(any(Conversation.class));
    }

    @Test
    void testCreateOrGetDirectMessage_New() {
        when(conversationRepository.findByParticipantIdsContainingAndType("USER-001", "DIRECT"))
            .thenReturn(new ArrayList<>());
        when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);

        Conversation result = messagingService.createOrGetDirectMessage("USER-001", "USER-002");

        assertNotNull(result);
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void testGetMessages() {
        when(conversationRepository.findById("CONV-001")).thenReturn(Optional.of(testConversation));
        when(messageRepository.findByConversationIdOrderBySentAtDesc("CONV-001"))
            .thenReturn(Arrays.asList(testMessage));

        List<Message> result = messagingService.getMessages("CONV-001", "USER-001", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetMessages_NoAccess() {
        testConversation.setParticipantIds(Arrays.asList("USER-002", "USER-003"));
        when(conversationRepository.findById("CONV-001")).thenReturn(Optional.of(testConversation));

        List<Message> result = messagingService.getMessages("CONV-001", "USER-001", 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSendMessage() {
        when(conversationRepository.findById("CONV-001")).thenReturn(Optional.of(testConversation));
        when(userRepository.findById("USER-001")).thenReturn(Optional.of(testUser));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
        when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);

        Message result = messagingService.sendMessage("CONV-001", "Hello", "text", "USER-001");

        assertNotNull(result);
        verify(messageRepository).save(any(Message.class));
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void testGetOnlineUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

        List<Map<String, Object>> result = messagingService.getOnlineUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testMarkConversationAsRead() {
        when(messageRepository.findUnreadMessages("CONV-001", "USER-001"))
            .thenReturn(Arrays.asList(testMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        messagingService.markConversationAsRead("CONV-001", "USER-001");

        verify(messageRepository).findUnreadMessages("CONV-001", "USER-001");
        verify(messageRepository).save(testMessage);
    }

    @Test
    void testGetUnreadCount() {
        when(conversationRepository.findByParticipantIdsContaining("USER-001"))
            .thenReturn(Arrays.asList(testConversation));
        when(messageRepository.countUnreadMessages("CONV-001", "USER-001")).thenReturn(5);

        Map<String, Object> result = messagingService.getUnreadCount("USER-001");

        assertNotNull(result);
        assertEquals(5, result.get("total"));
        assertTrue(result.containsKey("byConversation"));
    }

    @Test
    void testGetChannelsForUser_Exception() {
        when(conversationRepository.findByParticipantIdsContainingAndType("USER-001", "GROUP"))
            .thenThrow(new RuntimeException("Database error"));

        List<Conversation> result = messagingService.getChannelsForUser("USER-001");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testCreateChannel_Exception() {
        when(conversationRepository.save(any(Conversation.class)))
            .thenThrow(new RuntimeException("Database error"));

        Conversation result = messagingService.createChannel("Test", "Desc", true, "USER-001");

        assertNotNull(result);
    }

    @Test
    void testSendMessage_NoConversation() {
        when(conversationRepository.findById("CONV-001")).thenReturn(Optional.empty());

        // Le service gère l'exception et retourne un message dummy
        Message result = messagingService.sendMessage("CONV-001", "Hello", "text", "USER-001");
        
        assertNotNull(result);
    }

    @Test
    void testSendMessage_NoUser() {
        when(conversationRepository.findById("CONV-001")).thenReturn(Optional.of(testConversation));
        when(userRepository.findById("USER-001")).thenReturn(Optional.empty());

        // Le service gère l'exception et retourne un message dummy
        Message result = messagingService.sendMessage("CONV-001", "Hello", "text", "USER-001");
        
        assertNotNull(result);
    }
}
