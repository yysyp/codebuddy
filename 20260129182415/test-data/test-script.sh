#!/bin/bash

# Drools Rule Engine Test Script
# This script tests various rule scenarios using curl commands

BASE_URL="http://localhost:8080/api/v1/rules"

echo "=========================================="
echo "Drools Rule Engine Test Script"
echo "=========================================="
echo ""

# Test 1: VIP Customer Pricing
echo "Test 1: VIP Customer Pricing with Special Promotion"
curl -X POST "$BASE_URL/execute" \
  -H "Content-Type: application/json" \
  -d @test-data/pricing-test.json | jq '.'
echo ""
echo ""

# Test 2: Regular Customer Order
echo "Test 2: Regular Customer Order Processing"
curl -X POST "$BASE_URL/execute" \
  -H "Content-Type: application/json" \
  -d @test-data/order-test.json | jq '.'
echo ""
echo ""

# Test 3: Trusted User Risk Assessment
echo "Test 3: Trusted User Risk Assessment"
curl -X POST "$BASE_URL/execute" \
  -H "Content-Type: application/json" \
  -d @test-data/risk-assessment-test.json | jq '.'
echo ""
echo ""

# Test 4: Get Available Rules
echo "Test 4: Get Available Rules"
curl -X GET "$BASE_URL/available" | jq '.'
echo ""
echo ""

# Additional Test 5: Premium Customer
echo "Test 5: Premium Customer with 15 Purchases"
curl -X POST "$BASE_URL/execute" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleName": "pricing",
    "factData": {
      "customerId": "CUST-002",
      "customerType": "PREMIUM",
      "originalPrice": 500.00,
      "purchaseCount": 15,
      "specialPromotion": false
    }
  }' | jq '.'
echo ""
echo ""

# Additional Test 6: High Value Order
echo "Test 6: High Value Order ($6000)"
curl -X POST "$BASE_URL/execute" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleName": "order-processing",
    "factData": {
      "orderId": "ORD-99999",
      "customerId": "CUST-003",
      "status": "PENDING",
      "totalAmount": 6000.00,
      "itemCount": 2,
      "highValue": false,
      "suspicious": false,
      "requiresApproval": false,
      "approvalReason": "",
      "expressShipping": true,
      "warnings": []
    }
  }' | jq '.'
echo ""
echo ""

# Additional Test 7: Suspicious Activity
echo "Test 7: Suspicious Activity Risk Assessment"
curl -X POST "$BASE_URL/execute" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleName": "risk-assessment",
    "factData": {
      "requestId": "REQ-002",
      "userId": "USER-NEW",
      "riskLevel": "LOW",
      "riskScore": 0,
      "requiresVerification": false,
      "flagged": false,
      "failedAttempts": 4,
      "isNewUser": true,
      "unusualLocation": false
    }
  }' | jq '.'
echo ""
echo ""

echo "=========================================="
echo "All Tests Completed"
echo "=========================================="
