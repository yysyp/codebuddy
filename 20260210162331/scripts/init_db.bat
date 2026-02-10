@echo off
REM H2 Database Initialization Script for Windows
REM This script initializes the H2 database with sample transaction data

setlocal enabledelayedexpansion

echo Starting H2 database initialization...

REM Database configuration
set DB_URL=jdbc:h2:mem:transactions;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
set DB_USER=sa
set DB_PASSWORD=xxxxxxxx
set INIT_SCRIPT=..\data\init.sql

REM H2 JAR path (adjust as needed)
set H2_JAR=%USERPROFILE%\.m2\repository\com\h2database\h2\2.2.224\h2-2.2.224.jar

REM Check if H2 JAR exists
if not exist "%H2_JAR%" (
    echo H2 JAR not found. Please run 'mvn dependency:resolve' first.
    echo Or manually download H2 from: https://repo1.maven.org/maven2/com/h2database/h2/2.2.224/
    exit /b 1
)

REM Run initialization script
echo Running initialization script: %INIT_SCRIPT%
java -cp "%H2_JAR%" org.h2.tools.RunScript -url "%DB_URL%" -user "%DB_USER%" -password "%DB_PASSWORD%" -script "%INIT_SCRIPT%"

if %ERRORLEVEL% NEQ 0 (
    echo Failed to initialize database!
    exit /b 1
)

echo H2 database initialization completed successfully!
endlocal
