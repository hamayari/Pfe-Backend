package com.example.demo.service;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Utils;
import org.apache.http.HttpResponse;
import org.springframework.stereotype.Service;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Map;

@Service
public class PushNotificationService {
    // À générer une fois et stocker en base/config
    private static final String PUBLIC_KEY = "REPLACE_WITH_YOUR_PUBLIC_VAPID_KEY";
    private static final String PRIVATE_KEY = "REPLACE_WITH_YOUR_PRIVATE_VAPID_KEY";
    private static final String SUBJECT = "mailto:admin@votre-app.com";

    @SuppressWarnings("unchecked")
    public void sendPushNotification(Map<String, Object> subscription, String message) throws Exception {
        Map<String, String> keys = (Map<String, String>) subscription.get("keys");
        String endpoint = (String) subscription.get("endpoint");
        String p256dh = keys.get("p256dh");
        String auth = keys.get("auth");

        KeyPair keyPair = new KeyPair(
                Utils.loadPublicKey(PUBLIC_KEY),
                Utils.loadPrivateKey(PRIVATE_KEY)
        );
        PushService pushService = new PushService();
        pushService.setPublicKey((ECPublicKey) keyPair.getPublic());
        pushService.setPrivateKey((ECPrivateKey) keyPair.getPrivate());
        pushService.setSubject(SUBJECT);

        Notification notification = new Notification(
                endpoint,
                p256dh,
                auth,
                message.getBytes()
        );
        HttpResponse response = pushService.send(notification);
        System.out.println("Push sent: " + response.getStatusLine());
    }
} 