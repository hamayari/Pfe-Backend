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
public class SimpleWebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SimpleWebSocketHandler(), "/ws-simple")
                .setAllowedOriginPatterns("http://localhost:4200", "http://127.0.0.1:4200");
    }

    public static class SimpleWebSocketHandler extends TextWebSocketHandler {
        
        private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            String sessionId = session.getId();
            sessions.put(sessionId, session);
            System.out.println("🔌 WebSocket simple connecté: " + sessionId);
            
            // Envoyer un message de confirmation
            session.sendMessage(new TextMessage("{\"type\":\"connection\",\"status\":\"connected\",\"sessionId\":\"" + sessionId + "\"}"));
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            String payload = message.getPayload();
            System.out.println("📨 Message reçu: " + payload);
            
            // Diffuser le message à tous les autres utilisateurs
            for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
                WebSocketSession otherSession = entry.getValue();
                if (otherSession.isOpen() && !otherSession.getId().equals(session.getId())) {
                    try {
                        otherSession.sendMessage(new TextMessage(payload));
                    } catch (IOException e) {
                        System.err.println("❌ Erreur envoi message: " + e.getMessage());
                    }
                }
            }
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            String sessionId = session.getId();
            sessions.remove(sessionId);
            System.out.println("🔌 WebSocket simple déconnecté: " + sessionId);
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            System.err.println("❌ Erreur transport WebSocket: " + exception.getMessage());
            sessions.remove(session.getId());
        }
    }
}

