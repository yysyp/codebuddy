@echo off
echo Building project...
call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    exit /b %ERRORLEVEL%
)

echo.
echo Starting application...
java -jar target/transaction-rule-processor-1.0.0.jar
