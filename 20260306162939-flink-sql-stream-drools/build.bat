@echo off
chcp 65001 >nul
title Flink Transaction Tagging - Build
echo ========================================
echo Flink Transaction Tagging Project Build
echo ========================================
echo.

REM Set JAVA_HOME
set JAVA_HOME=D:\app\azul-17.0.10
set PATH=%JAVA_HOME%\bin;%PATH%

echo Using Java version:
java -version
echo.

echo Starting Maven build...
call mvn clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Build successful!
    echo ========================================
    echo JAR file location: target\flink-transaction-tagging-1.0.0.jar
    echo.
) else (
    echo.
    echo ========================================
    echo Build failed! Please check the error messages above.
    echo ========================================
    echo.
)

pause
