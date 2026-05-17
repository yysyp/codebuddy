@echo off
REM Run script for CodeBuddy Agent (Windows)

echo ========================================
echo Running CodeBuddy Dev Assistant Agent
echo ========================================

cd /d "%~dp0"

REM Set Python path
set PYTHONPATH=%PYTHONPATH%;%cd%

REM Run the agent
python -m .codebuddy.agent.core
