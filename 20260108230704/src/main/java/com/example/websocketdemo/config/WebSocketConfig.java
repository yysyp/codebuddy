package com.example.websocketdemo.config;

import com.example.websocketdemo.handler.ChatWebSocketHandler;
import com.example.websocketdemo.handler.EchoWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration class
 * Configures WebSocket endpoints and handlers
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final EchoWebSocketHandler echoWebSocketHandler;
    private final ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Register echo handler
        registry.addHandler(echoWebSocketHandler, "/ws/echo")
                .setAllowedOrigins("*");

        // Register chat handler with SockJS fallback
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}
