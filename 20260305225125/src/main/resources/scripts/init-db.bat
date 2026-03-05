@echo off
REM Initialize H2 Database with sample transaction data

echo ==========================================
echo Transaction Database Initialization
echo ==========================================

REM Check if Maven is available
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Maven not found. Please install Maven and add it to PATH.
    exit /b 1
)

REM Create data directory
if not exist "..\..\..\..\..\data" mkdir "..\..\..\..\..\data"

REM Run the data generator
echo Initializing database with sample transactions...
mvn compile exec:java -Dexec.mainClass="com.example.flink.DataGenerator" -q

if %errorlevel% equ 0 (
    echo.
    echo ==========================================
    echo Database initialized successfully!
    echo Data directory: .\data\
    echo ==========================================
) else (
    echo.
    echo ERROR: Failed to initialize database
    exit /b 1
)
