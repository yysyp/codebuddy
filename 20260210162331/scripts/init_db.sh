#!/bin/bash

# H2 Database Initialization Script
# This script initializes the H2 database with sample transaction data

set -e

echo "Starting H2 database initialization..."

# Database configuration
DB_URL="jdbc:h2:mem:transactions;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
DB_USER="sa"
DB_PASSWORD="xxxxxxxx"
INIT_SCRIPT="../data/init.sql"
H2_JAR="$HOME/.m2/repository/com/h2database/h2/2.2.224/h2-2.2.224.jar"

# Check if H2 JAR exists
if [ ! -f "$H2_JAR" ]; then
    echo "H2 JAR not found. Downloading..."
    mkdir -p "$HOME/.m2/repository/com/h2database/h2/2.2.224/"
    curl -L -o "$H2_JAR" "https://repo1.maven.org/maven2/com/h2database/h2/2.2.224/h2-2.2.224.jar"
fi

# Run initialization script
echo "Running initialization script: $INIT_SCRIPT"
java -cp "$H2_JAR" org.h2.tools.RunScript -url "$DB_URL" -user "$DB_USER" -password "$DB_PASSWORD" -script "$INIT_SCRIPT"

echo "H2 database initialization completed successfully!"
