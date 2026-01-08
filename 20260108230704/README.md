# WebSocket Demo Application

A comprehensive Spring Boot 3 application demonstrating WebSocket usage with real-time bidirectional communication.

## Features

- **Echo WebSocket**: Simple echo server that returns messages with timestamps
- **Chat Room WebSocket**: Multi-room chat functionality with broadcast capabilities
- **Concurrent Connection Support**: Handles multiple WebSocket connections simultaneously
- **Security**: CSRF protection, XSS protection, Content Security Policy
- **Observability**: TraceId for request tracing, structured logging
- **Resilience**: Circuit breaker and rate limiting patterns
- **API Documentation**: Swagger/OpenAPI v3 integration
- **Health Monitoring**: Spring Boot Actuator endpoints

## Technology Stack

- **Java 17**
- **Spring Boot 3.3.0**
- **Spring WebSocket**
- **Spring Security**
- **Resilience4j** (Circuit breaker, rate limiting)
- **Lombok**
- **Swagger/OpenAPI v3**
- **Spring Boot Actuator**

## Project Structure

```
websocket-demo/
├── src/
│   ├── main/
│   │   ├── java/com/example/websocketdemo/
│   │   │   ├── config/
│   │   │   │   ├── TraceIdFilter.java
│   │   │   │   ├── WebMvcConfig.java
│   │   │   │   ├── WebSecurityConfig.java
│   │   │   │   └── WebSocketConfig.java
│   │   │   ├── controller/
│   │   │   │   └── MessageController.java
│   │   │   ├── dto/
│   │   │   │   ├── MessageRequest.java
│   │   │   │   └── MessageResponse.java
│   │   │   ├── handler/
│   │   │   │   ├── ChatWebSocketHandler.java
│   │   │   │   └── EchoWebSocketHandler.java
│   │   │   ├── model/
│   │   │   │   └── ChatMessage.java
│   │   │   └── WebsocketDemoApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       │   └── index.html
│   │       └── application.yml
│   └── test/
├── pom.xml
└── README.md
```

## Prerequisites

- JDK 17 or higher
- Maven 3.6 or higher

## Installation

1. Clone or download the project
2. Navigate to the project directory
3. Build the project:

```bash
mvn clean install
```

## Running the Application

### Using Maven

```bash
mvn spring-boot:run
```

### Using Java JAR

```bash
mvn package
java -jar target/websocket-demo-1.0.0.jar
```

The application will start on `http://localhost:8080`

## WebSocket Endpoints

### Echo WebSocket

**URL**: `ws://localhost:8080/ws/echo`

**Description**: Simple echo server that returns any message received with a timestamp.

**Example Message Flow**:
```
Client -> Server: "Hello, WebSocket!"
Server -> Client: {"type":"ECHO","sessionId":"...","timestamp":"2026-01-08T12:00:00Z","originalMessage":"Hello, WebSocket!"}
```

### Chat WebSocket

**URL**: `ws://localhost:8080/ws/chat`

**Description**: Multi-room chat functionality with real-time broadcast to all users in the same room.

**Message Types**:

#### JOIN Message
Join a chat room.

```json
{
  "username": "john_doe",
  "room": "general",
  "type": "JOIN",
  "content": ""
}
```

#### LEAVE Message
Leave the current chat room.

```json
{
  "username": "john_doe",
  "room": "",
  "type": "LEAVE",
  "content": ""
}
```

#### CHAT Message
Send a chat message to the current room.

```json
{
  "username": "john_doe",
  "room": "",
  "type": "CHAT",
  "content": "Hello, everyone!"
}
```

**Note**: After joining a room, the server tracks your username and room. For CHAT messages, you only need to provide the content; the username and room will be filled automatically.

## REST API Endpoints

### Health Check

**GET** `/api/health`

Check application health status.

**Response**:
```json
{
  "status": "UP",
  "timestamp": 1704691200000,
  "application": "websocket-demo"
}
```

### WebSocket Information

**GET** `/api/websocket/info`

Get information about available WebSocket endpoints.

**Response**:
```json
{
  "echoEndpoint": "/ws/echo",
  "chatEndpoint": "/ws/chat",
  "description": "WebSocket Demo Application",
  "echoDescription": "Simple echo server that returns messages with timestamps",
  "chatDescription": "Chat room functionality with support for multiple rooms"
}
```

## Web Interface

Open your browser and navigate to `http://localhost:8080`

The web interface provides:
- **Echo Demo Tab**: Test the echo WebSocket functionality
- **Chat Room Tab**: Test multi-room chat functionality

### Using the Echo Demo

1. Click "Connect" to connect to the Echo WebSocket
2. Type a message in the textarea
3. Click "Send Message" or press Enter
4. View the echoed response with timestamp

### Using the Chat Room Demo

1. Click "Connect" to connect to the Chat WebSocket
2. Enter your username
3. Enter a room name (e.g., "general", "random")
4. Click "Join Room"
5. Type messages and click "Send Message"
6. Messages are broadcast to all users in the same room

## API Documentation

Swagger UI is available at: `http://localhost:8080/swagger-ui.html`

OpenAPI documentation is available at: `http://localhost:8080/api-docs`

## Monitoring

### Actuator Endpoints

- Health: `http://localhost:8080/actuator/health`
- Info: `http://localhost:8080/actuator/info`
- Metrics: `http://localhost:8080/actuator/metrics`
- Prometheus: `http://localhost:8080/actuator/prometheus`

## Security Features

- **CSRF Protection**: Configured for WebSocket endpoints
- **XSS Protection**: Content Security Policy and XSS headers
- **Rate Limiting**: Resilience4j rate limiter (100 requests per second)
- **Circuit Breaker**: Resilience4j circuit breaker for fault tolerance
- **Input Validation**: Message size limits and content validation

## Observability

### TraceId

All HTTP requests include a `X-Trace-Id` header for distributed tracing.

### Logging

Logs include TraceId and SpanId for request correlation:

```
2026-01-08 12:00:00.123 [abc123,def456] [http-nio-8080-exec-1] DEBUG c.e.w.h.EchoWebSocketHandler - Echo WebSocket received message: sessionId=xxx, payload=xxx
```

## Configuration

The application configuration is in `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  application:
    name: websocket-demo

# WebSocket endpoints are configured in WebSocketConfig.java
# Resilience4j configuration for rate limiting and circuit breaking
# Logging configuration for observability
```

## Development

### Adding New WebSocket Handlers

1. Create a new handler class extending `TextWebSocketHandler`
2. Implement required methods: `afterConnectionEstablished`, `handleTextMessage`, `afterConnectionClosed`
3. Register the handler in `WebSocketConfig.java`

Example:
```java
@Component
public class MyCustomHandler extends TextWebSocketHandler {
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Handle messages
    }
}
```

## Testing

### Unit Tests

```bash
mvn test
```

### Manual Testing

1. Use the web interface at `http://localhost:8080`
2. Use WebSocket testing tools like:
   - [WebSocket King](https://websocketking.com/)
   - [Postman](https://www.postman.com/)
   - Browser console: `new WebSocket('ws://localhost:8080/ws/echo')`

## Troubleshooting

### Connection Refused

Ensure the application is running and the port 8080 is not blocked by a firewall.

### WebSocket Connection Fails

Check that your browser supports WebSocket (modern browsers do). Also verify that CORS settings allow connections from your origin.

### Messages Not Appearing

Verify that you have joined a room (for chat WebSocket) and that the message format is correct JSON.

## Performance Considerations

- The application uses `ConcurrentHashMap` for thread-safe session management
- WebSocket handlers are stateless and thread-safe
- Rate limiting prevents abuse
- Circuit breaker prevents cascading failures

## Multi-Instance Deployment

For multi-instance deployment, consider using:
- Redis Pub/Sub for cross-instance message broadcasting
- Sticky sessions to maintain WebSocket connections
- External session storage for connection metadata

## License

This project is created for demonstration purposes.

## Support

For issues or questions, please check:
- Application logs in the console
- Actuator health endpoint: `http://localhost:8080/actuator/health`
- Swagger documentation: `http://localhost:8080/swagger-ui.html`
