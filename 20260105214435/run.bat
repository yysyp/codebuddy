@echo off
setlocal enabledelayedexpansion

cd /d "c:\Users\yysyp\CodeBuddy\20260105214435"

REM Set Java options for Ignite to work with Java 17
set JAVA_OPTS=--add-opens java.base/java.nio=ALL-UNNAMED ^
--add-opens java.base/sun.nio.ch=ALL-UNNAMED ^
--add-opens java.base/java.lang.reflect=ALL-UNNAMED ^
--add-opens java.base/java.text=ALL-UNNAMED ^
--add-opens java.desktop/java.awt.font=ALL-UNNAMED ^
--add-opens java.base/java.util=ALL-UNNAMED ^
--add-opens java.base/java.util.concurrent=ALL-UNNAMED ^
--add-opens java.base/java.util.concurrent.locks=ALL-UNNAMED

echo Starting Spring Boot application with Java 17 compatibility settings...
echo.

call "D:\app\ideaIC-2023.2.6.win\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run -Dspring-boot.run.jvmArguments="%JAVA_OPTS%"

if %ERRORLEVEL% NEQ 0 (
    echo Application failed to start with exit code: %ERRORLEVEL%
)

pause
