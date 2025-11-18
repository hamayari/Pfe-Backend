package com.example.demo.service;

import com.example.demo.dto.MessageDTO;
import com.example.demo.model.Message;
import com.example.demo.model.MessageReaction;
import com.example.demo.model.User;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConversationService conversationService;

    @Mock
    private WebSocketPinService webSocketPinService;

    @Mock
    private WebSocketReactionService webSocketReactionService;

    @InjectMocks
    private MessageService messageService;

    private Message testMessage;
    private MessageDTO testMessageDTO;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("USER-001");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testMessage = new Message();
        testMessage.setId("MSG-001");
        testMessage.setSenderId("USER-001");
        testMessage.setSenderName("testuser");
        testMessage.setRecipientIds(Arrays.asList("USER-002"));
        testMessage.setContent("Test message");
        testMessage.setType("text");
        testMessage.setSentAt(LocalDateTime.now());
        testMessage.setRead(false);
        testMessage.setConversationId("CONV-001");

        testMessageDTO = new MessageDTO();
        testMessageDTO.setSenderId("USER-001");
        testMessageDTO.setSenderName("testuser");
        testMessageDTO.setRecipientIds(Arrays.asList("USER-002"));
        testMessageDTO.setContent("Test message");
        testMessageDTO.setMessageType("DIRECT");
    }

    @Test
    void testSendMessage_Success() {
        when(userRepository.findById("USER-001")).thenReturn(Optional.of(testUser));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        MessageDTO result = messageService.sendMessage(testMessageDTO);

        assertNotNull(result);
        assertEquals("Test message", result.getContent());
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testGetMessagesByConversation() {
        when(messageRepository.findByConversationIdOrderBySentAtDesc("CONV-001"))
            .thenReturn(Arrays.asList(testMessage));

        List<MessageDTO> result = messageService.getMessagesByConversation("CONV-001", null, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(messageRepository).findByConversationIdOrderBySentAtDesc("CONV-001");
    }

    @Test
    void testAddReaction_Success() {
        when(messageRepository.findById("MSG-001")).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        Message result = messageService.addReaction("MSG-001", "👍", "USER-001", "testuser");

        assertNotNull(result);
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testRemoveReaction_Success() {
        MessageReaction reaction = new MessageReaction("👍", "USER-001", "testuser");
        testMessage.setReactions(new ArrayList<>(Arrays.asList(reaction)));

        when(messageRepository.findById("MSG-001")).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        Message result = messageService.removeReaction("MSG-001", "👍", "USER-001");

        assertNotNull(result);
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testReplyToMessage_Success() {
        when(messageRepository.findById("MSG-001")).thenReturn(Optional.of(testMessage));
        when(userRepository.findById("USER-001")).thenReturn(Optional.of(testUser));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        Message result = messageService.replyToMessage("MSG-001", "Reply content", "CONV-001", "USER-001");

        assertNotNull(result);
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testGetMessageThread() {
        when(messageRepository.findByParentMessageIdOrderBySentAtAsc("MSG-001"))
            .thenReturn(Arrays.asList(testMessage));

        List<Message> result = messageService.getMessageThread("MSG-001");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(messageRepository).findByParentMessageIdOrderBySentAtAsc("MSG-001");
    }

    @Test
    void testEditMessage_Success() {
        when(messageRepository.findById("MSG-001")).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        Message result = messageService.editMessage("MSG-001", "Updated content", "USER-001");

        assertNotNull(result);
        assertTrue(result.isEdited());
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testEditMessage_Unauthorized() {
        when(messageRepository.findById("MSG-001")).thenReturn(Optional.of(testMessage));

        assertThrows(RuntimeException.class, () -> {
            messageService.editMessage("MSG-001", "Updated content", "USER-999");
        });
    }

    @Test
    void testGetSystemStatistics() {
        when(messageRepository.count()).thenReturn(100L);
        when(conversationService.getTotalConversations()).thenReturn(20L);
        when(userRepository.findByStatus("online")).thenReturn(Arrays.asList(testUser));
        when(messageRepository.findAll()).thenReturn(Arrays.asList(testMessage));

        Map<String, Object> result = messageService.getSystemStatistics();

        assertNotNull(result);
        assertTrue(result.containsKey("totalMessages"));
        assertTrue(result.containsKey("totalConversations"));
        assertTrue(result.containsKey("activeUsers"));
    }

    @Test
    void testModerateMessage_Hide() {
        when(messageRepository.findById("MSG-001")).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        messageService.moderateMessage("MSG-001", "hide");

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testModerateMessage_Delete() {
        when(messageRepository.findById("MSG-001")).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        messageService.moderateMessage("MSG-001", "delete");

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testCleanupOldData() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        testMessage.setDeleted(true);
        testMessage.setDeletedAt(cutoff.minusDays(1));

        when(messageRepository.findByDeletedTrueAndDeletedAtBefore(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(testMessage));

        Map<String, Object> result = messageService.cleanupOldData(30);

        assertNotNull(result);
        assertTrue(result.containsKey("deletedMessages"));
        verify(messageRepository).delete(testMessage);
    }

    @Test
    void testGetUserMessages() {
        when(messageRepository.findByRecipientIdsContainingOrderBySentAtDesc("USER-001"))
            .thenReturn(Arrays.asList(testMessage));

        List<MessageDTO> result = messageService.getUserMessages("USER-001");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetSentMessages() {
        when(messageRepository.findBySenderIdOrderBySentAtDesc("USER-001"))
            .thenReturn(Arrays.asList(testMessage));

        List<MessageDTO> result = messageService.getSentMessages("USER-001");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetUnreadMessages() {
        when(messageRepository.findUnreadMessagesByRecipientId("USER-001"))
            .thenReturn(Arrays.asList(testMessage));

        List<MessageDTO> result = messageService.getUnreadMessages("USER-001");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testMarkAsRead() {
        when(messageRepository.findById("MSG-001")).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        MessageDTO result = messageService.markAsRead("MSG-001", "USER-002");

        assertNotNull(result);
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testTogglePin_Success() {
        when(messageRepository.findById("MSG-001")).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        Message result = messageService.togglePin("MSG-001", "USER-001");

        assertNotNull(result);
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testCountUnreadMessages() {
        when(messageRepository.countUnreadMessagesByRecipientId("USER-001")).thenReturn(5L);

        long count = messageService.countUnreadMessages("USER-001");

        assertEquals(5L, count);
    }

    @Test
    void testGetMessagesByType() {
        when(messageRepository.findByMessageTypeOrderBySentAtDesc("DIRECT"))
            .thenReturn(Arrays.asList(testMessage));

        List<MessageDTO> result = messageService.getMessagesByType("DIRECT");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetUrgentMessages() {
        when(messageRepository.findUrgentMessages()).thenReturn(Arrays.asList(testMessage));

        List<MessageDTO> result = messageService.getUrgentMessages();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testDeleteMessage() {
        when(messageRepository.findById("MSG-001")).thenReturn(Optional.of(testMessage));

        messageService.deleteMessage("MSG-001", "USER-001");

        verify(messageRepository).deleteById("MSG-001");
    }
}
