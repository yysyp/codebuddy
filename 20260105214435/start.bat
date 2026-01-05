@echo off
cd /d "c:\Users\yysyp\CodeBuddy\20260105214435"

echo Starting Ignite Spring Boot Demo...
echo.

set JAVA_OPTS=--add-opens java.base/java.nio=ALL-UNNAMED ^
--add-opens java.base/sun.nio.ch=ALL-UNNAMED ^
--add-opens java.base/java.lang.reflect=ALL-UNNAMED ^
--add-opens java.base/java.text=ALL-UNNAMED ^
--add-opens java.desktop/java.awt.font=ALL-UNNAMED ^
--add-opens java.base/java.util=ALL-UNNAMED ^
--add-opens java.base/java.util.concurrent=ALL-UNNAMED ^
--add-opens java.base/java.util.concurrent.locks=ALL-UNNAMED ^
-Xms512m ^
-Xmx1024m ^
-Djava.net.preferIPv4Stack=true

java %JAVA_OPTS% -jar target\ignite-spring-boot-demo-1.0.0.jar

pause
