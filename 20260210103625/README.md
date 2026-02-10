# Transaction Labeling with Flink and Drools

A robust, production-ready Spring Boot application for labeling transaction data based on Drools rules using Apache Flink for distributed processing.

## Features

- **Rule-Based Labeling**: Dynamic rule engine using Drools for transaction classification
- **Distributed Processing**: Apache Flink 1.16.3 for scalable, parallel transaction processing
- **Parquet Export**: Export processed transactions to Parquet format for analytics
- **Embedded H2 Database**: No external database setup required
- **REST API**: Comprehensive REST API with Swagger/OpenAPI documentation
- **Security**: Spring Security with configuration for authentication and authorization
- **Observability**: Distributed tracing with TraceId, structured logging
- **Resilience**: Circuit breaker, rate limiting, and fault tolerance patterns
- **Multi-threading**: Asynchronous processing with thread pool management
- **Validation**: Input validation and error handling

## Technology Stack

- **Java**: 17+
- **Spring Boot**: 3.3.0
- **Apache Flink**: 1.16.3
- **Drools**: 8.44.0.Final
- **H2 Database**: 2.2.224 (Embedded)
- **Swagger/OpenAPI**: 3.x
- **Resilience4j**: 2.2.0 (Circuit Breaker, Rate Limiter)
- **Maven**: 3.x
- **Docker**: Containerization support

## Architecture

The application follows a layered architecture:

```
┌─────────────────────────────────────────┐
│           Controller Layer              │
│      (REST API + Swagger)               │
└───────────────────┬─────────────────────┘
                    │
┌───────────────────▼─────────────────────┐
│          Service Layer                  │
│  - TransactionService                  │
│  - RuleManagementService               │
│  - DroolsRuleEngine                    │
│  - FlinkProcessingService              │
│  - ParquetExportService               │
└───────────────────┬─────────────────────┘
                    │
┌───────────────────▼─────────────────────┐
│        Repository Layer                 │
│  - TransactionRepository               │
│  - RuleRepository                      │
│  - ProcessingLogRepository             │
└───────────────────┬─────────────────────┘
                    │
┌───────────────────▼─────────────────────┐
│          Data Layer                    │
│       (H2 Embedded Database)           │
└─────────────────────────────────────────┘
```

## Getting Started

### Prerequisites

- JDK 17 or higher
- Maven 3.6 or higher
- Docker (optional, for containerized deployment)

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd transaction-labeling-flink
```

2. Build the application:
```bash
mvn clean package
```

3. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Docker Deployment

Build and run with Docker Compose:
```bash
docker-compose up --build
```

Or build the Docker image manually:
```bash
docker build -t transaction-labeling-flink:latest .
docker run -p 8080:8080 transaction-labeling-flink:latest
```

## API Documentation

Once the application is running, access the Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

API documentation is also available at:
```
http://localhost:8080/api-docs
```

### H2 Console

Access the H2 database console at:
```
http://localhost:8080/h2-console
```

Connection settings:
- **JDBC URL**: `jdbc:h2:mem:transactiondb`
- **User Name**: `sa`
- **Password**: (leave empty)

## API Endpoints

### Transaction Management

#### Create Transaction
```http
POST /api/transactions
Content-Type: application/json

{
  "transactionId": "TXN001",
  "accountNumber": "ACC001",
  "amount": 15000.00,
  "currency": "USD",
  "transactionType": "PURCHASE",
  "merchantCategory": "RETAIL",
  "location": "New York",
  "countryCode": "USA",
  "status": "PENDING",
  "description": "Sample transaction"
}
```

#### Get Transaction by ID
```http
GET /api/transactions/{id}
```

#### Get All Transactions (Paginated)
```http
GET /api/transactions?page=0&size=20&sortBy=createdAt&sortDir=DESC
```

#### Get Unprocessed Transactions
```http
GET /api/transactions/unprocessed
```

#### Process Unprocessed Transactions
```http
POST /api/transactions/process
Content-Type: application/json

{
  "batchSize": 50,
  "maxRecords": 1000,
  "useParquetOutput": true,
  "outputDirectory": "./output"
}
```

### Rule Management

#### Create Rule
```http
POST /api/rules
Content-Type: application/json

{
  "ruleName": "High Amount Rule",
  "ruleContent": "package com.example.transactionlabeling.rules\n\nimport com.example.transactionlabeling.entity.Transaction\n\nrule \"High Amount Rule\"\n    when\n        $transaction : Transaction(amount > 10000)\n    then\n        $transaction.getLabels().add(\"HIGH_AMOUNT\");\nend",
  "ruleCategory": "RISK",
  "priority": 100,
  "active": true,
  "description": "Flag transactions with amount greater than 10000"
}
```

#### Get All Rules
```http
GET /api/rules?page=0&size=20&sortBy=priority&sortDir=DESC
```

#### Get Active Rules
```http
GET /api/rules/active
```

#### Update Rule
```http
PUT /api/rules/{id}
```

#### Activate/Deactivate Rule
```http
POST /api/rules/{id}/activate
POST /api/rules/{id}/deactivate
```

#### Reload Rules
```http
POST /api/rules/reload
```

#### Validate Rule
```http
POST /api/rules/validate
Content-Type: text/plain

<rule content>
```

### Processing Logs

#### Get All Processing Logs
```http
GET /api/processing/logs
```

#### Get Latest Processing Log
```http
GET /api/processing/logs/latest
```

## Sample Rules

The application comes with 5 pre-configured sample rules:

1. **High Amount Transaction**: Labels transactions with amount > $10,000 as "HIGH_AMOUNT"
2. **International Transaction**: Labels non-USA transactions as "INTERNATIONAL"
3. **Merchant Category Risk**: Labels transactions from GAMBLING, CASH_ADVANCE, or CRYPTO categories as "HIGH_RISK_MERCHANT"
4. **Suspicious Pattern**: Labels transactions with suspicious amounts (e.g., $9,999.99, $10,000, $99,999.99, $100,000)
5. **Online Purchase**: Labels online purchases as "ONLINE"

## Sample Data

The application automatically initializes:
- 5 sample rules
- 100 sample transactions with various characteristics

## Processing Workflow

1. **Transaction Ingestion**: Transactions are created via REST API or bulk import
2. **Batch Processing**: Unprocessed transactions are retrieved in batches
3. **Flink Processing**: Transactions are processed in parallel using Apache Flink
4. **Rule Application**: Drools rules are applied to each transaction
5. **Label Assignment**: Transactions are labeled based on matching rules
6. **Parquet Export**: Processed transactions can be exported to Parquet format
7. **Log Persistence**: Processing logs are recorded for audit and monitoring

## Configuration

### Application Configuration (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:transactiondb
    username: sa
    password:

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
        waitDurationInOpenState: 10s

  ratelimiter:
    instances:
      rateLimiter:
        limitForPeriod: 100
        limitRefreshPeriod: 1s
```

## Security

- **Spring Security**: Configured with permissive settings for development
- **XSS Protection**: Input validation and sanitization
- **SQL Injection**: Parameterized queries via JPA
- **HTTPS**: Recommended for production deployment

## Observability

### Logging

- **TraceId**: Each request gets a unique trace ID for distributed tracing
- **Structured Logging**: JSON-formatted logs with consistent structure
- **Log Levels**: Configurable logging levels per package

### Monitoring

- **Spring Actuator**: Health, metrics, and info endpoints
- **Prometheus**: Metrics export available
- **Circuit Breaker**: Resilience4j circuit breaker metrics

## Testing

Run all tests:
```bash
mvn test
```

Run specific test class:
```bash
mvn test -Dtest=TransactionServiceTest
```

## Performance Considerations

- **Flink Parallelism**: Adjust `flink.parallelism` based on available CPU cores
- **Batch Size**: Tune batch size based on memory constraints
- **Connection Pooling**: JPA uses HikariCP by default
- **Async Processing**: Asynchronous service methods prevent blocking

## Troubleshooting

### Application won't start
- Check JDK version (requires 21+)
- Verify Maven dependencies are downloaded
- Check port 8080 is not in use

### Drools rules not loading
- Check rule syntax for errors
- Use `/api/rules/validate` endpoint to validate rules
- Check logs for rule compilation errors

### Flink processing fails
- Check available memory
- Reduce batch size
- Check logs for detailed error messages

### Parquet export fails
- Verify output directory has write permissions
- Check disk space availability
- Ensure Parquet schema matches transaction structure

## Production Deployment

### Recommendations

1. Use external database (PostgreSQL, MySQL) instead of embedded H2
2. Enable HTTPS
3. Configure proper authentication and authorization
4. Use distributed tracing (Zipkin, Jaeger)
5. Set up monitoring and alerting (Prometheus, Grafana)
6. Configure proper logging aggregation (ELK Stack)
7. Enable rate limiting and circuit breakers
8. Use distributed caching for rules
9. Implement proper backup and recovery
10. Configure proper SSL/TLS for external connections

### Docker Production Deployment

```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/transactiondb \
  -e SPRING_DATASOURCE_USERNAME=admin \
  -e SPRING_DATASOURCE_PASSWORD=xxxxxxxx \
  -e FLINK_PARALLELISM=4 \
  -e OUTPUT_DIRECTORY=/data/output \
  -v /data/output:/app/output \
  -v /data/logs:/app/logs \
  transaction-labeling-flink:latest
```

## License

This project is licensed under the Apache License 2.0.

## Support

For issues and questions, please open an issue on the GitHub repository.
