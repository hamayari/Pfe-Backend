package com.example.demo.controller;

import com.example.demo.dto.ConversationDTO;
import com.example.demo.service.MessageService;
import com.example.demo.dto.MessageDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.model.User;
import com.example.demo.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@Tag(name = "Conversations", description = "API de gestion des conversations")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageService messageService;

    @PostMapping("/create")
    @Operation(summary = "Créer une nouvelle conversation")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<ConversationDTO> createConversation(@RequestBody ConversationDTO conversationDTO) {
        try {
            // Sécuriser le créateur s'il n'est pas présent
            if (conversationDTO.getCreatedBy() == null && conversationDTO.getParticipantIds() != null && !conversationDTO.getParticipantIds().isEmpty()) {
                conversationDTO.setCreatedBy(conversationDTO.getParticipantIds().get(0));
            }
            ConversationDTO createdConversation = conversationService.createConversation(conversationDTO);
            return ResponseEntity.ok(createdConversation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Mettre à jour titre/description/confidentialité (owner)
    @PutMapping("/{conversationId}")
    @Operation(summary = "Mettre à jour une conversation (titre, description, confidentialité)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationDTO> updateConversation(
            @PathVariable String conversationId,
            @RequestBody ConversationDTO update,
            Authentication authentication) {
        try {
            ConversationDTO dto = conversationService.updateConversation(conversationId, update, authentication != null ? authentication.getName() : null);
            if (dto == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Récupérer les conversations d'un utilisateur")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<List<ConversationDTO>> getUserConversations(@PathVariable String userId) {
        try {
            List<ConversationDTO> conversations = conversationService.getUserConversations(userId);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Endpoint attendu par le frontend: GET /api/conversations
    // Retourne les conversations de l'utilisateur courant
    @GetMapping
    @Operation(summary = "Récupérer les conversations de l'utilisateur courant")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ConversationDTO>> getCurrentUserConversations(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.ok().body(List.of());
            }
            String username = authentication.getName();
            User current = userRepository.findByUsername(username).orElse(null);
            if (current == null) {
                return ResponseEntity.ok().body(List.of());
            }
            String userId = current.getId();
            List<ConversationDTO> conversations = conversationService.getUserConversations(userId);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.ok().body(List.of());
        }
    }

    // Proxy attendu par le frontend: GET /api/conversations/{conversationId}/messages
    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "Lister les messages d'une conversation (frontend)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MessageDTO>> getMessagesForConversation(
            @PathVariable String conversationId,
            @RequestParam(value = "before", required = false) String before,
            @RequestParam(value = "limit", required = false, defaultValue = "50") int limit) {
        try {
            List<MessageDTO> messages = messageService.getMessagesByConversation(conversationId, before, limit);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.ok().body(List.of());
        }
    }

    // Endpoint attendu par le frontend: POST /api/conversations/{conversationId}/read
    @PostMapping("/{conversationId}/read")
    @Operation(summary = "Marquer une conversation comme lue (frontend)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markConversationRead(
            @PathVariable String conversationId,
            Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.ok().build();
            }
            String username = authentication.getName();
            User current = userRepository.findByUsername(username).orElse(null);
            if (current == null) {
                return ResponseEntity.ok().build();
            }
            // Fallback 1: si conversationId est de la forme dm_<id1>_<id2>, marquer via userId pair
            if (conversationId.startsWith("dm_")) {
                String[] parts = conversationId.split("_");
                if (parts.length >= 3) {
                    String id1 = parts[1];
                    String id2 = parts[2];
                    String other = current.getId().equals(id1) ? id2 : id1;
                    messageService.markConversationAsRead(current.getId(), other);
                } else {
                    // Sinon, tenter via conversationId
                    messageService.markConversationReadByConversationId(conversationId, current.getId());
                }
            } else {
                // Fallback 2: conversation basée sur ID standard
                messageService.markConversationReadByConversationId(conversationId, current.getId());
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.ok().build();
        }
    }

    // Endpoint pour compter les non-lus par conversation
    @GetMapping("/unread-counts")
    @Operation(summary = "Compter les non-lus par conversation pour l'utilisateur courant")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<java.util.Map<String, Integer>> getUnreadCounts(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.ok(java.util.Map.of());
            }
            String username = authentication.getName();
            User current = userRepository.findByUsername(username).orElse(null);
            if (current == null) {
                return ResponseEntity.ok(java.util.Map.of());
            }
            List<ConversationDTO> conversations = conversationService.getUserConversations(current.getId());
            java.util.Map<String, Integer> counts = new java.util.HashMap<>();
            for (ConversationDTO c : conversations) {
                long countLong = messageService.getMessagesByConversation(c.getId(), null, Integer.MAX_VALUE).stream()
                        .filter(m -> m.getRecipientIds() != null && m.getRecipientIds().contains(current.getId()))
                        .filter(m -> m.getStatus() == null || !m.getStatus().equals("READ"))
                        .count();
                int count = countLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) countLong;
                counts.put(c.getId(), count);
            }
            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.Map.of());
        }
    }

    @GetMapping("/active/{userId}")
    @Operation(summary = "Récupérer les conversations actives d'un utilisateur")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<List<ConversationDTO>> getActiveConversations(@PathVariable String userId) {
        try {
            List<ConversationDTO> conversations = conversationService.getActiveConversations(userId);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/direct/{userId1}/{userId2}")
    @Operation(summary = "Récupérer une conversation directe entre deux utilisateurs")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<ConversationDTO> getDirectConversation(
            @PathVariable String userId1,
            @PathVariable String userId2) {
        try {
            ConversationDTO conversation = conversationService.getDirectConversation(userId1, userId2);
            if (conversation != null) {
                return ResponseEntity.ok(conversation);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/group")
    @Operation(summary = "Récupérer toutes les conversations de groupe")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<ConversationDTO>> getGroupConversations() {
        try {
            List<ConversationDTO> conversations = conversationService.getGroupConversations();
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/group/{userId}")
    @Operation(summary = "Récupérer les conversations de groupe d'un utilisateur")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<List<ConversationDTO>> getUserGroupConversations(@PathVariable String userId) {
        try {
            List<ConversationDTO> conversations = conversationService.getUserGroupConversations(userId);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/system")
    @Operation(summary = "Récupérer les conversations système")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<ConversationDTO>> getSystemConversations() {
        try {
            List<ConversationDTO> conversations = conversationService.getSystemConversations();
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des conversations par nom")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<List<ConversationDTO>> searchConversations(@RequestParam String name) {
        try {
            List<ConversationDTO> conversations = conversationService.searchConversations(name);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{conversationId}/participant/{userId}")
    @Operation(summary = "Ajouter un participant à une conversation")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<ConversationDTO> addParticipant(
            @PathVariable String conversationId,
            @PathVariable String userId) {
        try {
            ConversationDTO conversation = conversationService.addParticipant(conversationId, userId);
            if (conversation != null) {
                return ResponseEntity.ok(conversation);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{conversationId}/participant/{userId}")
    @Operation(summary = "Retirer un participant d'une conversation")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<ConversationDTO> removeParticipant(
            @PathVariable String conversationId,
            @PathVariable String userId) {
        try {
            ConversationDTO conversation = conversationService.removeParticipant(conversationId, userId);
            if (conversation != null) {
                return ResponseEntity.ok(conversation);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{conversationId}/archive")
    @Operation(summary = "Archiver une conversation")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<ConversationDTO> archiveConversation(@PathVariable String conversationId) {
        try {
            ConversationDTO conversation = conversationService.archiveConversation(conversationId);
            if (conversation != null) {
                return ResponseEntity.ok(conversation);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{conversationId}")
    @Operation(summary = "Supprimer une conversation")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deleteConversation(@PathVariable String conversationId) {
        try {
            conversationService.deleteConversation(conversationId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/count/{userId}")
    @Operation(summary = "Compter les conversations d'un utilisateur")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<Long> countUserConversations(@PathVariable String userId) {
        try {
            long count = conversationService.countUserConversations(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/active/count/{userId}")
    @Operation(summary = "Compter les conversations actives d'un utilisateur")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN', 'PROJECT_MANAGER', 'DECISION_MAKER')")
    public ResponseEntity<Long> countActiveUserConversations(@PathVariable String userId) {
        try {
            long count = conversationService.countActiveUserConversations(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/cleanup")
    @Operation(summary = "Nettoyer les conversations inactives anciennes")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> cleanupInactiveConversations(@RequestParam int daysToKeep) {
        try {
            conversationService.cleanupInactiveConversations(daysToKeep);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 