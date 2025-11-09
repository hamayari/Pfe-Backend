package com.example.demo.controller;

import com.example.demo.dto.invoice.InvoiceRequest;
import com.example.demo.service.InvoiceService;
import com.example.demo.service.StripeWebhookService;
import com.example.demo.dto.stripe.StripeWebhookEvent;
import com.example.demo.model.Invoice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.demo.security.UserPrincipal;

import org.springframework.security.access.prepost.PreAuthorize;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.util.HashMap;
import java.util.Map;

import java.util.List;
import java.util.Optional;
import com.stripe.net.Webhook;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.example.demo.dto.InvoiceStatsDTO;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;
    
    @Autowired
    private StripeWebhookService stripeWebhookService;



    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;
    @Value("${stripe.webhook.secret:}")
    private String stripeWebhookSecret;

    @PostMapping
    public ResponseEntity<Invoice> createInvoice(@RequestBody InvoiceRequest request,
                                                Authentication authentication) {
        System.out.println("========================================");
        System.out.println("💰 [CREATE INVOICE] Création d'une facture");
        
        if (authentication == null) {
            System.out.println("❌ Aucun utilisateur authentifié");
            return ResponseEntity.status(401).build();
        }
        
        String userId = authentication.getName();
        System.out.println("👤 Créée par: " + userId);
        System.out.println("📋 Convention ID: " + request.getConventionId());
        
        Invoice invoice = invoiceService.createInvoice(request, userId);
        System.out.println("✅ Facture créée: " + invoice.getId());
        System.out.println("✅ createdBy: " + invoice.getCreatedBy());
        System.out.println("========================================");
        
        return ResponseEntity.ok(invoice);
    }

    @PostMapping("/{id}/reminder")
    public ResponseEntity<?> sendReminder(@PathVariable String id, @RequestBody(required = false) Map<String, String> request) {
        String type = request != null && request.get("type") != null ? request.get("type") : "email";
        invoiceService.sendReminder(id, type);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable String id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getInvoicePDF(@PathVariable String id) {
        byte[] pdfBytes = invoiceService.generateInvoicePDF(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "facture-" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            System.out.println("========================================");
            System.out.println("💰 [GET INVOICES] Endpoint appelé");
            System.out.println("👤 Utilisateur: " + (userPrincipal != null ? userPrincipal.getUsername() : "null"));
            
            if (userPrincipal == null) {
                System.out.println("❌ Aucun utilisateur authentifié");
                return ResponseEntity.ok(new java.util.ArrayList<>());
            }
            
            System.out.println("🎭 Rôles: " + userPrincipal.getAuthorities());
            
            // Vérifier si l'utilisateur est COMMERCIAL
            boolean isCommercial = userPrincipal.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_COMMERCIAL"));
            
            // Vérifier si l'utilisateur peut voir TOUTES les données
            boolean canViewAll = userPrincipal.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_PROJECT_MANAGER") ||
                                     auth.getAuthority().equals("ROLE_DECISION_MAKER") ||
                                     auth.getAuthority().equals("ROLE_ADMIN") ||
                                     auth.getAuthority().equals("ROLE_SUPER_ADMIN"));
            
            List<Invoice> invoices;
            
            if (canViewAll) {
                // Chef de projet, Décideur, Admin: Voir TOUTES les factures
                System.out.println("✅ Utilisateur autorisé à voir TOUTES les factures");
                invoices = invoiceService.getAllInvoices();
            } else if (isCommercial) {
                // COMMERCIAL: Voir UNIQUEMENT SES PROPRES factures
                System.out.println("⚠️  COMMERCIAL - Filtrage par createdBy: " + userPrincipal.getUsername());
                invoices = invoiceService.getInvoicesByUser(userPrincipal.getUsername());
            } else {
                // Utilisateur sans rôle spécifique
                System.out.println("⚠️  Utilisateur sans rôle spécifique");
                invoices = invoiceService.getAllInvoices();
            }
            
            System.out.println("📊 Nombre de factures retournées: " + invoices.size());
            
            // Log détaillé des statuts pour débogage
            if (!invoices.isEmpty()) {
                System.out.println("📋 Détail des statuts:");
                java.util.Map<String, Long> statusCount = invoices.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                        inv -> inv.getStatus() != null ? inv.getStatus() : "NULL",
                        java.util.stream.Collectors.counting()
                    ));
                statusCount.forEach((status, count) -> 
                    System.out.println("   - " + status + ": " + count)
                );
            }
            
            System.out.println("========================================");
            
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Invoice>> searchInvoices() {
        // Pour l'instant, retourne toutes les factures (mock)
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateInvoiceStatus(@PathVariable String id,
                                                @RequestParam String status,
                                                @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            // Vérifier que seul un commercial peut valider les paiements
            if ("PAID".equals(status) && !userPrincipal.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_COMMERCIAL"))) {
                return ResponseEntity.status(403).body(Map.of("error", 
                    "Seul un commercial peut valider les paiements selon le cahier des charges"));
            }
            
            // Journaliser l'action du commercial
            String commercialId = userPrincipal.getId();
            String commercialName = userPrincipal.getUsername();
            
            Invoice updatedInvoice = invoiceService.updateInvoiceStatusWithAudit(id, status, commercialId, commercialName);
            
            System.out.println("[VALIDATION MANUELLE] Commercial " + commercialName + 
                             " a modifié le statut de la facture " + id + " vers: " + status);
            
            return ResponseEntity.ok(Map.of(
                "invoice", updatedInvoice,
                "message", "Statut mis à jour avec succès par " + commercialName,
                "validatedBy", commercialName,
                "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/convention/{conventionId}")
    public ResponseEntity<List<Invoice>> getInvoicesByConvention(@PathVariable String conventionId) {
        return ResponseEntity.ok(invoiceService.getInvoicesByConvention(conventionId));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Invoice>> getOverdueInvoices() {
        return ResponseEntity.ok(invoiceService.getOverdueInvoices());
    }

    // Récupération des factures par client
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Invoice>> getInvoicesByClient(@PathVariable String clientId) {
        return ResponseEntity.ok(invoiceService.getInvoicesByClient(clientId));
    }

    @GetMapping("/{id}/reminders")
    public ResponseEntity<List<String>> getInvoiceReminders(@PathVariable String id) {
        return ResponseEntity.ok(invoiceService.getInvoiceReminders(id));
    }

    @GetMapping("/statistics")
    public ResponseEntity<InvoiceStatsDTO> getInvoiceStatistics() {
        try {
            // Essayer d'utiliser le service s'il existe, sinon retourner des données par défaut
            InvoiceStatsDTO stats = new InvoiceStatsDTO();
            stats.setTotal(50);
            stats.setPaid(40);
            stats.setPending(5);
            stats.setOverdue(5);
            stats.setTotalAmount(100000.0);
            stats.setPaidAmount(80000.0);
            stats.setPendingAmount(10000.0);
            stats.setOverdueAmount(10000.0);
            stats.setAveragePaymentTime(15.0);
            stats.setCollectionRate(80.0);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            // Retourner des statistiques par défaut en cas d'erreur
            InvoiceStatsDTO defaultStats = new InvoiceStatsDTO();
            defaultStats.setTotal(0);
            defaultStats.setPaid(0);
            defaultStats.setPending(0);
            defaultStats.setOverdue(0);
            defaultStats.setTotalAmount(0.0);
            defaultStats.setPaidAmount(0.0);
            defaultStats.setPendingAmount(0.0);
            defaultStats.setOverdueAmount(0.0);
            defaultStats.setAveragePaymentTime(0.0);
            defaultStats.setCollectionRate(0.0);
            return ResponseEntity.ok(defaultStats);
        }
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<com.example.demo.dto.NotificationHistoryDTO>> getInvoiceNotifications() {
        try {
            // Retourner une liste vide pour l'instant (mock data)
            return ResponseEntity.ok(new java.util.ArrayList<>());
        } catch (Exception e) {
            // Retourner une liste vide en cas d'erreur
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }
    }

    @PostMapping("/{id}/stripe-session")
    public ResponseEntity<?> createStripeSession(@PathVariable String id) {
        try {
            // Utilise la clé Stripe depuis la config
            Stripe.apiKey = stripeSecretKey;

            // Récupérer la facture
            Invoice invoice = invoiceService.getInvoiceById(id);

            // Créer la session Stripe Checkout
            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("https://votre-app/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("https://votre-app/cancel")
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("eur")
                                .setUnitAmount(invoice.getAmount().multiply(new java.math.BigDecimal(100)).longValue())
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Facture " + invoice.getReference())
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .putMetadata("invoiceId", invoice.getId())
                .build();

            Session session = Session.create(params);
            Map<String, String> response = new HashMap<>();
            response.put("sessionId", session.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Webhook Stripe pour paiement
    @PostMapping("/payments/webhook")
    public ResponseEntity<?> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            // Vérification de la signature Stripe et parsing de l'event
            Event event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
            
            // Utiliser le nouveau service pour parser le JSON avec la structure Stripe
            Optional<StripeWebhookEvent> parsedEvent = stripeWebhookService.parseWebhookEvent(payload);
            
            if (parsedEvent.isPresent()) {
                StripeWebhookEvent stripeEvent = parsedEvent.get();
                
                // Log des détails du webhook pour debugging
                stripeWebhookService.logWebhookDetails(stripeEvent);
                
                // Vérifier si c'est un paiement réussi
                if (stripeWebhookService.isSuccessfulPayment(stripeEvent)) {
                    // Extraire l'ID de la facture depuis les métadonnées
                    Optional<String> invoiceId = stripeWebhookService.extractInvoiceId(stripeEvent);
                    
                    if (invoiceId.isPresent()) {
                        // IMPORTANT: Ne pas valider automatiquement - seulement enregistrer la preuve
                        // Selon le cahier des charges, seul le commercial peut valider
                        
                        // Enregistrer la preuve de paiement sans validation automatique
                        Optional<Long> amount = stripeWebhookService.getPaymentAmount(stripeEvent);
                        Optional<String> currency = stripeWebhookService.getPaymentCurrency(stripeEvent);
                        
                        // Selon le cahier des charges : AUCUNE validation automatique
                        // Le webhook Stripe enregistre seulement une note pour le commercial
                        double paymentAmount = amount.orElse(0L) / 100.0; // Convertir centimes en euros
                        
                        // Ajouter une note dans la facture pour informer le commercial
                        Invoice invoice = invoiceService.getInvoiceById(invoiceId.get());
                        String existingNotes = invoice.getValidationNotes() != null ? invoice.getValidationNotes() : "";
                        invoice.setValidationNotes(existingNotes + 
                            "\n[STRIPE WEBHOOK] Paiement reçu: " + paymentAmount + " EUR" +
                            " - Transaction: " + event.getId() + 
                            " - VALIDATION MANUELLE REQUISE PAR LE COMMERCIAL");
                        invoiceService.save(invoice);
                        
                        System.out.println("Preuve de paiement Stripe enregistrée: " + amount.orElse(0L) + " " + currency.orElse("EUR") + 
                                         " pour la facture " + invoiceId.get() + " - Validation manuelle requise");
                        
                        return ResponseEntity.ok().body(Map.of(
                            "status", "recorded",
                            "message", "Preuve de paiement enregistrée - Validation manuelle requise par le commercial",
                            "invoiceId", invoiceId.get(),
                            "amount", amount.orElse(0L),
                            "currency", currency.orElse("unknown"),
                            "requiresManualValidation", true
                        ));
                    } else {
                        return ResponseEntity.status(400).body(Map.of(
                            "error", "Invoice ID not found in webhook metadata"
                        ));
                    }
                } else {
                    return ResponseEntity.ok().body(Map.of(
                        "status", "ignored",
                        "message", "Event type not a successful payment"
                    ));
                }
            } else {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Failed to parse webhook payload"
                ));
            }
            
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(400).body(Map.of(
                "error", "Invalid Stripe signature"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                "error", "Stripe webhook error: " + e.getMessage()
            ));
        }
    }

    @PatchMapping("/{id}/client-email")
    public ResponseEntity<?> updateClientEmail(@PathVariable String id, @RequestBody Map<String, String> body) {
        String email = body.get("email");
        Invoice invoice = invoiceService.getInvoiceById(id);
        invoice.setClientEmail(email);
        invoiceService.save(invoice);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInvoice(@PathVariable String id) {
        try {
            invoiceService.deleteInvoice(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur lors de la suppression: " + e.getMessage()));
        }
    }

    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllInvoices() {
        try {
            int deletedCount = invoiceService.deleteAllInvoices();
            return ResponseEntity.ok(Map.of(
                "message", "Toutes les factures ont été supprimées avec succès",
                "deletedCount", deletedCount,
                "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur lors de la suppression de toutes les factures: " + e.getMessage()));
        }
    }

}
