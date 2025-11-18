package com.example.demo.service;

import com.example.demo.model.Conversation;
import com.example.demo.model.Message;
import com.example.demo.model.MessageAttachment;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.MessageAttachmentRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.service.SearchService.SearchFilters;
import com.example.demo.service.SearchService.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageAttachmentRepository attachmentRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private SearchService searchService;

    private Message testMessage;
    private Conversation testConversation;
    private MessageAttachment testAttachment;

    @BeforeEach
    void setUp() {
        testMessage = new Message();
        testMessage.setId("MSG-001");
        testMessage.setSenderId("USER-001");
        testMessage.setContent("Test message content");
        testMessage.setSentAt(LocalDateTime.now());
        testMessage.setConversationId("CONV-001");

        testConversation = new Conversation();
        testConversation.setId("CONV-001");
        testConversation.setName("Test Conversation");
        testConversation.setParticipantIds(Arrays.asList("USER-001", "USER-002"));

        testAttachment = new MessageAttachment();
        testAttachment.setId("ATT-001");
        testAttachment.setOriginalFileName("test.pdf");
        testAttachment.setConversationId("CONV-001");
    }

    @Test
    void testSearchMessages_WithQuery() {
        SearchFilters filters = new SearchFilters();
        when(mongoTemplate.find(any(Query.class), eq(Message.class)))
            .thenReturn(Arrays.asList(testMessage));
        when(mongoTemplate.find(any(Query.class), eq(Conversation.class)))
            .thenReturn(new ArrayList<>());
        when(attachmentRepository.findByOriginalFileNameContainingIgnoreCaseAndDeletedFalse(anyString()))
            .thenReturn(new ArrayList<>());
        when(conversationRepository.findByParticipantIdsContaining(anyString()))
            .thenReturn(new ArrayList<>());

        SearchResult result = searchService.searchMessages("test", "USER-001", filters);

        assertNotNull(result);
        assertEquals(1, result.getMessages().size());
        assertEquals("test", result.getQuery());
        assertNotNull(result.getSearchedAt());
    }

    @Test
    void testSearchMessages_WithFilters() {
        SearchFilters filters = new SearchFilters();
        filters.setConversationId("CONV-001");
        filters.setMessageType("DIRECT");
        filters.setPriority("HIGH");

        when(mongoTemplate.find(any(Query.class), eq(Message.class)))
            .thenReturn(Arrays.asList(testMessage));
        when(mongoTemplate.find(any(Query.class), eq(Conversation.class)))
            .thenReturn(new ArrayList<>());
        when(attachmentRepository.findByOriginalFileNameContainingIgnoreCaseAndDeletedFalse(anyString()))
            .thenReturn(new ArrayList<>());
        when(conversationRepository.findByParticipantIdsContaining(anyString()))
            .thenReturn(new ArrayList<>());

        SearchResult result = searchService.searchMessages("test", "USER-001", filters);

        assertNotNull(result);
        verify(mongoTemplate, atLeastOnce()).find(any(Query.class), eq(Message.class));
    }

    @Test
    void testSearchMessages_EmptyQuery() {
        SearchFilters filters = new SearchFilters();
        when(mongoTemplate.find(any(Query.class), eq(Message.class)))
            .thenReturn(new ArrayList<>());
        when(mongoTemplate.find(any(Query.class), eq(Conversation.class)))
            .thenReturn(new ArrayList<>());

        SearchResult result = searchService.searchMessages("", "USER-001", filters);

        assertNotNull(result);
        assertEquals(0, result.getTotalCount());
    }

    @Test
    void testSearchMessagesByMention() {
        when(mongoTemplate.find(any(Query.class), eq(Message.class)))
            .thenReturn(Arrays.asList(testMessage));

        List<Message> result = searchService.searchMessagesByMention("USER-001");

        assertNotNull(result);
        verify(mongoTemplate).find(any(Query.class), eq(Message.class));
    }

    @Test
    void testSearchMessagesByHashtag() {
        when(mongoTemplate.find(any(Query.class), eq(Message.class)))
            .thenReturn(Arrays.asList(testMessage));

        List<Message> result = searchService.searchMessagesByHashtag("important", "USER-001");

        assertNotNull(result);
        verify(mongoTemplate).find(any(Query.class), eq(Message.class));
    }

    @Test
    void testGetSearchStatistics() {
        when(messageRepository.findBySenderIdOrderBySentAtDesc("USER-001"))
            .thenReturn(Arrays.asList(testMessage));
        when(conversationRepository.findByParticipantIdsContaining("USER-001"))
            .thenReturn(Arrays.asList(testConversation));
        when(attachmentRepository.countByUploadedByAndDeletedFalse("USER-001"))
            .thenReturn(5L);
        when(messageRepository.findUnreadMessagesByRecipientId("USER-001"))
            .thenReturn(Arrays.asList(testMessage));

        Map<String, Long> result = searchService.getSearchStatistics("USER-001");

        assertNotNull(result);
        assertTrue(result.containsKey("totalMessages"));
        assertTrue(result.containsKey("totalConversations"));
        assertTrue(result.containsKey("totalAttachments"));
        assertTrue(result.containsKey("unreadMessages"));
    }

    @Test
    void testSearchMessages_WithDateFilters() {
        SearchFilters filters = new SearchFilters();
        filters.setDateFrom(LocalDateTime.now().minusDays(7));
        filters.setDateTo(LocalDateTime.now());

        when(mongoTemplate.find(any(Query.class), eq(Message.class)))
            .thenReturn(Arrays.asList(testMessage));
        when(mongoTemplate.find(any(Query.class), eq(Conversation.class)))
            .thenReturn(new ArrayList<>());
        when(attachmentRepository.findByOriginalFileNameContainingIgnoreCaseAndDeletedFalse(anyString()))
            .thenReturn(new ArrayList<>());
        when(conversationRepository.findByParticipantIdsContaining(anyString()))
            .thenReturn(new ArrayList<>());

        SearchResult result = searchService.searchMessages("test", "USER-001", filters);

        assertNotNull(result);
        verify(mongoTemplate).find(any(Query.class), eq(Message.class));
    }

    @Test
    void testSearchMessages_WithAttachmentFilter() {
        SearchFilters filters = new SearchFilters();
        filters.setHasAttachments(true);

        when(mongoTemplate.find(any(Query.class), eq(Message.class)))
            .thenReturn(Arrays.asList(testMessage));
        when(mongoTemplate.find(any(Query.class), eq(Conversation.class)))
            .thenReturn(new ArrayList<>());
        when(attachmentRepository.findByOriginalFileNameContainingIgnoreCaseAndDeletedFalse(anyString()))
            .thenReturn(new ArrayList<>());
        when(conversationRepository.findByParticipantIdsContaining(anyString()))
            .thenReturn(new ArrayList<>());

        SearchResult result = searchService.searchMessages("test", "USER-001", filters);

        assertNotNull(result);
    }

    @Test
    void testSearchMessages_UnreadOnly() {
        SearchFilters filters = new SearchFilters();
        filters.setUnreadOnly(true);

        when(mongoTemplate.find(any(Query.class), eq(Message.class)))
            .thenReturn(Arrays.asList(testMessage));
        when(mongoTemplate.find(any(Query.class), eq(Conversation.class)))
            .thenReturn(new ArrayList<>());
        when(attachmentRepository.findByOriginalFileNameContainingIgnoreCaseAndDeletedFalse(anyString()))
            .thenReturn(new ArrayList<>());
        when(conversationRepository.findByParticipantIdsContaining(anyString()))
            .thenReturn(new ArrayList<>());

        SearchResult result = searchService.searchMessages("test", "USER-001", filters);

        assertNotNull(result);
    }

    @Test
    void testSearchMessages_WithLimit() {
        SearchFilters filters = new SearchFilters();
        filters.setLimit(10);

        when(mongoTemplate.find(any(Query.class), eq(Message.class)))
            .thenReturn(Arrays.asList(testMessage));
        when(mongoTemplate.find(any(Query.class), eq(Conversation.class)))
            .thenReturn(new ArrayList<>());
        when(attachmentRepository.findByOriginalFileNameContainingIgnoreCaseAndDeletedFalse(anyString()))
            .thenReturn(new ArrayList<>());
        when(conversationRepository.findByParticipantIdsContaining(anyString()))
            .thenReturn(new ArrayList<>());

        SearchResult result = searchService.searchMessages("test", "USER-001", filters);

        assertNotNull(result);
    }
}
