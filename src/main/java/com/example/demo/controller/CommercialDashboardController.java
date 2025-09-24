package com.example.demo.controller;

import com.example.demo.dto.KPIMetricsDTO;
import com.example.demo.dto.NotificationSettingsDTO;
import com.example.demo.dto.NotificationHistoryDTO;
import com.example.demo.dto.ConventionStatsDTO;
import com.example.demo.dto.InvoiceStatsDTO;
import com.example.demo.service.CommercialDashboardService;
import com.example.demo.service.NotificationService;
import com.example.demo.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import com.example.demo.service.CommercialDashboardService.InvoiceBatchResult;
import com.example.demo.model.Invoice;
import com.example.demo.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/commercial/dashboard")
@PreAuthorize("hasRole('COMMERCIAL')")
public class CommercialDashboardController {

    @Autowired
    private CommercialDashboardService commercialDashboardService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    @SuppressWarnings("unused")
    private ReportService reportService;

    // KPI et métriques
    @GetMapping("/kpi")
    public ResponseEntity<KPIMetricsDTO> getKPIMetrics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String structureId,
            @RequestParam(required = false) String governorate,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        try {
            String userId = userPrincipal.getId(); // Utiliser l'ID au lieu du username
            KPIMetricsDTO kpi = commercialDashboardService.getKPIMetrics(userId, startDate, endDate, structureId, governorate);
            return ResponseEntity.ok(kpi);
        } catch (Exception e) {
            // Retourner des données par défaut en cas d'erreur
            KPIMetricsDTO defaultKpi = new KPIMetricsDTO();
            defaultKpi.setTotalConventions(0);
            defaultKpi.setActiveConventions(0);
            defaultKpi.setExpiredConventions(0);
            defaultKpi.setTotalInvoices(0);
            defaultKpi.setPaidInvoices(0);
            defaultKpi.setOverdueInvoices(0);
            defaultKpi.setCollectionRate(0.0);
            defaultKpi.setAveragePaymentTime(0);
            defaultKpi.setMonthlyRevenue(0.0);
            defaultKpi.setPendingAmount(0.0);
            return ResponseEntity.ok(defaultKpi);
        }
    }

    // Statistiques des conventions
    @GetMapping("/conventions/stats")
    public ResponseEntity<ConventionStatsDTO> getConventionStats(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        String userId = userPrincipal.getId();
        ConventionStatsDTO stats = commercialDashboardService.getConventionStats(userId);
        return ResponseEntity.ok(stats);
    }

    // Statistiques des factures
    @GetMapping("/invoices/stats")
    public ResponseEntity<InvoiceStatsDTO> getInvoiceStats(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        String userId = userPrincipal.getId();
        InvoiceStatsDTO stats = commercialDashboardService.getInvoiceStats(userId);
        return ResponseEntity.ok(stats);
    }

    // Conventions par échéance
    @GetMapping("/conventions/deadline/{days}")
    public ResponseEntity<?> getConventionsByDeadline(
            @PathVariable int days,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(commercialDashboardService.getConventionsByDeadline(userId, days));
    }

    // Conventions expirées
    @GetMapping("/conventions/expired")
    public ResponseEntity<?> getExpiredConventions(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(commercialDashboardService.getExpiredConventions(userId));
    }

    // Factures en retard
    @GetMapping("/invoices/overdue")
    public ResponseEntity<?> getOverdueInvoices(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(commercialDashboardService.getOverdueInvoices(userId));
    }

    // Factures à échéance proche
    @GetMapping("/invoices/upcoming/{days}")
    public ResponseEntity<?> getUpcomingInvoices(
            @PathVariable int days,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(commercialDashboardService.getUpcomingInvoices(userId, days));
    }

    // Paramètres de notification
    @GetMapping("/notifications/settings")
    public ResponseEntity<NotificationSettingsDTO> getNotificationSettings(Authentication authentication) {
        String userId = authentication.getName();
        NotificationSettingsDTO settings = notificationService.getNotificationSettings(userId);
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/notifications/settings")
    public ResponseEntity<NotificationSettingsDTO> updateNotificationSettings(
            @RequestBody NotificationSettingsDTO settings,
            Authentication authentication) {
        String userId = authentication.getName();
        NotificationSettingsDTO updatedSettings = notificationService.updateNotificationSettings(userId, settings);
        return ResponseEntity.ok(updatedSettings);
    }

    // Historique des notifications
    @GetMapping("/notifications/history")
    public ResponseEntity<List<NotificationHistoryDTO>> getNotificationHistory(Authentication authentication) {
        String userId = authentication.getName();
        List<NotificationHistoryDTO> history = notificationService.getNotificationHistory(userId);
        return ResponseEntity.ok(history);
    }

    // Relances manuelles
    @PostMapping("/notifications/reminder")
    public ResponseEntity<?> sendManualReminder(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        String userId = authentication.getName();
        String invoiceId = (String) request.get("invoiceId");
        String type = (String) request.get("type");
        
        notificationService.sendManualReminder(userId, invoiceId, type);
        return ResponseEntity.ok().build();
    }

    // Relances automatiques
    @PostMapping("/notifications/schedule-reminders")
    public ResponseEntity<?> scheduleAutomaticReminders(Authentication authentication) {
        String userId = authentication.getName();
        notificationService.scheduleAutomaticReminders(userId);
        return ResponseEntity.ok().build();
    }

    // Test de notification
    @PostMapping("/notifications/test")
    public ResponseEntity<?> testNotification(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        String userId = authentication.getName();
        String type = (String) request.get("type");
        String recipient = (String) request.get("recipient");
        
        notificationService.testNotification(userId, type, recipient);
        return ResponseEntity.ok().build();
    }

    // Recherche avancée de conventions
    @GetMapping("/conventions/search")
    public ResponseEntity<?> searchConventions(
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String structureId,
            @RequestParam(required = false) String governorate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Double amountMin,
            @RequestParam(required = false) Double amountMax,
            Authentication authentication) {
        
        String userId = authentication.getName();
        return ResponseEntity.ok(commercialDashboardService.searchConventions(
            userId, reference, title, structureId, governorate, status,
            startDate, endDate, tags, amountMin, amountMax));
    }

    // Recherche avancée de factures
    @GetMapping("/invoices/search")
    public ResponseEntity<?> searchInvoices(
            @RequestParam(required = false) String invoiceNumber,
            @RequestParam(required = false) String conventionId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Double amountMin,
            @RequestParam(required = false) Double amountMax,
            @RequestParam(required = false) String dueDateFrom,
            @RequestParam(required = false) String dueDateTo,
            Authentication authentication) {
        
        String userId = authentication.getName();
        return ResponseEntity.ok(commercialDashboardService.searchInvoices(
            userId, invoiceNumber, conventionId, status, startDate, endDate,
            amountMin, amountMax, dueDateFrom, dueDateTo));
    }

    // Génération de factures en lot
    @PostMapping("/invoices/generate-batch")
    public ResponseEntity<?> generateInvoicesBatch(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        String userId = userPrincipal.getId();
        Object idsObj = request.get("conventionIds");
        if (!(idsObj instanceof List<?> ids) || ids.isEmpty()) {
            // Toujours retourner la structure attendue
            return ResponseEntity.badRequest().body(Map.of(
                "invoices", List.of(),
                "errors", List.of("Le champ 'conventionIds' est requis et doit être une liste non vide.")
            ));
        }
        List<String> conventionIds = new ArrayList<>();
        for (Object o : ids) {
            if (o instanceof String s) conventionIds.add(s);
            else return ResponseEntity.badRequest().body(Map.of(
                "invoices", List.of(),
                "errors", List.of("Tous les éléments de 'conventionIds' doivent être des chaînes.")
            ));
        }
        String dueDate = request.get("dueDate") instanceof String d ? d : null;
        Integer paymentTerms = request.get("paymentTerms") instanceof Integer p ? p : null;
        Boolean sendEmail = request.get("sendEmail") instanceof Boolean b ? b : false;

        InvoiceBatchResult result = commercialDashboardService.generateInvoicesBatch(
            userId, conventionIds, dueDate, paymentTerms, sendEmail);
        // Toujours retourner la structure attendue
        return ResponseEntity.ok(Map.of(
            "invoices", result.getInvoices() != null ? result.getInvoices() : List.of(),
            "errors", result.getErrors() != null ? result.getErrors() : List.of()
        ));
    }

    // Export des conventions
    @GetMapping("/conventions/export")
    public ResponseEntity<?> exportConventions(
            @RequestParam String format,
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String structureId,
            @RequestParam(required = false) String governorate,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        
        String userId = authentication.getName();
        byte[] fileContent = commercialDashboardService.exportConventions(
            userId, format, reference, title, structureId, governorate, status);
        
        String filename = "conventions_" + System.currentTimeMillis() + "." + format;
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
            .body(fileContent);
    }

    // Export des factures
    @GetMapping("/invoices/export")
    public ResponseEntity<?> exportInvoices(
            @RequestParam String format,
            @RequestParam(required = false) String invoiceNumber,
            @RequestParam(required = false) String conventionId,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        
        String userId = authentication.getName();
        byte[] fileContent = commercialDashboardService.exportInvoices(
            userId, format, invoiceNumber, conventionId, status);
        
        String filename = "invoices_" + System.currentTimeMillis() + "." + format;
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
            .body(fileContent);
    }

    // Gestion des tags
    @PostMapping("/conventions/{conventionId}/tags")
    public ResponseEntity<?> addTagToConvention(
            @PathVariable String conventionId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String userId = authentication.getName();
        String tag = request.get("tag");
        
        return ResponseEntity.ok(commercialDashboardService.addTagToConvention(userId, conventionId, tag));
    }

    @DeleteMapping("/conventions/{conventionId}/tags/{tag}")
    public ResponseEntity<?> removeTagFromConvention(
            @PathVariable String conventionId,
            @PathVariable String tag,
            Authentication authentication) {
        String userId = authentication.getName();
        
        return ResponseEntity.ok(commercialDashboardService.removeTagFromConvention(userId, conventionId, tag));
    }

    // Historique d'audit
    @GetMapping("/conventions/{conventionId}/audit")
    public ResponseEntity<?> getConventionAudit(
            @PathVariable String conventionId,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(commercialDashboardService.getConventionAudit(userId, conventionId));
    }

    // Duplication de convention
    @PostMapping("/conventions/{conventionId}/duplicate")
    public ResponseEntity<?> duplicateConvention(
            @PathVariable String conventionId,
            @RequestBody(required = false) Map<String, Object> newData,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(commercialDashboardService.duplicateConvention(userId, conventionId, newData));
    }

    // Changement de statut en lot
    @PutMapping("/conventions/batch-status")
    public ResponseEntity<?> updateConventionsStatus(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        String userId = authentication.getName();
        @SuppressWarnings("unchecked")
        List<String> conventionIds = (List<String>) request.get("conventionIds");
        String status = (String) request.get("status");
        
        return ResponseEntity.ok(commercialDashboardService.updateConventionsStatus(userId, conventionIds, status));
    }

    // Calcul automatique des échéances
    @GetMapping("/invoices/calculate-due-date")
    public ResponseEntity<?> calculateDueDate(
            @RequestParam String conventionId,
            @RequestParam int paymentTerms,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(commercialDashboardService.calculateDueDate(userId, conventionId, paymentTerms));
    }

    // Envoi de facture par email
    @PostMapping("/invoices/{invoiceId}/send-email")
    public ResponseEntity<?> sendInvoiceByEmail(
            @PathVariable String invoiceId,
            @RequestBody(required = false) Map<String, String> request,
            Authentication authentication) {
        String userId = authentication.getName();
        String email = request != null ? request.get("email") : null;
        
        commercialDashboardService.sendInvoiceByEmail(userId, invoiceId, email);
        return ResponseEntity.ok().build();
    }

    // Génération PDF de facture
    @GetMapping("/invoices/{invoiceId}/pdf")
    public ResponseEntity<?> generateInvoicePDF(
            @PathVariable String invoiceId,
            Authentication authentication) {
        String userId = authentication.getName();
        byte[] pdfContent = commercialDashboardService.generateInvoicePDF(userId, invoiceId);
        
        String filename = "invoice_" + invoiceId + ".pdf";
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
            .body(pdfContent);
    }

    // Marquer facture comme payée
    @PutMapping("/invoices/{invoiceId}/mark-paid")
    public ResponseEntity<?> markInvoiceAsPaid(
            @PathVariable String invoiceId,
            @RequestBody(required = false) Map<String, Object> request,
            Authentication authentication) {
        String userId = authentication.getName();
        String paymentDate = request != null ? (String) request.get("paymentDate") : null;
        String paymentMethod = request != null ? (String) request.get("paymentMethod") : null;
        
        return ResponseEntity.ok(commercialDashboardService.markInvoiceAsPaid(userId, invoiceId, paymentDate, paymentMethod));
    }

    // Paiement partiel
    @PutMapping("/invoices/{invoiceId}/partial-payment")
    public ResponseEntity<?> recordPartialPayment(
            @PathVariable String invoiceId,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        String userId = authentication.getName();
        Double amount = (Double) request.get("amount");
        String paymentDate = (String) request.get("paymentDate");
        
        return ResponseEntity.ok(commercialDashboardService.recordPartialPayment(userId, invoiceId, amount, paymentDate));
    }

    @PatchMapping("/invoices/{id}/client-email")
    public ResponseEntity<?> updateClientEmail(@PathVariable String id, @RequestBody Map<String, String> body) {
        String email = body.get("email");
        Invoice invoice = commercialDashboardService.getInvoiceById(id);
        invoice.setClientEmail(email);
        commercialDashboardService.saveInvoice(invoice);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/invoices/{invoiceId}")
    public ResponseEntity<?> deleteInvoice(@PathVariable String invoiceId, Authentication authentication) {
        String userId = authentication.getName();
        commercialDashboardService.deleteInvoice(userId, invoiceId);
        return ResponseEntity.ok().build();
    }
}
