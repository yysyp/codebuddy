@echo off
REM Build script for CodeBuddy Agent (Windows)

echo ========================================
echo Building CodeBuddy Dev Assistant Agent
echo ========================================

REM Set working directory
cd /d "%~dp0"

REM Create necessary directories
echo Creating directories...
if not exist ".codebuddy\logs" mkdir ".codebuddy\logs"
if not exist ".codebuddy\memory\conversations" mkdir ".codebuddy\memory\conversations"
if not exist ".codebuddy\memory\patterns" mkdir ".codebuddy\memory\patterns"
if not exist ".codebuddy\memory\errors" mkdir ".codebuddy\memory\errors"
if not exist ".codebuddy\memory\skills" mkdir ".codebuddy\memory\skills"
if not exist ".codebuddy\security" mkdir ".codebuddy\security"

REM Install dependencies
echo.
echo Installing dependencies...
python -m pip install -r requirements.txt --quiet

REM Run tests
echo.
echo Running tests...
python -m pytest .codebuddy\utils\ -v --tb=short 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Warning: Some tests failed
)

REM Syntax check Python files
echo.
echo Checking Python syntax...
python -m py_compile .codebuddy\agent\core.py
python -m py_compile .codebuddy\utils\env_loader.py
python -m py_compile .codebuddy\utils\security_filter.py
python -m py_compile .codebuddy\utils\skill_manager.py
python -m py_compile .codebuddy\utils\hook_executor.py
python -m py_compile .codebuddy\utils\evolution_engine.py

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Build completed successfully!
    echo ========================================
) else (
    echo.
    echo Build failed with errors.
    exit /b 1
)
