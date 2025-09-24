package com.example.demo.controller;

import com.example.demo.model.Message;
import com.example.demo.model.MessageAttachment;

import com.example.demo.service.AttachmentService;
import com.example.demo.service.SearchService;
import com.example.demo.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messaging")
@Tag(name = "Messagerie Avancée", description = "API pour fonctionnalités avancées de messagerie")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class MessagingAdvancedController {

    @Autowired
    private SearchService searchService;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private MessageService messageService;

    // ================ RECHERCHE ================

    @GetMapping("/search")
    @Operation(summary = "Recherche avancée dans les messages et conversations")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<SearchService.SearchResult> search(
            @RequestParam String query,
            @RequestParam(required = false) String conversationId,
            @RequestParam(required = false) String messageType,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String senderId,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Boolean hasAttachments,
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestParam(required = false) String conversationType,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false, defaultValue = "100") Integer limit,
            Authentication authentication) {

        try {
            SearchService.SearchFilters filters = new SearchService.SearchFilters();
            filters.setConversationId(conversationId);
            filters.setMessageType(messageType);
            filters.setPriority(priority);
            filters.setSenderId(senderId);
            filters.setHasAttachments(hasAttachments);
            filters.setUnreadOnly(unreadOnly);
            filters.setConversationType(conversationType);
            filters.setActiveOnly(activeOnly);
            filters.setLimit(limit);

            if (dateFrom != null) {
                filters.setDateFrom(LocalDateTime.parse(dateFrom, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            if (dateTo != null) {
                filters.setDateTo(LocalDateTime.parse(dateTo, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

            SearchService.SearchResult result = searchService.searchMessages(query, authentication.getName(), filters);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search/mentions")
    @Operation(summary = "Rechercher les messages où l'utilisateur est mentionné")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<List<Message>> searchMentions(Authentication authentication) {
        try {
            List<Message> mentions = searchService.searchMessagesByMention(authentication.getName());
            return ResponseEntity.ok(mentions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search/hashtag/{hashtag}")
    @Operation(summary = "Rechercher les messages avec un hashtag")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<List<Message>> searchHashtag(
            @PathVariable String hashtag,
            Authentication authentication) {
        try {
            List<Message> messages = searchService.searchMessagesByHashtag(hashtag, authentication.getName());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search/statistics")
    @Operation(summary = "Statistiques de recherche de l'utilisateur")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<Map<String, Long>> getSearchStatistics(Authentication authentication) {
        try {
            Map<String, Long> stats = searchService.getSearchStatistics(authentication.getName());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ================ PIÈCES JOINTES ================

    @PostMapping("/attachments/upload")
    @Operation(summary = "Uploader une pièce jointe")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<MessageAttachment> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("conversationId") String conversationId,
            @RequestParam("messageId") String messageId,
            Authentication authentication) {
        try {
            MessageAttachment attachment = attachmentService.uploadFile(
                file, conversationId, messageId, authentication.getName(), authentication.getName()
            );
            return ResponseEntity.ok(attachment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/attachments/{attachmentId}/download")
    @Operation(summary = "Télécharger une pièce jointe")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable String attachmentId,
            HttpServletRequest request) {
        try {
            Resource resource = attachmentService.loadFileAsResource(attachmentId);
            
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                // Fallback
            }

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/attachments/message/{messageId}")
    @Operation(summary = "Obtenir les pièces jointes d'un message")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<List<MessageAttachment>> getMessageAttachments(@PathVariable String messageId) {
        try {
            List<MessageAttachment> attachments = attachmentService.getMessageAttachments(messageId);
            return ResponseEntity.ok(attachments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/attachments/conversation/{conversationId}")
    @Operation(summary = "Obtenir toutes les pièces jointes d'une conversation")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<List<MessageAttachment>> getConversationAttachments(@PathVariable String conversationId) {
        try {
            List<MessageAttachment> attachments = attachmentService.getConversationAttachments(conversationId);
            return ResponseEntity.ok(attachments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/attachments/{attachmentId}")
    @Operation(summary = "Supprimer une pièce jointe")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable String attachmentId,
            Authentication authentication) {
        try {
            attachmentService.deleteAttachment(attachmentId, authentication.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ================ RÉACTIONS ================

    @PostMapping("/messages/{messageId}/reactions")
    @Operation(summary = "Ajouter une réaction à un message")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<Message> addReaction(
            @PathVariable String messageId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String emoji = request.get("emoji");
            Message message = messageService.addReaction(messageId, emoji, authentication.getName(), authentication.getName());
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/messages/{messageId}/reactions/{emoji}")
    @Operation(summary = "Supprimer une réaction d'un message")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<Message> removeReaction(
            @PathVariable String messageId,
            @PathVariable String emoji,
            Authentication authentication) {
        try {
            Message message = messageService.removeReaction(messageId, emoji, authentication.getName());
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ================ THREADS ================

    @PostMapping("/messages/{parentMessageId}/reply")
    @Operation(summary = "Répondre à un message (thread)")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<Message> replyToMessage(
            @PathVariable String parentMessageId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String content = request.get("content");
            String conversationId = request.get("conversationId");
            Message reply = messageService.replyToMessage(parentMessageId, content, conversationId, authentication.getName());
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/messages/{messageId}/thread")
    @Operation(summary = "Obtenir le thread d'un message")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<List<Message>> getMessageThread(@PathVariable String messageId) {
        try {
            List<Message> thread = messageService.getMessageThread(messageId);
            return ResponseEntity.ok(thread);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ================ ÉDITION ================

    @PutMapping("/messages/{messageId}")
    @Operation(summary = "Modifier un message")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<Message> editMessage(
            @PathVariable String messageId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String newContent = request.get("content");
            Message message = messageService.editMessage(messageId, newContent, authentication.getName());
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ================ ADMINISTRATION ================

    @GetMapping("/admin/stats")
    @Operation(summary = "Statistiques système de messagerie")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        try {
            Map<String, Object> stats = messageService.getSystemStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/admin/moderate/{messageId}")
    @Operation(summary = "Modérer un message")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> moderateMessage(
            @PathVariable String messageId,
            @RequestBody Map<String, String> request) {
        try {
            String action = request.get("action");
            messageService.moderateMessage(messageId, action);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/admin/cleanup")
    @Operation(summary = "Nettoyer les anciennes données")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cleanupOldData(
            @RequestParam(defaultValue = "90") int daysOld) {
        try {
            Map<String, Object> result = messageService.cleanupOldData(daysOld);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}