# Transaction Rule Engine

A Java Spring Boot application for financial transaction data tagging based on dynamic rules using Drools rule engine, Apache Flink for distributed stream processing, and Parquet file generation.

## Prerequisites

- JDK 17+ (project configured for JDK 21 but works with 17)
- Maven 3.8+

## Quick Start

```bash
# Build the project
mvnw.cmd clean package -DskipTests

# Run the application
java -jar target/transaction-rule-engine-1.0.0.jar
```

Or use the provided scripts:
- Windows: `run.bat`
- Linux/Mac: `run.sh`

## Configuration

Edit `src/main/resources/application.yml` to configure:
- Drools rules directory and reload interval
- Flink parallelism and checkpointing
- Parquet output directory
- Rate limiting and circuit breaker settings

## API Documentation

Once running, access Swagger UI at: http://localhost:8080/swagger-ui.html

## Architecture

- **H2 Database**: Embedded in-memory database for transaction storage
- **Drools Rule Engine**: Dynamic rule loading and execution for transaction tagging
- **Apache Flink**: Distributed stream processing
- **Parquet**: Columnar file format for efficient data storage

## Features

- RESTful API with Swagger documentation
- Dynamic rule loading without application restart
- Distributed transaction processing with Flink
- Circuit breakers, rate limiters, and retry logic
- Trace ID for request tracking
- Comprehensive logging

## Project Status

This is a comprehensive implementation with all required components. Some files may need minor adjustments based on your specific Java version and Maven environment.
