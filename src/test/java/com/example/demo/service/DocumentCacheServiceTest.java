package com.example.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class DocumentCacheServiceTest {

    private DocumentCacheService documentCacheService;

    @BeforeEach
    void setUp() {
        documentCacheService = new DocumentCacheService();
    }

    @Test
    void testPutAndGetDocument() {
        String cacheKey = "test_key";
        byte[] content = "Test content".getBytes();

        documentCacheService.putDocument(cacheKey, content, 10);
        byte[] retrieved = documentCacheService.getDocument(cacheKey);

        assertNotNull(retrieved);
        assertArrayEquals(content, retrieved);
    }

    @Test
    void testGetDocument_NotFound() {
        byte[] result = documentCacheService.getDocument("non_existent_key");

        assertNull(result);
    }

    @Test
    void testGenerateCacheKey() {
        String cacheKey = documentCacheService.generateCacheKey("invoice", "123", "pdf");

        assertNotNull(cacheKey);
        assertEquals("invoice_123_pdf", cacheKey);
    }

    @Test
    void testGenerateBulkCacheKey() {
        String cacheKey = documentCacheService.generateBulkCacheKey("invoice", Arrays.asList("1", "2", "3"), "pdf");

        assertNotNull(cacheKey);
        assertTrue(cacheKey.startsWith("bulk_invoice_"));
    }

    @Test
    void testClearCache() {
        documentCacheService.putDocument("key1", "content1".getBytes(), 10);
        documentCacheService.putDocument("key2", "content2".getBytes(), 10);

        assertEquals(2, documentCacheService.getCacheSize());

        documentCacheService.clearCache();

        assertEquals(0, documentCacheService.getCacheSize());
    }

    @Test
    void testGetCacheSize() {
        assertEquals(0, documentCacheService.getCacheSize());

        documentCacheService.putDocument("key1", "content1".getBytes(), 10);
        assertEquals(1, documentCacheService.getCacheSize());

        documentCacheService.putDocument("key2", "content2".getBytes(), 10);
        assertEquals(2, documentCacheService.getCacheSize());
    }

    @Test
    void testExpiredDocument() throws InterruptedException {
        String cacheKey = "test_key";
        byte[] content = "Test content".getBytes();

        // Mettre en cache avec expiration très courte (1 milliseconde)
        documentCacheService.putDocument(cacheKey, content, 0);
        
        // Attendre que le document expire
        Thread.sleep(100);

        byte[] retrieved = documentCacheService.getDocument(cacheKey);

        assertNull(retrieved);
    }
}
