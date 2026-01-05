@echo off
setlocal enabledelayedexpansion

cd /d "c:\Users\yysyp\CodeBuddy\20260105214435"

echo ========================================
echo Starting Ignite Spring Boot Demo
echo ========================================
echo.

REM Set Java options for Ignite
set JAVA_OPTS=--add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED --add-opens java.desktop/java.awt.font=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.util.concurrent=ALL-UNNAMED --add-opens java.base/java.util.concurrent.locks=ALL-UNNAMED
set MEM_OPTS=-Xms512m -Xmx1024m
set NET_OPTS=-Djava.net.preferIPv4Stack=true

echo Java Options: %JAVA_OPTS% %MEM_OPTS% %NET_OPTS%
echo.

REM Run with Maven
"D:\app\ideaIC-2023.2.6.win\plugins\maven\lib\maven3\bin\mvn.cmd" clean spring-boot:run -Dspring-boot.run.jvmArguments="%JAVA_OPTS% %MEM_OPTS% %NET_OPTS%"

endlocal
pause
