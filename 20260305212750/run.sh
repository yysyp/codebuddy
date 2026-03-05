#!/bin/bash

set -e

echo "========================================="
echo "Transaction Tagging Application Launcher"
echo "========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}[ERROR] Maven is not installed or not in PATH${NC}"
    echo "Please install Maven first: https://maven.apache.org/download.cgi"
    exit 1
fi

# Check Java version
if ! java -version 2>&1 | grep -q "21\|22\|23"; then
    echo -e "${YELLOW}[WARNING] Java 21+ is recommended${NC}"
    java -version
fi

# Create necessary directories
mkdir -p logs data output

# Clean and compile
echo ""
echo "[1/3] Cleaning and compiling project..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo -e "${RED}[ERROR] Compilation failed${NC}"
    exit 1
fi
echo -e "${GREEN}[SUCCESS] Compilation successful${NC}"

# Package the application
echo ""
echo "[2/3] Packaging application..."
mvn package -DskipTests -q
if [ $? -ne 0 ]; then
    echo -e "${RED}[ERROR] Packaging failed${NC}"
    exit 1
fi
echo -e "${GREEN}[SUCCESS] Packaging successful${NC}"

# Run the application
echo ""
echo "[3/3] Running Flink Transaction Tagging Job..."
echo ""

java -jar target/flink-transaction-tagging-1.0.0.jar "$@"

if [ $? -ne 0 ]; then
    echo ""
    echo -e "${RED}[ERROR] Application failed${NC}"
    exit 1
fi

echo ""
echo "========================================="
echo -e "${GREEN}Application completed successfully${NC}"
echo "Output files are in: ./output/"
echo "========================================="
