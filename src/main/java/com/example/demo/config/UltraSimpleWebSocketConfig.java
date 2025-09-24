package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.CloseStatus;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Configuration
@EnableWebSocket
public class UltraSimpleWebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new UltraSimpleWebSocketHandler(), "/ws-ultra-simple")
                .setAllowedOriginPatterns("*"); // Autoriser toutes les origines
    }

    public static class UltraSimpleWebSocketHandler extends TextWebSocketHandler {
        
        private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            String sessionId = session.getId();
            sessions.put(sessionId, session);
            System.out.println("🎉 WebSocket ULTRA-SIMPLE connecté: " + sessionId);
            
            // Envoyer immédiatement un message de confirmation
            String confirmationMessage = "{\"type\":\"connection\",\"status\":\"connected\",\"sessionId\":\"" + sessionId + "\",\"message\":\"Connexion réussie!\"}";
            session.sendMessage(new TextMessage(confirmationMessage));
            
            // Diffuser à tous les autres utilisateurs qu'un nouveau user est connecté
            broadcastMessage("{\"type\":\"user_joined\",\"sessionId\":\"" + sessionId + "\",\"message\":\"Nouvel utilisateur connecté\"}", sessionId);
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            String payload = message.getPayload();
            System.out.println("📨 Message reçu: " + payload);
            
            // Diffuser le message à tous les autres utilisateurs
            broadcastMessage(payload, session.getId());
        }

        private void broadcastMessage(String message, String excludeSessionId) {
            for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
                WebSocketSession otherSession = entry.getValue();
                if (otherSession.isOpen() && !otherSession.getId().equals(excludeSessionId)) {
                    try {
                        otherSession.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        System.err.println("❌ Erreur diffusion message: " + e.getMessage());
                    }
                }
            }
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            String sessionId = session.getId();
            sessions.remove(sessionId);
            System.out.println("🔌 WebSocket ULTRA-SIMPLE déconnecté: " + sessionId);
            
            // Informer les autres utilisateurs
            broadcastMessage("{\"type\":\"user_left\",\"sessionId\":\"" + sessionId + "\",\"message\":\"Utilisateur déconnecté\"}", sessionId);
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            System.err.println("❌ Erreur transport WebSocket: " + exception.getMessage());
            sessions.remove(session.getId());
        }
    }
}

