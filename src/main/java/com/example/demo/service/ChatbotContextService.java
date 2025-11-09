package com.example.demo.service;

import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service qui prépare les données contextuelles pour le chatbot
 */
@Service
public class ChatbotContextService {

    @Autowired
    private ConventionRepository conventionRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    /**
     * Prépare le contexte de données selon la question posée
     */
    public Map<String, Object> prepareContext(String question) {
        Map<String, Object> context = new HashMap<>();
        
        String questionLower = question.toLowerCase();
        
        // Charger les données pertinentes selon la question
        if (questionLower.contains("facture") || questionLower.contains("retard") || 
            questionLower.contains("payé") || questionLower.contains("paiement")) {
            addInvoiceContext(context, questionLower);
        }
        
        if (questionLower.contains("convention") || questionLower.contains("actif") || 
            questionLower.contains("expiré")) {
            addConventionContext(context, questionLower);
        }
        
        if (questionLower.contains("région") || questionLower.contains("gouvernorat") || 
            questionLower.contains("zone") || questionLower.contains("géographique")) {
            addGeographicContext(context);
        }
        
        // Ajouter des statistiques globales
        addGlobalStats(context);
        
        return context;
    }
    
    /**
     * Ajoute le contexte des factures
     */
    private void addInvoiceContext(Map<String, Object> context, String questionLower) {
        List<Invoice> allInvoices = invoiceRepository.findAll();
        
        // Factures en retard
        List<Invoice> overdueInvoices = allInvoices.stream()
            .filter(inv -> "OVERDUE".equals(inv.getStatus()) || 
                          (inv.getDueDate() != null && inv.getDueDate().isBefore(LocalDate.now()) && 
                           !"PAID".equals(inv.getStatus())))
            .collect(Collectors.toList());
        
        // Factures payées
        List<Invoice> paidInvoices = allInvoices.stream()
            .filter(inv -> "PAID".equals(inv.getStatus()))
            .collect(Collectors.toList());
        
        // Factures en attente
        List<Invoice> pendingInvoices = allInvoices.stream()
            .filter(inv -> "PENDING".equals(inv.getStatus()))
            .collect(Collectors.toList());
        
        // Calculs
        double totalAmount = allInvoices.stream()
            .mapToDouble(inv -> inv.getAmount() != null ? inv.getAmount().doubleValue() : 0.0)
            .sum();
        
        double paidAmount = paidInvoices.stream()
            .mapToDouble(inv -> inv.getAmount() != null ? inv.getAmount().doubleValue() : 0.0)
            .sum();
        
        double overdueAmount = overdueInvoices.stream()
            .mapToDouble(inv -> inv.getAmount() != null ? inv.getAmount().doubleValue() : 0.0)
            .sum();
        
        double paymentRate = allInvoices.isEmpty() ? 0 : 
            (paidInvoices.size() * 100.0 / allInvoices.size());
        
        // Ajouter au contexte
        context.put("totalFactures", (long) allInvoices.size());
        context.put("facturesPayees", (long) paidInvoices.size());
        context.put("facturesEnRetard", (long) overdueInvoices.size());
        context.put("facturesEnAttente", (long) pendingInvoices.size());
        context.put("montantTotal", totalAmount);
        context.put("montantPaye", paidAmount);
        context.put("montantEnRetard", overdueAmount);
        context.put("tauxPaiement", paymentRate);
        
        // Répartition par gouvernorat si demandé (commenté car Invoice n'a pas de champ governorate)
        // if (questionLower.contains("région") || questionLower.contains("gouvernorat")) {
        //     Map<String, Long> byGovernorate = overdueInvoices.stream()
        //         .filter(inv -> inv.getGovernorate() != null)
        //         .collect(Collectors.groupingBy(Invoice::getGovernorate, Collectors.counting()));
        //     context.put("factures_retard_par_gouvernorat", byGovernorate);
        // }
    }
    
    /**
     * Ajoute le contexte des conventions
     */
    private void addConventionContext(Map<String, Object> context, String questionLower) {
        List<Convention> allConventions = conventionRepository.findAll();
        
        // Conventions actives
        List<Convention> activeConventions = allConventions.stream()
            .filter(conv -> "ACTIVE".equals(conv.getStatus()))
            .collect(Collectors.toList());
        
        // Conventions expirées
        List<Convention> expiredConventions = allConventions.stream()
            .filter(conv -> "EXPIRED".equals(conv.getStatus()))
            .collect(Collectors.toList());
        
        // Conventions en attente
        List<Convention> pendingConventions = allConventions.stream()
            .filter(conv -> "PENDING".equals(conv.getStatus()))
            .collect(Collectors.toList());
        
        // Calculs
        double totalAmount = allConventions.stream()
            .mapToDouble(conv -> conv.getAmount() != null ? conv.getAmount().doubleValue() : 0.0)
            .sum();
        
        double activeAmount = activeConventions.stream()
            .mapToDouble(conv -> conv.getAmount() != null ? conv.getAmount().doubleValue() : 0.0)
            .sum();
        
        // Ajouter au contexte
        context.put("totalConventions", (long) allConventions.size());
        context.put("activeConventions", (long) activeConventions.size());
        context.put("conventionsExpirees", (long) expiredConventions.size());
        context.put("conventionsEnAttente", (long) pendingConventions.size());
        context.put("montantTotalConventions", totalAmount);
        context.put("montantActifConventions", activeAmount);
    }
    
    /**
     * Ajoute le contexte géographique
     * Note: Utilise le champ 'governorate' des conventions car Invoice n'a pas ce champ
     */
    private void addGeographicContext(Map<String, Object> context) {
        List<Convention> allConventions = conventionRepository.findAll();
        
        // Répartition par gouvernorat depuis les conventions
        Map<String, Long> conventionsByGovernorate = allConventions.stream()
            .filter(conv -> conv.getGovernorate() != null)
            .collect(Collectors.groupingBy(Convention::getGovernorate, Collectors.counting()));
        
        Map<String, Double> amountByGovernorate = allConventions.stream()
            .filter(conv -> conv.getGovernorate() != null && conv.getAmount() != null)
            .collect(Collectors.groupingBy(
                Convention::getGovernorate,
                Collectors.summingDouble(conv -> conv.getAmount().doubleValue())
            ));
        
        context.put("conventions_par_gouvernorat", conventionsByGovernorate);
        context.put("montant_par_gouvernorat", amountByGovernorate);
    }
    
    /**
     * Ajoute des statistiques globales
     */
    private void addGlobalStats(Map<String, Object> context) {
        context.put("date_analyse", LocalDate.now().toString());
        context.put("mois_courant", LocalDate.now().getMonth().toString());
        context.put("annee_courante", LocalDate.now().getYear());
    }
}
