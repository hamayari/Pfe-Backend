package com.example.demo.controller;

import com.example.demo.dto.MessageDTO;
import com.example.demo.model.Message;
import com.example.demo.service.MessageService;
import com.example.demo.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Messages", description = "API de gestion des messages internes")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/send")
    @Operation(summary = "Envoyer un message")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<MessageDTO> sendMessage(@RequestBody MessageDTO messageDTO) {
        try {
            MessageDTO sentMessage = messageService.sendMessage(messageDTO);
            return ResponseEntity.ok(sentMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Supprimer un message
    @DeleteMapping("/{messageId}")
    @Operation(summary = "Supprimer un message")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteMessage(@PathVariable String messageId, Authentication authentication) {
        try {
            String userId = authentication != null ? authentication.getName() : null;
            messageService.deleteMessage(messageId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Épingler / désépingler (soft, via status or pinned flag)
    @PostMapping("/{messageId}/pin")
    @Operation(summary = "Épingler/Désépingler un message")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> togglePin(@PathVariable String messageId, Authentication authentication) {
        try {
            // Récupérer l'ID technique (ObjectId) de l'utilisateur connecté
            String userId = null;
            if (authentication != null) {
                // 1) Essayer d'abord via UserPrincipal (contient l'ID MongoDB)
                try {
                    Object p = authentication.getPrincipal();
                    if (p instanceof com.example.demo.security.UserPrincipal up) {
                        userId = up.getId();
                    }
                } catch (Exception ignored) {}

                String principal = authentication.getName(); // souvent username ou email
                // Essayer username
                var optUser = userRepository.findByUsername(principal);
                if (optUser.isEmpty()) {
                    // Essayer email si disponible
                    try {
                        var byEmail = userRepository.findByEmail(principal);
                        if (byEmail.isPresent()) optUser = byEmail;
                    } catch (Exception ignored) {}
                }
                if (userId == null && optUser.isPresent()) {
                    userId = optUser.get().getId();
                } else if (userId == null) {
                    // fallback: utiliser ce qui est fourni (peut déjà être un id)
                    userId = principal;
                }
            }
            Message updatedMessage = messageService.togglePin(messageId, userId);
            return ResponseEntity.ok(updatedMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    // Réactions emoji
    @PostMapping("/{messageId}/reactions")
    @Operation(summary = "Ajouter une réaction à un message")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addReaction(@PathVariable String messageId, @RequestBody java.util.Map<String,String> body, Authentication auth) {
        try {
            String emoji = body.getOrDefault("emoji", ":+1:");
            String userName = auth != null ? auth.getName() : "";
            String userId = userName;
            Message updatedMessage = messageService.addReaction(messageId, emoji, userId, userName);
            return ResponseEntity.ok(updatedMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{messageId}/reactions/{emoji}")
    @Operation(summary = "Supprimer une réaction d'un message")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> removeReaction(@PathVariable String messageId, @PathVariable String emoji, Authentication auth) {
        try {
            String userName = auth != null ? auth.getName() : "";
            String userId = userName;
            messageService.removeReaction(messageId, emoji, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    // Endpoint attendu par le frontend: POST /api/messages
    @PostMapping
    @Operation(summary = "Envoyer un message (frontend)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageDTO> sendMessageFrontend(@RequestBody MessageDTO dto, Authentication authentication) {
        try {
            // Sécurité minimale: si senderId manquant, utiliser l'utilisateur courant
            if (authentication != null && (dto.getSenderId() == null || dto.getSenderId().isEmpty())) {
                // Le principal est un username, on le résout en userId dans le service
                dto.setSenderName(authentication.getName());
            }
            // Par défaut considérer comme DIRECT si non spécifié
            if (dto.getMessageType() == null || dto.getMessageType().isEmpty()) {
                dto.setMessageType("DIRECT");
            }
            MessageDTO sent = messageService.sendMessage(dto);
            return ResponseEntity.ok(sent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Endpoint attendu par le frontend: GET /api/conversations/{conversationId}/messages
    @GetMapping("/conversation/{conversationId}/messages")
    @Operation(summary = "Lister messages par conversation (frontend)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MessageDTO>> listMessagesByConversation(@PathVariable String conversationId,
                                                                       @RequestParam(value = "before", required = false) String before,
                                                                       @RequestParam(value = "limit", required = false, defaultValue = "50") int limit) {
        try {
            List<MessageDTO> messages = messageService.getMessagesByConversation(conversationId, before, limit);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.ok().body(List.of());
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Récupérer les messages d'un utilisateur")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<List<MessageDTO>> getUserMessages(@PathVariable String userId) {
        try {
            List<MessageDTO> messages = messageService.getUserMessages(userId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/sent/{userId}")
    @Operation(summary = "Récupérer les messages envoyés par un utilisateur")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<List<MessageDTO>> getSentMessages(@PathVariable String userId) {
        try {
            List<MessageDTO> messages = messageService.getSentMessages(userId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/unread/{userId}")
    @Operation(summary = "Récupérer les messages non lus d'un utilisateur")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<List<MessageDTO>> getUnreadMessages(@PathVariable String userId) {
        try {
            List<MessageDTO> messages = messageService.getUnreadMessages(userId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/conversation/{userId1}/{userId2}")
    @Operation(summary = "Récupérer les messages d'une conversation directe")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<List<MessageDTO>> getDirectConversationMessages(
            @PathVariable String userId1,
            @PathVariable String userId2) {
        try {
            List<MessageDTO> messages = messageService.getDirectConversationMessages(userId1, userId2);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/group/{userId}")
    @Operation(summary = "Récupérer les messages de groupe d'un utilisateur")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<List<MessageDTO>> getGroupMessages(@PathVariable String userId) {
        try {
            List<MessageDTO> messages = messageService.getGroupMessages(userId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/system/{userId}")
    @Operation(summary = "Récupérer les messages système d'un utilisateur")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<List<MessageDTO>> getSystemMessages(@PathVariable String userId) {
        try {
            List<MessageDTO> messages = messageService.getSystemMessages(userId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{messageId}/read/{userId}")
    @Operation(summary = "Marquer un message comme lu")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<MessageDTO> markAsRead(
            @PathVariable String messageId,
            @PathVariable String userId) {
        try {
            MessageDTO message = messageService.markAsRead(messageId, userId);
            if (message != null) {
                return ResponseEntity.ok(message);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/conversation/read/{userId1}/{userId2}")
    @Operation(summary = "Marquer tous les messages d'une conversation comme lus")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<Void> markConversationAsRead(
            @PathVariable String userId1,
            @PathVariable String userId2) {
        try {
            messageService.markConversationAsRead(userId1, userId2);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{messageId}/{userId}")
    @Operation(summary = "Supprimer un message")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable String messageId,
            @PathVariable String userId) {
        try {
            messageService.deleteMessage(messageId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/type/{messageType}")
    @Operation(summary = "Récupérer les messages par type")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<MessageDTO>> getMessagesByType(@PathVariable String messageType) {
        try {
            List<MessageDTO> messages = messageService.getMessagesByType(messageType);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/priority/{priority}")
    @Operation(summary = "Récupérer les messages par priorité")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<MessageDTO>> getMessagesByPriority(@PathVariable String priority) {
        try {
            List<MessageDTO> messages = messageService.getMessagesByPriority(priority);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/urgent")
    @Operation(summary = "Récupérer les messages urgents")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<MessageDTO>> getUrgentMessages() {
        try {
            List<MessageDTO> messages = messageService.getUrgentMessages();
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/attachments")
    @Operation(summary = "Récupérer les messages avec pièces jointes")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<MessageDTO>> getMessagesWithAttachments() {
        try {
            List<MessageDTO> messages = messageService.getMessagesWithAttachments();
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/entity/{entityId}/{entityType}")
    @Operation(summary = "Récupérer les messages liés à une entité")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<List<MessageDTO>> getMessagesByEntity(
            @PathVariable String entityId,
            @PathVariable String entityType) {
        try {
            List<MessageDTO> messages = messageService.getMessagesByEntity(entityId, entityType);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/unread/count/{userId}")
    @Operation(summary = "Compter les messages non lus d'un utilisateur")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<Long> countUnreadMessages(@PathVariable String userId) {
        try {
            long count = messageService.countUnreadMessages(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/cleanup")
    @Operation(summary = "Nettoyer les anciens messages")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> cleanupOldMessages(@RequestParam int daysToKeep) {
        try {
            messageService.cleanupOldMessages(daysToKeep);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 