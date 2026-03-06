@echo off
REM Flink Stream Tagging Job Runner

echo ==========================================
echo Flink SQL Drools Tagging Application
echo ==========================================

REM Build the project
echo Building project...
call mvn clean package -DskipTests -q
if %errorlevel% neq 0 (
    echo Build failed!
    exit /b 1
)

REM Create output directory
if not exist "output" mkdir output

REM Run the application with required JVM arguments
echo Starting Flink job...
java --add-opens java.base/java.util=ALL-UNNAMED ^
     --add-opens java.base/java.lang=ALL-UNNAMED ^
     --add-opens java.base/java.time=ALL-UNNAMED ^
     --add-opens java.base/java.math=ALL-UNNAMED ^
     -jar target\flink-sql-drools-tagging-1.0.0.jar ^
     --output ./output/tagged_transactions.csv ^
     --local true

if %errorlevel% equ 0 (
    echo.
    echo ==========================================
    echo Job completed successfully!
    echo Output: output\tagged_transactions.csv
    echo ==========================================
    
    echo.
    echo Sample output (first 10 lines):
    head -10 output\tagged_transactions.csv 2>nul || type output\tagged_transactions.csv | findstr /n "^" | findstr "^[1-10]:"
) else (
    echo.
    echo Job failed!
    exit /b 1
)
