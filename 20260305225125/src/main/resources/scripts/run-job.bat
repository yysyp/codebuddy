@echo off
REM Run Flink SQL Tagging Job

echo ==========================================
echo Flink SQL Tagging Job
echo ==========================================

REM Check if Maven is available
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Maven not found. Please install Maven and add it to PATH.
    exit /b 1
)

REM Create output directory
if not exist "..\..\..\..\..\output" mkdir "..\..\..\..\..\output"

REM Parse arguments
set OUTPUT_PATH=./output/tagged_transactions.csv
set LOCAL_MODE=true

:parse_args
if "%~1"=="" goto run_job
if "%~1"=="--output" (
    set OUTPUT_PATH=%~2
    shift
    shift
    goto parse_args
)
if "%~1"=="--cluster" (
    set LOCAL_MODE=false
    shift
    goto parse_args
)
shift
goto parse_args

:run_job
echo Output path: %OUTPUT_PATH%
echo Local mode: %LOCAL_MODE%
echo.

REM Run the Flink job
echo Starting Flink job...
mvn compile exec:java -Dexec.mainClass="com.example.flink.FlinkSqlTaggingJob" -Dexec.args="--output %OUTPUT_PATH% --local %LOCAL_MODE%" -q

if %errorlevel% equ 0 (
    echo.
    echo ==========================================
    echo Job completed successfully!
    echo Output file: %OUTPUT_PATH%
    echo ==========================================
    
    REM Display first 20 lines of output
    if exist "%OUTPUT_PATH%" (
        echo.
        echo Sample output (first 20 lines):
        head -20 "%OUTPUT_PATH%" 2>nul || type "%OUTPUT_PATH%" | more
    )
) else (
    echo.
    echo ERROR: Job failed
    exit /b 1
)
