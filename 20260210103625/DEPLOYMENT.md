# Transaction Labeling with Flink - Deployment Guide

## Prerequisites

- **JDK 17** or higher
- **Maven 3.6+**
- **Git** (for cloning)
- **Docker** (optional, for containerized deployment)

## Installation Steps

### 1. Clone or Extract
```bash
cd /path/to/workspace
git clone <repository-url>
cd transaction-labeling-flink
```

### 2. Build the Project
```bash
mvn clean package -DskipTests
```

### 3. Run the Application
```bash
# Option 1: Using Maven
mvn spring-boot:run

# Option 2: Using JAR
java -jar target/transaction-labeling-flink-1.0.0.jar

# Option 3: Using batch script (Windows)
run.bat

# Option 4: Using Docker
docker-compose up
```

## Initial Data

The application automatically initializes:
- **5 sample rules** with different priorities and categories
- **100 sample transactions** with various characteristics
- **Database indexes** for performance optimization

## Access Points

### Application Endpoints
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics

### H2 Console
- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:transactiondb`
- **Username**: `sa`
- **Password**: (leave empty)

## Quick Start Guide

### Step 1: Verify Installation
```bash
java -version  # Should show Java 17+
mvn -version   # Should show Maven 3.6+
```

### Step 2: Start Application
```bash
mvn spring-boot:run -DskipTests
```

Wait for the message: `Started TransactionLabelingApplication in X.XXX seconds`

### Step 3: Access Swagger UI
Open browser and navigate to: http://localhost:8080/swagger-ui.html

### Step 4: Test Create Transaction
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TEST001",
    "accountNumber": "ACC001",
    "amount": 15000.00,
    "currency": "USD",
    "transactionType": "PURCHASE",
    "merchantCategory": "RETAIL",
    "location": "New York",
    "countryCode": "USA",
    "status": "PENDING",
    "description": "Test transaction"
  }'
```

### Step 5: Process Transactions
```bash
curl -X POST http://localhost:8080/api/transactions/process \
  -H "Content-Type: application/json" \
  -d '{
    "batchSize": 10,
    "useParquetOutput": false,
    "outputDirectory": "./output"
  }'
```

### Step 6: View Processed Transactions
```bash
curl http://localhost:8080/api/transactions/unprocessed
```

## Configuration

### Environment Variables
```bash
export SERVER_PORT=8080
export FLINK_PARALLELISM=2
export OUTPUT_DIRECTORY=./output
```

### Application Properties
Edit `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

flink:
  parallelism: 2

output:
  directory: ./output

logging:
  level:
    root: INFO
    com.example.transactionlabeling: DEBUG
```

## Docker Deployment

### Build Docker Image
```bash
docker build -t transaction-labeling-flink:latest .
```

### Run Container
```bash
docker run -d \
  -p 8080:8080 \
  -v $(pwd)/output:/app/output \
  -v $(pwd)/logs:/app/logs \
  transaction-labeling-flink:latest
```

### Docker Compose
```bash
docker-compose up -d
```

## Production Deployment

### Recommended Changes

1. **Database**: Switch from H2 to PostgreSQL or MySQL
2. **Security**: Enable HTTPS and configure authentication
3. **Monitoring**: Set up centralized logging (ELK Stack)
4. **Flink Cluster**: Deploy to Flink cluster for scalability
5. **Load Balancer**: Add Nginx or AWS ALB
6. **CI/CD**: Implement automated deployment pipeline

### Configuration for Production

```yaml
spring:
  datasource:
    url: jdbc:postgresql://db-server:5432/transactiondb
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  jpa:
    hibernate.ddl-auto: validate

server:
  port: 8080
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}

logging:
  file:
    name: /var/log/transaction-labeling/app.log
```

## Monitoring

### Actuator Endpoints
```bash
# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# Info
curl http://localhost:8080/actuator/info

# Environment
curl http://localhost:8080/actuator/env
```

### Circuit Breaker Status
```bash
# Check if circuit breaker is open
curl http://localhost:8080/actuator/health
```

## Performance Tuning

### JVM Options
```bash
java -Xmx2G -Xms2G \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -jar transaction-labeling-flink.jar
```

### Flink Parallelism
```yaml
flink:
  parallelism: 4  # Increase based on CPU cores
```

### Batch Size
```yaml
processing:
  default-batch-size: 100
  max-batch-size: 1000
```

## Troubleshooting

### Application Won't Start

1. **Port in use**: Change server.port in application.yml
2. **Java version**: Ensure Java 17 is installed
3. **Maven issues**: Run `mvn clean install -U`
4. **Memory issues**: Increase JVM heap with `-Xmx2G`

### Flink Processing Fails

1. **Check parallelism**: Reduce if running out of resources
2. **Review logs**: Look for Flink exceptions
3. **Validate rules**: Use /api/rules/validate endpoint

### Drools Rules Not Working

1. **Rule syntax**: Validate with /api/rules/validate
2. **Active status**: Ensure rule is active
3. **Reload rules**: Call /api/rules/reload
4. **Check logs**: Review Drools error messages

### Performance Issues

1. **Database indexes**: Check if indexes are created
2. **Batch size**: Adjust batch size for optimal performance
3. **Connection pool**: Increase HikariCP pool size
4. **GC tuning**: Adjust JVM GC settings

## Backup & Recovery

### Database Backup
```bash
# For H2, backup is done via file copy
cp -r data/ backup/
```

### Log Backup
```bash
tar -czf logs-backup-$(date +%Y%m%d).tar.gz logs/
```

## Security Checklist

- [ ] Enable HTTPS in production
- [ ] Configure authentication and authorization
- [ ] Set up rate limiting
- [ ] Enable firewall rules
- [ ] Regular security updates
- [ ] Secure sensitive data
- [ ] Implement audit logging
- [ ] Use secrets management (HashiCorp Vault)
- [ ] Enable CSRF protection
- [ ] Configure CORS properly

## Maintenance

### Regular Tasks

1. **Daily**
   - Check application health
   - Review error logs
   - Monitor processing performance

2. **Weekly**
   - Review processing logs
   - Analyze rule effectiveness
   - Clean up old logs

3. **Monthly**
   - Update dependencies
   - Review and update rules
   - Performance tuning
   - Security audit

## Support

For issues or questions:
1. Check logs: `logs/application.log`
2. Review troubleshooting section
3. Check GitHub issues
4. Contact development team

## Documentation

- **API Documentation**: Swagger UI at /swagger-ui.html
- **Project Summary**: PROJECT_SUMMARY.md
- **Quick Start**: QUICK_START.md
- **README**: README.md
