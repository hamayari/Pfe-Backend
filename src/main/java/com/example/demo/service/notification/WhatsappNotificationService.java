package com.example.demo.service.notification;

import com.example.demo.model.Notification;
import org.springframework.stereotype.Service;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;

@Service
public class WhatsappNotificationService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.number}")
    private String twilioWhatsappNumber;

    @Async
    public void sendWhatsappNotification(String to, String message) {
        try {
            Twilio.init(accountSid, authToken);
            
            // Format WhatsApp number
            String whatsappTo = "whatsapp:" + to;
            String whatsappFrom = "whatsapp:" + twilioWhatsappNumber;
            
            Message.creator(
                new PhoneNumber(whatsappTo),
                new PhoneNumber(whatsappFrom),
                message
            ).create();
            
        } catch (Exception e) {
            // Log error but don't throw to prevent notification chain breaking
            System.err.println("Erreur WhatsApp: " + e.getMessage());
        }
    }

    public String formatNotificationForWhatsapp(Notification notification) {
        StringBuilder message = new StringBuilder();
        message.append("🔔 *").append(notification.getTitle()).append("*\n\n");
        message.append(notification.getMessage()).append("\n\n");
        
        if (notification.getMetadata() != null && !notification.getMetadata().isEmpty()) {
            message.append("*Détails:*\n");
            notification.getMetadata().forEach((key, value) -> 
                message.append("• ").append(key).append(": ").append(value).append("\n")
            );
        }
        
        return message.toString();
    }
}