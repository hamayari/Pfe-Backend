package com.example.demo.service;

import com.example.demo.config.DocumentConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Service optimisé pour la génération de documents
 * Génération à la demande sans stockage en base
 * Cache intelligent et gestion des ressources
 */
@Service
public class OptimizedDocumentService {

    @Autowired
    private PDFGenerationService pdfGenerationService;

    @Autowired
    private ExcelExportService excelExportService;

    @Autowired
    private DocumentCacheService documentCacheService;

    @Autowired
    private DocumentConfig.DocumentLimits documentLimits;

    @Autowired
    private Executor documentExecutor;

    /**
     * Génère un PDF de facture avec cache intelligent
     */
    public byte[] generateInvoicePDFOptimized(String invoiceId) throws Exception {
        // Vérifier le cache d'abord
        String cacheKey = documentCacheService.generateCacheKey("invoice", invoiceId, "pdf");
        byte[] cachedContent = documentCacheService.getDocument(cacheKey);
        
        if (cachedContent != null) {
            return cachedContent;
        }
        
        // Générer le document
        byte[] pdfContent = pdfGenerationService.generateInvoicePDF(invoiceId);
        
        // Mettre en cache
        documentCacheService.putDocument(cacheKey, pdfContent, documentLimits.getCacheExpirationMinutes());
        
        return pdfContent;
    }

    /**
     * Génère un PDF de convention avec cache intelligent
     */
    public byte[] generateConventionPDFOptimized(String conventionId) throws Exception {
        // Vérifier le cache d'abord
        String cacheKey = documentCacheService.generateCacheKey("convention", conventionId, "pdf");
        byte[] cachedContent = documentCacheService.getDocument(cacheKey);
        
        if (cachedContent != null) {
            return cachedContent;
        }
        
        // Générer le document
        byte[] pdfContent = pdfGenerationService.generateConventionPDF(conventionId);
        
        // Mettre en cache
        documentCacheService.putDocument(cacheKey, pdfContent, documentLimits.getCacheExpirationMinutes());
        
        return pdfContent;
    }

    /**
     * Génère un Excel de factures avec cache intelligent
     */
    public byte[] generateInvoicesExcelOptimized(List<String> invoiceIds) throws Exception {
        // Vérifier les limites
        if (invoiceIds.size() > documentLimits.getMaxDocumentsPerRequest()) {
            throw new IllegalArgumentException("Trop de documents demandés. Maximum: " + documentLimits.getMaxDocumentsPerRequest());
        }
        
        // Vérifier le cache d'abord
        String cacheKey = documentCacheService.generateBulkCacheKey("invoices", invoiceIds, "excel");
        byte[] cachedContent = documentCacheService.getDocument(cacheKey);
        
        if (cachedContent != null) {
            return cachedContent;
        }
        
        // Générer le document
        byte[] excelContent = excelExportService.exportInvoicesToExcel(invoiceIds);
        
        // Vérifier la taille du fichier
        if (excelContent.length > documentLimits.getMaxFileSizeMB() * 1024 * 1024) {
            throw new IllegalArgumentException("Fichier trop volumineux. Maximum: " + documentLimits.getMaxFileSizeMB() + "MB");
        }
        
        // Mettre en cache
        documentCacheService.putDocument(cacheKey, excelContent, documentLimits.getCacheExpirationMinutes());
        
        return excelContent;
    }

    /**
     * Génère un Excel de conventions avec cache intelligent
     */
    public byte[] generateConventionsExcelOptimized(List<String> conventionIds) throws Exception {
        // Vérifier les limites
        if (conventionIds.size() > documentLimits.getMaxDocumentsPerRequest()) {
            throw new IllegalArgumentException("Trop de documents demandés. Maximum: " + documentLimits.getMaxDocumentsPerRequest());
        }
        
        // Vérifier le cache d'abord
        String cacheKey = documentCacheService.generateBulkCacheKey("conventions", conventionIds, "excel");
        byte[] cachedContent = documentCacheService.getDocument(cacheKey);
        
        if (cachedContent != null) {
            return cachedContent;
        }
        
        // Générer le document
        byte[] excelContent = excelExportService.exportConventionsToExcel(conventionIds);
        
        // Vérifier la taille du fichier
        if (excelContent.length > documentLimits.getMaxFileSizeMB() * 1024 * 1024) {
            throw new IllegalArgumentException("Fichier trop volumineux. Maximum: " + documentLimits.getMaxFileSizeMB() + "MB");
        }
        
        // Mettre en cache
        documentCacheService.putDocument(cacheKey, excelContent, documentLimits.getCacheExpirationMinutes());
        
        return excelContent;
    }

    /**
     * Génération asynchrone de PDF de facture
     */
    @Async("documentExecutor")
    public CompletableFuture<byte[]> generateInvoicePDFAsync(String invoiceId) {
        try {
            byte[] content = generateInvoicePDFOptimized(invoiceId);
            return CompletableFuture.completedFuture(content);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Génération asynchrone de PDF de convention
     */
    @Async("documentExecutor")
    public CompletableFuture<byte[]> generateConventionPDFAsync(String conventionId) {
        try {
            byte[] content = generateConventionPDFOptimized(conventionId);
            return CompletableFuture.completedFuture(content);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Génération asynchrone d'Excel de factures
     */
    @Async("documentExecutor")
    public CompletableFuture<byte[]> generateInvoicesExcelAsync(List<String> invoiceIds) {
        try {
            byte[] content = generateInvoicesExcelOptimized(invoiceIds);
            return CompletableFuture.completedFuture(content);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Génération asynchrone d'Excel de conventions
     */
    @Async("documentExecutor")
    public CompletableFuture<byte[]> generateConventionsExcelAsync(List<String> conventionIds) {
        try {
            byte[] content = generateConventionsExcelOptimized(conventionIds);
            return CompletableFuture.completedFuture(content);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Nettoie le cache des documents
     */
    public void clearCache() {
        documentCacheService.clearCache();
    }

    /**
     * Retourne les statistiques du cache
     */
    public int getCacheSize() {
        return documentCacheService.getCacheSize();
    }

    /**
     * Valide les paramètres d'entrée
     */
    public void validateRequest(List<String> ids, String type) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Liste d'IDs vide");
        }
        
        if (ids.size() > documentLimits.getMaxDocumentsPerRequest()) {
            throw new IllegalArgumentException("Trop de documents demandés. Maximum: " + documentLimits.getMaxDocumentsPerRequest());
        }
        
        if (!"invoices".equals(type) && !"conventions".equals(type)) {
            throw new IllegalArgumentException("Type de document invalide: " + type);
        }
    }
} 