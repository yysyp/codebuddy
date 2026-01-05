#!/bin/bash

# Script to test SSE endpoints using curl
# This script demonstrates how to connect to SSE endpoints programmatically

echo "=========================================="
echo "Spring SSE Demo - Testing Script"
echo "=========================================="
echo ""

# Base URL
BASE_URL="http://localhost:8080/sse-demo"

echo "1. Testing health endpoint..."
curl -s "$BASE_URL/api/sse/health" | python -m json.tool
echo ""
echo ""

echo "2. Testing stats endpoint..."
curl -s "$BASE_URL/api/sse/stats" | python -m json.tool
echo ""
echo ""

echo "3. Testing SSE subscription (will stream events for 30 seconds)..."
echo "Press Ctrl+C to stop early"
echo ""

# Generate random client ID
CLIENT_ID="curl-client-$(date +%s)"

# Subscribe to SSE stream (timeout after 30s)
timeout 30 curl -N -H "Accept: text/event-stream" \
  "$BASE_URL/api/sse/subscribe?clientId=$CLIENT_ID"

echo ""
echo ""
echo "4. Broadcasting a custom event..."
curl -X POST "$BASE_URL/api/sse/broadcast" \
  -H "Content-Type: application/json" \
  -d '{"eventType":"TEST","message":"Custom test event from curl","data":{"source":"test-script","timestamp":"$(date)"}}' | python -m json.tool

echo ""
echo ""
echo "=========================================="
echo "Testing completed!"
echo "=========================================="
