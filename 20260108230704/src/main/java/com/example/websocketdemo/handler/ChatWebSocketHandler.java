package com.example.websocketdemo.handler;

import com.example.websocketdemo.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Chat WebSocket Handler
 * Handles chat room functionality with broadcast capabilities
 */
@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Store sessions by room: Map<roomName, Map<sessionId, WebSocketSession>>
    private final Map<String, Map<String, WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    // Store session info: Map<sessionId, userInfo>
    private final Map<String, UserInfo> sessionUsers = new ConcurrentHashMap<>();

    // Message ID generator
    private final AtomicLong messageIdGenerator = new AtomicLong(0);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Chat WebSocket connection established: sessionId={}, remoteAddress={}",
                session.getId(), session.getRemoteAddress());

        // Send welcome message
        ChatMessage welcomeMessage = ChatMessage.builder()
                .id("welcome-" + Instant.now().toEpochMilli())
                .content("Welcome to Chat WebSocket! Please join a room by sending a JOIN message.")
                .type(ChatMessage.MessageType.CHAT)
                .createdAt(Instant.now())
                .build();

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(welcomeMessage)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Chat WebSocket received message: sessionId={}, payload={}", session.getId(), payload);

        try {
            ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);

            // Validate message
            if (chatMessage.getContent() != null && chatMessage.getContent().length() > 1000) {
                sendError(session, "Message too large (max 1000 characters)");
                return;
            }

            handleChatMessage(session, chatMessage);
        } catch (Exception e) {
            log.error("Error parsing chat message: {}", e.getMessage(), e);
            sendError(session, "Invalid message format. Expected: {\"username\":\"name\",\"content\":\"message\",\"room\":\"room\",\"type\":\"CHAT\"}");
        }
    }

    private void handleChatMessage(WebSocketSession session, ChatMessage chatMessage) throws IOException {
        String sessionId = session.getId();

        switch (chatMessage.getType()) {
            case JOIN:
                handleJoin(session, chatMessage);
                break;

            case LEAVE:
                handleLeave(session, chatMessage);
                break;

            case CHAT:
                handleChat(session, chatMessage);
                break;

            default:
                sendError(session, "Unknown message type: " + chatMessage.getType());
        }
    }

    private void handleJoin(WebSocketSession session, ChatMessage chatMessage) throws IOException {
        String sessionId = session.getId();
        String room = chatMessage.getRoom();
        String username = chatMessage.getUsername();

        if (room == null || room.trim().isEmpty()) {
            sendError(session, "Room name is required for JOIN");
            return;
        }

        if (username == null || username.trim().isEmpty()) {
            sendError(session, "Username is required for JOIN");
            return;
        }

        // Remove from previous room if exists
        UserInfo existingUser = sessionUsers.get(sessionId);
        if (existingUser != null && existingUser.getRoom() != null) {
            leaveRoom(sessionId, existingUser.getRoom());
        }

        // Add to room
        roomSessions.computeIfAbsent(room, k -> new ConcurrentHashMap<>()).put(sessionId, session);
        sessionUsers.put(sessionId, new UserInfo(username, room));

        log.info("User joined room: sessionId={}, username={}, room={}", sessionId, username, room);

        // Send join notification to room
        ChatMessage joinMessage = ChatMessage.builder()
                .id(String.valueOf(messageIdGenerator.incrementAndGet()))
                .username(username)
                .content(username + " joined the room")
                .type(ChatMessage.MessageType.JOIN)
                .room(room)
                .createdAt(Instant.now())
                .build();

        broadcastToRoom(room, joinMessage);
    }

    private void handleLeave(WebSocketSession session, ChatMessage chatMessage) throws IOException {
        String sessionId = session.getId();
        UserInfo user = sessionUsers.get(sessionId);

        if (user == null) {
            sendError(session, "You are not in any room");
            return;
        }

        leaveRoom(sessionId, user.getRoom());
    }

    private void handleChat(WebSocketSession session, ChatMessage chatMessage) throws IOException {
        String sessionId = session.getId();
        UserInfo user = sessionUsers.get(sessionId);

        if (user == null) {
            sendError(session, "Please join a room first");
            return;
        }

        // Validate message content
        if (chatMessage.getContent() == null || chatMessage.getContent().trim().isEmpty()) {
            sendError(session, "Message content cannot be empty");
            return;
        }

        // Use stored username and room
        chatMessage.setUsername(user.getUsername());
        chatMessage.setRoom(user.getRoom());
        chatMessage.setId(String.valueOf(messageIdGenerator.incrementAndGet()));
        chatMessage.setCreatedAt(Instant.now());

        log.debug("Broadcasting chat message: room={}, username={}, content={}",
                chatMessage.getRoom(), chatMessage.getUsername(), chatMessage.getContent());

        // Broadcast to room
        broadcastToRoom(user.getRoom(), chatMessage);
    }

    private void leaveRoom(String sessionId, String room) throws IOException {
        Map<String, WebSocketSession> sessions = roomSessions.get(room);
        UserInfo user = sessionUsers.get(sessionId);

        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                roomSessions.remove(room);
            }
        }

        if (user != null) {
            log.info("User left room: sessionId={}, username={}, room={}", sessionId, user.getUsername(), room);

            // Send leave notification to room
            ChatMessage leaveMessage = ChatMessage.builder()
                    .id(String.valueOf(messageIdGenerator.incrementAndGet()))
                    .username(user.getUsername())
                    .content(user.getUsername() + " left the room")
                    .type(ChatMessage.MessageType.LEAVE)
                    .room(room)
                    .createdAt(Instant.now())
                    .build();

            broadcastToRoom(room, leaveMessage);
            sessionUsers.remove(sessionId);
        }
    }

    private void broadcastToRoom(String room, ChatMessage message) throws IOException {
        Map<String, WebSocketSession> sessions = roomSessions.get(room);

        if (sessions != null) {
            String messageJson = objectMapper.writeValueAsString(message);
            sessions.forEach((sessionId, session) -> {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(messageJson));
                    } catch (IOException e) {
                        log.error("Error sending message to session: sessionId={}, error={}",
                                sessionId, e.getMessage());
                    }
                }
            });
        }
    }

    private void sendError(WebSocketSession session, String errorMessage) throws IOException {
        ChatMessage error = ChatMessage.builder()
                .id("error-" + Instant.now().toEpochMilli())
                .content(errorMessage)
                .type(ChatMessage.MessageType.CHAT)
                .createdAt(Instant.now())
                .build();
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Chat WebSocket transport error: sessionId={}, error={}",
                session.getId(), exception.getMessage(), exception);

        // Remove from room on error
        String sessionId = session.getId();
        UserInfo user = sessionUsers.get(sessionId);
        if (user != null) {
            leaveRoom(sessionId, user.getRoom());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Chat WebSocket connection closed: sessionId={}, status={}",
                session.getId(), status);

        // Remove from room on close
        String sessionId = session.getId();
        UserInfo user = sessionUsers.get(sessionId);
        if (user != null) {
            leaveRoom(sessionId, user.getRoom());
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * User info class to store session metadata
     */
    private static class UserInfo {
        private final String username;
        private final String room;

        public UserInfo(String username, String room) {
            this.username = username;
            this.room = room;
        }

        public String getUsername() {
            return username;
        }

        public String getRoom() {
            return room;
        }
    }
}
