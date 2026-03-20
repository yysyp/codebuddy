@echo off
REM Build script for ETL Tagging System
REM This script builds both Control Panel and Data Panel modules

echo ========================================
echo Building ETL Tagging System
echo ========================================

REM Set Java home to JDK 17
set JAVA_HOME=D:\app\azul-17.0.10
set PATH=%JAVA_HOME%\bin;%PATH%

echo Using Java: %JAVA_HOME%
java -version
echo.

REM Clean and build the project
echo Cleaning and building project...
call mvn clean install -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo Build FAILED!
    echo ========================================
    exit /b 1
)

echo.
echo ========================================
echo Build SUCCESSFUL!
echo ========================================
echo.
echo Modules built:
echo   - etl-control-panel: target\etl-control-panel-1.0.0-SNAPSHOT.jar
echo   - etl-data-panel: target\etl-data-panel-1.0.0-SNAPSHOT.jar
echo.
