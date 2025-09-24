package com.example.demo.controller;

import com.example.demo.service.PaymentProofOcrService;
import com.example.demo.service.PaymentProofMatchingService;
import com.example.demo.service.PaymentProofArchivingService;
import com.example.demo.service.PaymentReceiptService;
import com.example.demo.service.NotificationService;
import com.example.demo.service.PaymentProofData;
import com.example.demo.service.MatchingResult;
import com.example.demo.service.MultipartFileEmailAttachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/twilio/payment-proof")
public class TwilioWebhookController {
    @Autowired
    private PaymentProofOcrService ocrService;
    @Autowired
    private PaymentProofMatchingService matchingService;
    @Autowired
    private PaymentProofArchivingService archivingService;
    @Autowired
    private PaymentReceiptService receiptService;
    @Autowired
    private NotificationService notificationService;

    @PostMapping
    public ResponseEntity<String> receiveProof(@RequestParam("file") MultipartFile file) {
        try {
            // 1. OCR
            PaymentProofData data = ocrService.extractData(new MultipartFileEmailAttachment(file));
            // 2. Matching
            MatchingResult match = matchingService.matchProofToInvoice(data);
            // 3. Archivage
            String proofId = archivingService.archiveProof(new MultipartFileEmailAttachment(file), data, match);
            // 4. Génération reçu si validé
            if (match.isValid()) {
                byte[] receipt = receiptService.generateReceipt(match.getInvoiceId(), data, proofId);
                archivingService.archiveReceipt(receipt, match.getInvoiceId());
                notificationService.notifyPaymentValidated(match.getInvoiceId(), proofId);
            } else {
                notificationService.notifyPaymentPendingReview(match.getInvoiceId(), proofId);
            }
            return ResponseEntity.ok("Preuve reçue et traitée");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur traitement Twilio: " + e.getMessage());
        }
    }
} 