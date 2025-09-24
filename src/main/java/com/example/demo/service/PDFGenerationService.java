package com.example.demo.service;

import com.example.demo.model.Invoice;
import com.example.demo.repository.InvoiceRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.io.image.ImageDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PDFGenerationService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    // Constantes de l'entreprise
    private static final String COMPANY_NAME = "GestionPro";
    private static final String COMPANY_ADDRESS = "123 Avenue Habib Bourguiba, 1000 Tunis, Tunisie";
    private static final String COMPANY_PHONE = "+216 71 123 456";
    private static final String COMPANY_EMAIL = "contact@gestionpro.tn";
    private static final String COMPANY_WEBSITE = "www.gestionpro.tn";
    private static final String FISCAL_ID = "12345678/A/M/000";
    private static final String BANK_IBAN = "TN59 1234 5678 9012 3456 7890";
    private static final String BANK_NAME = "Banque de Tunisie";

    public byte[] generateInvoicePDF(String invoiceId) throws IOException {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        System.out.println("📄 Génération PDF pour: " + invoice.getReference());
        
        // Version simplifiée pour test
        return generateSimpleInvoicePDF(invoice);
    }

    public byte[] generateConventionPDF(String conventionId) throws IOException {
        // Méthode temporaire pour les conventions
        System.out.println("📄 Génération PDF convention pour: " + conventionId);
        return new byte[0];
    }
    
    private byte[] generateSimpleInvoicePDF(Invoice invoice) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        // Marges personnalisées
        document.setMargins(40, 40, 40, 40);

        // === EN-TÊTE PROFESSIONNEL ===
        addSimpleHeader(document, invoice);

        // === INFORMATIONS CLIENT ===
        addSimpleClientSection(document, invoice);

        // === DÉTAILS DE LA FACTURE ===
        addSimpleInvoiceDetails(document, invoice);

        // === TABLEAU DES PRESTATIONS ===
        addSimpleServicesTable(document, invoice);

        // === TOTAL ET PAIEMENT ===
        addSimpleTotalSection(document, invoice);

        // === PIED DE PAGE ===
        addSimpleFooter(document);

        document.close();
        
        byte[] result = baos.toByteArray();
        System.out.println("✅ PDF professionnel généré - Taille: " + result.length + " bytes");
        return result;
    }
    
    private void addSimpleHeader(Document document, Invoice invoice) throws IOException {
        // En-tête avec logo GestionPro
        try {
            // Essayer de charger l'image du logo
            ClassPathResource logoResource = new ClassPathResource("static/images/logo.jpg");
            System.out.println("🔍 Recherche du logo: static/images/logo.jpg");
            System.out.println("🔍 Logo existe: " + logoResource.exists());
            
            if (logoResource.exists()) {
                System.out.println("✅ Logo trouvé, chargement de l'image...");
                Image logoImage = new Image(ImageDataFactory.create(logoResource.getURL()));
                logoImage.setWidth(200);
                logoImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
                document.add(logoImage);
                System.out.println("✅ Logo ajouté au PDF avec succès");
            } else {
                System.out.println("❌ Logo non trouvé, utilisation du texte");
                // Fallback: texte si l'image n'existe pas
                Paragraph logo = new Paragraph("GestionPro")
                        .setFontSize(28)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER);
                document.add(logo);
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur lors du chargement du logo: " + e.getMessage());
            e.printStackTrace();
            // Fallback: texte si erreur de chargement
            Paragraph logo = new Paragraph("GestionPro")
                    .setFontSize(28)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(logo);
        }
        
        Paragraph tagline = new Paragraph("Gestion Professionnelle & Solutions Digitales")
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(tagline);
        
        document.add(new Paragraph(" ").setFontSize(20)); // Espace
        
        // Titre de la facture
        Paragraph invoiceTitle = new Paragraph("FACTURE")
                .setFontSize(28)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(invoiceTitle);
        
        Paragraph invoiceNumber = new Paragraph("N° " + invoice.getReference())
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(invoiceNumber);
        
        document.add(new Paragraph(" ").setFontSize(20)); // Espace
    }
    
    private void addSimpleClientSection(Document document, Invoice invoice) {
        // Informations entreprise GestionPro
        Paragraph companyTitle = new Paragraph("ÉMIS PAR")
                .setFontSize(14)
                .setBold();
        document.add(companyTitle);
        
        Paragraph companyInfo = new Paragraph(COMPANY_NAME + "\n" + COMPANY_ADDRESS + "\nTél: " + COMPANY_PHONE + "\nEmail: " + COMPANY_EMAIL)
                .setFontSize(10);
        document.add(companyInfo);
        
        document.add(new Paragraph(" ").setFontSize(10)); // Espace
        
        // Client
        Paragraph clientTitle = new Paragraph("FACTURÉ À")
                .setFontSize(14)
                .setBold();
        document.add(clientTitle);

        Paragraph clientInfo = new Paragraph("Client Standard\nAdresse non spécifiée")
                .setFontSize(12);
        document.add(clientInfo);
        
        document.add(new Paragraph(" ").setFontSize(15)); // Espace
    }
    
    private void addSimpleInvoiceDetails(Document document, Invoice invoice) {
        Paragraph detailsTitle = new Paragraph("DÉTAILS DE LA FACTURE")
                .setFontSize(14)
                .setBold();
        document.add(detailsTitle);

        document.add(new Paragraph("Référence: " + invoice.getReference())
                .setFontSize(11));
        document.add(new Paragraph("Convention: " + (invoice.getConventionId() != null ? invoice.getConventionId() : "N/A"))
                .setFontSize(11));
        document.add(new Paragraph("Statut: " + invoice.getStatus())
                .setFontSize(11));
        document.add(new Paragraph("Créée par: " + (invoice.getCreatedBy() != null ? invoice.getCreatedBy() : "N/A"))
                .setFontSize(11));
        document.add(new Paragraph("Date d'émission: " + (invoice.getIssueDate() != null ? invoice.getIssueDate().toString() : "N/A"))
                .setFontSize(11));
        document.add(new Paragraph("Date d'échéance: " + (invoice.getDueDate() != null ? invoice.getDueDate().toString() : "N/A"))
                .setFontSize(11));
        
        document.add(new Paragraph(" ").setFontSize(15)); // Espace
    }
    
    private void addSimpleServicesTable(Document document, Invoice invoice) {
        Paragraph servicesTitle = new Paragraph("PRESTATIONS")
                .setFontSize(14)
                .setBold();
        document.add(servicesTitle);
        
        // Tableau simple
        Table table = new Table(4).useAllAvailableWidth();
        
        // En-têtes
        table.addHeaderCell(new Cell().add(new Paragraph("Description").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Quantité").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Prix unitaire").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Total").setBold()));
        
        // Ligne de service
        table.addCell(new Cell().add(new Paragraph("Prestation Convention " + invoice.getReference())));
        table.addCell(new Cell().add(new Paragraph("1")));
        table.addCell(new Cell().add(new Paragraph(invoice.getAmount() + " €")));
        table.addCell(new Cell().add(new Paragraph(invoice.getAmount() + " €")));
        
        // Totaux
        table.addCell(new Cell(1, 3).add(new Paragraph("TOTAL TTC").setBold()));
        table.addCell(new Cell().add(new Paragraph(invoice.getAmount() + " €").setBold()));
        
        document.add(table);
        document.add(new Paragraph(" ").setFontSize(20)); // Espace
    }
    
    private void addSimpleTotalSection(Document document, Invoice invoice) {
        Paragraph totalTitle = new Paragraph("TOTAL À PAYER")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(totalTitle);
        
        Paragraph totalAmount = new Paragraph(invoice.getAmount() + " €")
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(totalAmount);
        
        document.add(new Paragraph(" ").setFontSize(15)); // Espace
        
        Paragraph paymentInfo = new Paragraph("INFORMATIONS DE PAIEMENT")
                .setFontSize(12)
                .setBold();
        document.add(paymentInfo);
        
        document.add(new Paragraph("Banque: " + BANK_NAME)
                .setFontSize(10));
        document.add(new Paragraph("IBAN: " + BANK_IBAN)
                .setFontSize(10));
        document.add(new Paragraph("Merci pour votre confiance !")
                .setFontSize(10)
                .setItalic());
    }
    
    private void addSimpleFooter(Document document) {
        document.add(new Paragraph(" ").setFontSize(30)); // Espace
        
        Paragraph footer = new Paragraph("GestionPro - Gestion Professionnelle & Solutions Digitales")
        .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(footer);
        
        Paragraph contact = new Paragraph("Tél: " + COMPANY_PHONE + " | Email: " + COMPANY_EMAIL + " | Web: " + COMPANY_WEBSITE)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(contact);
    }
} 