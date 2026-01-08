package com.example.websocketdemo.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Instant;

/**
 * Echo WebSocket Handler
 * Simple handler that echoes back any message received with a timestamp
 */
@Slf4j
@Component
public class EchoWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Echo WebSocket connection established: sessionId={}, remoteAddress={}",
                session.getId(), session.getRemoteAddress());

        // Send welcome message
        String welcomeMessage = String.format(
                "{\"type\":\"WELCOME\",\"sessionId\":\"%s\",\"timestamp\":\"%s\",\"message\":\"Connected to Echo WebSocket\"}",
                session.getId(),
                Instant.now()
        );
        session.sendMessage(new TextMessage(welcomeMessage));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Echo WebSocket received message: sessionId={}, payload={}", session.getId(), payload);

        // Validate message size
        if (payload.length() > 10000) {
            String errorMessage = String.format(
                    "{\"type\":\"ERROR\",\"message\":\"Message too large (max 10000 characters)\"}"
            );
            session.sendMessage(new TextMessage(errorMessage));
            return;
        }

        // Echo back with timestamp
        String echoMessage = String.format(
                "{\"type\":\"ECHO\",\"sessionId\":\"%s\",\"timestamp\":\"%s\",\"originalMessage\":\"%s\"}",
                session.getId(),
                Instant.now(),
                payload.replace("\"", "\\\"")
        );

        session.sendMessage(new TextMessage(echoMessage));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Echo WebSocket transport error: sessionId={}, error={}",
                session.getId(), exception.getMessage(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Echo WebSocket connection closed: sessionId={}, status={}",
                session.getId(), status);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
