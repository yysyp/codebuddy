@echo off
setlocal enabledelayedexpansion

cd /d "c:\Users\yysyp\CodeBuddy\20260105214435"

echo ========================================
echo Running with JVM Arguments
echo ========================================

set JVM_ARGS=--add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.util.concurrent=ALL-UNNAMED -Xms512m -Xmx1024m -Djava.net.preferIPv4Stack=true

echo JVM_ARGS: %JVM_ARGS%
echo.

"D:\app\ideaIC-2023.2.6.win\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run -Dspring-boot.run.jvmArguments="%JVM_ARGS%"

endlocal
pause
