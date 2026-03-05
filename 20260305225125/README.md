# Flink SQL Drools Tagging Application

A high-performance, production-ready Flink SQL application that applies Drools-based rules to tag financial transaction data. The application reads transactions from an embedded H2 database, applies business rules for risk assessment and tagging, and outputs the results to CSV format.

## Architecture Overview

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   H2 Database   │────▶│   Flink SQL Job  │────▶│   CSV Output    │
│  (Transactions) │     │  + Drools Rules  │     │(Tagged Results) │
└─────────────────┘     └──────────────────┘     └─────────────────┘
                               │
                               ▼
                        ┌──────────────────┐
                        │   Tagging UDF    │
                        │ (Drools Engine)  │
                        └──────────────────┘
```

## Features

### Core Capabilities
- **Flink SQL Processing**: Uses Flink SQL with custom UDFs for declarative data processing
- **Drools Rule Engine**: Advanced rule-based tagging with 20+ predefined business rules
- **H2 Embedded Database**: Zero-configuration database with sample transaction data
- **CSV Output**: Generated tagged transaction files for downstream processing

### Technical Features
- **Thread-Safe Design**: Concurrent access handling with proper synchronization
- **Fault Tolerance**: Checkpointing and restart strategies for reliability
- **Observability**: Comprehensive logging with SLF4J/Log4j2
- **Error Handling**: Graceful degradation with error tagging
- **UTC Time Handling**: All timestamps use UTC (Instant) for consistency

### Rule Categories
1. **High-Value Transaction Rules**: Tag transactions based on amount thresholds
2. **Transaction Type Rules**: Identify wire transfers, cash, crypto transactions
3. **Channel-Based Rules**: Classify by online, mobile, ATM, branch channels
4. **Geographic Risk Rules**: Flag high-risk countries and offshore jurisdictions
5. **Suspicious Pattern Rules**: Detect round amounts and suspicious keywords
6. **Compound Risk Rules**: Complex multi-factor risk assessment

## Technology Stack

| Component        | Version        |
|------------------|----------------|
| Java             | 21+            |
| Apache Flink     | 1.18.1         |
| Drools           | 9.44.0.Final   |
| H2 Database      | 2.2.224        |
| Maven            | 3.8+           |

## Project Structure

```
├── src/
│   ├── main/
│   │   ├── java/com/example/flink/
│   │   │   ├── FlinkSqlTaggingJob.java      # Main Flink job
│   │   │   ├── DataGenerator.java           # Database initializer
│   │   │   ├── database/
│   │   │   │   └── H2DatabaseManager.java   # H2 database management
│   │   │   ├── model/
│   │   │   │   ├── Transaction.java         # Transaction entity
│   │   │   │   └── TaggedTransaction.java   # Output entity
│   │   │   ├── rules/
│   │   │   │   ├── DroolsRuleEngine.java    # Rule engine wrapper
│   │   │   │   └── TaggingResult.java       # Rule result holder
│   │   │   └── udf/
│   │   │       ├── TaggingUdf.java          # UDF with structured output
│   │   │       └── TaggingStringUdf.java    # UDF with string output
│   │   └── resources/
│   │       ├── rules/
│   │       │   └── transaction-rules.drl    # Drools rules file
│   │       ├── scripts/
│   │       │   ├── init-db.bat              # Windows DB init script
│   │       │   └── run-job.bat              # Windows run script
│   │       └── log4j2.xml                   # Logging configuration
├── data/                                      # H2 database files
├── output/                                    # CSV output directory
├── logs/                                      # Application logs
├── pom.xml                                    # Maven configuration
├── docker-compose.yml                         # Docker Compose for Flink cluster
└── README.md                                  # This file
```

## Quick Start

### Prerequisites
- JDK 21 or higher
- Apache Maven 3.8+
- Docker (optional, for cluster deployment)

### 1. Clone and Build

```bash
# Clone the repository
git clone <repository-url>
cd flink-sql-drools-tagging

# Build the project
mvn clean package -DskipTests
```

### 2. Run Locally

#### Option A: Using Maven (Recommended for Development)

```bash
# Initialize database with sample data
mvn compile exec:java -Dexec.mainClass="com.example.flink.DataGenerator"

# Run the Flink job
mvn compile exec:java -Dexec.mainClass="com.example.flink.FlinkSqlTaggingJob"
```

#### Option B: Using Scripts (Windows)

```batch
# Initialize database
src\main\resources\scripts\init-db.bat

# Run the job
src\main\resources\scripts\run-job.bat
```

#### Option C: Using JAR File

```bash
# Initialize database
java -cp target/flink-sql-drools-tagging-1.0.0.jar com.example.flink.DataGenerator

# Run Flink job
java -jar target/flink-sql-drools-tagging-1.0.0.jar
```

### 3. View Results

After the job completes, the tagged transactions will be in:
```
./output/tagged_transactions.csv
```

Sample output format:
```csv
transaction_id,account_id,counterparty_account,amount,currency,transaction_type,...
TXN001,ACC001,,150.00,USD,PAYMENT,ONLINE,US,...,LOW,STANDARD,DEFAULT_LOW_RISK_TAG
TXN004,ACC004,,150000.00,USD,WIRE_TRANSFER,BRANCH,US,...,HIGH,HIGH_VALUE,HIGH_VALUE_TRANSACTION
```

### 4. Run with Docker (Flink Cluster)

```bash
# Start Flink cluster
docker-compose up -d

# Build the JAR
mvn clean package -DskipTests

# Submit job to Flink cluster
docker exec -it flink-jobmanager flink run \
    /opt/flink/jars/flink-sql-drools-tagging-1.0.0.jar \
    --output /opt/flink/output/tagged_transactions.csv

# Access Flink Web UI
open http://localhost:8081

# Stop cluster
docker-compose down
```

## Configuration

### Command Line Arguments

| Argument      | Description                    | Default                          |
|---------------|--------------------------------|----------------------------------|
| `--output`    | Output CSV file path           | `./output/tagged_transactions.csv` |
| `--local`     | Run in local mode              | `true`                           |
| `--cluster`   | Run in cluster mode (Docker)   | `false`                          |

### Example with Custom Output

```bash
mvn compile exec:java -Dexec.mainClass="com.example.flink.FlinkSqlTaggingJob" \
    -Dexec.args="--output /tmp/my-output.csv --local true"
```

### Database Configuration

The H2 database is configured in `H2DatabaseManager.java`:
- **URL**: `jdbc:h2:file:./data/transaction_db`
- **Username**: `sa`
- **Password**: `xxxxxxxx`

### Logging Configuration

Logging is configured in `src/main/resources/log4j2.xml`:
- Console output: INFO level and above
- File output: `logs/application.log`
- Flink-specific logs: `logs/flink.log`

## Rule Reference

### High-Value Transaction Rules

| Rule                        | Condition                          | Tags                                    | Risk Level |
|-----------------------------|------------------------------------|-----------------------------------------|------------|
| HIGH_VALUE_TRANSACTION      | amount >= 100,000                  | HIGH_VALUE                              | HIGH       |
| VERY_HIGH_VALUE_TRANSACTION | amount >= 1,000,000                | VERY_HIGH_VALUE, REQUIRES_APPROVAL      | CRITICAL   |

### Transaction Type Rules

| Rule                        | Condition                          | Tags                                    | Risk Level |
|-----------------------------|------------------------------------|-----------------------------------------|------------|
| WIRE_TRANSFER_TAG           | type = WIRE_TRANSFER               | WIRE_TRANSFER                           | LOW        |
| CASH_TRANSACTION_TAG        | type = CASH                        | CASH                                    | MEDIUM     |
| CRYPTO_TRANSACTION_TAG      | type = CRYPTO                      | CRYPTO, BLOCKCHAIN                      | HIGH       |
| INTERNATIONAL_TRANSFER_TAG  | type = INTERNATIONAL_TRANSFER      | INTERNATIONAL                           | MEDIUM     |

### Channel Rules

| Rule                        | Condition                          | Tags                                    | Risk Level |
|-----------------------------|------------------------------------|-----------------------------------------|------------|
| ONLINE_CHANNEL_TAG          | channel = ONLINE                   | ONLINE, DIGITAL                         | LOW        |
| MOBILE_CHANNEL_TAG          | channel = MOBILE                   | MOBILE, DIGITAL                         | LOW        |
| ATM_CHANNEL_TAG             | channel = ATM                      | ATM                                     | LOW        |
| BRANCH_CHANNEL_TAG          | channel = BRANCH                   | BRANCH, IN_PERSON                       | LOW        |

### Geographic Risk Rules

| Rule                        | Condition                          | Tags                                    | Risk Level |
|-----------------------------|------------------------------------|-----------------------------------------|------------|
| HIGH_RISK_COUNTRY           | country in (IR, KP, SY, etc.)      | HIGH_RISK_COUNTRY, SANCTIONS_CHECK      | HIGH       |
| OFFSHORE_TRANSACTION        | country in (KY, BS, PA, CH)        | OFFSHORE                                | MEDIUM     |

### Compound Risk Rules

| Rule                        | Condition                          | Tags                                    | Risk Level |
|-----------------------------|------------------------------------|-----------------------------------------|------------|
| HIGH_RISK_COMBINATION_CASH  | type=CASH AND amount>=50,000       | LARGE_CASH, AML_CHECK                   | CRITICAL   |
| OFFSHORE_HIGH_VALUE         | offshore AND amount>=100,000       | OFFSHORE_HIGH_VALUE, TAX_HAVEN          | CRITICAL   |

## Extending the Rules

To add new rules, edit `src/main/resources/rules/transaction-rules.drl`:

```drl
rule "MY_NEW_RULE"
    salience 50
    when
        $t : Transaction(
            amount >= 50000,
            transactionType.equalsIgnoreCase("PAYMENT")
        )
        $r : TaggingResult()
    then
        $r.addTag("MY_TAG");
        $r.updateRiskLevel("HIGH");
        $r.recordAppliedRule("MY_NEW_RULE");
end
```

## Troubleshooting

### Issue: Database Connection Failed
```
Solution: Check if the data directory exists and has write permissions.
mkdir -p data && chmod 755 data
```

### Issue: Out of Memory
```
Solution: Increase JVM heap size
export JAVA_OPTS="-Xmx4g -Xms2g"
mvn exec:java ...
```

### Issue: Drools Compilation Error
```
Solution: Check the syntax in transaction-rules.drl
Look for detailed errors in logs/application.log
```

### Issue: Port Already in Use (Docker)
```
Solution: Change port mapping in docker-compose.yml
ports:
  - "8082:8081"  # Use port 8082 instead of 8081
```

## Security Considerations

1. **Password**: Default database password is `xxxxxxxx`. Change in production.
2. **Data**: No sensitive data is logged by default.
3. **Rules**: Rules are loaded from classpath; ensure they are not tampered with.
4. **Output**: CSV files contain transaction data; secure appropriately.

## Performance Tuning

### For Large Datasets

1. **Increase Flink Parallelism**:
```java
env.setParallelism(4);
```

2. **Adjust Checkpointing Interval**:
```java
env.enableCheckpointing(300000); // 5 minutes
```

3. **Optimize JDBC Fetch Size**:
```sql
'scan.fetch-size' = '1000'
```

### Memory Settings

```bash
export JAVA_OPTS="-Xmx8g -Xms4g -XX:+UseG1GC"
```

## Monitoring and Observability

### Key Metrics

| Metric                      | Description                          | Location           |
|-----------------------------|--------------------------------------|--------------------|
| Processed Transactions      | Total transactions processed         | Application logs   |
| Processing Latency          | Average time per transaction         | Application logs   |
| Error Count                 | Number of processing errors          | Application logs   |
| Rules Fired                 | Count of rule executions             | DEBUG logs         |

### Flink Web UI

Access at `http://localhost:8081` when running with Docker.

## License

MIT License - See LICENSE file for details

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## Support

For issues and questions:
- Create an issue in the repository
- Check existing troubleshooting section
- Review application logs in `logs/` directory
