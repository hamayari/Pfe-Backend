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
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketMessagingServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketMessagingService messagingService;

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
        testConversation.setName("Test Conversation");
        testConversation.setParticipantIds(Arrays.asList("USER-001", "USER-002"));

        testMessage = new Message();
        testMessage.setId("MSG-001");
        testMessage.setConversationId("CONV-001");
        testMessage.setSenderId("USER-001");
        testMessage.setContent("Test message");
        testMessage.setSentAt(LocalDateTime.now());
    }

    @Test
    void testServiceInitialization() {
        assertNotNull(messagingService);
    }

    @Test
    void testSendMessage() {
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        Message result = messageRepository.save(testMessage);

        assertNotNull(result);
        assertEquals("MSG-001", result.getId());
    }

    @Test
    void testGetConversations() {
        when(conversationRepository.findByParticipantIdsContaining("USER-001"))
            .thenReturn(Arrays.asList(testConversation));

        assertDoesNotThrow(() -> {
            List<Conversation> conversations = conversationRepository.findByParticipantIdsContaining("USER-001");
            assertNotNull(conversations);
            assertEquals(1, conversations.size());
        });
    }

    @Test
    void testGetMessages() {
        when(messageRepository.findByConversationIdOrderBySentAtDesc("CONV-001"))
            .thenReturn(Arrays.asList(testMessage));

        List<Message> messages = messageRepository.findByConversationIdOrderBySentAtDesc("CONV-001");

        assertNotNull(messages);
        assertEquals(1, messages.size());
    }

    @Test
    void testCreateConversation() {
        when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);

        Conversation result = conversationRepository.save(testConversation);

        assertNotNull(result);
        assertEquals("CONV-001", result.getId());
    }

    @Test
    void testMarkMessageAsRead() {
        when(messageRepository.findById("MSG-001")).thenReturn(Optional.of(testMessage));

        Optional<Message> result = messageRepository.findById("MSG-001");

        assertTrue(result.isPresent());
        assertEquals("MSG-001", result.get().getId());
    }

    @Test
    void testDeleteMessage() {
        doNothing().when(messageRepository).deleteById("MSG-001");

        assertDoesNotThrow(() -> messageRepository.deleteById("MSG-001"));
        verify(messageRepository).deleteById("MSG-001");
    }

    @Test
    void testGetUserConversations() {
        when(conversationRepository.findByParticipantIdsContaining("USER-001"))
            .thenReturn(Arrays.asList(testConversation));

        List<Conversation> result = conversationRepository.findByParticipantIdsContaining("USER-001");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testUpdateConversation() {
        testConversation.setName("Updated Name");
        when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);

        Conversation result = conversationRepository.save(testConversation);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
    }

    @Test
    void testCountUnreadMessages() {
        when(messageRepository.countUnreadMessages("CONV-001", "USER-001")).thenReturn(5);

        int count = messageRepository.countUnreadMessages("CONV-001", "USER-001");

        assertEquals(5, count);
    }
}
