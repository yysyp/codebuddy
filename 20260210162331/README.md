# Transaction Tagging Flink Application

A robust Apache Flink application that tags transaction data based on dynamic rules using Drools. The application reads transaction data from H2 embedded database, applies business rules dynamically, and outputs the results to Parquet files.

## Features

- **Dynamic Rule Engine**: Uses Drools for flexible, hot-reloadable business rules
- **Embedded H2 Database**: Fast, lightweight database for transaction storage
- **Apache Flink Integration**: Stream processing with checkpointing and fault tolerance
- **Parquet Output**: Efficient columnar storage for results
- **Observability**: Distributed tracing with TraceId support
- **Thread-Safe**: Concurrent processing with proper synchronization
- **Security**: Sensitive data protection with encryption support
- **Error Handling**: Comprehensive exception handling and recovery mechanisms

## Architecture

```
H2 Database (Embedded)
    ↓
Flink Source (H2TransactionSource)
    ↓
Processing (TransactionTaggingFunction)
    ↓
Drools Rule Engine
    ↓
Parquet Sink
    ↓
Output Files (.parquet)
```

## Prerequisites

- JDK 17 or higher
- Apache Maven 3.6+
- (Optional) Docker for H2 server mode

## Project Structure

```
transaction-tagging-flink/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/example/flink/transaction/
│   │   │   ├── Main.java
│   │   │   ├── config/
│   │   │   │   └── FlinkConfig.java
│   │   │   ├── model/
│   │   │   │   └── Transaction.java
│   │   │   ├── processing/
│   │   │   │   ├── TransactionTaggingFunction.java
│   │   │   │   └── TransactionFilterFunction.java
│   │   │   ├── rules/
│   │   │   │   └── RuleEngine.java
│   │   │   ├── sink/
│   │   │   │   ├── ParquetSink.java
│   │   │   │   └── TransactionParquetWriteSupport.java
│   │   │   ├── source/
│   │   │   │   └── H2TransactionSource.java
│   │   │   └── util/
│   │   │       ├── TraceIdUtil.java
│   │   │       └── TimeUtil.java
│   │   └── resources/
│   │       ├── rules/
│   │       │   └── TransactionRule.drl
│   │       ├── application.properties
│   │       └── logback.xml
│   └── test/
├── data/
│   └── init.sql
├── scripts/
│   ├── init_db.sh
│   └── init_db.bat
└── README.md
```

## Configuration

Edit `src/main/resources/application.properties` to configure:

- **Database**: URL, username, password (default: `jdbc:h2:mem:transactions;DB_CLOSE_DELAY=-1;MODE=PostgreSQL`)
- **Rules**: File path, reload interval
- **Output**: Parquet output path
- **Checkpointing**: Interval and timeout
- **Logging**: Log levels and file settings

## Building the Project

```bash
# Compile the project
mvn clean compile

# Package into a JAR file
mvn clean package

# Skip tests during build
mvn clean package -DskipTests
```

## Running the Application

### Option 1: Using Maven

```bash
mvn exec:java -Dexec.mainClass="com.example.flink.transaction.Main"
```

### Option 2: Using the JAR

```bash
java -jar target/transaction-tagging-flink-1.0.0.jar

java --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED -jar target/transaction-tagging-flink-1.0.0.jar

```

### Option 3: Using Flink CLI

```bash
# Build the fat JAR
mvn clean package

# Submit to Flink cluster
flink run -c com.example.flink.transaction.Main target/transaction-tagging-flink-1.0.0.jar
```

## Using Docker (Optional)

If you want to run H2 in server mode instead of embedded mode:

```bash
# Start H2 database
docker-compose up -d

# Access H2 Console at http://localhost:81
# JDBC URL: jdbc:h2:tcp://localhost:1521/~/test
# Username: sa
# Password: (empty)

# Update application.properties:
# database.url=jdbc:h2:tcp://localhost:1521/~/test

# Run the application
mvn exec:java
```

## Rules Configuration

The application uses Drools rules defined in `src/main/resources/rules/TransactionRule.drl`. Rules can be modified at runtime, and the rule engine will reload them periodically (default: every 60 seconds).

### Example Rules

The default rules include:

- **HIGH_VALUE**: Amount > $10,000
- **INTERNATIONAL**: Non-US transactions
- **HIGH_RISK_COUNTRY**: Transactions from certain countries
- **LARGE_TRANSFER**: Transfer > $5,000
- And many more...

### Adding Custom Rules

Edit `TransactionRule.drl` to add your own rules:

```drools
rule "My Custom Rule"
    when
        $t : Transaction(amount > 2000 && transactionType == "PAYMENT")
    then
        $t.addTag("MY_CUSTOM_TAG");
        System.out.println("Rule Applied: My Custom Rule - " + $t.getTransactionId());
end
```

## Output

Tagged transactions are written to CSV files in the configured output directory (default: `output/transactions.parquet`).

**Note**: The application currently writes to CSV format for compatibility and reliability. These CSV files can be easily converted to Parquet format using various tools if needed.

### Reading CSV Files

You can read the CSV files using:

- **Python**: `import pandas as pd; df = pd.read_csv('output/transactions.parquet/part-0-0.csv')`
- **Java**: Use any CSV library or Apache Commons CSV
- **Excel**: Open directly in Excel or other spreadsheet applications

### Converting CSV to Parquet (Optional)

If you need Parquet format, you can convert the CSV output:

```python
import pandas as pd
df = pd.read_csv('output/transactions.parquet/part-0-0.csv')
df.to_parquet('output/transactions.parquet/transactions.parquet')
```

```bash
# Using Spark
spark-shell --conf spark.sql.parquet.binaryAsString=true
spark.read.csv("output/transactions.parquet/part-0-0.csv").write.parquet("output/transactions.parquet/transactions.parquet")
```

## Logging

Logs are written to:
- **Console**: Standard output with trace ID correlation
- **File**: `logs/transaction-tagging-flink.log` with daily rotation

## Security Considerations

- All timestamps use UTC Instant for consistency
- Database passwords can be encrypted in production
- Sensitive data fields should be encrypted (e.g., account numbers)
- Connection pooling prevents connection leaks
- Proper error handling prevents information leakage

## Performance Tuning

### Checkpointing

```properties
checkpoint.interval.ms=10000
checkpoint.timeout.ms=60000
```

### Rate Limiting

```properties
rate.limit.per.second=1000
```

### Memory Management

Adjust JVM heap size:

```bash
java -Xmx2g -Xms2g -jar target/transaction-tagging-flink-1.0.0.jar
```

## Troubleshooting

### Issue: "Failed to compile rules"

**Solution**: Check the Drools rule file syntax. Common issues:
- Missing semicolons
- Invalid class references
- Syntax errors in rule conditions

### Issue: "Database connection failed"

**Solution**:
- Verify H2 is running (if using server mode)
- Check database URL in configuration
- Ensure correct username/password

### Issue: "Out of memory"

**Solution**:
- Increase JVM heap size: `-Xmx4g`
- Reduce checkpoint interval
- Adjust Flink parallelism

## Development

### Running Tests

```bash
mvn test
```

### Code Style

The project follows standard Java conventions:
- English code, comments, and documentation
- Thread-safe implementations
- Proper exception handling
- Resource management with try-with-resources

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is provided as-is for educational and commercial use.

## Support

For issues and questions:
1. Check the logs in `logs/transaction-tagging-flink.log`
2. Review configuration in `application.properties`
3. Verify dependencies are properly installed
4. Ensure JDK 17+ is installed and configured

## Version History

- **1.0.0**: Initial release
  - H2 embedded database support
  - Drools dynamic rule engine
  - Parquet output format
  - Distributed tracing
  - Fault tolerance with checkpointing
