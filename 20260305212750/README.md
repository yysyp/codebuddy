# Flink Transaction Tagging Application

A high-performance Flink application for tagging financial transaction data using Drools rules engine. The application reads transactions from an embedded H2 database, applies configurable business rules to tag transactions based on risk factors, and outputs the tagged results to Parquet files.

## Features

- **Flink 1.16.3**: Stream processing with exactly-once semantics
- **Drools Rule Engine**: Dynamic rule-based transaction tagging
- **H2 Database**: Embedded database for mock transaction storage
- **JSON Output**: Human-readable output format
- **Fault Tolerance**: Checkpointing and restart strategies
- **Distributed Tracing**: Trace IDs for transaction tracking
- **Dynamic Rule Reloading**: Rules can be updated at runtime

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        Transaction Tagging Application                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌───────────┐ │
│  │   H2 DB      │───▶│  Flink Job   │───▶│   Drools     │───▶│  Parquet  │ │
│  │ (Mock Data)  │    │  (JDBC Src)  │    │   Rules      │    │   Files   │ │
│  └──────────────┘    └──────────────┘    └──────────────┘    └───────────┘ │
│                            │                     │                          │
│                            ▼                     ▼                          │
│                     ┌──────────────┐    ┌──────────────┐                   │
│                     │  Table API   │    │  UDF/MapFunc │                   │
│                     └──────────────┘    └──────────────┘                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Prerequisites

- JDK 21 or higher
- Apache Maven 3.8+
- Windows/Linux/MacOS

## Project Structure

```
flink-transaction-tagging/
├── src/
│   ├── main/
│   │   ├── java/com/example/flink/
│   │   │   ├── TransactionTaggingApplication.java    # Main entry point
│   │   │   ├── config/
│   │   │   │   ├── DatabaseInitializer.java          # H2 DB setup
│   │   │   │   └── RuleEngineConfig.java             # Drools config
│   │   │   ├── job/
│   │   │   │   └── TransactionTaggingJob.java        # Flink job
│   │   │   ├── model/
│   │   │   │   ├── Transaction.java                  # Input model
│   │   │   │   └── TaggedTransaction.java            # Output model
│   │   │   └── udf/
│   │   │       ├── TransactionTaggingUDF.java        # SQL UDF
│   │   │       └── DroolsTaggingFunction.java        # DataStream function
│   │   └── resources/
│   │       ├── rules/
│   │       │   └── transaction-rules.drl             # Drools rules
│   │       ├── META-INF/
│   │       │   └── kmodule.xml                       # Drools config
│   │       └── logback.xml                           # Logging config
│   └── test/
├── data/                                               # H2 database files
├── output/                                             # Parquet output
├── logs/                                               # Application logs
├── pom.xml                                             # Maven config
├── run.bat                                             # Windows runner
└── README.md                                           # This file
```

## Rules Overview

The application applies the following rules to tag transactions:

| Rule | Condition | Tag | Risk Score |
|------|-----------|-----|------------|
| High Value | Amount > 50,000 | HIGH_VALUE | +20 |
| Very High Value | Amount > 100,000 | VERY_HIGH_VALUE | +30 |
| Off Hours | Outside 9AM-6PM UTC | OFF_HOURS | +15 |
| High Risk Location | Location contains XX/YY/ZZ | HIGH_RISK_LOCATION | +40 |
| International | Location not US/CN | INTERNATIONAL | +10 |
| Unknown Counterparty | Counterparty contains "Unknown" | UNKNOWN_COUNTERPARTY | +25 |
| Cash Related | Type is WITHDRAWAL/DEPOSIT | CASH_RELATED | +5 |
| Automated | Channel is API | AUTOMATED | 0 |
| Suspicious Pattern | Amount > 25K AND Off Hours | SUSPICIOUS_PATTERN | +35 |
| Risk Level | Score >= 50 | HIGH_RISK | - |
| Risk Level | Score 20-49 | MEDIUM_RISK | - |
| Risk Level | Score < 20 | LOW_RISK | - |

## Quick Start

### 1. Build and Run

**Windows:**
```batch
run.bat
```

**Linux/Mac:**
```bash
mvn clean package -DskipTests
java -jar target/flink-transaction-tagging-1.0.0.jar
```

### 2. Verify Output

After execution, check the output directory:
```
output/
└── tagged-transactions/
    └── yyyy-MM-dd/
        └── tagged-transactions-{part}-{uuid}.parquet
```

### 3. View Results

You can read the Parquet files using:
- Apache Spark
- Pandas (Python)
- Parquet CLI tools

Example with Python:
```python
import pandas as pd
df = pd.read_parquet('output/tagged-transactions/2026-03-05/')
print(df.head())
```

## Configuration

### Command Line Arguments

| Parameter | Default | Description |
|-----------|---------|-------------|
| `--db-url` | `jdbc:h2:file:./data/transactions` | H2 JDBC URL |
| `--db-user` | `sa` | Database username |
| `--db-password` | `xxxxxxxx` | Database password |
| `--output-path` | `./output/tagged-transactions` | Output directory |
| `--parallelism` | `2` | Flink parallelism |
| `--checkpoint-interval` | `60000` | Checkpoint interval (ms) |
| `--use-datastream` | `true` | Use DataStream API (vs SQL) |

### Example with Custom Config

```batch
run.bat --parallelism 4 --output-path ./custom-output --checkpoint-interval 30000
```

## Modifying Rules

Rules are defined in `src/main/resources/rules/transaction-rules.drl`.

### Adding a New Rule

```drl
rule "Custom Rule"
    salience 80
    when
        $t : Transaction(amount > 10000, channel == "MOBILE")
    then
        $t.addTag("MOBILE_HIGH_VALUE");
        $t.increaseRiskScore(15);
end
```

### Dynamic Rule Reloading

The application supports dynamic rule reloading:
1. Modify the `.drl` file while the application is running
2. Changes are automatically detected and loaded
3. New sessions use the updated rules

## Database Schema

### Transactions Table

```sql
CREATE TABLE transactions (
    transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id VARCHAR(50) NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    counterparty VARCHAR(100),
    transaction_time TIMESTAMP NOT NULL,
    currency VARCHAR(3) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    location VARCHAR(10),
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Monitoring

### Logs

Application logs are written to:
- Console (INFO level)
- `logs/application.log` (INFO level)

### Metrics

The application logs:
- Number of processed transactions
- Processing errors
- Checkpoint status

Example log output:
```
2026-03-05 21:30:00.000 [main] INFO  c.e.f.TransactionTaggingApplication - Application completed successfully
2026-03-05 21:30:00.000 [main] INFO  c.e.f.job.TransactionTaggingJob - Task 0 processed 1000 transactions
```

## Troubleshooting

### OutOfMemoryError

Increase heap size:
```batch
set MAVEN_OPTS=-Xmx4g
run.bat
```

### Port Already in Use

Flink uses random ports for internal communication. If you see port conflicts:
```batch
java -Dflink.rest.port=8082 -jar target/flink-transaction-tagging-1.0.0.jar
```

### Database Connection Issues

Check if the H2 database file is locked:
```batch
# Delete existing database
del /q data\*
# Re-run the application
run.bat
```

## Security Considerations

- Database password is set to `xxxxxxxx` (change for production)
- No sensitive data is logged
- Parquet files contain transaction data - secure the output directory

## Performance Tuning

### Increase Throughput

1. Increase parallelism:
   ```batch
   run.bat --parallelism 8
   ```

2. Adjust checkpoint interval:
   ```batch
   run.bat --checkpoint-interval 120000
   ```

3. Increase JVM heap size for large datasets

### Resource Requirements

| Dataset Size | Heap | Parallelism | Checkpoint Interval |
|--------------|------|-------------|---------------------|
| 10K records | 2GB | 2 | 60s |
| 100K records | 4GB | 4 | 60s |
| 1M+ records | 8GB+ | 8+ | 120s |

## Development

### Adding New Transaction Fields

1. Update `Transaction.java` model
2. Update `TaggedTransaction.java` model
3. Update database schema in `DatabaseInitializer.java`
4. Update Flink job SQL/table definitions
5. Add relevant rules in `.drl` file

### Testing

```bash
# Run unit tests
mvn test

# Run with test data
mvn exec:java -Dexec.mainClass="com.example.flink.TransactionTaggingApplication"
```

## License

This project is provided as-is for educational and demonstration purposes.

## Support

For issues or questions:
1. Check the logs in `logs/application.log`
2. Review the Flink web UI (if running locally)
3. Verify H2 database connectivity
