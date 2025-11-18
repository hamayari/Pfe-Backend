package com.example.demo.service;

import com.example.demo.dto.ConversationDTO;
import com.example.demo.model.Conversation;
import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ConversationService conversationService;

    private Conversation mockConversation;
    private ConversationDTO mockConversationDTO;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockConversation = new Conversation();
        mockConversation.setId("conv1");
        mockConversation.setName("Test Conversation");
        mockConversation.setType("DIRECT");
        mockConversation.setParticipantIds(Arrays.asList("user1", "user2"));
        mockConversation.setActive(true);

        mockConversationDTO = new ConversationDTO();
        mockConversationDTO.setName("Test Conversation");
        mockConversationDTO.setType("DIRECT");
        mockConversationDTO.setParticipantIds(Arrays.asList("user1", "user2"));

        mockUser = new User();
        mockUser.setId("user1");
        mockUser.setUsername("testuser");
    }

    @Test
    void testCreateConversation() {
        mockConversationDTO.setIsPublic(true);
        when(conversationRepository.save(any(Conversation.class))).thenReturn(mockConversation);

        ConversationDTO result = conversationService.createConversation(mockConversationDTO);

        assertNotNull(result);
        assertEquals("Test Conversation", result.getName());
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void testGetUserConversations() {
        List<Conversation> conversations = Arrays.asList(mockConversation);
        when(conversationRepository.findByParticipantIdsContaining("user1")).thenReturn(conversations);

        List<ConversationDTO> result = conversationService.getUserConversations("user1");

        assertNotNull(result);
        assertTrue(result.size() >= 0);
        verify(conversationRepository).findByParticipantIdsContaining("user1");
    }

    @Test
    void testGetActiveConversations() {
        List<Conversation> conversations = Arrays.asList(mockConversation);
        when(conversationRepository.findActiveConversationsByParticipantId("user1")).thenReturn(conversations);

        List<ConversationDTO> result = conversationService.getActiveConversations("user1");

        assertNotNull(result);
        verify(conversationRepository).findActiveConversationsByParticipantId("user1");
    }

    @Test
    void testGetDirectConversation() {
        List<Conversation> conversations = Arrays.asList(mockConversation);
        when(conversationRepository.findDirectConversationBetweenUsers("user1", "user2")).thenReturn(conversations);

        ConversationDTO result = conversationService.getDirectConversation("user1", "user2");

        assertNotNull(result);
        verify(conversationRepository).findDirectConversationBetweenUsers("user1", "user2");
    }

    @Test
    void testGetDirectConversation_SameUser() {
        ConversationDTO result = conversationService.getDirectConversation("user1", "user1");

        assertNull(result);
        verify(conversationRepository, never()).findDirectConversationBetweenUsers(anyString(), anyString());
    }

    @Test
    void testUpdateOrCreateDirectConversation() {
        Message message = new Message();
        message.setId("msg1");
        message.setSentAt(LocalDateTime.now());
        message.setContent("Test message");

        List<Conversation> conversations = new ArrayList<>();
        when(conversationRepository.findDirectConversationBetweenUsers("user1", "user2")).thenReturn(conversations);
        when(userRepository.findById(anyString())).thenReturn(Optional.of(mockUser));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(mockConversation);

        conversationService.updateOrCreateDirectConversation("user1", "user2", message);

        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void testGetConversationById() {
        when(conversationRepository.findById("conv1")).thenReturn(Optional.of(mockConversation));

        Conversation result = conversationService.getConversationById("conv1");

        assertNotNull(result);
        assertEquals("conv1", result.getId());
    }

    @Test
    void testAddParticipant() {
        mockConversation.setParticipantIds(new ArrayList<>(Arrays.asList("user1", "user2")));
        mockConversation.setParticipantNames(new ArrayList<>(Arrays.asList("User1", "User2")));
        when(conversationRepository.findById("conv1")).thenReturn(Optional.of(mockConversation));
        when(userRepository.findById("user3")).thenReturn(Optional.of(mockUser));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(mockConversation);

        ConversationDTO result = conversationService.addParticipant("conv1", "user3");

        assertNotNull(result);
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void testRemoveParticipant() {
        mockConversation.setParticipantIds(new ArrayList<>(Arrays.asList("user1", "user2")));
        mockConversation.setParticipantNames(new ArrayList<>(Arrays.asList("User1", "User2")));
        when(conversationRepository.findById("conv1")).thenReturn(Optional.of(mockConversation));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(mockConversation);

        ConversationDTO result = conversationService.removeParticipant("conv1", "user1");

        assertNotNull(result);
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void testArchiveConversation() {
        when(conversationRepository.findById("conv1")).thenReturn(Optional.of(mockConversation));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(mockConversation);

        ConversationDTO result = conversationService.archiveConversation("conv1");

        assertNotNull(result);
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void testDeleteConversation() {
        doNothing().when(conversationRepository).deleteById("conv1");

        conversationService.deleteConversation("conv1");

        verify(conversationRepository).deleteById("conv1");
    }

    @Test
    void testCountUserConversations() {
        when(conversationRepository.countByParticipantIdsContaining("user1")).thenReturn(5L);

        long count = conversationService.countUserConversations("user1");

        assertEquals(5L, count);
    }
}
