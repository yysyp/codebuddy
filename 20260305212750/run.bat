@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

echo =========================================
echo Transaction Tagging Application Launcher
echo =========================================

REM Check if Maven is installed
mvn -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven is not installed or not in PATH
    echo Please install Maven first: https://maven.apache.org/download.cgi
    exit /b 1
)

REM Check if Java 21+ is installed
java -version 2>&1 | findstr "21\." >nul
if %ERRORLEVEL% NEQ 0 (
    java -version 2>&1 | findstr "openjdk version" >nul
    if %ERRORLEVEL% NEQ 0 (
        echo [WARNING] Java version check failed, continuing anyway...
    )
)

REM Create necessary directories
if not exist "logs" mkdir logs
if not exist "data" mkdir data
if not exist "output" mkdir output

REM Clean and compile
echo.
echo [1/3] Cleaning and compiling project...
call mvn clean compile -q
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed
    exit /b 1
)
echo [SUCCESS] Compilation successful

REM Package the application
echo.
echo [2/3] Packaging application...
call mvn package -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Packaging failed
    exit /b 1
)
echo [SUCCESS] Packaging successful

REM Run the application
echo.
echo [3/3] Running Flink Transaction Tagging Job...
echo.

java -jar target\flink-transaction-tagging-1.0.0.jar %*

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Application failed with exit code %ERRORLEVEL%
    exit /b 1
)

echo.
echo =========================================
echo Application completed successfully
echo Output files are in: .\output\
echo =========================================

endlocal
