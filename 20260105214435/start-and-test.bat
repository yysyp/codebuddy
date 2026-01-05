@echo off
cd /d "c:\Users\yysyp\CodeBuddy\20260105214435"

echo Starting application...
start /B "D:\app\ideaIC-2023.2.6.win\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run -Dspring-boot.run.jvmArguments="-Xms512m -Xmx1024m -Djava.net.preferIPv4Stack=true --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.util.concurrent=ALL-UNNAMED" > app.log 2>&1

echo Waiting for application to start...
timeout /t 45

echo Testing API...
powershell -Command "Invoke-RestMethod -Uri 'http://localhost:8080/api/test/status' -Method Get | ConvertTo-Json"

echo.
echo Done.
pause
