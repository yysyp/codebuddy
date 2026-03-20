#!/bin/bash
# Run script for ETL Tagging System
# This script starts both Control Panel and Data Panel

echo "========================================"
echo "Starting ETL Tagging System"
echo "========================================"

# Set Java home to JDK 17
export JAVA_HOME=/path/to/jdk17
export PATH=$JAVA_HOME/bin:$PATH

echo "Using Java: $JAVA_HOME"
java -version
echo

# Start Control Panel
echo "Starting Control Panel on port 8080..."
java -jar etl-control-panel/target/etl-control-panel-1.0.0-SNAPSHOT.jar &
CONTROL_PID=$!
echo "Control Panel PID: $CONTROL_PID"

# Wait for Control Panel to start
echo "Waiting for Control Panel to start..."
sleep 10

# Start Data Panel
echo "Starting Data Panel on port 8081..."
java -jar etl-data-panel/target/etl-data-panel-1.0.0-SNAPSHOT.jar &
DATA_PID=$!
echo "Data Panel PID: $DATA_PID"

echo
echo "========================================"
echo "ETL Tagging System Started!"
echo "========================================"
echo
echo "Services running:"
echo "  - Control Panel: http://localhost:8080"
echo "  - Data Panel: http://localhost:8081"
echo "  - H2 Console: http://localhost:8080/h2-console"
echo "  - Swagger UI: http://localhost:8080/swagger-ui.html"
echo
echo "PIDs:"
echo "  - Control Panel: $CONTROL_PID"
echo "  - Data Panel: $DATA_PID"
echo
echo "Press Ctrl+C to stop all services..."

# Trap Ctrl+C to stop services
trap "echo 'Stopping services...'; kill $CONTROL_PID $DATA_PID; exit" INT TERM

# Wait for services
wait
