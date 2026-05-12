package com.tusas.vitrin.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration enabling STOMP messaging over WebSocket.
 *
 * <p>Two endpoints are registered:
 * <ul>
 *   <li>{@code /ws-native} — plain WebSocket (used by the Vue.js frontend)</li>
 *   <li>{@code /ws}        — WebSocket with SockJS fallback</li>
 * </ul>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configures a simple in-memory message broker for /topic destinations
     * and sets /app as the destination prefix for @MessageMapping methods.
     *
     * @param config the MessageBrokerRegistry to configure
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registers STOMP endpoints.
     * /ws-native supports native WebSocket clients (Vue.js frontend).
     * /ws supports SockJS fallback for environments where WebSocket is not available.
     *
     * @param registry the StompEndpointRegistry to register endpoints on
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
