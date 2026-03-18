@echo off
REM Run script for Transaction Tagging System on Windows
REM Usage: run.bat [control-panel|data-panel|all]

setlocal EnableDelayedExpansion

set JAVA_HOME=D:\app\azul-17.0.10
set PATH=%JAVA_HOME%\bin;%PATH%

set COMPONENT=%1
if "%COMPONENT%"=="" set COMPONENT=all

echo ============================================
echo Transaction Tagging System - Run Script
echo ============================================
echo.
echo Component: %COMPONENT%
echo.

if "%COMPONENT%"=="control-panel" goto run_control_panel
if "%COMPONENT%"=="data-panel" goto run_data_panel
if "%COMPONENT%"=="all" goto run_all
goto usage

:run_control_panel
echo Starting Control Panel...
echo.
cd control-panel
java -jar target\control-panel-1.0.0-SNAPSHOT.jar
goto end

:run_data_panel
echo Starting Data Panel...
echo.
echo Note: Data Panel requires Flink runtime. Running in standalone mode.
echo.
cd data-panel
java -cp "target\data-panel-1.0.0-SNAPSHOT.jar" com.transaction.tagging.datapanel.DataPanelApplication
goto end

:run_all
echo Starting all components...
echo.
echo Starting Control Panel in background...
start "Control Panel" cmd /c "cd control-panel && java -jar target\control-panel-1.0.0-SNAPSHOT.jar"
echo Waiting for Control Panel to start...
timeout /t 10 /nobreak > nul
echo.
echo Starting Data Panel...
cd data-panel
java -cp "target\data-panel-1.0.0-SNAPSHOT.jar" com.transaction.tagging.datapanel.DataPanelApplication
goto end

:usage
echo Usage: run.bat [control-panel^|data-panel^|all]
echo.
echo   control-panel - Start only the Control Panel (rule management API)
echo   data-panel    - Start only the Data Panel (Flink processor)
echo   all           - Start both Control Panel and Data Panel
goto end

:end
endlocal
