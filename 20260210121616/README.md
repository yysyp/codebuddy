# Transaction Rule Processor

A distributed transaction data labeling application with H2 database, rule-based processing, and CSV output.

## Features

- **Embedded Database**: H2 database for transaction storage
- **Rule-Based Processing**: Dynamic rules for transaction labeling
- **Concurrent Processing**: Multi-threaded transaction processing with thread pools
- **Thread-Safe**: Proper locking mechanisms for data consistency
- **Observability**: TraceId for distributed tracing and logging
- **CSV Output**: Results exported to CSV format

## Technology Stack

- Java 17
- H2 Database (Embedded Mode)
- Maven
- Standard Java Concurrency (ExecutorService)
- Standard Logging (java.util.logging)

## Project Structure

```
transaction-rule-processor/
├── src/
│   ├── main/
│   │   ├── java/com/example/transaction/
│   │   │   ├── Application.java          # Main entry point
│   │   │   ├── config/
│   │   │   │   └── AppConfig.java         # Application configuration
│   │   │   ├── model/
│   │   │   │   └── Transaction.java       # Transaction model
│   │   │   ├── database/
│   │   │   │   └── DatabaseManager.java   # H2 database manager
│   │   │   ├── service/
│   │   │   │   ├── RuleProcessor.java      # Rule engine
│   │   │   │   └── TransactionProcessor.java # Main processor
│   │   │   └── output/
│   │   │       └── CsvWriter.java          # CSV output writer
│   │   └── resources/
│   │       ├── application.properties     # Application config
│   │       ├── logback.xml               # Logging config
│   │       └── transaction-rules.drl     # Rule definitions (for reference)
├── pom.xml                               # Maven configuration
├── run.bat                               # Windows run script
├── run.sh                                # Unix run script
└── README.md                             # This file
```

## Quick Start

### Prerequisites

- JDK 17 or higher
- Maven 3.6+

### Build the Project

```bash
mvn clean package
```

### Run the Application

**Windows:**
```bash
run.bat
```

**Unix/Linux/Mac:**
```bash
chmod +x run.sh
./run.sh
```

**Or directly with Maven:**
```bash
mvn exec:java -Dexec.mainClass="com.example.transaction.Application"
```

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Database Configuration
db.url=jdbc:h2:./data/transactions;MODE=PostgreSQL;AUTO_SERVER=TRUE
db.driver=org.h2.Driver
db.username=sa
db.password=xxxxxxxx

# Processing Configuration
parallelism=4

# Directories
rules.directory=rules
output.directory=output
```

## Transaction Rules

Rules are implemented in `src/main/java/com/example/transaction/service/RuleProcessor.java`:

- **HIGH_VALUE**: Amount >= 10000
- **MEDIUM_VALUE**: 1000 <= Amount < 10000
- **LOW_VALUE**: Amount < 1000
- **LUXURY**: Luxury or jewelry merchants
- **ELECTRONICS**: Electronics category
- **FOOD**: Food or grocery category
- **ONLINE**: Unknown location
- **SUSPICIOUS**: High value + unknown location
- **TRAVEL**: Travel category
- **BANKING**: Banking category
- **VERY_HIGH_RISK**: Very high value transactions (>= 20000)
- **NORMAL_RISK**: Small transactions (< 100)

## Output

Processed transactions are saved as CSV files in the `output` directory:
```
output/
└── transactions_<timestamp>.csv
```

## Sample Transactions

The application automatically creates sample transaction data on first run:

| Transaction ID | Amount | Merchant | Category | Location |
|---------------|--------|----------|----------|----------|
| T001          | 5000   | Luxury Store | Retail | New York |
| T002          | 15000  | Electronics Shop | Electronics | New York |
| T003          | 100    | Grocery Store | Grocery | Los Angeles |
| T004          | 25000  | Jewelry Store | Luxury | Chicago |
| T005          | 7500   | Hotel | Travel | Miami |
| T006          | 50     | Coffee Shop | Food | Seattle |
| T007          | 20000  | Car Dealership | Automotive | Chicago |
| T008          | 300    | Restaurant | Food | Los Angeles |
| T009          | 12000  | Online Retailer | E-commerce | Unknown |
| T010          | 500    | ATM Withdrawal | Banking | New York |

## Logging

Logs are written to:
- Console (INFO level)
- `logs/application.log` (DEBUG level for application)

Each log entry includes a TraceId for distributed tracing.

## Thread Safety

The application uses `ReentrantLock` for:
- Database operations
- CSV file writing
- Ensuring data consistency in concurrent scenarios

## Observability

- **TraceId**: Unique identifier for each operation flow
- **Structured Logging**: Formatted logs with context
- **Transaction Tracing**: Full trace from input to output

## Security

- Database password: `xxxxxxxx` (placeholder)
- No hardcoded credentials
- Data validation in rule processing

## Error Handling

- Comprehensive exception handling
- Graceful shutdown
- Resource cleanup with try-finally blocks

## Performance

- Concurrent processing with configurable thread pool
- Batch processing for efficiency
- Minimal resource usage

## Development

### Clean and Build

```bash
mvn clean package
```

### Run with Different Configuration

```bash
java -jar target/transaction-rule-processor-1.0.0.jar
```

## Troubleshooting

### Port Already in Use
No network ports used - standalone application.

### Out of Memory
Increase JVM heap size:
```bash
java -Xmx2g -jar target/transaction-rule-processor-1.0.0.jar
```

### Database Issues
Delete `./data/transactions.mv.db` and restart to recreate database.

## License

This project is provided as-is for educational and commercial use.
