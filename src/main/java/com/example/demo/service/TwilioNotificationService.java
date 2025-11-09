package com.example.demo.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class TwilioNotificationService {
    @Value("${twilio.account.sid}")
    private String accountSid;
    @Value("${twilio.auth.token}")
    private String authToken;
    @Value("${twilio.phone.number}")
    private String toNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void sendSms(String toNumber, String message) {
        try {
            System.out.println(" [TWILIO] Envoi SMS...");
            System.out.println(" [TWILIO] From: " + this.toNumber);
            System.out.println(" [TWILIO] To: " + toNumber);
            
            Message twilioMessage = Message.creator(
                new PhoneNumber(toNumber),        //Destinataire
                new PhoneNumber(this.toNumber),   // Expéditeur (votre numéro Twilio)
                message
            ).create();
            
            System.out.println(" [TWILIO] SMS envoyé - SID: " + twilioMessage.getSid());
        } catch (Exception e) {
            System.err.println(" [TWILIO] Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}