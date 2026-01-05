@echo off
REM Script to test SSE endpoints on Windows using curl
REM This script demonstrates how to connect to SSE endpoints programmatically

echo ==========================================
echo Spring SSE Demo - Testing Script
echo ==========================================
echo.

REM Base URL
set BASE_URL=http://localhost:8080/sse-demo

echo 1. Testing health endpoint...
curl -s "%BASE_URL%/api/sse/health"
echo.
echo.

echo 2. Testing stats endpoint...
curl -s "%BASE_URL%/api/sse/stats"
echo.
echo.

echo 3. Broadcasting a custom event...
curl -X POST "%BASE_URL%/api/sse/broadcast" -H "Content-Type: application/json" -d "{\"eventType\":\"TEST\",\"message\":\"Custom test event from curl\",\"data\":{\"source\":\"test-script\"}}"
echo.
echo.

echo 4. Testing SSE subscription (will stream events for 10 seconds)...
REM Generate random client ID
set CLIENT_ID=curl-client-%random%

REM Subscribe to SSE stream (timeout after 10s)
timeout /t 10 >nul
echo To test SSE stream, run: curl -N -H "Accept: text/event-stream" "%BASE_URL%/api/sse/subscribe?clientId=%CLIENT_ID%"
echo.

echo ==========================================
echo Testing completed!
echo ==========================================

pause
