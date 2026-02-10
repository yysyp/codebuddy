#!/bin/bash

echo "Starting Transaction Rule Engine Application..."

# Check if JAVA_HOME is set
if [ -z "$JAVA_HOME" ]; then
    echo "Warning: JAVA_HOME environment variable is not set."
    echo "Using java from PATH."
fi

# Check Java version
if ! command -v java &> /dev/null; then
    echo "Error: Java is not found in PATH."
    echo "Please install JDK 21 or higher."
    exit 1
fi

# Make Maven wrapper executable
chmod +x mvnw

# Run Maven build
echo "Building application with Maven..."
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "Build failed. Please check the error messages above."
    exit 1
fi

echo "Starting application..."
java -jar target/transaction-rule-engine-1.0.0.jar
