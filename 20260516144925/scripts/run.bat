@echo off
REM Command Wrapper Service - Windows Run Script
REM Usage: run.bat [encryption-password]
REM 
REM Example:
REM   run.bat mySecretPassword
REM   run.bat

setlocal

set "SCRIPT_DIR=%~dp0"
set "PROJECT_DIR=%SCRIPT_DIR%.."

REM Set default encryption password if not provided
if "%1"=="" (
    echo [WARN] No encryption password provided. Using empty password.
    set "ENCRYPTOR_PASSWORD="
) else (
    set "ENCRYPTOR_PASSWORD=%1"
)

REM Set Java home
set "JAVA_HOME=D:\app\zulu21.48.17-ca-jdk21.0.10"
set "PATH=%JAVA_HOME%\bin;%PATH%"

REM Set Maven home
set "MAVEN_HOME=D:\app\apache-maven"
set "PATH=%MAVEN_HOME%\bin;%PATH%"

cd /d "%PROJECT_DIR%"

echo ========================================
echo Command Wrapper Service
echo ========================================
echo Project Directory: %PROJECT_DIR%
echo Java Version: 
java -version
echo ========================================

REM Build the project
echo Building project...
call mvn clean package -DskipTests

if %ERRORLEVEL% neq 0 (
    echo Build failed!
    exit /b %ERRORLEVEL%
)

echo Build successful!
echo.

REM Run the application
echo Starting application...
echo Encryption Password: %ENCRYPTOR_PASSWORD%
echo.

java -jar target\command-wrapper-service-1.0.0.jar ^
    --jasypt.encryptor.password=%ENCRYPTOR_PASSWORD% ^
    --spring.profiles.active=dev

endlocal
