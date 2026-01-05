@echo off
setlocal

cd /d "c:\Users\yysyp\CodeBuddy\20260105214435"

echo Starting application...

set MAVEN_OPTS=--add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.util.concurrent=ALL-UNNAMED -Xms512m -Xmx1024m -Djava.net.preferIPv4Stack=true

start /B "D:\app\ideaIC-2023.2.6.win\plugins\maven\lib\maven3\bin\mvn.cmd" clean spring-boot:run > app-output.log 2>&1

echo Application starting... Check app-output.log for details.
echo Waiting 30 seconds for startup...

timeout /t 30

type app-output.log

endlocal
pause
