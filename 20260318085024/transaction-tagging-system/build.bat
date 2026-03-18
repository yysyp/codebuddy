@echo off
REM Build script for Transaction Tagging System on Windows
REM Usage: build.bat [clean|package|install|test]

setlocal EnableDelayedExpansion

set JAVA_HOME=D:\app\azul-17.0.10
set PATH=%JAVA_HOME%\bin;%PATH%

echo ============================================
echo Transaction Tagging System - Build Script
echo ============================================
echo.

REM Check Java version
java -version
if errorlevel 1 (
    echo ERROR: Java is not properly configured
    exit /b 1
)

echo.
echo Java home: %JAVA_HOME%
echo Current directory: %CD%
echo.

REM Set Maven goals based on argument
set GOAL=%1
if "%GOAL%"=="" set GOAL=install

echo Executing Maven goal: %GOAL%
echo.

REM Run Maven
call mvn clean %GOAL% -DskipTests

if errorlevel 1 (
    echo.
    echo ============================================
    echo BUILD FAILED
    echo ============================================
    exit /b 1
)

echo.
echo ============================================
echo BUILD SUCCESSFUL
echo ============================================
echo.

REM List generated artifacts
if "%GOAL%"=="install" (
    echo Generated artifacts:
    echo   - common\target\common-1.0.0-SNAPSHOT.jar
    echo   - control-panel\target\control-panel-1.0.0-SNAPSHOT.jar
    echo   - data-panel\target\data-panel-1.0.0-SNAPSHOT.jar
    echo.
)

endlocal
