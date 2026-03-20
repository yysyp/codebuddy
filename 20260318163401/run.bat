@echo off
REM Run script for ETL Tagging System
REM This script starts both Control Panel and Data Panel

echo ========================================
echo Starting ETL Tagging System
echo ========================================

REM Set Java home to JDK 17
set JAVA_HOME=D:\app\azul-17.0.10
set PATH=%JAVA_HOME%\bin;%PATH%

echo Using Java: %JAVA_HOME%
java -version
echo.

REM Start Control Panel
echo Starting Control Panel on port 8080...
start "ETL Control Panel" java -jar etl-control-panel\target\etl-control-panel-1.0.0-SNAPSHOT.jar

REM Wait for Control Panel to start
echo Waiting for Control Panel to start...
timeout /t 10 /nobreak > nul

REM Start Data Panel
echo Starting Data Panel on port 8081...
start "ETL Data Panel" java -jar etl-data-panel\target\etl-data-panel-1.0.0-SNAPSHOT.jar

echo.
echo ========================================
echo ETL Tagging System Started!
echo ========================================
echo.
echo Services running:
echo   - Control Panel: http://localhost:8080
echo   - Data Panel: http://localhost:8081
echo   - H2 Console: http://localhost:8080/h2-console
echo   - Swagger UI: http://localhost:8080/swagger-ui.html
echo.
echo Press any key to stop all services...
pause > nul

REM Stop services
echo.
echo Stopping services...
taskkill /FI "WINDOWTITLE eq ETL Control Panel*" /F
taskkill /FI "WINDOWTITLE eq ETL Data Panel*" /F

echo Services stopped.
