# ETL Tagging System

A transaction data tagging system based on Drools rules and Flink processing. This system consists of two main modules:

- **ETL Control Panel**: Rule management and metadata control panel based on Drools
- **ETL Data Panel**: Data processing panel based on Flink with rule-based tagging

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     ETL Control Panel                        │
│  - Rule Definition & Management (Drools)                     │
│  - Schema Definition & Management                            │
│  - SQL Definition & Management                               │
│  - REST API for rule/schema/SQL operations                   │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           │ REST API
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                     ETL Data Panel                           │
│  - Flink SQL for CSV processing                              │
│  - Drools rule execution                                     │
│  - Transaction tagging                                       │
│  - CSV input/output                                          │
└─────────────────────────────────────────────────────────────┘
```

## Technology Stack

- **JDK**: 17
- **Spring Boot**: 3.2.0
- **Drools**: 8.44.0.Final
- **Apache Flink**: 1.16.3
- **Database**: H2 (Embedded)
- **Build Tool**: Maven

## Project Structure

```
etl-tagging-system/
├── etl-control-panel/           # Control Panel module
│   ├── src/main/java/
│   │   └── com/etl/control/
│   │       ├── config/          # Configuration
│   │       ├── controller/      # REST Controllers
│   │       ├── dto/             # Data Transfer Objects
│   │       ├── entity/          # JPA Entities
│   │       ├── exception/       # Exception handling
│   │       ├── repository/      # JPA Repositories
│   │       └── service/         # Business logic
│   └── src/main/resources/
│       └── application.yml      # Configuration
│
├── etl-data-panel/              # Data Panel module
│   ├── src/main/java/
│   │   └── com/etl/data/
│   │       ├── client/          # Control Panel client
│   │       ├── controller/      # REST Controllers
│   │       ├── dto/             # Data Transfer Objects
│   │       ├── model/           # Data models
│   │       └── service/         # Business logic
│   └── src/main/resources/
│       └── application.yml      # Configuration
│
├── data/                        # Test data
│   ├── input/                   # Input CSV files
│   └── output/                  # Output CSV files
│
├── pom.xml                      # Parent POM
├── build.bat / build.sh         # Build scripts
├── run.bat / run.sh             # Run scripts
└── README.md                    # This file
```

## Features

### ETL Control Panel

1. **Rule Management**
   - Create, update, delete rules
   - Publish and deprecate rules
   - Version management
   - Rule validation using Drools

2. **Schema Management**
   - Define data schemas
   - Schema versioning
   - JSON schema support

3. **SQL Management**
   - Flink SQL definitions
   - Source and sink table definitions
   - Transformation SQL

4. **REST API**
   - Swagger UI documentation
   - H2 Console for database access

### ETL Data Panel

1. **Data Processing**
   - Read CSV files using Flink SQL
   - Apply Drools rules for tagging
   - Write tagged results to CSV

2. **Rule Execution**
   - Dynamic rule loading
   - Real-time rule application
   - Transaction tagging

## Getting Started

### Prerequisites

- JDK 17 installed
- Maven 3.6+ installed
- Windows or Linux/Unix system

### Build

**Windows:**
```cmd
build.bat
```

**Linux/Unix:**
```bash
chmod +x build.sh
./build.sh
```

### Run

**Windows:**
```cmd
run.bat
```

**Linux/Unix:**
```bash
chmod +x run.sh
./run.sh
```

### Access Services

After starting the services:

- **Control Panel API**: http://localhost:8080
- **Control Panel Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:etl_control`
  - Username: `sa`
  - Password: (empty)
- **Data Panel API**: http://localhost:8081

## Usage Examples

### 1. Create a Rule

**POST** `http://localhost:8080/api/v1/rules`

```json
{
  "name": "high-value-transaction",
  "description": "Tag transactions above 50000",
  "ruleContent": "package com.etl.rules\n\nimport com.etl.data.model.Transaction\n\nrule \"high-value-transaction\"\n    dialect \"mvel\"\n    when\n        $transaction : Transaction(amount > 50000.00)\n    then\n        $transaction.addTag(\"HIGH_VALUE\");\n    end",
  "ruleType": "TAGGING",
  "targetType": "TRANSACTION",
  "priority": "HIGH",
  "tags": "value,review"
}
```

### 2. Publish a Rule

**POST** `http://localhost:8080/api/v1/rules/{id}/publish`

### 3. Execute Tagging Job

**POST** `http://localhost:8081/api/v1/jobs/tagging/execute?inputPath=file:///data/input/transactions.csv&outputPath=file:///data/output/tagged_transactions.csv`

## Sample Rules

The system comes with pre-configured sample rules:

1. **high-amount-transaction**: Tags transactions > 10,000 with `HIGH_AMOUNT` and `REQUIRES_REVIEW`
2. **fraud-suspicious**: Tags suspicious transactions with `FRAUD_SUSPICIOUS` and `URGENT_REVIEW`
3. **small-transaction**: Tags transactions < 100 with `SMALL_AMOUNT`
4. **international-transaction**: Tags non-USD/CNY transactions with `INTERNATIONAL` and `CURRENCY_EXCHANGE`

## API Documentation

### Control Panel APIs

#### Rule Management

- `POST /api/v1/rules` - Create a new rule
- `PUT /api/v1/rules/{id}` - Update a rule
- `GET /api/v1/rules/{id}` - Get rule by ID
- `GET /api/v1/rules/name/{name}` - Get rule by name
- `GET /api/v1/rules` - Get all rules (paginated)
- `GET /api/v1/rules/status/{status}` - Get rules by status
- `GET /api/v1/rules/published` - Get all published rules
- `POST /api/v1/rules/{id}/publish` - Publish a rule
- `POST /api/v1/rules/{id}/deprecate` - Deprecate a rule
- `DELETE /api/v1/rules/{id}` - Archive a rule

#### Schema Management

- `POST /api/v1/schemas` - Create a new schema
- `PUT /api/v1/schemas/{id}` - Update a schema
- `GET /api/v1/schemas/{id}` - Get schema by ID
- `GET /api/v1/schemas/name/{name}` - Get schema by name
- `GET /api/v1/schemas` - Get all schemas
- `GET /api/v1/schemas/active` - Get all active schemas
- `DELETE /api/v1/schemas/{id}` - Deactivate a schema

#### SQL Management

- `POST /api/v1/sql` - Create a new SQL definition
- `PUT /api/v1/sql/{id}` - Update a SQL definition
- `GET /api/v1/sql/{id}` - Get SQL by ID
- `GET /api/v1/sql/name/{name}` - Get SQL by name
- `GET /api/v1/sql` - Get all SQL definitions
- `GET /api/v1/sql/active` - Get all active SQL definitions
- `GET /api/v1/sql/type/{type}` - Get SQL by type
- `DELETE /api/v1/sql/{id}` - Deactivate a SQL definition

### Data Panel APIs

- `POST /api/v1/jobs/tagging/execute` - Execute tagging job
- `GET /api/v1/jobs/health` - Health check

## Rule Examples

### Drools Rule Format

```java
package com.etl.rules

import com.etl.data.model.Transaction

rule "rule-name"
    dialect "mvel"
    salience 100  // Priority (higher = more important)
    when
        $transaction : Transaction(
            // Conditions
            amount > 10000.00
        )
    then
        // Actions
        $transaction.addTag("TAG_NAME");
    end
```

### Rule Conditions

You can use various conditions in rules:

- Amount comparison: `amount > 10000.00`
- Currency check: `currency != "USD"`
- Transaction type: `transactionType == "WITHDRAWAL"`
- Combined conditions: `amount > 5000 && transactionType == "TRANSFER"`
- Location check: `location == "UNKNOWN_LOCATION"`

### Rule Actions

- Add single tag: `$transaction.addTag("TAG_NAME")`
- Add multiple tags: 
  ```java
  $transaction.addTag("TAG1");
  $transaction.addTag("TAG2");
  ```

## Testing

### Test Data

Sample transaction data is available in `data/input/transactions.csv`:

| transaction_id | user_id | amount   | currency | transaction_type | transaction_time        | merchant_id | location          | status    |
|----------------|---------|----------|----------|------------------|-------------------------|-------------|-------------------|-----------|
| TXN001         | USER001 | 15000.00 | USD      | DEPOSIT          | 2024-01-01T10:00:00Z    | M001        | NEW_YORK          | COMPLETED |
| TXN002         | USER002 | 50.00    | USD      | WITHDRAWAL       | 2024-01-01T11:00:00Z    | M002        | LOS_ANGELES       | COMPLETED |
| TXN003         | USER003 | 8000.00  | EUR      | TRANSFER         | 2024-01-01T12:00:00Z    | M003        | LONDON            | COMPLETED |

### Run Tests

```bash
mvn test
```

## Configuration

### Control Panel Configuration

Key configuration in `etl-control-panel/src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:etl_control
    username: sa
    password: 

etl:
  control:
    drools:
      auto-update: true
      validation-enabled: true
```

### Data Panel Configuration

Key configuration in `etl-data-panel/src/main/resources/application.yml`:

```yaml
server:
  port: 8081

etl:
  control-panel:
    url: http://localhost:8080
  data:
    input-path: file:///data/input/transactions.csv
    output-path: file:///data/output/tagged_transactions.csv
```

## Monitoring

### Health Check

- Control Panel: `GET http://localhost:8080/actuator/health`
- Data Panel: `GET http://localhost:8081/actuator/health`

### Metrics

- Control Panel: `GET http://localhost:8080/actuator/metrics`
- Data Panel: `GET http://localhost:8081/actuator/metrics`

## Troubleshooting

### Common Issues

1. **Port Already in Use**
   - Change the port in `application.yml`
   - Kill the process using the port

2. **Rule Validation Error**
   - Check Drools rule syntax
   - Verify import statements
   - Ensure Transaction class is accessible

3. **CSV File Not Found**
   - Use absolute file paths
   - Ensure file exists and is readable
   - Check file permissions

4. **Flink Job Fails**
   - Check Flink logs for errors
   - Verify SQL syntax
   - Ensure data types match schema

### Logs

Logs are written to the console. Set logging level in `application.yml`:

```yaml
logging:
  level:
    com.etl: DEBUG
    org.apache.flink: INFO
    org.drools: INFO
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.

## Contact

For questions and support, please create an issue in the repository.
