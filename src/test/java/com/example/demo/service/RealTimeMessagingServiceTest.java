package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RealTimeMessagingServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RealTimeMessagingService messagingService;

    private Conversation testConversation;
    private Message testMessage;
    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setName(com.example.demo.enums.ERole.ROLE_COMMERCIAL);

        testUser = new User();
        testUser.setId("USER-001");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRoles(new HashSet<>(Arrays.asList(testRole)));

        testConversation = new Conversation();
        testConversation.setId("CONV-001");
        testConversation.setName("Test Channel");
        testConversation.setType("GROUP");
        testConversation.setParticipantIds(Arrays.asList("USER-001", "USER-002"));
        testConversation.setCreatedAt(LocalDateTime.now());
        testConversation.setIsPublic(true);
        testConversation.setActive(true);

        testMessage = new Message();
        testMessage.setId("MSG-001");
        testMessage.setConversationId("CONV-001");
        testMessage.setSenderId("USER-001");
        testMessage.setSenderName("testuser");
        testMessage.setContent("Test message");
        testMessage.setType("text");
        testMessage.setSentAt(LocalDateTime.now());
        testMessage.setRead(false);
    }

    @Test
    void testGetChannelsForUser() {
        when(conversationRepository.findByParticipantIdsContainingAndType("USER-001", "GROUP"))
            .thenReturn(Arrays.asList(testConversation));
        when(messageRepository.countUnreadMessages("CONV-001", "USER-001")).thenReturn(5);

        List<Conversation> result = messagingService.getChannelsForUser("USER-001");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getUnreadCount());
        verify(conversationRepository).findByParticipantIdsContainingAndType("USER-001", "GROUP");
    }

    @Test
    void testCreateChannel() {
        when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);

        Conversation result = messagingService.createChannel(
            "New Channel",
            "Channel description",
            true,
            Arrays.asList("USER-002"),
            "USER-001"
        );

        assertNotNull(result);
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void testGetDirectMessagesForUser() {
        testConversation.setType("DIRECT");
        when(conversationRepository.findByParticipantIdsContainingAndType("USER-001", "DIRECT"))
            .thenReturn(Arrays.asList(testConversation));
        when(messageRepository.countUnreadMessages("CONV-001", "USER-001")).thenReturn(2);
        when(userRepository.findById("USER-002")).thenReturn(Optional.of(testUser));

        List<Conversation> result = messagingService.getDirectMessagesForUser("USER-001");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(conversationRepository).findByParticipantIdsContainingAndType("USER-001", "DIRECT");
    }

    @Test
    void testCreateOrGetDirectMessage() {
        when(conversationRepository.findDirectConversationBetweenUsers("USER-001", "USER-002"))
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
        verify(messageRepository).findByConversationIdOrderBySentAtDesc("CONV-001");
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
        verify(userRepository).findAll();
    }

    @Test
    void testUpdateUserStatus() {
        when(userRepository.findById("USER-001")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        messagingService.updateUserStatus("USER-001", "away", "In a meeting");

        verify(userRepository).save(testUser);
        assertEquals("away", testUser.getStatus());
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
        when(messageRepository.countUnreadMessages("CONV-001", "USER-001")).thenReturn(3);

        Map<String, Object> result = messagingService.getUnreadCount("USER-001");

        assertNotNull(result);
        assertEquals(3, result.get("total"));
        verify(conversationRepository).findByParticipantIdsContaining("USER-001");
    }
}
