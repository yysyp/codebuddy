#!/bin/bash
# Command Wrapper Service - Linux/Mac Run Script
# Usage: ./run.sh [encryption-password]
#
# Example:
#   ./run.sh mySecretPassword
#   ./run.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# Set default encryption password if not provided
ENCRYPTOR_PASSWORD="${1:-}"

if [ -z "$ENCRYPTOR_PASSWORD" ]; then
    echo "[WARN] No encryption password provided. Using empty password."
fi

cd "$PROJECT_DIR"

echo "========================================"
echo "Command Wrapper Service"
echo "========================================"
echo "Project Directory: $PROJECT_DIR"
echo "Java Version:"
java -version
echo "========================================"

# Build the project
echo "Building project..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo "Build successful!"
echo ""

# Run the application
echo "Starting application..."
echo "Encryption Password: ${ENCRYPTOR_PASSWORD:-(not shown)}"
echo ""

java -jar target/command-wrapper-service-1.0.0.jar \
    --jasypt.encryptor.password="$ENCRYPTOR_PASSWORD" \
    --spring.profiles.active=dev
