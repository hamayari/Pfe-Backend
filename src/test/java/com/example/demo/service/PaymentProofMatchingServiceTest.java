package com.example.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaymentProofMatchingServiceTest {

    @InjectMocks
    private PaymentProofMatchingService service;

    private PaymentProofData mockData;

    @BeforeEach
    void setUp() {
        mockData = new PaymentProofData();
    }

    @Test
    void testMatchProofToInvoice_Success() {
        // When
        MatchingResult result = service.matchProofToInvoice(mockData);

        // Then
        assertNotNull(result);
        assertTrue(result.isValid());
        assertNotNull(result.getInvoiceId());
        assertTrue(result.getInvoiceId().startsWith("INV-"));
        assertTrue(result.getConfidence() > 0);
        assertTrue(result.getConfidence() <= 1.0);
    }

    @Test
    void testMatchProofToInvoice_GeneratesUniqueIds() throws InterruptedException {
        // When
        MatchingResult result1 = service.matchProofToInvoice(mockData);
        Thread.sleep(10); // Ensure different timestamps
        MatchingResult result2 = service.matchProofToInvoice(mockData);

        // Then
        assertNotNull(result1.getInvoiceId());
        assertNotNull(result2.getInvoiceId());
        assertNotEquals(result1.getInvoiceId(), result2.getInvoiceId());
    }

    @Test
    void testMatchProofToInvoice_HighConfidence() {
        // When
        MatchingResult result = service.matchProofToInvoice(mockData);

        // Then
        assertTrue(result.getConfidence() >= 0.8);
    }

    @Test
    void testMatchProofToInvoice_WithNullData() {
        // When
        MatchingResult result = service.matchProofToInvoice(null);

        // Then
        assertNotNull(result);
        assertTrue(result.isValid());
    }

    @Test
    void testMatchProofToInvoice_MultipleInvocations() {
        // When
        for (int i = 0; i < 10; i++) {
            MatchingResult result = service.matchProofToInvoice(mockData);
            
            // Then
            assertNotNull(result);
            assertTrue(result.isValid());
            assertNotNull(result.getInvoiceId());
        }
    }
}
