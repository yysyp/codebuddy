@echo off
chcp 65001 >nul
title Flink Transaction Tagging - 编译
echo ========================================
echo Flink Transaction Tagging 项目编译
echo ========================================
echo.

REM 设置 JAVA_HOME
set JAVA_HOME=D:\app\azul-17.0.10
set PATH=%JAVA_HOME%\bin;%PATH%

echo 使用 Java 版本:
java -version
echo.

echo 开始 Maven 编译...
call mvn clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo 编译成功！
    echo ========================================
    echo JAR 文件位置: target\flink-transaction-tagging-1.0.0.jar
    echo.
) else (
    echo.
    echo ========================================
    echo 编译失败！请查看上方错误信息。
    echo ========================================
    echo.
)

pause
