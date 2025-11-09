package com.example.demo.controller;

import com.example.demo.dto.SmsRequestDTO;
import com.example.demo.dto.SmsResponseDTO;
import com.example.demo.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sms/test")
@CrossOrigin(origins = "*")
public class SmsTestController {

    @Autowired
    private SmsService smsService;

    /**
     * Endpoint de test simple pour envoyer un SMS
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> testSendSms(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String phoneNumber = payload.get("phoneNumber");
            String message = payload.getOrDefault("message", "Test SMS depuis GestionPro");
            
            System.out.println("📱 [TEST SMS] Requête reçue");
            System.out.println("📱 [TEST SMS] Numéro: " + phoneNumber);
            System.out.println("📱 [TEST SMS] Message: " + message);
            
            // Valider les paramètres
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Numéro de téléphone requis");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Créer la requête SMS
            SmsRequestDTO request = new SmsRequestDTO();
            request.setTo(phoneNumber);
            request.setMessage(message);
            request.setType("TEST");
            
            // Envoyer le SMS
            SmsResponseDTO smsResponse = smsService.sendSms(request);
            
            response.put("success", smsResponse.isSuccess());
            response.put("message", smsResponse.getMessage());
            response.put("smsId", smsResponse.getSmsId());
            response.put("twilioSid", smsResponse.getTwilioSid());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ [TEST SMS] Erreur: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint pour tester avec un template
     */
    @PostMapping("/send-template")
    public ResponseEntity<Map<String, Object>> testSendSmsWithTemplate(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String phoneNumber = (String) payload.get("phoneNumber");
            String templateType = (String) payload.getOrDefault("templateType", "invoice_reminder");
            @SuppressWarnings("unchecked")
            Map<String, String> variables = (Map<String, String>) payload.getOrDefault("variables", new HashMap<>());
            
            System.out.println("📱 [TEST SMS TEMPLATE] Requête reçue");
            System.out.println("📱 [TEST SMS TEMPLATE] Numéro: " + phoneNumber);
            System.out.println("📱 [TEST SMS TEMPLATE] Template: " + templateType);
            
            // Valider les paramètres
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Numéro de téléphone requis");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Valeurs par défaut pour les variables
            if (variables.isEmpty()) {
                variables.put("invoiceNumber", "TEST-001");
                variables.put("amount", "100.00");
                variables.put("dueDate", "31/12/2025");
            }
            
            // Envoyer le SMS avec template
            SmsResponseDTO smsResponse = smsService.sendSmsWithTemplate(phoneNumber, templateType, variables);
            
            response.put("success", smsResponse.isSuccess());
            response.put("message", smsResponse.getMessage());
            response.put("smsId", smsResponse.getSmsId());
            response.put("twilioSid", smsResponse.getTwilioSid());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ [TEST SMS TEMPLATE] Erreur: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Vérifier la configuration Twilio
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> checkConfig() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Réinitialiser Twilio
            smsService.initializeTwilio();
            
            response.put("success", true);
            response.put("message", "Configuration Twilio vérifiée - consultez les logs serveur");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur de configuration: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
