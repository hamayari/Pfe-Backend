package com.example.demo.service;

import com.example.demo.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class InvoiceNumberGenerator {

    private final InvoiceRepository invoiceRepository;

    /**
     * Génère un numéro de facture unique au format INV-YYYY-XXX
     * Exemple: INV-2025-001, INV-2025-002, etc.
     */
    public String generateInvoiceNumber() {
        int currentYear = LocalDate.now().getYear();
        String prefix = "INV-" + currentYear + "-";
        
        // Trouver le dernier numéro de facture de l'année en cours
        String lastInvoiceNumber = findLastInvoiceNumberForYear(currentYear);
        
        int nextSequence = 1;
        if (lastInvoiceNumber != null) {
            // Extraire le numéro de séquence du dernier numéro de facture
            Pattern pattern = Pattern.compile("INV-\\d{4}-(\\d+)");
            Matcher matcher = pattern.matcher(lastInvoiceNumber);
            if (matcher.find()) {
                nextSequence = Integer.parseInt(matcher.group(1)) + 1;
            }
        }
        
        // Formater avec des zéros devant (3 chiffres minimum)
        return prefix + String.format("%03d", nextSequence);
    }

    /**
     * Trouve le dernier numéro de facture pour une année donnée
     */
    private String findLastInvoiceNumberForYear(int year) {
        String prefix = "INV-" + year + "-";
        
        // Récupérer toutes les factures de l'année et trouver le plus grand numéro
        return invoiceRepository.findAll().stream()
            .map(invoice -> invoice.getInvoiceNumber())
            .filter(number -> number != null && number.startsWith(prefix))
            .max(String::compareTo)
            .orElse(null);
    }

    /**
     * Génère un numéro de facture basé sur une convention
     * Format: INV-{ANNÉE}-{CONVENTION_REF}-{SÉQUENCE}
     * Exemple: INV-2025-CONV001-01
     */
    public String generateInvoiceNumberForConvention(String conventionReference, int echeanceNumber) {
        int currentYear = LocalDate.now().getYear();
        
        // Nettoyer la référence de la convention (enlever les caractères spéciaux)
        String cleanRef = conventionReference.replaceAll("[^A-Za-z0-9]", "");
        
        return String.format("INV-%d-%s-%02d", currentYear, cleanRef, echeanceNumber);
    }

    /**
     * Valide si un numéro de facture est unique
     */
    public boolean isInvoiceNumberUnique(String invoiceNumber) {
        return invoiceRepository.findAll().stream()
            .noneMatch(invoice -> invoiceNumber.equals(invoice.getInvoiceNumber()));
    }
}
