@echo off
echo Testing WebSocket Demo Application
echo ================================
echo.

echo Testing Health Endpoint...
curl -s http://localhost:8080/api/health
echo.
echo.

echo Testing WebSocket Info Endpoint...
curl -s http://localhost:8080/api/websocket/info
echo.
echo.

echo Testing Actuator Health...
curl -s http://localhost:8080/actuator/health
echo.
echo.

echo ================================
echo Tests completed!
echo.
echo Open http://localhost:8080 in your browser to test the WebSocket UI
echo Open http://localhost:8080/swagger-ui.html for API documentation
pause
