#!/bin/bash

echo "Building project..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo ""
echo "Starting application..."
java -jar target/transaction-rule-processor-1.0.0.jar
