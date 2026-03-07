# Flink Transaction Tagging

A Flink SQL application with Drools rule engine for transaction tagging. Supports SQL mode, DataStream mode, and Hybrid mode, with support for loading rules from DRL files, CSV table definitions, and Drools Decision Tables.

## Features

- **Multiple Processing Modes**: Supports SQL, DataStream, and Hybrid processing modes
- **Rule Engine**: Integrates Drools for separation of business rules and computation logic
- **Multiple Rule Sources**: Supports loading rules from DRL files, CSV table definitions, or Drools Decision Tables
- **CSV Support**: Reads transaction data in CSV format and outputs tagged results
- **Flexible Deployment**: Supports local execution and cluster deployment
- **UDF Integration**: Calls Drools rule engine through user-defined functions (UDF) in SQL mode

## Tech Stack

- Apache Flink 1.16.3
- Drools 8.44.0.Final
- Spring Boot 3.2.0
- Java 17+

## Quick Start

### Prerequisites

- Java 17+ (Zulu or Azul OpenJDK recommended)
- Maven 3.8+

### Build and Package

#### Method 1: Using Maven Commands

```bash
mvn clean package -DskipTests
```

#### Method 2: Using Batch Script (Windows)

```bash
# Double-click to run build.bat
build.bat
```

### Running the Application

#### Method 1: Using Batch Script (Recommended)

Double-click `run.bat` and select the mode:

1. SQL Mode (Recommended) - Supports DRL, CSV Table, and Decision Table rule sources
2. DataStream Mode
3. Hybrid Mode
4. Generate Test Data

#### Method 2: Command Line Execution

All running commands require JVM parameters on JDK 17+:

```bash
java --add-opens=java.base/java.util=ALL-UNNAMED ^
     --add-opens=java.base/java.lang=ALL-UNNAMED ^
     --add-opens=java.base/java.util.concurrent=ALL-UNNAMED ^
     --add-opens=java.base/java.lang.reflect=ALL-UNNAMED ^
     -jar target/flink-transaction-tagging-1.0.0.jar <mode> <input> <output> [rule-source] [rule-path]
```

Parameters:
- `mode`: Processing mode, options: `sql`, `datastream`, `hybrid`, `generate`
- `input`: Input CSV file path (for generate mode, this is the output path)
- `output`: Output CSV file path (not required for generate mode)
- `rule-source`: Optional, rule source type, options: `drl` (default, uses DRL file), `table` (uses CSV table definition), `decision-table` (uses Drools Decision Table)
- `rule-path`: Optional, rule file path, default is `src/main/resources/rules/transaction-tagging.drl` or `src/main/resources/rules/table-rules.csv` or `src/main/resources/rules/decision-table.xls`

#### 1. Generate Test Data

```bash
java --add-opens=java.base/java.util=ALL-UNNAMED ^
     --add-opens=java.base/java.lang=ALL-UNNAMED ^
     --add-opens=java.base/java.util.concurrent=ALL-UNNAMED ^
     -jar target/flink-transaction-tagging-1.0.0.jar generate <output-path>
```

Example:
```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar generate target/test-data/test.csv
```

#### 2. SQL Mode (Recommended)

Uses Flink SQL for declarative processing and calls Drools rule engine through UDF:

##### Using DRL Rule File (Default)

```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar sql <input-csv> <output-csv>
```

Example:
```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar sql src/main/resources/data/transactions.csv output/tagged_result.csv
```

##### Using CSV Table Rule Definition

```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar sql <input-csv> <output-csv> table <table-rules-path>
```

Example:
```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar sql src/main/resources/data/transactions.csv output/tagged_result.csv table src/main/resources/rules/table-rules.csv
```

##### Using Drools Decision Table

```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar sql <input-csv> <output-csv> decision-table <decision-table-path>
```

Example:
```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar sql src/main/resources/data/transactions.csv output/tagged_result.csv decision-table src/main/resources/rules/decision-table.xls
```

#### 3. DataStream Mode

Uses DataStream API integrated with Drools rule engine:

```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar datastream <input-csv> <output-csv>
```

#### 4. Hybrid Mode

Combines SQL and DataStream APIs:

```bash
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar hybrid <input-csv> <output-csv>
```

## Configuration

### Application Configuration (application.yml)

```yaml
flink:
  job:
    mode: sql                    # Processing mode: sql, datastream, hybrid
    parallelism: 2               # Parallelism
    checkpoint-interval: 60000   # Checkpoint interval (milliseconds)
    input-path: ""               # Input path
    output-path: ""              # Output path
    rule-source: drl             # Rule source: drl (DRL file), table (CSV table), decision-table (Drools Decision Table)
    table-rules-path: rules/table-rules.csv  # Table rules file path
    decision-table-path: rules/decision-table.xls  # Decision table file path
```

### Rule Configuration

The application supports three rule configuration methods:

#### 1. DRL Rule File (Traditional Method)

Rule file location: `src/main/resources/rules/transaction-tagging.drl`

This method writes Drools DRL rule files directly, suitable for complex rule logic.

#### 2. CSV Table Rule Definition (Recommended)

Rule file location: `src/main/resources/rules/table-rules.csv`

This method uses CSV tables to define rules, easier to maintain and understand, and business personnel can also understand it.

##### Table Rule Format

```csv
rule_name,field_name,operator,threshold_value,tag,priority,condition_type
HIGH_AMOUNT,amount,>,10000,HIGH_AMOUNT,100,simple
VERY_HIGH_AMOUNT,amount,>,50000,VERY_HIGH_AMOUNT,110,simple
HIGH_RISK,riskScore,>,50,HIGH_RISK,90,simple
INTERNATIONAL,countryCode,<>,US,INTERNATIONAL,45,simple
```

Field descriptions:
- `rule_name`: Rule name (unique identifier)
- `field_name`: Field name to check (amount, risk_score, country_code, transaction_type, etc.)
- `operator`: Comparison operator (>, >=, <, <=, =, !=, in, not_in)
- `threshold_value`: Threshold value (number, string, or comma-separated list)
- `tag`: Tag to add when matched
- `priority`: Priority (higher number = higher priority, used for primary_tag selection)
- `condition_type`: Condition type (simple: simple condition, compound: complex condition combination)

Supported comparison operators:
- `>` Greater than
- `>=` Greater than or equal to
- `<` Less than
- `<=` Less than or equal to
- `=` Equal to
- `!=` Not equal to
- `in` Included in list (for enumeration values)
- `not_in` Not included in list

#### 3. Drools Decision Table (Advanced)

Rule file location: `src/main/resources/rules/decision-table.xlsx` (Excel format)

Drools Decision Tables are spreadsheet-based rule definitions that provide a visual way to manage business rules. They compile to DRL format at runtime.

**Important Notes:** 
- Drools Decision Tables require Excel (.xls or .xlsx) format for proper functionality
- The application automatically detects the file format based on extension
- CSV-based decision tables are not fully supported by Drools and may produce parsing errors
- For production use, always use Excel format decision tables

**Fallback Behavior:** If the decision table fails to compile or has errors, the application will automatically fall back to using the default DRL rules (`src/main/resources/rules/transaction-tagging.drl`). This ensures the application continues to function even if there are issues with the decision table format.

**Running with Decision Tables:**

```bash
# Using run.bat, select option 3 for Decision Table
# Default path: src/main/resources/rules/decision-table.xlsx

# Or using command line:
java -jar target/flink-transaction-tagging-1.0.0.jar sql input.csv output.csv decision-table src/main/resources/rules/decision-table.xlsx
```

##### Creating a Decision Table

Drools Decision Tables must be in Excel (.xls) format with specific structure:

**Basic Structure:**

| Column | Description |
|--------|-------------|
| RuleSet | Rule set name |
| Import | Import statements (e.g., com.example.flink.model.Transaction) |
| RuleTable | Start of a rule table definition |
| CONDITION | Condition columns (LHS of rules) |
| ACTION | Action columns (RHS of rules) |
| | Rule-specific values |

**Example Decision Table Format:**

| RuleSet | | | |
|---------|---|---|---|
| TransactionRules | | | |
| Import | | | |
| com.example.flink.model.Transaction | | | |
| Import | | | |
| org.slf4j.Logger | | | |
| RuleTable High Amount Rules | | | |
| CONDITION | ACTION | | |
| $tx:Transaction( | $tx.addTag("") | | |
| $tx.amount > | | | |
| 10000 | HIGH_AMOUNT | | |
| 50000 | VERY_HIGH_AMOUNT | | |

**Key Points:**
- Decision tables must be in Excel format (.xls or .xlsx)
- The application automatically detects the format based on file extension
- Each `RuleTable` defines a set of related rules
- Conditions and Actions are specified in column headers
- Each row after the header represents a rule
- Empty cells mean that condition/action is not part of that rule

**Best Practices:**
- Group related rules in separate RuleTables
- Use meaningful names for RuleTables
- Keep decision tables focused on specific rule categories
- Test generated DRL to ensure correctness

**Advantages of Decision Tables:**
- Visual, business-friendly rule definition
- Easy to maintain and update
- Non-technical users can manage rules
- Automatic DRL generation
- Version control friendly

**When to Use:**
- Many similar rules with pattern-based conditions
- Business users need to manage rules
- Frequent rule updates expected
- Complex rule combinations needed

#### Built-in Rules

| Rule Name | Description | Tag |
|-----------|-------------|-----|
| HIGH_AMOUNT | Amount > 10000 | HIGH_AMOUNT |
| VERY_HIGH_AMOUNT | Amount > 50000 | VERY_HIGH_AMOUNT |
| HIGH_RISK | Risk score > 70 | HIGH_RISK |
| CRITICAL_RISK | Risk score > 90 | CRITICAL_RISK |
| VERY_HIGH_RISK | Risk score >= 80 | VERY_HIGH_RISK |
| TRANSFER | Transfer transaction | TRANSFER |
| PAYMENT | Payment transaction | PAYMENT |
| DEBIT | Debit transaction | DEBIT |
| CREDIT | Credit transaction | CREDIT |
| REFUND | Refund transaction | REFUND |
| INTERNATIONAL | International transaction | INTERNATIONAL |
| NO_COUNTRY | No country information | NO_COUNTRY |
| SUSPICIOUS | Suspicious transaction | SUSPICIOUS |
| HIGH_RISK_INTERNATIONAL | High-risk international transaction | HIGH_RISK_INTERNATIONAL |
| LOW_AMOUNT | Amount < 10 | LOW_AMOUNT |

### Input Data Format (CSV)

```csv
transaction_id,timestamp,amount,currency,transaction_type,risk_score,country,merchant_name,account_id
TXN001,2024-01-15T10:30:00,15000.00,USD,TRANSFER,85,US,ABC Corp,ACC123
TXN002,2024-01-15T11:45:00,250.50,CNY,PAYMENT,20,CN,XYZ Store,ACC456
```

Field descriptions:
- `transaction_id`: Unique transaction identifier
- `account_id`: Account ID
- `amount`: Transaction amount
- `currency`: Currency type
- `transaction_type`: Transaction type (TRANSFER, PAYMENT, DEBIT, CREDIT, REFUND)
- `counterparty_id`: Counterparty ID
- `counterparty_name`: Counterparty name
- `description`: Transaction description
- `transaction_time`: Transaction time
- `country_code`: Country code
- `ip_address`: IP address
- `device_id`: Device ID
- `risk_score`: Risk score (0-100)

### Output Data Format (CSV)

The output file contains the following fields (in addition to input fields):

```csv
transaction_id,account_id,amount,currency,transaction_type,counterparty_id,counterparty_name,description,transaction_time,country_code,ip_address,device_id,risk_score,tags,primary_tag,tag_count,processing_time,trace_id
TXN-001,ACC001,150.00,USD,DEBIT,MERCH001,Amazon,Online purchase,2024-01-15T10:30:00Z,US,192.168.1.1,DEV001,25,"DEBIT",DEBIT,1,2024-01-15T10:30:35.000Z,FLINK-abc123
```

New field descriptions:
- `tags`: All matched rule tags, comma-separated
- `primary_tag`: Primary tag (highest priority tag)
- `tag_count`: Total number of matched tags
- `processing_time`: Processing timestamp
- `trace_id`: Trace ID for troubleshooting

**Note:** Flink CSV output may contain some empty columns. In actual use, please focus on the three core fields: `tags`, `primary_tag`, and `tag_count`.

### Example Output

```csv
TXN-001,ACC001,150.00,USD,DEBIT,MERCH001,Amazon,Online purchase,2024-01-15T10:30:00Z,US,192.168.1.1,DEV001,25,"DEBIT",DEBIT,1,2024-01-15T10:30:35.000Z,FLINK-abc123
TXN-002,ACC002,25000.00,USD,TRANSFER,MERCH002,Bank Transfer,Wire transfer,2024-01-15T11:00:00Z,US,192.168.1.2,DEV002,85,"SUSPICIOUS,VERY_HIGH_RISK,HIGH_AMOUNT,HIGH_RISK,TRANSFER",SUSPICIOUS,5,2024-01-15T11:05:40.000Z,FLINK-def456
TXN-003,ACC003,5.50,USD,DEBIT,MERCH003,Starbucks,Coffee purchase,2024-01-15T11:15:00Z,US,192.168.1.3,DEV001,10,"DEBIT,LOW_AMOUNT",DEBIT,2,2024-01-15T11:20:45.000Z,FLINK-ghi789
TXN-004,ACC001,75000.00,EUR,TRANSFER,MERCH004,International Bank,International wire,2024-01-15T12:00:00Z,DE,192.168.1.4,DEV003,95,"HIGH_RISK_INTERNATIONAL,SUSPICIOUS,CRITICAL_RISK,VERY_HIGH_RISK,VERY_HIGH_AMOUNT,HIGH_AMOUNT,HIGH_RISK,TRANSFER,INTERNATIONAL",HIGH_RISK_INTERNATIONAL,9,2024-01-15T12:05:50.000Z,FLINK-jkl012
```

Result descriptions:
- TXN-001: Normal debit transaction, no special tags
- TXN-002: High amount high-risk transfer, marked as suspicious transaction
- TXN-003: Low amount debit transaction, marked as low amount
- TXN-004: High-risk international transfer, matched 9 tags

## Running Tests

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=RuleEngineServiceTest
```

## Project Structure

```
src/
├── main/
│   ├── java/com/example/flink/
│   │   ├── TransactionTaggingApplication.java  # Main application entry point
│   │   ├── config/
│   │   │   └── FlinkJobConfig.java             # Flink configuration class
│   │   ├── function/
│   │   │   ├── TransactionTaggingFunction.java # DataStream processing function
│   │   │   └── DroolsTaggingUDF.java           # Drools UDF
│   │   ├── job/
│   │   │   └── TransactionTaggingJob.java       # Job execution logic
│   │   ├── model/
│   │   │   ├── Transaction.java                 # Transaction data model
│   │   │   ├── TaggedTransaction.java          # Tagged transaction
│   │   │   └── TaggingResult.java              # Tagging result
│   │   ├── service/
│   │   │   ├── RuleEngineService.java          # Drools rule engine service
│   │   │   └── TableRuleParserService.java     # Table rule parser
│   │   └── util/
│   │       ├── CsvUtils.java                    # CSV utility class
│   │       └── TraceIdGenerator.java            # Trace ID generator
│   └── resources/
│       ├── application.yml                      # Application configuration
│       ├── logback.xml                           # Log configuration
│       ├── rules/
│       │   ├── transaction-tagging.drl          # DRL rules
│       │   ├── table-rules.csv                  # CSV table rules
│       │   └── decision-table.xls               # Drools Decision Table
│       └── data/
│           └── transactions.csv                 # Sample data
└── test/
    └── java/com/example/flink/                  # Unit tests
```

## Common Issues

### 1. JDK 17+ Serialization Issue

Flink uses Kryo serialization and encounters module restrictions on JDK 17+. JVM parameters must be added:

```bash
java --add-opens=java.base/java.util=ALL-UNNAMED ^
     --add-opens=java.base/java.lang=ALL-UNNAMED ^
     --add-opens=java.base/java.util.concurrent=ALL-UNNAMED ^
     -jar target/flink-transaction-tagging-1.0.0.jar <mode> <input> <output>
```

Or use the `run.bat` script, which automatically includes these parameters.

### 2. Compilation Error (ExceptionInInitializerError)

If you encounter a Maven compilation error, ensure the compiler plugin configuration in `pom.xml` includes:

```xml
<release>17</release>
<fork>true</fork>
<compilerArgs>
    <arg>--add-opens=java.base/java.lang=ALL-UNNAMED</arg>
    <arg>--add-opens=java.base/java.util=ALL-UNNAMED</arg>
</compilerArgs>
```

### 3. Checkpoint Failure

Ensure the output directory exists and has write permissions:

```bash
mkdir output
```

### 4. Rule File Not Found

Rule files must be located at:
- DRL file: `src/main/resources/rules/transaction-tagging.drl`
- CSV table: `src/main/resources/rules/table-rules.csv`
- Decision table: `src/main/resources/rules/decision-table.xls`

### 5. CSV Output Format Issue

Flink's CSV file output may contain some empty columns, which is normal. Focus on the following fields:
- `tags`: All matched rule tags (comma-separated)
- `primary_tag`: Primary tag (highest priority tag)
- `tag_count`: Total number of matched tags

### 6. How to Choose a Rule Source?

- **DRL File**: Suitable for complex rule logic, requires developer maintenance
- **CSV Table**: Suitable for simple conditional rules, business personnel can understand, recommended for general use
- **Decision Table**: Suitable for rule sets with many similar patterns, visual management, suitable for business user management

### 7. How to Customize Rules?

Using CSV table definitions to define rules is simpler:

1. Open `src/main/resources/rules/table-rules.csv`
2. Add or modify rule rows
3. Recompile and run the application

Example: Add a new rule to detect medium amount transactions
```csv
MEDIUM_AMOUNT,amount,>=,5000,MEDIUM_AMOUNT,80,simple
```

### 8. Performance Optimization Recommendations

- For large batch data processing, adjust the `flink.job.parallelism` parameter to increase parallelism
- Using CSV table rules is faster than DRL file parsing
- Ensure JVM heap memory is sufficient: `-Xmx2g -Xms2g`

## Quick Reference

### Common Commands

```bash
# Build project
mvn clean package -DskipTests

# Run SQL mode (using DRL rules)
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar sql input.csv output.csv

# Run SQL mode (using CSV table rules)
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar sql input.csv output.csv table src/main/resources/rules/table-rules.csv

# Run SQL mode (using Decision Table)
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar sql input.csv output.csv decision-table src/main/resources/rules/decision-table.xls

# Run DataStream mode
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar datastream input.csv output.csv

# Generate test data
java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED -jar target/flink-transaction-tagging-1.0.0.jar generate test.csv
```

### File Structure

```
Project Root/
├── build.bat                          # Build script
├── run.bat                            # Run script
├── pom.xml                            # Maven configuration
├── README.md                          # Project documentation
├── src/
│   └── main/
│       ├── java/com/example/flink/
│       │   ├── TransactionTaggingApplication.java  # Main application entry point
│       │   ├── config/
│       │   │   └── FlinkJobConfig.java             # Flink configuration
│       │   ├── function/
│       │   │   └── DroolsTaggingUDF.java           # Drools UDF
│       │   ├── job/
│       │   │   └── TransactionTaggingJob.java       # Job execution
│       │   ├── model/
│       │   │   └── Transaction.java                 # Transaction model
│       │   ├── service/
│       │   │   ├── RuleEngineService.java          # Rule engine service
│       │   │   └── TableRuleParserService.java     # Table rule parser
│       │   └── util/
│       │       └── CsvUtils.java                    # CSV utility
│       └── resources/
│           ├── application.yml                      # Application configuration
│           ├── rules/
│           │   ├── transaction-tagging.drl          # DRL rules
│           │   ├── table-rules.csv                  # CSV table rules
│           │   └── decision-table.xls               # Drools Decision Table
│           └── data/
│               └── transactions.csv                 # Test data
└── output/                           # Output directory
```

## Decision Table Best Practices

### Structure Guidelines

1. **Organize by Category**: Group related rules into separate RuleTables
2. **Clear Naming**: Use descriptive names for RuleTables
3. **Consistent Format**: Maintain consistent structure across tables
4. **Test Regularly**: Compile and test decision tables after changes

### Maintenance Tips

1. **Version Control**: Commit decision table files to git
2. **Documentation**: Add comments within the decision table
3. **Review Process**: Establish a review process for rule changes
4. **Backup**: Keep backups of working decision table versions

### Troubleshooting Decision Tables

If decision tables fail to compile:

1. **Check Format**: Ensure the file is in .xls format (not .xlsx)
2. **Verify Structure**: Check that RuleTable headers are correct
3. **Review Conditions**: Ensure conditions are valid Drools syntax
4. **Test DRL**: Compile to DRL and review the generated rules
5. **Check Dependencies**: Verify all imports are correct

## License

MIT License
