@echo off
REM Quick Start Script for ETL Tagging System

echo ========================================
echo ETL Tagging System - Quick Start
echo ========================================
echo.

echo Step 1: Starting Control Panel on port 8080...
start "ETL Control Panel" cmd /k "cd c:\Users\yysyp\CodeBuddy\20260318163401 && java -jar etl-control-panel\target\etl-control-panel-1.0.0-SNAPSHOT.jar"

echo Waiting 20 seconds for Control Panel to start...
timeout /t 20 /nobreak > nul

echo.
echo Step 2: Starting Data Panel on port 8081...
start "ETL Data Panel" cmd /k "cd c:\Users\yysyp\CodeBuddy\20260318163401 && java -jar etl-data-panel\target\etl-data-panel-1.0.0-SNAPSHOT.jar"

echo Waiting 10 seconds for Data Panel to start...
timeout /t 10 /nobreak > nul

echo.
echo ========================================
echo Services Started Successfully!
echo ========================================
echo.
echo Control Panel: http://localhost:8080
echo Data Panel: http://localhost:8081
echo.
echo Swagger UI: http://localhost:8080/swagger-ui.html
echo H2 Console: http://localhost:8080/h2-console
echo   - JDBC URL: jdbc:h2:mem:etl_control
echo   - Username: sa
echo   - Password: (empty)
echo.
echo ========================================
echo Quick Test Commands:
echo ========================================
echo.
echo Test Control Panel Health:
echo   curl http://localhost:8080/actuator/health
echo.
echo Get Published Rules:
echo   curl http://localhost:8080/api/v1/rules/published
echo.
echo Execute Tagging Job:
echo   curl -X POST "http://localhost:8081/api/v1/jobs/tagging/execute"
echo.
echo Press any key to open Swagger UI in browser...
pause > nul
start http://localhost:8080/swagger-ui.html
