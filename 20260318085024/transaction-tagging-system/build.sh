#!/bin/bash
# Build script for Transaction Tagging System on Linux/Mac
# Usage: ./build.sh [clean|package|install|test]

set -e

echo "============================================"
echo "Transaction Tagging System - Build Script"
echo "============================================"
echo

# Check Java version
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    exit 1
fi

java -version

echo
echo "Current directory: $(pwd)"
echo

# Set Maven goals based on argument
GOAL=${1:-package}

echo "Executing Maven goal: $GOAL"
echo

# Run Maven
mvn $GOAL -DskipTests=false -Dmaven.test.skip=false

echo
echo "============================================"
echo "BUILD SUCCESSFUL"
echo "============================================"
echo

# List generated artifacts
if [ "$GOAL" = "package" ]; then
    echo "Generated artifacts:"
    echo "  - common/target/common-1.0.0-SNAPSHOT.jar"
    echo "  - control-panel/target/control-panel-1.0.0-SNAPSHOT.jar"
    echo "  - data-panel/target/data-panel-1.0.0-SNAPSHOT.jar"
    echo
fi
