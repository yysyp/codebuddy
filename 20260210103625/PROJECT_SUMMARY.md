# Transaction Labeling with Flink - Project Summary

## Project Overview
A robust, production-ready Spring Boot application for labeling transaction data based on Drools rules using Apache Flink for distributed processing.

## Technology Stack

### Core Technologies
- **Java**: 17
- **Spring Boot**: 3.3.0
- **Apache Flink**: 1.17.1
- **Drools**: 8.44.0.Final
- **H2 Database**: 2.2.224 (Embedded)

### Supporting Technologies
- **Spring Boot Web**: REST API support
- **Spring Boot Data JPA**: Database access
- **Spring Boot Validation**: Input validation
- **Spring Security**: Security configuration
- **Spring Boot Actuator**: Monitoring and metrics
- **Swagger/OpenAPI 3.x**: API documentation
- **Lombok 1.18.30**: Code generation
- **Resilience4j 2.2.0**: Circuit breaker and rate limiting
- **Parquet 1.14.1**: Data export format

## Architecture

### Layered Architecture
```
┌─────────────────────────────────────────┐
│         Controller Layer            │  REST API with Swagger
│   (Transaction, Rule, Processing)  │
└───────────────────┬─────────────────────┘
                    │
┌───────────────────▼─────────────────────┐
│          Service Layer                 │  Business Logic
│  - TransactionService                │
│  - RuleManagementService            │
│  - DroolsRuleEngine               │  Drools Integration
│  - FlinkProcessingService          │  Flink Integration
│  - ParquetExportService           │
│  - DataInitializationService       │
└───────────────────┬─────────────────────┘
                    │
┌───────────────────▼─────────────────────┐
│        Repository Layer               │  Data Access
│  - TransactionRepository             │
│  - RuleRepository                │
│  - ProcessingLogRepository         │
└───────────────────┬─────────────────────┘
                    │
┌───────────────────▼─────────────────────┐
│          Data Layer                    │  H2 Embedded DB
│    - transactions                     │
│    - rules                          │
│    - processing_logs                 │
│    - transaction_labels              │
└─────────────────────────────────────────┘
```

## Key Features

### 1. Rule-Based Labeling
- Dynamic rule engine using Drools
- Rules stored in database for runtime updates
- Rule validation before deployment
- Priority-based rule execution
- Rule categories (RISK, GEOGRAPHIC, FRAUD, NORMAL)

### 2. Distributed Processing
- Apache Flink for parallel processing
- Configurable parallelism
- Streaming data source from transaction list
- Map function for rule application
- Efficient resource utilization

### 3. Data Management
- H2 embedded database (no external DB required)
- JPA/Hibernate ORM
- Transaction support for data consistency
- Audit logging for all processing operations

### 4. REST API
- Comprehensive CRUD operations for transactions and rules
- Swagger/OpenAPI 3.x documentation
- Request validation
- Standardized response format
- Pagination support

### 5. Observability
- TraceId for distributed tracing
- Structured logging with SLF4J
- Spring Actuator endpoints
- Health checks and metrics
- Processing log tracking

### 6. Resilience
- Circuit breaker pattern
- Rate limiting
- Async processing
- Error handling
- Graceful degradation

## Data Model

### Transaction Entity
```java
- id: Long
- transactionId: String (unique)
- accountNumber: String
- amount: BigDecimal
- currency: String
- transactionType: String
- merchantCategory: String
- location: String
- countryCode: String
- riskScore: BigDecimal
- status: String
- description: String
- labels: Set<String>
- processedAt: Instant
- createdAt: Instant
- updatedAt: Instant
```

### Rule Entity
```java
- id: Long
- ruleName: String (unique)
- ruleContent: String (Drools DRL)
- ruleCategory: String
- priority: Integer
- active: Boolean
- description: String
- createdAt: Instant
- updatedAt: Instant
```

### ProcessingLog Entity
```java
- id: Long
- operationName: String
- status: String
- recordsProcessed: Long
- executionTimeMs: Long
- errorMessage: String
- details: String
- startTime: Instant
- endTime: Instant
```

## API Endpoints

### Transactions
```
POST   /api/transactions                    - Create transaction
GET    /api/transactions                    - List all (paginated)
GET    /api/transactions/{id}             - Get by ID
GET    /api/transactions/transaction-id/{id} - Get by transaction ID
GET    /api/transactions/account/{number}    - Get by account
GET    /api/transactions/unprocessed         - Get unprocessed
GET    /api/transactions/unprocessed/count    - Count unprocessed
POST    /api/transactions/process            - Process transactions
DELETE /api/transactions/{id}             - Delete by ID
```

### Rules
```
POST   /api/rules               - Create rule
GET    /api/rules               - List all (paginated)
GET    /api/rules/{id}          - Get by ID
GET    /api/rules/rule-name/{name} - Get by name
GET    /api/rules/active       - Get active rules
GET    /api/rules/category/{cat} - Get by category
PUT    /api/rules/{id}          - Update rule
DELETE /api/rules/{id}          - Delete rule
POST   /api/rules/{id}/activate  - Activate rule
POST   /api/rules/{id}/deactivate- Deactivate rule
POST   /api/rules/reload       - Reload all rules
GET    /api/rules/loaded       - Get loaded rule names
POST   /api/rules/validate     - Validate rule content
```

### Processing Logs
```
GET /api/processing/logs              - Get all logs
GET /api/processing/logs/{id}       - Get by ID
GET /api/processing/logs/operation/{name} - Get by operation
GET /api/processing/logs/status/{status} - Get by status
GET /api/processing/logs/latest     - Get latest log
```

## Sample Rules

### Rule 1: High Amount Transaction
```drools
rule "High Amount Transaction"
    when
        $transaction : Transaction(amount > 10000)
    then
        $transaction.getLabels().add("HIGH_AMOUNT");
        $transaction.setRiskScore(new BigDecimal("0.8"));
end
```

### Rule 2: International Transaction
```drools
rule "International Transaction"
    when
        $transaction : Transaction(countryCode != null && countryCode != "USA")
    then
        $transaction.getLabels().add("INTERNATIONAL");
end
```

### Rule 3: Merchant Category Risk
```drools
rule "Merchant Category Risk"
    when
        $transaction : Transaction(merchantCategory in ("GAMBLING", "CASH_ADVANCE", "CRYPTO"))
    then
        $transaction.getLabels().add("HIGH_RISK_MERCHANT");
        if ($transaction.getRiskScore() == null) {
            $transaction.setRiskScore(new BigDecimal("0.7"));
        } else {
            BigDecimal current = $transaction.getRiskScore();
            $transaction.setRiskScore(current.add(new BigDecimal("0.1")));
        }
end
```

## Configuration

### application.yml
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:transactiondb
    username: sa
    password:

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate.ddl-auto: create-drop

flink:
  parallelism: 2

output:
  directory: ./output

resilience4j:
  circuitbreaker:
    instances:
      circuitBreaker:
        slidingWindowSize: 10
        failureRateThreshold: 50

  ratelimiter:
    instances:
      rateLimiter:
        limitForPeriod: 100
        limitRefreshPeriod: 1s
```

## Processing Workflow

1. **Ingestion**: Transactions created via REST API or bulk import
2. **Queue**: Unprocessed transactions identified
3. **Flink Processing**: Parallel processing using Apache Flink
4. **Rule Application**: Drools rules applied to each transaction
5. **Label Assignment**: Labels added based on matching rules
6. **Persistence**: Updated transactions saved to database
7. **Logging**: Processing details recorded for audit
8. **Export**: Optional Parquet export for analytics

## Security Features

- SQL Injection protection (JPA parameterized queries)
- XSS protection (input validation)
- CSRF protection (Spring Security)
- Rate limiting (Resilience4j)
- Circuit breaker (Resilience4j)
- HTTPS support (configurable)

## Observability Features

- **TraceId**: Distributed tracing with unique IDs
- **Logging**: Structured logging with SLF4J
- **Metrics**: Spring Actuator with Prometheus
- **Health Checks**: /actuator/health
- **Processing Logs**: Detailed operation tracking

## Concurrency & Thread Safety

- ReentrantReadWriteLock for Drools rule engine
- Thread-safe repository operations
- Async processing with ThreadPoolTaskExecutor
- ConcurrentHashMap for caching (if needed)

## Data Consistency

- @Transactional annotations for data consistency
- Optimistic locking (JPA)
- Atomic operations on shared resources
- Eventual consistency with Flink

## Error Handling

- Global exception handler
- Standardized error responses
- Validation error messages
- Circuit breaker fallback
- Rate limit handling

## Testing

- Unit tests with JUnit 5
- Integration tests with Spring Boot Test
- MockMvc for controller testing
- Mockito for service testing

## Docker Support

- Dockerfile included
- docker-compose.yml for multi-container setup
- Volume mounting for data persistence
- Network configuration

## Performance Considerations

- Configurable Flink parallelism
- Batch processing for large datasets
- Connection pooling (HikariCP)
- Efficient Drools rule engine
- Minimal object creation in hot paths

## Monitoring & Maintenance

- Health check endpoints
- Metrics export
- Log aggregation
- Processing log tracking
- Rule performance metrics

## Future Enhancements

1. Distributed tracing with Zipkin/Jaeger
2. Kafka integration for real-time processing
3. Distributed caching with Redis
4. Load balancing for multi-instance deployment
5. Advanced analytics dashboard
6. Machine learning for rule optimization

## Deployment Options

### Local Development
```bash
mvn spring-boot:run
```

### Docker
```bash
docker-compose up
```

### Production
- External database (PostgreSQL/MySQL)
- Distributed Flink cluster
- Load balancer
- Kubernetes deployment
- CI/CD pipeline

## Troubleshooting

### Common Issues

1. **Lombok not working**: Enable annotation processor in IDE
2. **Flink connection issues**: Check parallelism settings
3. **Drools rule errors**: Validate rule syntax before saving
4. **Memory issues**: Increase JVM heap size: `-Xmx2G`
5. **Port conflicts**: Change server.port in application.yml

## Best Practices

1. Always validate rule content before saving
2. Use appropriate batch sizes for processing
3. Monitor processing logs for errors
4. Configure appropriate Flink parallelism
5. Use circuit breakers for external services
6. Implement proper logging and tracing
7. Regular backup of database
8. Monitor resource usage
9. Update dependencies regularly
10. Follow security best practices

## Conclusion

This application provides a robust, scalable solution for transaction labeling using modern technologies. The combination of Apache Flink for distributed processing and Drools for rule-based classification creates a flexible and powerful system for financial transaction analysis.
