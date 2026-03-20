@echo off
REM Test Script for ETL Tagging System API

echo ========================================
echo ETL Tagging System - API Test
echo ========================================
echo.

echo [1/5] Testing Control Panel Health...
curl -s http://localhost:8080/actuator/health
echo.
echo.

echo [2/5] Fetching Published Rules...
curl -s http://localhost:8080/api/v1/rules/published
echo.
echo.

echo [3/5] Testing Data Panel Health...
curl -s http://localhost:8081/api/v1/jobs/health
echo.
echo.

echo [4/5] Creating a New Rule...
curl -s -X POST http://localhost:8080/api/v1/rules ^
  -H "Content-Type: application/json" ^
  -H "X-User: testuser" ^
  -d "{\"name\":\"test-rule-$(date +%s)\",\"description\":\"Test rule for demonstration\",\"ruleContent\":\"package com.etl.rules\n\nimport com.etl.data.model.Transaction\n\nrule \\\"test-rule\\\"\n    dialect \\\"mvel\\\"\n    when\n        $transaction : Transaction(amount ^\> 5000.00)\n    then\n        $transaction.addTag(\\\"TEST_TAG\\\");\n    end\",\"ruleType\":\"TAGGING\",\"targetType\":\"TRANSACTION\",\"priority\":\"MEDIUM\"}"
echo.
echo.

echo [5/5] Getting All Schemas...
curl -s http://localhost:8080/api/v1/schemas
echo.
echo.

echo ========================================
echo API Test Complete!
echo ========================================
echo.
echo To execute the tagging job, run:
echo   curl -X POST "http://localhost:8081/api/v1/jobs/tagging/execute"
echo.
pause
