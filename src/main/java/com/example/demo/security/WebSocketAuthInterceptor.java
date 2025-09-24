package com.example.demo.security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.security.jwt.AuthTokenFilter;
import com.example.demo.service.UserService;
import com.example.demo.model.User;

import java.security.Principal;
import java.util.List;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Autowired
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private UserService userService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Authentification lors de la connexion WebSocket
            String token = getTokenFromHeaders(accessor);
            
            if (token != null) {
                try {
                    // Validation simplifiée pour WebSocket
                    String username = extractUsernameFromToken(token);
                    User user = userService.getUserByUsername(username);
                    
                    if (user != null) {
                        // Créer un principal pour l'utilisateur
                        Principal principal = new WebSocketUserPrincipal(user.getId(), user.getUsername());
                        accessor.setUser(principal);
                        
                        // Définir l'utilisateur comme en ligne
                        userService.setUserOnlineStatus(user.getId(), true);
                        
                        System.out.println("🔌 WebSocket connecté: " + user.getUsername());
                    }
                } catch (Exception e) {
                    System.err.println("❌ Erreur authentification WebSocket: " + e.getMessage());
                    return null; // Refuser la connexion
                }
            } else {
                System.err.println("❌ Token WebSocket invalide");
                return null; // Refuser la connexion
            }
        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            // Marquer l'utilisateur comme hors ligne lors de la déconnexion
            Principal user = accessor.getUser();
            if (user instanceof WebSocketUserPrincipal) {
                WebSocketUserPrincipal wsPrincipal = (WebSocketUserPrincipal) user;
                userService.setUserOnlineStatus(wsPrincipal.getUserId(), false);
                System.out.println("🔌 WebSocket déconnecté: " + wsPrincipal.getName());
            }
        }
        
        return message;
    }

    private String getTokenFromHeaders(StompHeaderAccessor accessor) {
        // Essayer de récupérer le token depuis les headers
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }
        
        // Essayer de récupérer le token depuis les paramètres de connexion
        List<String> tokenParams = accessor.getNativeHeader("token");
        if (tokenParams != null && !tokenParams.isEmpty()) {
            return tokenParams.get(0);
        }
        
        // Essayer de récupérer le token depuis l'URL de connexion WebSocket
        String connectUrl = accessor.getFirstNativeHeader("connect-url");
        if (connectUrl != null && connectUrl.contains("token=")) {
            String token = connectUrl.substring(connectUrl.indexOf("token=") + 6);
            if (token.contains("&")) {
                token = token.substring(0, token.indexOf("&"));
            }
            return token;
        }
        
        return null;
    }
    
    private String extractUsernameFromToken(String token) {
        // Extraction simplifiée du username depuis le token JWT
        // En production, utilisez une vraie validation JWT
        try {
            // Décoder le payload du JWT (partie centrale)
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                // Extraire le username du payload JSON
                if (payload.contains("\"sub\"")) {
                    int start = payload.indexOf("\"sub\":\"") + 7;
                    int end = payload.indexOf("\"", start);
                    return payload.substring(start, end);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur extraction username du token: " + e.getMessage());
        }
        return "anonymous";
    }

    // Classe interne pour représenter l'utilisateur WebSocket
    public static class WebSocketUserPrincipal implements Principal {
        private final String userId;
        private final String username;

        public WebSocketUserPrincipal(String userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        @Override
        public String getName() {
            return username;
        }

        public String getUserId() {
            return userId;
        }
    }
}
