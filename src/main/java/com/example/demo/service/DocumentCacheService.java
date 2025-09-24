package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Service de cache léger pour les documents générés
 * Évite la régénération fréquente des mêmes documents
 * Cache en mémoire avec expiration automatique
 */
@Service
public class DocumentCacheService {

    private final ConcurrentMap<String, CachedDocument> documentCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newScheduledThreadPool(1);

    public DocumentCacheService() {
        // Nettoyage automatique toutes les 30 minutes
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredDocuments, 30, 30, TimeUnit.MINUTES);
    }

    /**
     * Récupère un document du cache s'il existe et n'est pas expiré
     */
    public byte[] getDocument(String cacheKey) {
        CachedDocument cached = documentCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.getContent();
        }
        if (cached != null) {
            documentCache.remove(cacheKey);
        }
        return null;
    }

    /**
     * Stocke un document dans le cache avec expiration
     */
    public void putDocument(String cacheKey, byte[] content, long expirationMinutes) {
        CachedDocument document = new CachedDocument(content, System.currentTimeMillis() + (expirationMinutes * 60 * 1000));
        documentCache.put(cacheKey, document);
    }

    /**
     * Génère une clé de cache pour un document
     */
    public String generateCacheKey(String type, String id, String format) {
        return String.format("%s_%s_%s", type, id, format);
    }

    /**
     * Génère une clé de cache pour plusieurs documents
     */
    public String generateBulkCacheKey(String type, java.util.List<String> ids, String format) {
        String idsHash = String.join("_", ids);
        return String.format("bulk_%s_%s_%s", type, idsHash.hashCode(), format);
    }

    /**
     * Nettoie les documents expirés
     */
    private void cleanupExpiredDocuments() {
        documentCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * Vide complètement le cache
     */
    public void clearCache() {
        documentCache.clear();
    }

    /**
     * Retourne la taille actuelle du cache
     */
    public int getCacheSize() {
        return documentCache.size();
    }

    /**
     * Classe interne pour représenter un document en cache
     */
    private static class CachedDocument {
        private final byte[] content;
        private final long expirationTime;

        public CachedDocument(byte[] content, long expirationTime) {
            this.content = content;
            this.expirationTime = expirationTime;
        }

        public byte[] getContent() {
            return content;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }

    /**
     * Arrête proprement le service
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 