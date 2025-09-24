package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.security.WebSocketAuthInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketMessagingConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Activer le broker simple pour les topics et queues
        config.enableSimpleBroker("/topic", "/queue", "/user");
        
        // Préfixe pour les messages de l'application
        config.setApplicationDestinationPrefixes("/app");
        
        // Préfixe pour les messages utilisateur spécifiques
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint WebSocket principal
        registry.addEndpoint("/ws-messaging")
                .setAllowedOriginPatterns("http://localhost:4200", "http://127.0.0.1:4200")
                .withSockJS();
        
        // Endpoint WebSocket sans SockJS pour les navigateurs modernes
        registry.addEndpoint("/ws-messaging-native")
                .setAllowedOriginPatterns("http://localhost:4200", "http://127.0.0.1:4200");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Intercepteur d'authentification pour WebSocket
        registration.interceptors(webSocketAuthInterceptor);
    }
}






