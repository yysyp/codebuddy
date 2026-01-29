# Drools Rule Engine with Spring Boot 3

A dynamic rule engine implementation using Drools and Spring Boot 3. This project demonstrates how to integrate Drools rules engine into a Spring Boot application for dynamic business rule execution.

## Features

- **Dynamic Rule Execution**: Execute Drools rules dynamically via REST API
- **Multiple Rule Sets**: Three different rule domains:
  - **Pricing Rules**: Calculate discounts based on customer type and purchase history
  - **Order Processing Rules**: Validate and process orders with business logic
  - **Risk Assessment Rules**: Assess transaction risk and determine appropriate actions
- **Spring Boot 3**: Built on the latest Spring Boot 3.x with Java 17
- **Swagger/OpenAPI Documentation**: Complete API documentation with Swagger UI
- **TraceId Support**: Request tracing for observability
- **Error Handling**: Comprehensive exception handling with standardized response format
- **Validation**: Input validation with Jakarta Validation

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Drools 8.44.0.Final**
- **Maven** for dependency management
- **Swagger/OpenAPI 3** for API documentation
- **Lombok** for reducing boilerplate code

## Project Structure

```
drools-rule-engine/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/drools/
│   │   │       ├── common/          # Common utilities (response, exception, etc.)
│   │   │       ├── config/          # Configuration classes
│   │   │       ├── controller/     # REST controllers
│   │   │       ├── domain/          # Domain models (entities, facts, DTOs)
│   │   │       ├── interceptor/     # Request interceptors
│   │   │       └── service/         # Business logic services
│   │   └── resources/
│   │       ├── application.yml      # Application configuration
│   │       └── rules/               # Drools rule files (.drl)
│   │           ├── pricing.drl
│   │           ├── order-processing.drl
│   │           └── risk-assessment.drl
│   └── test/
├── test-data/                        # Test data files and scripts
│   ├── pricing-test.json
│   ├── order-test.json
│   ├── risk-assessment-test.json
│   ├── test-script.sh              # Unix/Linux test script
│   └── test-script.bat             # Windows test script
├── pom.xml                          # Maven configuration
└── README.md                        # This file
```

## Prerequisites

- JDK 17 or higher
- Maven 3.6 or higher
- (Optional) curl or Postman for API testing

## Installation and Setup

### 1. Clone or Navigate to Project

```bash
cd /path/to/drools-rule-engine
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

### 4. Access Swagger UI

Open your browser and navigate to:
```
http://localhost:8080/api/swagger-ui.html
```

## API Endpoints

### 1. Execute a Rule

**POST** `/api/v1/rules/execute`

Execute a specific rule with provided fact data.

**Request Body:**
```json
{
  "ruleName": "pricing",
  "factData": {
    "customerId": "CUST-001",
    "customerType": "VIP",
    "originalPrice": 1000.00,
    "purchaseCount": 25,
    "specialPromotion": true
  }
}
```

**Available Rule Names:**
- `pricing` - Calculate discounts based on customer type
- `order-processing` - Process and validate orders
- `risk-assessment` - Assess transaction risk

**Response:**
```json
{
  "success": true,
  "code": "200",
  "message": "Rule executed successfully",
  "timestamp": "2024-01-29T12:00:00Z",
  "traceId": "abc123...",
  "data": {
    "ruleName": "pricing",
    "matched": true,
    "executionTimeMs": 45,
    "result": {
      "originalPrice": 1000.00,
      "discountedPrice": 680.00,
      "discountRate": 0.20,
      "discountReason": "VIP customer discount",
      "rulesFired": 3
    }
  }
}
```

### 2. Get Available Rules

**GET** `/api/v1/rules/available`

Retrieve all available rule sets and their descriptions.

**Response:**
```json
{
  "success": true,
  "code": "200",
  "message": "Available rules retrieved successfully",
  "timestamp": "2024-01-29T12:00:00Z",
  "traceId": "abc123...",
  "data": {
    "pricing": "Calculate discounts based on customer type and purchase history",
    "order-processing": "Validate and process orders with business rules",
    "risk-assessment": "Assess transaction risk and determine appropriate actions"
  }
}
```

## Testing

### Using Test Scripts

The project includes test scripts in the `test-data/` directory:

**Unix/Linux:**
```bash
chmod +x test-data/test-script.sh
./test-data/test-script.sh
```

**Windows:**
```cmd
test-data\test-script.bat
```

### Manual Testing with curl

**Execute Pricing Rules:**
```bash
curl -X POST http://localhost:8080/api/v1/rules/execute \
  -H "Content-Type: application/json" \
  -d '{
    "ruleName": "pricing",
    "factData": {
      "customerId": "CUST-001",
      "customerType": "VIP",
      "originalPrice": 1000.00,
      "purchaseCount": 25,
      "specialPromotion": true
    }
  }'
```

**Execute Order Processing Rules:**
```bash
curl -X POST http://localhost:8080/api/v1/rules/execute \
  -H "Content-Type: application/json" \
  -d '{
    "ruleName": "order-processing",
    "factData": {
      "orderId": "ORD-12345",
      "customerId": "CUST-001",
      "status": "PENDING",
      "totalAmount": 150.50,
      "itemCount": 5,
      "highValue": false,
      "suspicious": false,
      "requiresApproval": false,
      "expressShipping": false,
      "warnings": []
    }
  }'
```

**Execute Risk Assessment Rules:**
```bash
curl -X POST http://localhost:8080/api/v1/rules/execute \
  -H "Content-Type: application/json" \
  -d '{
    "ruleName": "risk-assessment",
    "factData": {
      "requestId": "REQ-001",
      "userId": "USER-001",
      "riskLevel": "LOW",
      "riskScore": 0,
      "requiresVerification": false,
      "flagged": false,
      "failedAttempts": 0,
      "isNewUser": false,
      "unusualLocation": false
    }
  }'
```

## Rule Descriptions

### Pricing Rules (`pricing.drl`)

1. **VIP Customer Discount**: 20% discount for VIP customers
2. **Premium Customer Discount**: 10% discount for premium customers
3. **Regular Customer Volume Discount**: 5% discount for regular customers with 10+ purchases
4. **Special Promotion Discount**: Additional 15% discount when special promotion is active
5. **Minimum Price Protection**: Ensures price never goes below 10% of original
6. **Large Order Bonus**: Additional 5% discount for orders over $1000

### Order Processing Rules (`order-processing.drl`)

1. **Auto-approve Small Orders**: Auto-approve orders under $100
2. **Flag High Value Orders**: Flag orders over $5000 for manual review
3. **Detect Suspicious Orders**: Flag orders with unusual patterns
4. **Express Shipping High Value Review**: Require review for express shipping with high value
5. **Approve Medium Orders**: Auto-approve orders between $100-$1000
6. **Reject Invalid Amount Orders**: Reject orders with zero or negative amounts
7. **Large Item Count Warning**: Warn about large item counts
8. **Reject Express Shipping for Small Orders**: Reject express shipping for orders under $50

### Risk Assessment Rules (`risk-assessment.drl`)

1. **New User High Risk**: Flag new users with 3+ failed attempts as high risk
2. **Unusual Location Risk**: Flag users accessing from unusual locations
3. **Failed Attempts Increase Risk**: Increase risk score based on failed attempts
4. **Trusted User Low Risk**: Low risk for trusted users with clean history
5. **Critical Risk Extreme Attempts**: Block users with 5+ failed attempts
6. **New User Medium Risk**: Medium risk for new users with 1-2 failed attempts
7. **Single Failed Attempt Low Risk**: Low risk with challenge question
8. **New User Unusual Location**: Require phone verification for new users from unusual locations

## Response Format

### Success Response
```json
{
  "success": true,
  "code": "200",
  "message": "Success message",
  "timestamp": "2024-01-29T12:00:00Z",
  "traceId": "unique-trace-id",
  "data": {},
  "meta": {}
}
```

### Error Response
```json
{
  "success": false,
  "code": "400",
  "message": "Error message",
  "timestamp": "2024-01-29T12:00:00Z",
  "traceId": "unique-trace-id",
  "data": null,
  "errors": [
    {
      "field": "fieldName",
      "code": "ERROR_CODE",
      "message": "Error description"
    }
  ]
}
```

## Configuration

Application configuration is located in `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: drools-rule-engine

server:
  port: 8080
  servlet:
    context-path: /api

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

logging:
  level:
    com.example.drools: INFO
    org.drools: INFO
```

## Observability

- **TraceId**: Each request is assigned a unique traceId for tracking
- **Logging**: Structured logging with traceId correlation
- **Actuator Endpoints**: Health check and metrics at `/actuator`
  - Health: `http://localhost:8080/api/actuator/health`
  - Info: `http://localhost:8080/api/actuator/info`
  - Metrics: `http://localhost:8080/api/actuator/metrics`

## Security Considerations

This example focuses on rule engine functionality. For production use, consider adding:

- Authentication and authorization (Spring Security)
- Rate limiting
- Input sanitization
- HTTPS/TLS encryption
- API key management
- CORS configuration

## Architecture

The application follows a layered architecture:

1. **Controller Layer**: REST API endpoints
2. **Service Layer**: Business logic and rule execution
3. **Domain Layer**: Models (Entities, DTOs, Facts)
4. **Common Layer**: Cross-cutting concerns (response, exception handling)

## Future Enhancements

- Dynamic rule loading from database
- Rule versioning and rollback
- Rule performance monitoring
- Distributed rule execution
- Rule editor UI
- Audit logging for rule executions
- Integration with message queues for async processing

## Troubleshooting

### Drools Compilation Errors
If you see Drools compilation errors at startup:
- Check rule file syntax in `src/main/resources/rules/`
- Ensure all referenced classes exist and are properly imported
- Check for syntax errors in .drl files

### Port Already in Use
If port 8080 is already in use:
- Change the port in `application.yml`
- Or run: `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`

### Memory Issues
For large rule sets:
- Increase JVM heap size: `java -Xmx2g -Xms1g`
- Consider using Drools streaming mode for large datasets

## License

This project is provided as an example for educational purposes.

## Support

For issues or questions, please refer to:
- [Drools Documentation](https://docs.drools.org/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [OpenAPI Specification](https://swagger.io/specification/)
