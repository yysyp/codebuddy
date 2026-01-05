package com.example.ssedemo.service;

import com.example.ssedemo.model.SseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service to generate and broadcast simulated real-time events
 * Demonstrates different types of SSE events: notifications, metrics, alerts
 */
@Slf4j
@Service
public class EventSimulationService {

    private final SseEmitterService emitterService;
    private final Random random = new Random();
    private final AtomicInteger notificationCounter = new AtomicInteger(0);

    private static final String[] NOTIFICATION_TYPES = {
        "INFO", "WARNING", "SUCCESS", "ALERT"
    };

    private static final String[] SYSTEM_MESSAGES = {
        "System health check completed",
        "Database backup finished",
        "New user registered",
        "Security scan initiated",
        "Cache cleared successfully",
        "API rate limit warning",
        "Deployment completed",
        "Service restart required"
    };

    public EventSimulationService(SseEmitterService emitterService) {
        this.emitterService = emitterService;
    }

    /**
     * Broadcast random notifications every 10 seconds
     */
    @Scheduled(fixedRate = 10000)
    public void broadcastRandomNotification() {
        String type = NOTIFICATION_TYPES[random.nextInt(NOTIFICATION_TYPES.length)];
        String message = SYSTEM_MESSAGES[random.nextInt(SYSTEM_MESSAGES.length)];
        int counter = notificationCounter.incrementAndGet();

        Map<String, Object> data = new HashMap<>();
        data.put("severity", type);
        data.put("counter", counter);
        data.put("source", "system");

        SseEvent event = SseEvent.create("NOTIFICATION", 
            String.format("[%s] %s ( #%d )", type, message, counter), data);
        
        emitterService.broadcast(event);
        log.info("Broadcasted notification: {}", message);
    }

    /**
     * Broadcast system metrics every 15 seconds
     */
    @Scheduled(fixedRate = 15000)
    public void broadcastSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cpuUsage", 20 + random.nextInt(60));
        metrics.put("memoryUsage", 30 + random.nextInt(50));
        metrics.put("activeConnections", emitterService.getActiveConnectionsCount());
        metrics.put("diskUsage", 40 + random.nextInt(40));
        metrics.put("networkIn", random.nextInt(1000));
        metrics.put("networkOut", random.nextInt(800));

        SseEvent event = SseEvent.create("METRICS", 
            "System metrics update", metrics);
        
        emitterService.broadcast(event);
        log.debug("Broadcasted system metrics");
    }

    /**
     * Broadcast time updates every second
     */
    @Scheduled(fixedRate = 1000)
    public void broadcastTimeUpdate() {
        Map<String, Object> timeData = new HashMap<>();
        timeData.put("currentTime", LocalDateTime.now().toString());
        timeData.put("timestamp", System.currentTimeMillis());
        timeData.put("activeClients", emitterService.getActiveConnectionsCount());

        SseEvent event = SseEvent.create("TIME", 
            "Time update", timeData);
        
        emitterService.broadcast(event);
    }

    /**
     * Broadcast alert events randomly (approx every 30-60 seconds)
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    public void broadcastRandomAlert() {
        if (random.nextDouble() > 0.5) {
            Map<String, Object> alertData = new HashMap<>();
            alertData.put("priority", random.nextInt(3) + 1);
            alertData.put("source", "monitoring-system");
            alertData.put("autoResolvable", random.nextBoolean());

            SseEvent event = SseEvent.create("ALERT", 
                String.format("Alert: High traffic detected at %s", LocalDateTime.now()), 
                alertData);
            
            emitterService.broadcast(event);
            log.warn("Broadcasted alert event");
        }
    }

    /**
     * Broadcast custom event on demand
     */
    public void broadcastCustomEvent(String eventType, String message, Object data) {
        SseEvent event = SseEvent.create(eventType, message, data);
        emitterService.broadcast(event);
        log.info("Broadcasted custom event: {}", eventType);
    }
}
