package com.example.demo.service;

import com.example.demo.dto.stripe.StripeWebhookEvent;
import com.example.demo.dto.stripe.StripePaymentIntent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

@Service
public class StripeWebhookService {
    
    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookService.class);
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Parse Stripe webhook payload using proper JSON structure
     * @param payload Raw JSON payload from Stripe
     * @return Parsed StripeWebhookEvent or empty if parsing fails
     */
    public Optional<StripeWebhookEvent> parseWebhookEvent(String payload) {
        try {
            StripeWebhookEvent event = objectMapper.readValue(payload, StripeWebhookEvent.class);
            logger.info("Successfully parsed Stripe webhook event: {}", event.getType());
            return Optional.of(event);
        } catch (Exception e) {
            logger.error("Failed to parse Stripe webhook payload: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Extract invoice ID from Stripe payment intent metadata
     * @param event Parsed Stripe webhook event
     * @return Invoice ID if found in metadata
     */
    public Optional<String> extractInvoiceId(StripeWebhookEvent event) {
        try {
            if (event.getData() != null && 
                event.getData().getObject() != null && 
                event.getData().getObject().getMetadata() != null) {
                
                Map<String, String> metadata = event.getData().getObject().getMetadata();
                String invoiceId = metadata.get("invoiceId");
                
                if (invoiceId != null && !invoiceId.trim().isEmpty()) {
                    logger.info("Extracted invoice ID from Stripe metadata: {}", invoiceId);
                    return Optional.of(invoiceId);
                }
            }
            
            logger.warn("No invoice ID found in Stripe webhook metadata");
            return Optional.empty();
            
        } catch (Exception e) {
            logger.error("Error extracting invoice ID from Stripe webhook: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Validate if the webhook event is a successful payment
     * @param event Parsed Stripe webhook event
     * @return true if it's a successful payment event
     */
    public boolean isSuccessfulPayment(StripeWebhookEvent event) {
        return "payment_intent.succeeded".equals(event.getType()) &&
               event.getData() != null &&
               event.getData().getObject() != null &&
               "succeeded".equals(event.getData().getObject().getStatus());
    }
    
    /**
     * Get payment amount from Stripe event
     * @param event Parsed Stripe webhook event
     * @return Payment amount in cents, or empty if not available
     */
    public Optional<Long> getPaymentAmount(StripeWebhookEvent event) {
        try {
            if (event.getData() != null && event.getData().getObject() != null) {
                StripePaymentIntent paymentIntent = event.getData().getObject();
                if (paymentIntent.getAmountReceived() != null && paymentIntent.getAmountReceived() > 0) {
                    return Optional.of(paymentIntent.getAmountReceived());
                } else if (paymentIntent.getAmount() != null) {
                    return Optional.of(paymentIntent.getAmount());
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error extracting payment amount from Stripe webhook: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Get payment currency from Stripe event
     * @param event Parsed Stripe webhook event
     * @return Payment currency, or empty if not available
     */
    public Optional<String> getPaymentCurrency(StripeWebhookEvent event) {
        try {
            if (event.getData() != null && 
                event.getData().getObject() != null && 
                event.getData().getObject().getCurrency() != null) {
                return Optional.of(event.getData().getObject().getCurrency());
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error extracting payment currency from Stripe webhook: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Get payment method from Stripe event
     * @param event Parsed Stripe webhook event
     * @return Payment method, or empty if not available
     */
    public Optional<String> getPaymentMethod(StripeWebhookEvent event) {
        try {
            if (event.getData() != null && 
                event.getData().getObject() != null && 
                event.getData().getObject().getPaymentMethod() != null) {
                return Optional.of(event.getData().getObject().getPaymentMethod());
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error extracting payment method from Stripe webhook: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Log detailed webhook information for debugging
     * @param event Parsed Stripe webhook event
     */
    public void logWebhookDetails(StripeWebhookEvent event) {
        logger.info("Stripe Webhook Details:");
        logger.info("  Event ID: {}", event.getId());
        logger.info("  Event Type: {}", event.getType());
        logger.info("  Created: {}", event.getCreated());
        logger.info("  Live Mode: {}", event.getLivemode());
        
        if (event.getData() != null && event.getData().getObject() != null) {
            StripePaymentIntent paymentIntent = event.getData().getObject();
            logger.info("  Payment Intent ID: {}", paymentIntent.getId());
            logger.info("  Amount: {}", paymentIntent.getAmount());
            logger.info("  Amount Received: {}", paymentIntent.getAmountReceived());
            logger.info("  Currency: {}", paymentIntent.getCurrency());
            logger.info("  Status: {}", paymentIntent.getStatus());
            logger.info("  Payment Method: {}", paymentIntent.getPaymentMethod());
            
            if (paymentIntent.getMetadata() != null) {
                logger.info("  Metadata: {}", paymentIntent.getMetadata());
            }
        }
    }
} 