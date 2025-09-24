package com.example.demo.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.signatures.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.time.format.DateTimeFormatter;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;

@Service
public class PaymentReceiptService {
    @Value("${pdf.signature.keystore}")
    private Resource keystoreResource;
    @Value("${pdf.signature.password}")
    private String keystorePassword;
    @Value("${pdf.signature.alias}")
    private String keystoreAlias;
    @Value("${pdf.signature.reason}")
    private String signatureReason;
    @Value("${pdf.signature.location}")
    private String signatureLocation;

    public byte[] generateReceipt(String invoiceId, com.example.demo.service.PaymentProofData proofData, String proofUri) {
        try {
            // 1. Générer le PDF du reçu
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Reçu de Paiement Officiel").setFontSize(18));
            document.add(new Paragraph("Référence facture : " + proofData.getReference()));
            document.add(new Paragraph("Montant payé : " + proofData.getAmount() + " DT"));
            document.add(new Paragraph("Date de paiement : " + (proofData.getPaymentDate() != null ? proofData.getPaymentDate().format(DateTimeFormatter.ISO_DATE) : "")));
            document.add(new Paragraph("Preuve archivée : " + proofUri));
            document.add(new Paragraph("Numéro unique : " + invoiceId));
            document.add(new Paragraph("Généré le : " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
            document.close();

            // 2. Charger le certificat de signature
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (InputStream ksStream = keystoreResource.getInputStream()) {
                ks.load(ksStream, keystorePassword.toCharArray());
            }
            PrivateKey pk = (PrivateKey) ks.getKey(keystoreAlias, keystorePassword.toCharArray());
            Certificate[] chain = ks.getCertificateChain(keystoreAlias);

            // 3. Signer le PDF (PAdES)
            ByteArrayInputStream pdfInput = new ByteArrayInputStream(baos.toByteArray());
            ByteArrayOutputStream signedBaos = new ByteArrayOutputStream();
            PdfReader reader = new PdfReader(pdfInput);
            PdfSigner signer = new PdfSigner(reader, signedBaos, new StampingProperties());

            // Apparence de la signature (invisible ici, peut être rendue visible)
            @SuppressWarnings("unused")
            PdfSignatureAppearance appearance = signer.getSignatureAppearance()
                .setReason(signatureReason)
                .setLocation(signatureLocation)
                .setReuseAppearance(false);
            signer.setFieldName("Signature1");

            IExternalSignature pks = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, null);
            IExternalDigest digest = new BouncyCastleDigest();
            signer.signDetached(digest, pks, chain, null, null, null, 0, PdfSigner.CryptoStandard.CADES);

            return signedBaos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
} 