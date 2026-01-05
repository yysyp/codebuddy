@echo off
cd /d "c:\Users\yysyp\CodeBuddy\20260105214435"
call "D:\app\ideaIC-2023.2.6.win\plugins\maven\lib\maven3\bin\mvn.cmd" clean package -DskipTests
if %ERRORLEVEL% EQU 0 (
    echo Build successful! Starting application...
    call "D:\app\ideaIC-2023.2.6.win\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run
) else (
    echo Build failed with error code: %ERRORLEVEL%
)
pause
