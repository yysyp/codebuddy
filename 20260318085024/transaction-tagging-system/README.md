# Transaction Tagging System

A rule-based transaction tagging system that uses **Drools** for rule management and **Apache Flink** for data processing. The system automatically applies tags to financial transactions based on configurable business rules.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Rule Definition](#rule-definition)
- [Examples](#examples)
- [Development Guide](#development-guide)
- [Troubleshooting](#troubleshooting)

---

## Architecture Overview

The system consists of two main components:

```
┌─────────────────────────────────────────────────────────────────────┐
│                    Transaction Tagging System                        │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌───────────────────────┐         ┌───────────────────────┐       │
│  │    Control Panel       │         │      Data Panel        │       │
│  │                       │         │                       │       │
│  │  ┌─────────────────┐  │         │  ┌─────────────────┐  │       │
│  │  │  Rule Management│  │ Rules   │  │  Flink Streaming│  │       │
│  │  │     (Drools)    │◄─┼─────────┼──│    Processor    │  │       │
│  │  └─────────────────┘  │         │  └─────────────────┘  │       │
│  │                       │         │          │            │       │
│  │  ┌─────────────────┐  │         │          ▼            │       │
│  │  │  Schema Manager │  │         │  ┌─────────────────┐  │       │
│  │  └─────────────────┘  │         │  │   Transaction   │  │       │
│  │                       │         │  │     Source      │  │       │
│  │  ┌─────────────────┐  │         │  └─────────────────┘  │       │
│  │  │  REST API       │  │         │          │            │       │
│  │  │  (Swagger UI)   │  │         │          ▼            │       │
│  │  └─────────────────┘  │         │  ┌─────────────────┐  │       │
│  │                       │         │  │ Tagged Trans.   │  │       │
│  │  Database: H2         │         │  │     Sink        │  │       │
│  └───────────────────────┘         │  └─────────────────┘  │       │
│                                     └───────────────────────┘       │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
```

### Control Panel
- **Purpose**: Rule management, schema definition, and metadata management
- **Technology**: Spring Boot 3.x + Drools 8.x
- **Features**:
  - CRUD operations for rules
  - Rule lifecycle management (Draft → Published → Deprecated → Archived)
  - Drools rule validation
  - REST API with Swagger documentation
  - In-memory H2 database for development

### Data Panel
- **Purpose**: Process transaction data and apply tags based on rules
- **Technology**: Apache Flink 1.16.3 + Drools 8.x
- **Features**:
  - Real-time stream processing
  - Dynamic rule loading from Control Panel
  - Multiple source/sink support (Generator, Kafka, File)
  - Fault tolerance with checkpointing
  - Distributed processing

---

## Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Java JDK | Azul Zulu | 17 |
| Rule Engine | Drools | 8.44.0.Final |
| Stream Processing | Apache Flink | 1.16.3 |
| Backend Framework | Spring Boot | 3.2.0 |
| Database | H2 (embedded) | 2.2.224 |
| Build Tool | Maven | 3.9.x |
| API Documentation | SpringDoc OpenAPI | 2.3.0 |

---

## Project Structure

```
transaction-tagging-system/
├── pom.xml                          # Parent POM
├── build.bat                        # Windows build script
├── build.sh                         # Linux/Mac build script
├── run.bat                          # Windows run script
├── run.sh                           # Linux/Mac run script
├── README.md                        # This file
│
├── common/                          # Shared module
│   ├── pom.xml
│   └── src/main/java/
│       └── com/transaction/tagging/common/
│           ├── entity/              # Domain entities
│           │   ├── Transaction.java
│           │   ├── Tag.java
│           │   └── RuleMetadata.java
│           ├── dto/                 # Data Transfer Objects
│           │   └── ApiResponse.java
│           ├── exception/           # Exception classes
│           │   ├── BusinessException.java
│           │   └── ErrorCode.java
│           └── util/                # Utility classes
│               └── TraceContext.java
│
├── control-panel/                   # Control Panel module
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/transaction/tagging/controlpanel/
│       │   ├── ControlPanelApplication.java
│       │   ├── config/              # Configuration classes
│       │   ├── controller/          # REST Controllers
│       │   ├── service/             # Business logic
│       │   ├── repository/          # Data access
│       │   ├── entity/              # JPA entities
│       │   ├── dto/                 # Request/Response DTOs
│       │   └── exception/           # Exception handlers
│       └── resources/
│           └── application.yml      # Application configuration
│
└── data-panel/                      # Data Panel module
    ├── pom.xml
    └── src/main/
        ├── java/com/transaction/tagging/datapanel/
        │   ├── DataPanelApplication.java
        │   ├── config/              # Configuration
        │   ├── function/            # Flink functions
        │   ├── source/              # Data sources
        │   ├── sink/                # Data sinks
        │   └── rule/                # Rule engine integration
        └── resources/
            └── logback.xml          # Logging configuration
```

---

## Prerequisites

### Required Software

1. **Java Development Kit (JDK) 17**
   ```bash
   # Verify installation
   java -version
   # Should output: openjdk version "17.x.x"
   ```

2. **Maven 3.9+**
   ```bash
   # Verify installation
   mvn -version
   ```

3. **(Optional) Apache Flink 1.16.3** - For distributed deployment
   ```bash
   # Set FLINK_HOME environment variable
   export FLINK_HOME=/path/to/flink-1.16.3
   ```

### Environment Variables (Windows)

```batch
set JAVA_HOME=D:\app\azul-17.0.10
set PATH=%JAVA_HOME%\bin;%PATH%
```

### Environment Variables (Linux/Mac)

```bash
export JAVA_HOME=/path/to/jdk-17
export PATH=$JAVA_HOME/bin:$PATH
```

---

## Quick Start

### 1. Build the Project

**Windows:**
```batch
cd transaction-tagging-system
build.bat package
```

**Linux/Mac:**
```bash
cd transaction-tagging-system
chmod +x build.sh
./build.sh package
```

### 2. Start Control Panel

**Windows:**
```batch
run.bat control-panel
```

**Linux/Mac:**
```bash
chmod +x run.sh
./run.sh control-panel
```

Access the Control Panel:
- **API Base URL**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **H2 Console**: http://localhost:8080/api/h2-console (JDBC URL: `jdbc:h2:mem:controlpanel`)

### 3. Create a Rule

Using `curl` or Swagger UI:

```bash
curl -X POST http://localhost:8080/api/v1/rules \
  -H "Content-Type: application/json" \
  -d '{
    "ruleId": "HIGH_AMOUNT_RULE",
    "name": "High Amount Transaction Alert",
    "description": "Flags transactions with amount greater than 10000",
    "ruleContent": "rule \"High Amount Transaction\"\n    when\n        $txn : Transaction(amount > 10000)\n    then\n        $txn.addTag(Tag.builder()\n            .code(\"HIGH_AMOUNT\")\n            .name(\"High Amount Transaction\")\n            .category(\"RISK\")\n            .severity(\"MEDIUM\")\n            .appliedAt(java.time.Instant.now())\n            .appliedByRuleId(\"HIGH_AMOUNT_RULE\")\n            .build());\nend",
    "tagCode": "HIGH_AMOUNT",
    "tagName": "High Amount Transaction",
    "tagCategory": "RISK",
    "tagSeverity": "MEDIUM",
    "createdBy": "admin"
  }'
```

### 4. Publish the Rule

```bash
curl -X POST "http://localhost:8080/api/v1/rules/HIGH_AMOUNT_RULE/publish?publishedBy=admin"
```

### 5. Start Data Panel

**Windows:**
```batch
run.bat data-panel
```

**Linux/Mac:**
```bash
./run.sh data-panel
```

The Data Panel will start processing transactions and applying rules.

### 6. Run All Components Together

**Windows:**
```batch
run.bat all
```

**Linux/Mac:**
```bash
./run.sh all
```

---

## Configuration

### Control Panel Configuration

Edit `control-panel/src/main/resources/application.yml`:

```yaml
server:
    port: 8080
    servlet:
        context-path: /api

spring:
    datasource:
        url: jdbc:h2:mem:controlpanel
        username: sa
        password:
        
drools:
    rules-path: rules/
    auto-scan: true
    scan-interval: 30000

rule-engine:
    max-rules-per-session: 10000
```

### Data Panel Configuration

Pass configuration via command-line arguments:

```bash
java -jar data-panel.jar \
  --controlPanelUrl=http://localhost:8080/api \
  --ruleRefreshIntervalSeconds=60 \
  --sourceType=generator \
  --generatorRatePerSecond=100 \
  --sinkType=console
```

#### Configuration Options

| Parameter | Default | Description |
|-----------|---------|-------------|
| `jobId` | auto-generated | Unique job identifier |
| `controlPanelUrl` | http://localhost:8080/api | Control Panel API URL |
| `ruleRefreshIntervalSeconds` | 60 | Rule refresh interval |
| `sourceType` | generator | Source type (generator/kafka/file) |
| `generatorRatePerSecond` | 100 | Transactions per second |
| `generatorTotalRecords` | 10000 | Total transactions to generate |
| `sinkType` | console | Sink type (console/kafka/file) |
| `parallelism` | 1 | Flink job parallelism |
| `checkpointingEnabled` | true | Enable checkpointing |

---

## API Documentation

### Rule Management APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/v1/rules` | Create a new rule |
| GET | `/v1/rules/{ruleId}` | Get rule by ID |
| PUT | `/v1/rules/{ruleId}` | Update a rule |
| DELETE | `/v1/rules/{ruleId}` | Delete a rule |
| POST | `/v1/rules/{ruleId}/publish` | Publish a rule |
| POST | `/v1/rules/{ruleId}/deprecate` | Deprecate a rule |
| POST | `/v1/rules/{ruleId}/archive` | Archive a rule |
| GET | `/v1/rules` | Get all rules (paginated) |
| GET | `/v1/rules/status/{status}` | Get rules by status |
| GET | `/v1/rules/active` | Get all active rules |
| GET | `/v1/rules/statistics` | Get rule statistics |
| POST | `/v1/rules/validate` | Validate rule content |

### Internal APIs (for Data Panel)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/v1/internal/rules/published` | Get all published rules |

### API Response Format

**Success Response:**
```json
{
    "success": true,
    "code": "SUCCESS",
    "message": "Operation completed successfully",
    "timestamp": "2024-01-01T00:00:00Z",
    "traceId": "abc123",
    "data": { ... }
}
```

**Error Response:**
```json
{
    "success": false,
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "timestamp": "2024-01-01T00:00:00Z",
    "traceId": "abc123",
    "errors": [
        {
            "field": "amount",
            "code": "REQUIRED",
            "message": "Amount is required"
        }
    ]
}
```

---

## Rule Definition

### Drools Rule Syntax

Rules are defined using Drools DRL (Drools Rule Language) syntax:

```java
rule "Rule Name"
    when
        // Conditions (LHS - Left Hand Side)
        $txn : Transaction(
            amount > 10000,
            transactionType == "TRANSFER"
        )
    then
        // Actions (RHS - Right Hand Side)
        $txn.addTag(Tag.builder()
            .code("HIGH_VALUE_TRANSFER")
            .name("High Value Transfer")
            .category("COMPLIANCE")
            .severity("HIGH")
            .appliedAt(java.time.Instant.now())
            .appliedByRuleId("RULE_ID")
            .build());
end
```

### Available Transaction Fields

| Field | Type | Description |
|-------|------|-------------|
| `transactionId` | String | Unique transaction ID |
| `accountId` | String | Account ID |
| `transactionType` | String | Type (TRANSFER, PAYMENT, etc.) |
| `amount` | BigDecimal | Transaction amount |
| `currency` | String | Currency code |
| `merchantName` | String | Merchant name |
| `merchantCategory` | String | Merchant category |
| `transactionTime` | Instant | Transaction timestamp |
| `location` | String | Geographic location |
| `channel` | String | Channel (WEB, MOBILE, etc.) |
| `status` | String | Transaction status |

### Example Rules

#### 1. High Amount Transaction
```java
rule "High Amount Transaction"
    when
        $txn : Transaction(amount > 10000)
    then
        $txn.addTag(Tag.builder()
            .code("HIGH_AMOUNT")
            .name("High Amount Transaction")
            .category("RISK")
            .severity("MEDIUM")
            .appliedAt(java.time.Instant.now())
            .appliedByRuleId("HIGH_AMOUNT_RULE")
            .build());
end
```

#### 2. Suspicious Location
```java
rule "Suspicious Location"
    when
        $txn : Transaction(
            location in ("Unknown", "Restricted Zone"),
            amount > 5000
        )
    then
        $txn.addTag(Tag.builder()
            .code("SUSPICIOUS_LOCATION")
            .name("Suspicious Location Transaction")
            .category("FRAUD")
            .severity("HIGH")
            .appliedAt(java.time.Instant.now())
            .appliedByRuleId("SUSPICIOUS_LOCATION_RULE")
            .build());
end
```

#### 3. Late Night Transaction
```java
rule "Late Night Transaction"
    when
        $txn : Transaction(
            channel == "ONLINE",
            eval(isLateNight($txn.getTransactionTime()))
        )
    then
        $txn.addTag(Tag.builder()
            .code("LATE_NIGHT")
            .name("Late Night Transaction")
            .category("BEHAVIOR")
            .severity("LOW")
            .appliedAt(java.time.Instant.now())
            .appliedByRuleId("LATE_NIGHT_RULE")
            .build());
end
```

---

## Examples

### Complete Workflow Example

```bash
# 1. Start Control Panel
./run.sh control-panel

# 2. Create a rule (in a new terminal)
curl -X POST http://localhost:8080/api/v1/rules \
  -H "Content-Type: application/json" \
  -d '{
    "ruleId": "LARGE_TRANSFER",
    "name": "Large Transfer Detection",
    "ruleContent": "rule \"Large Transfer\"\n    when\n        $txn : Transaction(transactionType == \"TRANSFER\", amount > 50000)\n    then\n        $txn.addTag(Tag.builder().code(\"LARGE_TRANSFER\").name(\"Large Transfer\").category(\"RISK\").severity(\"HIGH\").appliedAt(java.time.Instant.now()).build());\nend",
    "tagCode": "LARGE_TRANSFER",
    "tagName": "Large Transfer",
    "tagCategory": "RISK",
    "tagSeverity": "HIGH"
  }'

# 3. Publish the rule
curl -X POST "http://localhost:8080/api/v1/rules/LARGE_TRANSFER/publish"

# 4. Verify the rule
curl http://localhost:8080/api/v1/rules/LARGE_TRANSFER

# 5. Start Data Panel (in a new terminal)
./run.sh data-panel

# 6. Watch the console output for tagged transactions
```

---

## Development Guide

### Adding a New Rule Type

1. Define the rule in Drools DRL syntax
2. Create the rule via Control Panel API
3. Publish the rule
4. Data Panel will automatically pick up the new rule

### Extending Transaction Entity

1. Add fields to `common/src/main/java/.../entity/Transaction.java`
2. Update the schema in Control Panel
3. Modify source generators if needed

### Adding a New Data Source

1. Implement `SourceFunction<Transaction>` interface
2. Add source type configuration
3. Update `TransactionSource.create()` factory method

### Running Tests

```bash
# Run all tests
mvn test

# Run specific module tests
cd control-panel
mvn test

# Run with coverage
mvn test jacoco:report
```

---

## Troubleshooting

### Common Issues

#### 1. Port Already in Use
```
Error: Port 8080 already in use
```
**Solution**: Change the port in `application.yml` or stop the conflicting application.

#### 2. Rule Compilation Error
```
Error: Rule validation failed
```
**Solution**: Check Drools syntax. Use the `/v1/rules/validate` endpoint to validate before creating.

#### 3. Data Panel Cannot Connect to Control Panel
```
Error: Failed to fetch rules from Control Panel
```
**Solution**: 
- Verify Control Panel is running
- Check `controlPanelUrl` configuration
- Check network connectivity

#### 4. Java Version Mismatch
```
Error: Unsupported class version
```
**Solution**: Ensure JDK 17 is installed and `JAVA_HOME` is set correctly.

### Debug Mode

Enable debug logging:

**Control Panel** (`application.yml`):
```yaml
logging:
    level:
        com.transaction.tagging: DEBUG
        org.drools: DEBUG
```

**Data Panel** (`logback.xml`):
```xml
<logger name="com.transaction.tagging" level="DEBUG"/>
```

---

## License

This project is licensed under the Apache License 2.0.

---

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests
5. Submit a pull request

---

## Contact

For questions and support, please open an issue on the project repository.
