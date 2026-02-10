# Quick Start Guide

## Overview
This is a Flink-based transaction labeling application that uses Drools for rule-based classification.

## Current Status
The application has been developed with the following features:
- Spring Boot 3.3.0 with Java 17
- Apache Flink 1.17.1 for distributed processing
- Drools 8.44.0.Final for rule engine
- H2 embedded database
- REST API with Swagger/OpenAPI documentation
- Sample data initialization (5 rules, 100 transactions)

## Known Compilation Issues
Due to Lombok annotation processor configuration issues in this environment, some compilation errors may occur. The following options are available:

### Option 1: Run with Maven Spring Boot Plugin (Recommended)
```bash
mvn spring-boot:run -DskipTests
```

### Option 2: Use IDE (IntelliJ IDEA or Eclipse)
Import the project as a Maven project and run directly from the IDE.

### Option 3: Fix Lombok Issues
Add the following to your IDE's Lombok plugin settings:
- Enable annotation processing
- Configure Lombok plugin path

## Key Features

### API Endpoints
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console (URL: jdbc:h2:mem:transactiondb, User: sa, Password: [empty])

### REST API
1. **Transactions**
   - POST /api/transactions - Create transaction
   - GET /api/transactions - List all transactions (paginated)
   - GET /api/transactions/{id} - Get by ID
   - GET /api/transactions/unprocessed - Get unprocessed transactions
   - POST /api/transactions/process - Process transactions with Flink and Drools

2. **Rules**
   - POST /api/rules - Create rule
   - GET /api/rules - List all rules
   - GET /api/rules/active - Get active rules
   - PUT /api/rules/{id} - Update rule
   - POST /api/rules/{id}/activate - Activate rule
   - POST /api/rules/{id}/deactivate - Deactivate rule
   - POST /api/rules/reload - Reload rules in engine
   - POST /api/rules/validate - Validate rule content

3. **Processing Logs**
   - GET /api/processing/logs - Get all logs
   - GET /api/processing/logs/latest - Get latest log

## Sample Rules
The application includes 5 pre-configured rules:

1. **High Amount Transaction**: Labels transactions > $10,000
2. **International Transaction**: Labels non-USA transactions
3. **Merchant Category Risk**: Labels GAMBLING, CASH_ADVANCE, CRYPTO transactions
4. **Suspicious Pattern**: Labels amounts like $9,999.99, $10,000, $99,999.99, $100,000
5. **Online Purchase**: Labels ONLINE_PURCHASE transactions

## Processing Workflow
1. Create transactions via REST API or use sample data
2. Process unprocessed transactions: POST /api/transactions/process
3. Flink processes transactions in parallel
4. Drools applies rules to each transaction
5. Labels are assigned based on matching rules
6. Results are saved to database

## Configuration
Edit `src/main/resources/application.yml` to configure:
- Flink parallelism
- Output directory
- Logging levels
- Resilience4j settings

## Testing Sample Data
The application automatically initializes:
- 5 rules with different priorities and categories
- 100 sample transactions with various characteristics

## Next Steps
1. Run the application
2. Access Swagger UI at http://localhost:8080/swagger-ui.html
3. Create new transactions or use existing sample data
4. Process transactions to apply Drools rules
5. View processed transactions with labels

## Troubleshooting
If the application fails to start:
1. Ensure Java 17 is installed: `java -version`
2. Check port 8080 is not in use
3. Verify Maven dependencies are downloaded: `mvn dependency:resolve`
4. Check logs in `logs/application.log`
