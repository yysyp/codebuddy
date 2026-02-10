@echo off
echo Starting Transaction Rule Engine Application...

REM Check if JAVA_HOME is set
if "%JAVA_HOME%"=="" (
    echo Error: JAVA_HOME environment variable is not set.
    echo Please set JAVA_HOME to JDK 21 or higher.
    pause
    exit /b 1
)

REM Check Java version
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not found in PATH.
    pause
    exit /b 1
)

REM Run Maven build and run
echo Building application with Maven...
call mvnw.cmd clean package -DskipTests

if errorlevel 1 (
    echo Build failed. Please check the error messages above.
    pause
    exit /b 1
)

echo Starting application...
java -jar target\transaction-rule-engine-1.0.0.jar

pause
