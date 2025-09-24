package com.example.demo.service;

import org.springframework.stereotype.Service;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import java.io.InputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Value;

@Service
public class PaymentProofOcrService {
    @Value("${tesseract.datapath:C:/Program Files/Tesseract-OCR/tessdata/}")
    private String tesseractDataPath;

    public PaymentProofData extractData(EmailAttachment attachment) {
        PaymentProofData data = new PaymentProofData();
        try {
            InputStream is = attachment.getInputStream();
            File tempFile = File.createTempFile("preuve", ".pdf");
            Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            // Vérification de signature numérique PDF
            try (PDDocument doc = PDDocument.load(tempFile)) {
                boolean hasSignature = !doc.getSignatureDictionaries().isEmpty();
                data.setHasDigitalSignature(hasSignature);
            }
            try {
                Tesseract tesseract = new Tesseract();
                tesseract.setDatapath(tesseractDataPath);
                tesseract.setLanguage("fra+eng");
                String ocrText = tesseract.doOCR(tempFile);
                // Nettoyage du texte OCR
                String cleanText = ocrText.replaceAll("[\r\t]+", " ").replaceAll(" +", " ").trim();
                data.setRawText(cleanText);
                // Extraction référence (plusieurs formats)
                String ref = null;
                java.util.regex.Pattern[] refPatterns = new java.util.regex.Pattern[] {
                    java.util.regex.Pattern.compile("REF-\\d+", java.util.regex.Pattern.CASE_INSENSITIVE),
                    java.util.regex.Pattern.compile("FACT-\\d+", java.util.regex.Pattern.CASE_INSENSITIVE),
                    java.util.regex.Pattern.compile("n[°oO]\\s*:?\\s*\\d+", java.util.regex.Pattern.CASE_INSENSITIVE),
                    java.util.regex.Pattern.compile("[Ff]acture\\s*:?\\s*\\d+")
                };
                for (java.util.regex.Pattern p : refPatterns) {
                    java.util.regex.Matcher m = p.matcher(cleanText);
                    if (m.find()) { ref = m.group(); break; }
                }
                if (ref != null) data.setReference(ref);
                // Extraction montant (supporte séparateur milliers, virgule ou point)
                String montant = null;
                java.util.regex.Pattern montantPattern = java.util.regex.Pattern.compile("(\u20AC|EUR)?\\s*([0-9]{1,3}(?:[ .][0-9]{3})*[.,][0-9]{2})");
                java.util.regex.Matcher montantMatcher = montantPattern.matcher(cleanText);
                if (montantMatcher.find()) {
                    montant = montantMatcher.group(2).replace(" ", "").replace(".", "").replace(",", ".");
                    try { data.setAmount(new java.math.BigDecimal(montant)); } catch (Exception ignore) {}
                }
                // Extraction date (plusieurs formats)
                java.util.regex.Pattern[] datePatterns = new java.util.regex.Pattern[] {
                    java.util.regex.Pattern.compile("(\\d{2}/\\d{2}/\\d{4})"),
                    java.util.regex.Pattern.compile("(\\d{4}-\\d{2}-\\d{2})")
                };
                for (java.util.regex.Pattern p : datePatterns) {
                    java.util.regex.Matcher m = p.matcher(cleanText);
                    if (m.find()) {
                        String dateStr = m.group(1);
                        java.time.LocalDate date = null;
                        try { date = java.time.LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")); }
                        catch (Exception e1) {
                            try { date = java.time.LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")); } catch (Exception e2) { }
                        }
                        if (date != null) { data.setPaymentDate(date); break; }
                    }
                }
                // Log debug
                System.out.println("[OCR] Texte extrait: " + cleanText);
                System.out.println("[OCR] Référence: " + data.getReference());
                System.out.println("[OCR] Montant: " + data.getAmount());
                System.out.println("[OCR] Date: " + data.getPaymentDate());
            } catch (UnsatisfiedLinkError | TesseractException nativeErr) {
                System.err.println("[OCR ERROR - Native] " + nativeErr.getMessage());
                data.setRawText("Erreur OCR native: " + nativeErr.getMessage());
            } catch (Exception ocrEx) {
                System.err.println("[OCR ERROR] " + ocrEx.getMessage());
                data.setRawText("Erreur OCR: " + ocrEx.getMessage());
            }
            tempFile.delete();
        } catch (Exception e) {
            data.setRawText("Erreur OCR: " + e.getMessage());
        }
        return data;
    }
} 