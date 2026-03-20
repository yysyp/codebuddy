#!/bin/bash
# Build script for ETL Tagging System
# This script builds both Control Panel and Data Panel modules

echo "========================================"
echo "Building ETL Tagging System"
echo "========================================"

# Set Java home to JDK 17
export JAVA_HOME=/path/to/jdk17
export PATH=$JAVA_HOME/bin:$PATH

echo "Using Java: $JAVA_HOME"
java -version
echo

# Clean and build the project
echo "Cleaning and building project..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo
    echo "========================================"
    echo "Build FAILED!"
    echo "========================================"
    exit 1
fi

echo
echo "========================================"
echo "Build SUCCESSFUL!"
echo "========================================"
echo
echo "Modules built:"
echo "  - etl-control-panel: target/etl-control-panel-1.0.0-SNAPSHOT.jar"
echo "  - etl-data-panel: target/etl-data-panel-1.0.0-SNAPSHOT.jar"
echo
