@echo off
REM Drools Rule Engine Test Script for Windows
REM This script tests various rule scenarios using curl commands

set BASE_URL=http://localhost:8080/api/v1/rules

echo ==========================================
echo Drools Rule Engine Test Script
echo ==========================================
echo.

REM Test 1: VIP Customer Pricing
echo Test 1: VIP Customer Pricing with Special Promotion
curl -X POST "%BASE_URL%/execute" -H "Content-Type: application/json" -d @test-data/pricing-test.json
echo.
echo.

REM Test 2: Regular Customer Order
echo Test 2: Regular Customer Order Processing
curl -X POST "%BASE_URL%/execute" -H "Content-Type: application/json" -d @test-data/order-test.json
echo.
echo.

REM Test 3: Trusted User Risk Assessment
echo Test 3: Trusted User Risk Assessment
curl -X POST "%BASE_URL%/execute" -H "Content-Type: application/json" -d @test-data/risk-assessment-test.json
echo.
echo.

REM Test 4: Get Available Rules
echo Test 4: Get Available Rules
curl -X GET "%BASE_URL%/available"
echo.
echo.

echo ==========================================
echo All Tests Completed
echo ==========================================
pause
