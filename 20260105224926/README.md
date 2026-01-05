# Spring Boot Server-Sent Events (SSE) Demo

A comprehensive demonstration of Server-Sent Events (SSE) implementation using Spring Boot 3 and Java 17. This project showcases real-time event streaming from server to client with a robust, production-ready architecture.

## 🌟 Features

- **Real-time Event Streaming**: Server-to-client push notifications using SSE protocol
- **Multi-Client Support**: Handle multiple simultaneous connections with thread-safe implementation
- **Automatic Reconnection**: Built-in reconnection support with configurable retry delays
- **Heartbeat Mechanism**: Keep-alive heartbeats to prevent connection timeouts
- **Event Broadcasting**: Send events to all connected clients or specific clients
- **Multiple Event Types**: Support for notifications, metrics, alerts, and custom events
- **Interactive Dashboard**: Beautiful web-based dashboard to visualize real-time events
- **REST API**: Complete REST API for managing SSE connections and broadcasting events
- **Health Monitoring**: Built-in health checks and connection statistics
- **Configurable**: Flexible configuration via application.yml

## 📋 Prerequisites

- **Java 17** or higher (OpenJDK, Oracle JDK, or Azul Zulu recommended)
- **Maven 3.6+** for building the project
- **Modern web browser** (Chrome, Firefox, Safari, Edge) for the dashboard

## 🚀 Quick Start

### 1. Clone or Download the Project

```bash
git clone <repository-url>
cd sse-demo
```

### 2. Build the Project

```bash
mvn clean package -DskipTests
```

This will compile the code and create an executable JAR file in the `target/` directory.

### 3. Run the Application

**Option A: Using Maven**
```bash
mvn spring-boot:run
```

**Option B: Using Java**
```bash
java -jar target/sse-demo-1.0.0.jar
```

The application will start on `http://localhost:8080/sse-demo`

### 4. Access the Dashboard

Open your web browser and navigate to:
```
http://localhost:8080/sse-demo/
```

You'll see an interactive dashboard where you can:
- Connect to SSE stream
- View real-time events
- Monitor connection status
- See system metrics and notifications

## 📁 Project Structure

```
sse-demo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/ssedemo/
│   │   │       ├── SseDemoApplication.java      # Main application class
│   │   │       ├── config/
│   │   │       │   └── SseConfig.java           # Configuration and CORS setup
│   │   │       ├── controller/
│   │   │       │   └── SseController.java       # REST API endpoints
│   │   │       ├── model/
│   │   │       │   └── SseEvent.java            # Event domain model
│   │   │       └── service/
│   │   │           ├── SseEmitterService.java    # SSE connection management
│   │   │           └── EventSimulationService.java # Event generation
│   │   └── resources/
│   │       ├── application.yml                  # Application configuration
│   │       └── static/
│   │           └── index.html                   # Web dashboard
├── scripts/
│   ├── test-sse.sh                              # Linux/Mac test script
│   ├── test-sse.bat                             # Windows test script
│   ├── generate-test-data.json                  # Sample test events
│   └── broadcast-test-events.py                 # Python event broadcaster
├── pom.xml                                      # Maven configuration
└── README.md                                    # This file
```

## 🔧 Configuration

Edit `src/main/resources/application.yml` to customize:

```yaml
server:
  port: 8080                                    # Server port
  servlet:
    context-path: /sse-demo                      # Context path

sse:
  timeout:
    heartbeat: 30000                            # Heartbeat interval (ms)
  retry:
    delay: 1000                                 # Reconnection delay (ms)

logging:
  level:
    com.example.ssedemo: DEBUG                  # Logging level
```

## 📡 API Endpoints

### SSE Endpoints

#### Subscribe to Events
```
GET /api/sse/subscribe
```

**Parameters:**
- `clientId` (optional): Unique client identifier (auto-generated if not provided)
- `timeout` (optional): Connection timeout in milliseconds (default: 30 minutes)

**Response:** Server-Sent Events stream

**Example:**
```bash
curl -N -H "Accept: text/event-stream" \
  "http://localhost:8080/sse-demo/api/sse/subscribe?clientId=my-client"
```

### REST API Endpoints

#### Health Check
```
GET /api/sse/health
```

**Response:**
```json
{
  "status": "UP",
  "service": "SSE Demo",
  "activeConnections": "3"
}
```

#### Get Statistics
```
GET /api/sse/stats
```

**Response:**
```json
{
  "activeConnections": 3,
  "timestamp": 1736140800000,
  "status": "running"
}
```

#### Broadcast Custom Event
```
POST /api/sse/broadcast
Content-Type: application/json
```

**Request Body:**
```json
{
  "eventType": "CUSTOM",
  "message": "Test event message",
  "data": {
    "key": "value",
    "timestamp": "2024-01-05T10:30:00"
  }
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Event broadcasted to all clients"
}
```

#### Disconnect Client
```
DELETE /api/sse/disconnect/{clientId}
```

**Response:**
```json
{
  "status": "success",
  "message": "Client disconnected: client-abc123"
}
```

## 🎨 Event Types

The demo automatically generates several types of events:

### 1. NOTIFICATION Events
- Occurs every 10 seconds
- Various severity levels: INFO, WARNING, SUCCESS, ALERT
- System messages about operations

**Example:**
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "NOTIFICATION",
  "message": "[INFO] System health check completed ( #1 )",
  "data": {
    "severity": "INFO",
    "counter": 1,
    "source": "system"
  },
  "timestamp": "2024-01-05T10:30:00"
}
```

### 2. METRICS Events
- Occurs every 15 seconds
- System performance metrics

**Example:**
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440001",
  "eventType": "METRICS",
  "message": "System metrics update",
  "data": {
    "cpuUsage": 45,
    "memoryUsage": 62,
    "activeConnections": 5,
    "diskUsage": 55,
    "networkIn": 723,
    "networkOut": 456
  },
  "timestamp": "2024-01-05T10:30:00"
}
```

### 3. TIME Events
- Occurs every second
- Real-time clock updates

**Example:**
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440002",
  "eventType": "TIME",
  "message": "Time update",
  "data": {
    "currentTime": "2024-01-05T10:30:00.123",
    "timestamp": 1736140800123,
    "activeClients": 5
  },
  "timestamp": "2024-01-05T10:30:00.123"
}
```

### 4. ALERT Events
- Occurs randomly (every 30-60 seconds)
- High-priority system alerts

**Example:**
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440003",
  "eventType": "ALERT",
  "message": "Alert: High traffic detected at 2024-01-05T10:30:00",
  "data": {
    "priority": 2,
    "source": "monitoring-system",
    "autoResolvable": true
  },
  "timestamp": "2024-01-05T10:30:00"
}
```

### 5. HEARTBEAT Events
- Occurs every 15 seconds
- Keeps connections alive

### 6. CONNECTION Events
- Sent when client connects
- Confirms successful connection

## 🧪 Testing

### Using the Web Dashboard

1. Open `http://localhost:8080/sse-demo/` in your browser
2. Click the "Connect" button
3. Watch real-time events stream in
4. Use controls to disconnect or clear events

### Using Test Scripts

#### Windows
```bash
cd scripts
test-sse.bat
```

#### Linux/Mac
```bash
cd scripts
chmod +x test-sse.sh
./test-sse.sh
```

#### Python Event Broadcaster
```bash
cd scripts
pip install requests  # First time only
python broadcast-test-events.py
```

### Using curl

**Subscribe to events (Linux/Mac):**
```bash
curl -N -H "Accept: text/event-stream" \
  "http://localhost:8080/sse-demo/api/sse/subscribe?clientId=test-client"
```

**Subscribe to events (Windows PowerShell):**
```powershell
curl -N -H "Accept: text/event-stream" `
  "http://localhost:8080/sse-demo/api/sse/subscribe?clientId=test-client"
```

**Broadcast custom event:**
```bash
curl -X POST http://localhost:8080/sse-demo/api/sse/broadcast \
  -H "Content-Type: application/json" \
  -d '{"eventType":"TEST","message":"Custom test","data":{"source":"curl"}}'
```

**Get health status:**
```bash
curl http://localhost:8080/sse-demo/api/sse/health
```

### Using JavaScript

```javascript
// Connect to SSE stream
const eventSource = new EventSource(
  'http://localhost:8080/sse-demo/api/sse/subscribe?clientId=js-client'
);

// Handle connection open
eventSource.onopen = function() {
  console.log('SSE Connection opened');
};

// Handle incoming messages
eventSource.onmessage = function(event) {
  const data = JSON.parse(event.data);
  console.log('Event:', data.eventType, data.message);
};

// Handle specific event types
eventSource.addEventListener('NOTIFICATION', function(event) {
  const data = JSON.parse(event.data);
  console.log('Notification:', data);
});

// Handle errors
eventSource.onerror = function(error) {
  console.error('SSE Error:', error);
};

// Close connection when done
eventSource.close();
```

## 🔍 Monitoring

### Health Check Endpoint
```bash
curl http://localhost:8080/sse-demo/api/sse/health
```

### Spring Boot Actuator
The application includes Spring Boot Actuator for monitoring:

- **Health**: `http://localhost:8080/sse-demo/actuator/health`
- **Info**: `http://localhost:8080/sse-demo/actuator/info`
- **Metrics**: `http://localhost:8080/sse-demo/actuator/metrics`

### Logs

View application logs in the console or check:
```bash
# If running with log redirection
tail -f app.log
```

## 🏗️ Architecture

### Key Components

1. **SseController**: REST API endpoints for SSE subscriptions and event management
2. **SseEmitterService**: Manages SSE connections, broadcasts, and heartbeats
3. **EventSimulationService**: Generates simulated real-time events
4. **SseEvent**: Domain model for event data
5. **SseConfig**: Configuration for scheduling and CORS

### Design Patterns

- **Singleton Service Pattern**: Services are Spring-managed singletons
- **Observer Pattern**: SSE clients observe server events
- **Scheduled Tasks**: Background event generation using Spring's `@Scheduled`
- **Thread Safety**: `ConcurrentHashMap` for managing multiple clients

### Data Flow

```
EventSimulationService
       ↓ (generates events)
SseEmitterService
       ↓ (broadcasts to all emitters)
SseEmitter (per client)
       ↓ (SSE stream)
Client (Browser, curl, etc.)
```

## 🛡️ Security Considerations

This is a demo application. For production use, consider:

1. **Authentication**: Add Spring Security for user authentication
2. **Authorization**: Implement role-based access control
3. **TLS/SSL**: Enable HTTPS for secure communication
4. **Rate Limiting**: Prevent abuse with rate limiting
5. **Input Validation**: Validate all incoming event data
6. **CORS**: Configure CORS appropriately for your use case
7. **Connection Limits**: Set maximum concurrent connections

## 🚦 Troubleshooting

### Port 8080 Already in Use

**Error:** `Web server failed to start. Port 8080 was already in use.`

**Solution:** Change the port in `application.yml` or stop the process using port 8080:

**Windows:**
```bash
netstat -ano | findstr :8080
taskkill /F /PID <PID>
```

**Linux/Mac:**
```bash
lsof -i :8080
kill -9 <PID>
```

### Connection Timeout

**Issue:** Clients disconnecting unexpectedly

**Solution:** Increase timeout values in `application.yml`:
```yaml
sse:
  timeout:
    heartbeat: 60000  # Increase heartbeat interval
```

### No Events Received

**Check:**
1. Application is running: `curl http://localhost:8080/sse-demo/api/sse/health`
2. Client is connected (check browser console)
3. Firewall isn't blocking connections

### Build Errors

**Issue:** Maven compilation fails

**Solution:**
```bash
# Clean and rebuild
mvn clean
mvn install -U
```

## 📚 Technical Details

### Technology Stack

- **Java**: 17+
- **Spring Boot**: 3.2.0
- **Spring Framework**: 6.1.1
- **Maven**: 3.9.2
- **Lombok**: For reducing boilerplate code
- **Jackson**: For JSON processing
- **Tomcat**: Embedded web server

### SSE Protocol

Server-Sent Events is a standard allowing servers to push data to web clients over HTTP. Key features:

- **One-way communication**: Server → Client only
- **Automatic reconnection**: Built-in reconnection support
- **Event types**: Support for typed events
- **Text-based**: Uses plain text/JSON data
- **Browser support**: Native support in all modern browsers

### Thread Safety

The implementation uses:
- `ConcurrentHashMap` for thread-safe client management
- `ScheduledExecutorService` for heartbeats
- Spring's `@Async` for asynchronous operations

## 🎯 Use Cases

This demo can be adapted for:

1. **Real-time Notifications**: Push notifications to web clients
2. **Live Dashboards**: Update charts and metrics in real-time
3. **Chat Applications**: Broadcast messages to connected users
4. **Monitoring Systems**: Push alerts and system updates
5. **Collaborative Tools**: Real-time collaboration features
6. **Stock Prices**: Live market data updates
7. **Sports Scores**: Real-time score updates
8. **IoT Monitoring**: Push sensor data to dashboards

## 📖 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Server-Sent Events MDN](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events)
- [Spring MVC SSE Guide](https://spring.io/guides/gs/messaging-stomp-websocket/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

## 🤝 Contributing

This is a demonstration project. Feel free to:
- Fork and modify for your needs
- Submit issues and feature requests
- Share improvements and optimizations

## 📄 License

This demo is provided as-is for educational purposes.

## 📞 Support

For questions or issues:
1. Check the troubleshooting section
2. Review the API documentation
3. Examine the code comments
4. Test using the provided scripts

---

**Happy coding! 🚀**

Built with ❤️ using Spring Boot 3 and Java 17
