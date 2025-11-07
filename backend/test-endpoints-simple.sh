#!/bin/bash

# Simple Customer API Endpoint Testing Script
BASE_URL="http://localhost:8080"
API_URL="${BASE_URL}/api/customers"

echo "========================================="
echo "Customer API Endpoint Testing"
echo "========================================="
echo ""

# Test 1: Create Customer
echo "1. Testing POST /api/customers (Create)"
curl -s -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john.doe@example.com","address":"123 Main St","phone":"555-1234"}' \
  -w "\nHTTP_CODE:%{http_code}\nTIME:%{time_total}" > /tmp/create_response.txt

BODY=$(cat /tmp/create_response.txt | sed '/^HTTP_CODE:/d' | sed '/^TIME:/d' | tr -d '\n')
STATUS=$(grep "HTTP_CODE:" /tmp/create_response.txt | cut -d: -f2)
TIME=$(grep "TIME:" /tmp/create_response.txt | cut -d: -f2)

echo "  Status: $STATUS"
if [ "$STATUS" == "201" ]; then
    echo "  ✓ Create successful"
    CUSTOMER_ID=$(echo "$BODY" | sed -E 's/.*"id":"([^"]+)".*/\1/')
    if [ -z "$CUSTOMER_ID" ] || [ "$CUSTOMER_ID" == "$BODY" ]; then
        # Try alternative extraction
        CUSTOMER_ID=$(echo "$BODY" | grep -o '[a-f0-9]\{8\}-[a-f0-9]\{4\}-[a-f0-9]\{4\}-[a-f0-9]\{4\}-[a-f0-9]\{12\}' | head -1)
    fi
    echo "  Customer ID: $CUSTOMER_ID"
    if [ -z "$CUSTOMER_ID" ]; then
        echo "  ⚠ Warning: Could not extract customer ID, but create succeeded"
        echo "  Response body: $BODY"
    fi
else
    echo "  ✗ Create failed"
    echo "  Response: $BODY"
    exit 1
fi
echo ""

# Test 2: Get Customer by ID
echo "2. Testing GET /api/customers/{id}"
RESPONSE=$(curl -s -w "\n%{http_code}\n%{time_total}" -X GET "$API_URL/$CUSTOMER_ID")
BODY=$(echo "$RESPONSE" | sed '$d' | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -2 | head -1)
TIME=$(echo "$RESPONSE" | tail -1)
TIME_MS=$(echo "$TIME * 1000" | bc)

echo "  Status: $STATUS"
echo "  Time: ${TIME_MS}ms"
if [ "$STATUS" == "200" ]; then
    echo "  ✓ Get by ID successful"
    if (( $(echo "$TIME_MS < 200" | bc -l) )); then
        echo "  ✓ Performance: ${TIME_MS}ms < 200ms"
    else
        echo "  ⚠ Performance: ${TIME_MS}ms >= 200ms"
    fi
else
    echo "  ✗ Get by ID failed"
fi
echo ""

# Test 3: List Customers
echo "3. Testing GET /api/customers (List with pagination)"
RESPONSE=$(curl -s -w "\n%{http_code}\n%{time_total}" -X GET "$API_URL?page=0&size=20&sort=name,asc")
BODY=$(echo "$RESPONSE" | sed '$d' | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -2 | head -1)
TIME=$(echo "$RESPONSE" | tail -1)
TIME_MS=$(echo "$TIME * 1000" | bc)

echo "  Status: $STATUS"
echo "  Time: ${TIME_MS}ms"
if [ "$STATUS" == "200" ]; then
    echo "  ✓ List successful"
    if (( $(echo "$TIME_MS < 200" | bc -l) )); then
        echo "  ✓ Performance: ${TIME_MS}ms < 200ms"
    else
        echo "  ⚠ Performance: ${TIME_MS}ms >= 200ms"
    fi
else
    echo "  ✗ List failed"
fi
echo ""

# Test 4: Update Customer
echo "4. Testing PUT /api/customers/{id} (Update)"
RESPONSE=$(curl -s -w "\n%{http_code}\n%{time_total}" -X PUT "$API_URL/$CUSTOMER_ID" \
  -H "Content-Type: application/json" \
  -d '{"name":"John Updated","email":"john.updated@example.com","address":"456 New St","phone":"555-9999"}')
BODY=$(echo "$RESPONSE" | sed '$d' | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -2 | head -1)
TIME=$(echo "$RESPONSE" | tail -1)
TIME_MS=$(echo "$TIME * 1000" | bc)

echo "  Status: $STATUS"
echo "  Time: ${TIME_MS}ms"
if [ "$STATUS" == "200" ]; then
    echo "  ✓ Update successful"
    if (( $(echo "$TIME_MS < 200" | bc -l) )); then
        echo "  ✓ Performance: ${TIME_MS}ms < 200ms"
    else
        echo "  ⚠ Performance: ${TIME_MS}ms >= 200ms"
    fi
else
    echo "  ✗ Update failed"
fi
echo ""

# Test 5: Delete Customer
echo "5. Testing DELETE /api/customers/{id}"
RESPONSE=$(curl -s -w "\n%{http_code}\n%{time_total}" -X DELETE "$API_URL/$CUSTOMER_ID")
STATUS=$(echo "$RESPONSE" | tail -2 | head -1)
TIME=$(echo "$RESPONSE" | tail -1)
TIME_MS=$(echo "$TIME * 1000" | bc)

echo "  Status: $STATUS"
echo "  Time: ${TIME_MS}ms"
if [ "$STATUS" == "204" ]; then
    echo "  ✓ Delete successful"
    if (( $(echo "$TIME_MS < 200" | bc -l) )); then
        echo "  ✓ Performance: ${TIME_MS}ms < 200ms"
    else
        echo "  ⚠ Performance: ${TIME_MS}ms >= 200ms"
    fi
else
    echo "  ✗ Delete failed"
fi
echo ""

# Test 6: 404 Error
echo "6. Testing GET /api/customers/{id} (404 Error)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$API_URL/00000000-0000-0000-0000-000000000000")
STATUS=$(echo "$RESPONSE" | tail -1)
echo "  Status: $STATUS"
if [ "$STATUS" == "404" ]; then
    echo "  ✓ 404 handling correct"
else
    echo "  ✗ Expected 404, got $STATUS"
fi
echo ""

echo "========================================="
echo "All endpoint tests completed!"
echo "========================================="

