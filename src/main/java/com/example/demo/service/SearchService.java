package com.example.demo.service;

import com.example.demo.model.Message;
import com.example.demo.model.Conversation;
import com.example.demo.model.MessageAttachment;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.MessageAttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageAttachmentRepository attachmentRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public SearchResult searchMessages(String searchQuery, String userId, SearchFilters filters) {
        SearchResult result = new SearchResult();

        // Recherche dans les messages
        List<Message> messages = searchInMessages(searchQuery, userId, filters);
        result.setMessages(messages);

        // Recherche dans les conversations
        List<Conversation> conversations = searchInConversations(searchQuery, userId, filters);
        result.setConversations(conversations);

        // Recherche dans les pièces jointes
        List<MessageAttachment> attachments = searchInAttachments(searchQuery, userId, filters);
        result.setAttachments(attachments);

        result.setTotalCount(messages.size() + conversations.size() + attachments.size());
        result.setQuery(searchQuery);
        result.setSearchedAt(LocalDateTime.now());

        return result;
    }

    private List<Message> searchInMessages(String query, String userId, SearchFilters filters) {
        Query mongoQuery = new Query();
        List<Criteria> criteria = new ArrayList<>();

        // Recherche textuelle dans le contenu
        if (query != null && !query.trim().isEmpty()) {
            criteria.add(Criteria.where("content").regex(query, "i"));
        }

        // Filtrer par utilisateur (messages où l'utilisateur est participant)
        criteria.add(new Criteria().orOperator(
            Criteria.where("senderId").is(userId),
            Criteria.where("recipientIds").in(userId)
        ));

        // Filtres additionnels
        if (filters != null) {
            if (filters.getConversationId() != null) {
                criteria.add(Criteria.where("conversationId").is(filters.getConversationId()));
            }

            if (filters.getMessageType() != null) {
                criteria.add(Criteria.where("messageType").is(filters.getMessageType()));
            }

            if (filters.getPriority() != null) {
                criteria.add(Criteria.where("priority").is(filters.getPriority()));
            }

            if (filters.getSenderId() != null) {
                criteria.add(Criteria.where("senderId").is(filters.getSenderId()));
            }

            if (filters.getDateFrom() != null) {
                criteria.add(Criteria.where("sentAt").gte(filters.getDateFrom()));
            }

            if (filters.getDateTo() != null) {
                criteria.add(Criteria.where("sentAt").lte(filters.getDateTo()));
            }

            if (filters.getHasAttachments() != null && filters.getHasAttachments()) {
                criteria.add(Criteria.where("attachments").exists(true).ne(Collections.emptyList()));
            }

            if (filters.getUnreadOnly() != null && filters.getUnreadOnly()) {
                criteria.add(Criteria.where("read").is(false));
            }
        }

        // Combiner tous les critères
        if (!criteria.isEmpty()) {
            mongoQuery.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }

        // Tri par date décroissante
        mongoQuery.with(org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Direction.DESC, "sentAt"));

        // Limite
        if (filters != null && filters.getLimit() != null) {
            mongoQuery.limit(filters.getLimit());
        } else {
            mongoQuery.limit(100); // Limite par défaut
        }

        return mongoTemplate.find(mongoQuery, Message.class);
    }

    private List<Conversation> searchInConversations(String query, String userId, SearchFilters filters) {
        Query mongoQuery = new Query();
        List<Criteria> criteria = new ArrayList<>();

        // Recherche dans le nom et description
        if (query != null && !query.trim().isEmpty()) {
            criteria.add(new Criteria().orOperator(
                Criteria.where("name").regex(query, "i"),
                Criteria.where("description").regex(query, "i")
            ));
        }

        // Filtrer par utilisateur participant
        criteria.add(Criteria.where("participantIds").in(userId));

        // Filtres additionnels
        if (filters != null) {
            if (filters.getConversationType() != null) {
                criteria.add(Criteria.where("type").is(filters.getConversationType()));
            }

            if (filters.getActiveOnly() != null && filters.getActiveOnly()) {
                criteria.add(Criteria.where("isActive").is(true));
            }
        }

        // Combiner tous les critères
        if (!criteria.isEmpty()) {
            mongoQuery.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }

        // Tri par dernière activité
        mongoQuery.with(org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Direction.DESC, "lastMessageAt"));

        mongoQuery.limit(50); // Limite pour les conversations

        return mongoTemplate.find(mongoQuery, Conversation.class);
    }

    private List<MessageAttachment> searchInAttachments(String query, String userId, SearchFilters filters) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Recherche par nom de fichier
        List<MessageAttachment> attachments = attachmentRepository
            .findByOriginalFileNameContainingIgnoreCaseAndDeletedFalse(query);

        // Filtrer par utilisateur (pièces jointes des conversations où il participe)
        List<String> userConversationIds = conversationRepository
            .findByParticipantIdsContaining(userId)
            .stream()
            .map(Conversation::getId)
            .collect(Collectors.toList());

        return attachments.stream()
            .filter(att -> userConversationIds.contains(att.getConversationId()))
            .limit(20)
            .collect(Collectors.toList());
    }

    public List<Message> searchMessagesByMention(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("mentions").in(userId));
        query.with(org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Direction.DESC, "sentAt"));
        query.limit(50);

        return mongoTemplate.find(query, Message.class);
    }

    public List<Message> searchMessagesByHashtag(String hashtag, String userId) {
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
            Criteria.where("content").regex("#" + hashtag, "i"),
            new Criteria().orOperator(
                Criteria.where("senderId").is(userId),
                Criteria.where("recipientIds").in(userId)
            )
        ));
        query.with(org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Direction.DESC, "sentAt"));
        query.limit(50);

        return mongoTemplate.find(query, Message.class);
    }

    public Map<String, Long> getSearchStatistics(String userId) {
        Map<String, Long> stats = new HashMap<>();

        // Nombre total de messages de l'utilisateur
        stats.put("totalMessages", (long) messageRepository.findBySenderIdOrderBySentAtDesc(userId).size());

        // Nombre de conversations
        stats.put("totalConversations", (long) conversationRepository.findByParticipantIdsContaining(userId).size());

        // Nombre de pièces jointes
        stats.put("totalAttachments", attachmentRepository.countByUploadedByAndDeletedFalse(userId));

        // Messages non lus
        stats.put("unreadMessages", (long) messageRepository.findUnreadMessagesByRecipientId(userId).size());

        return stats;
    }

    // Classes internes pour les résultats et filtres
    public static class SearchResult {
        private List<Message> messages = new ArrayList<>();
        private List<Conversation> conversations = new ArrayList<>();
        private List<MessageAttachment> attachments = new ArrayList<>();
        private int totalCount;
        private String query;
        private LocalDateTime searchedAt;

        // Getters et Setters
        public List<Message> getMessages() { return messages; }
        public void setMessages(List<Message> messages) { this.messages = messages; }

        public List<Conversation> getConversations() { return conversations; }
        public void setConversations(List<Conversation> conversations) { this.conversations = conversations; }

        public List<MessageAttachment> getAttachments() { return attachments; }
        public void setAttachments(List<MessageAttachment> attachments) { this.attachments = attachments; }

        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }

        public LocalDateTime getSearchedAt() { return searchedAt; }
        public void setSearchedAt(LocalDateTime searchedAt) { this.searchedAt = searchedAt; }
    }

    public static class SearchFilters {
        private String conversationId;
        private String messageType;
        private String priority;
        private String senderId;
        private LocalDateTime dateFrom;
        private LocalDateTime dateTo;
        private Boolean hasAttachments;
        private Boolean unreadOnly;
        private String conversationType;
        private Boolean activeOnly;
        private Integer limit;

        // Getters et Setters
        public String getConversationId() { return conversationId; }
        public void setConversationId(String conversationId) { this.conversationId = conversationId; }

        public String getMessageType() { return messageType; }
        public void setMessageType(String messageType) { this.messageType = messageType; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getSenderId() { return senderId; }
        public void setSenderId(String senderId) { this.senderId = senderId; }

        public LocalDateTime getDateFrom() { return dateFrom; }
        public void setDateFrom(LocalDateTime dateFrom) { this.dateFrom = dateFrom; }

        public LocalDateTime getDateTo() { return dateTo; }
        public void setDateTo(LocalDateTime dateTo) { this.dateTo = dateTo; }

        public Boolean getHasAttachments() { return hasAttachments; }
        public void setHasAttachments(Boolean hasAttachments) { this.hasAttachments = hasAttachments; }

        public Boolean getUnreadOnly() { return unreadOnly; }
        public void setUnreadOnly(Boolean unreadOnly) { this.unreadOnly = unreadOnly; }

        public String getConversationType() { return conversationType; }
        public void setConversationType(String conversationType) { this.conversationType = conversationType; }

        public Boolean getActiveOnly() { return activeOnly; }
        public void setActiveOnly(Boolean activeOnly) { this.activeOnly = activeOnly; }

        public Integer getLimit() { return limit; }
        public void setLimit(Integer limit) { this.limit = limit; }
    }
}





