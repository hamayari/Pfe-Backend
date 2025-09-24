package com.example.demo.controller;

import com.example.demo.dto.SmsRequestDTO;
import com.example.demo.dto.SmsResponseDTO;
import com.example.demo.dto.SmsStatsDTO;
import com.example.demo.model.SmsNotification;
import com.example.demo.service.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sms")
@Tag(name = "SMS", description = "API de gestion des notifications SMS")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class SmsController {

    @Autowired
    private SmsService smsService;

    @PostMapping("/send")
    @Operation(summary = "Envoyer un SMS")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN')")
    public ResponseEntity<SmsResponseDTO> sendSms(@RequestBody SmsRequestDTO request) {
        try {
            SmsResponseDTO response = smsService.sendSms(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            SmsResponseDTO errorResponse = new SmsResponseDTO(false, "Erreur lors de l'envoi: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/echeance-reminder")
    @Operation(summary = "Envoyer un rappel d'échéance par SMS")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN')")
    public ResponseEntity<SmsResponseDTO> sendEcheanceReminder(
            @RequestParam String phoneNumber,
            @RequestParam String conventionRef,
            @RequestParam String amount,
            @RequestParam String dueDate,
            @RequestParam String userId) {
        
        try {
            SmsResponseDTO response = smsService.sendEcheanceReminder(phoneNumber, conventionRef, amount, dueDate, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            SmsResponseDTO errorResponse = new SmsResponseDTO(false, "Erreur lors de l'envoi: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/invoice-notification")
    @Operation(summary = "Envoyer une notification de facture par SMS")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN')")
    public ResponseEntity<SmsResponseDTO> sendInvoiceNotification(
            @RequestParam String phoneNumber,
            @RequestParam String invoiceNumber,
            @RequestParam String amount,
            @RequestParam String dueDate,
            @RequestParam String userId) {
        
        try {
            SmsResponseDTO response = smsService.sendInvoiceNotification(phoneNumber, invoiceNumber, amount, dueDate, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            SmsResponseDTO errorResponse = new SmsResponseDTO(false, "Erreur lors de l'envoi: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/payment-confirmation")
    @Operation(summary = "Envoyer une confirmation de paiement par SMS")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN')")
    public ResponseEntity<SmsResponseDTO> sendPaymentConfirmation(
            @RequestParam String phoneNumber,
            @RequestParam String invoiceNumber,
            @RequestParam String amount,
            @RequestParam String userId) {
        
        try {
            SmsResponseDTO response = smsService.sendPaymentConfirmation(phoneNumber, invoiceNumber, amount, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            SmsResponseDTO errorResponse = new SmsResponseDTO(false, "Erreur lors de l'envoi: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/overdue-alert")
    @Operation(summary = "Envoyer une alerte de retard par SMS")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN')")
    public ResponseEntity<SmsResponseDTO> sendOverdueAlert(
            @RequestParam String phoneNumber,
            @RequestParam String reference,
            @RequestParam String amount,
            @RequestParam int daysOverdue,
            @RequestParam String userId) {
        
        try {
            SmsResponseDTO response = smsService.sendOverdueAlert(phoneNumber, reference, amount, daysOverdue, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            SmsResponseDTO errorResponse = new SmsResponseDTO(false, "Erreur lors de l'envoi: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/system-notification")
    @Operation(summary = "Envoyer une notification système par SMS")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<SmsResponseDTO> sendSystemNotification(
            @RequestParam String phoneNumber,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String userId) {
        
        try {
            SmsResponseDTO response = smsService.sendSystemNotification(phoneNumber, title, content, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            SmsResponseDTO errorResponse = new SmsResponseDTO(false, "Erreur lors de l'envoi: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/history/{userId}")
    @Operation(summary = "Récupérer l'historique des SMS d'un utilisateur")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN')")
    public ResponseEntity<List<SmsNotification>> getSmsHistory(@PathVariable String userId) {
        try {
            List<SmsNotification> history = smsService.getSmsHistory(userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/stats/{userId}")
    @Operation(summary = "Récupérer les statistiques SMS d'un utilisateur")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN')")
    public ResponseEntity<SmsStatsDTO> getSmsStats(@PathVariable String userId) {
        try {
            SmsStatsDTO stats = smsService.getSmsStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/status/{twilioSid}")
    @Operation(summary = "Vérifier le statut d'un SMS via Twilio")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN')")
    public ResponseEntity<String> checkSmsStatus(@PathVariable String twilioSid) {
        try {
            String status = smsService.checkSmsStatus(twilioSid);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la vérification du statut");
        }
    }

    @PutMapping("/status/{smsId}")
    @Operation(summary = "Mettre à jour le statut d'un SMS")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN')")
    public ResponseEntity<Void> updateSmsStatus(
            @PathVariable String smsId,
            @RequestParam String status) {
        try {
            smsService.updateSmsStatus(smsId, status);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{smsId}")
    @Operation(summary = "Supprimer un SMS de l'historique")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deleteSms(@PathVariable String smsId) {
        try {
            smsService.deleteSms(smsId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/cleanup")
    @Operation(summary = "Nettoyer les anciens SMS")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> cleanupOldSms() {
        try {
            smsService.cleanupOldSms();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/format-phone")
    @Operation(summary = "Formater un numéro de téléphone français")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN')")
    public ResponseEntity<String> formatPhoneNumber(@RequestParam String phoneNumber) {
        try {
            String formatted = smsService.formatFrenchPhoneNumber(phoneNumber);
            return ResponseEntity.ok(formatted);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors du formatage");
        }
    }
} 