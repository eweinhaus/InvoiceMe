#!/bin/bash

# Customer API Endpoint Testing Script
# This script tests all Customer endpoints and validates performance

BASE_URL="${BASE_URL:-http://localhost:8080}"
API_URL="${BASE_URL}/api/customers"

echo "========================================="
echo "Customer API Endpoint Testing"
echo "========================================="
echo "Base URL: $BASE_URL"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0
PERFORMANCE_FAILED=0

# Function to test endpoint and measure time
test_endpoint() {
    local method=$1
    local endpoint=$2
    local data=$3
    local expected_status=$4
    local description=$5
    
    echo "Testing: $description"
    echo "  $method $endpoint"
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}\n%{time_total}" -X $method \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}\n%{time_total}" -X $method \
            "$endpoint")
    fi
    
    # Extract response body, status code, and time
    body=$(echo "$response" | sed '$d' | sed '$d')
    status_code=$(echo "$response" | tail -n 2 | head -n 1)
    time_total=$(echo "$response" | tail -n 1)
    time_ms=$(echo "$time_total * 1000" | bc)
    
    # Check status code
    if [ "$status_code" == "$expected_status" ]; then
        echo -e "  ${GREEN}✓ Status: $status_code (expected $expected_status)${NC}"
        ((TESTS_PASSED++))
    else
        echo -e "  ${RED}✗ Status: $status_code (expected $expected_status)${NC}"
        echo "  Response: $body"
        ((TESTS_FAILED++))
        return 1
    fi
    
    # Check performance (< 200ms)
    if (( $(echo "$time_ms < 200" | bc -l) )); then
        echo -e "  ${GREEN}✓ Performance: ${time_ms}ms < 200ms${NC}"
    else
        echo -e "  ${YELLOW}⚠ Performance: ${time_ms}ms >= 200ms${NC}"
        ((PERFORMANCE_FAILED++))
    fi
    
    echo "$body"
    echo ""
    return 0
}

# Wait for server to be ready
echo "Waiting for server to be ready..."
for i in {1..30}; do
    if curl -s -f "$BASE_URL/actuator/health" > /dev/null 2>&1 || \
       curl -s -f "$BASE_URL/swagger-ui/index.html" > /dev/null 2>&1; then
        echo "Server is ready!"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}Server is not responding. Please start the application first.${NC}"
        exit 1
    fi
    sleep 1
done

echo ""
echo "========================================="
echo "1. CREATE CUSTOMER"
echo "========================================="

CUSTOMER_DATA='{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "address": "123 Main St, City, State 12345",
  "phone": "+1-555-123-4567"
}'

CREATE_RESPONSE=$(test_endpoint "POST" "$API_URL" "$CUSTOMER_DATA" "201" "Create customer")
# Extract customer ID using multiple methods for compatibility
CUSTOMER_ID=$(echo "$CREATE_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
if [ -z "$CUSTOMER_ID" ]; then
    # Try with Python if available
    CUSTOMER_ID=$(echo "$CREATE_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['id'])" 2>/dev/null)
fi
if [ -z "$CUSTOMER_ID" ]; then
    # Try with sed as fallback
    CUSTOMER_ID=$(echo "$CREATE_RESPONSE" | sed -n 's/.*"id":"\([^"]*\)".*/\1/p' | head -1)
fi

if [ -z "$CUSTOMER_ID" ]; then
    echo -e "${RED}Failed to extract customer ID from response${NC}"
    exit 1
fi

echo "Created Customer ID: $CUSTOMER_ID"
echo ""

echo "========================================="
echo "2. GET CUSTOMER BY ID"
echo "========================================="

test_endpoint "GET" "$API_URL/$CUSTOMER_ID" "" "200" "Get customer by ID"
echo ""

echo "========================================="
echo "3. LIST ALL CUSTOMERS (PAGINATION)"
echo "========================================="

test_endpoint "GET" "$API_URL?page=0&size=20&sort=name,asc" "" "200" "List customers with pagination"
echo ""

echo "========================================="
echo "4. UPDATE CUSTOMER"
echo "========================================="

UPDATE_DATA='{
  "name": "John Updated",
  "email": "john.updated@example.com",
  "address": "456 New St, City, State 54321",
  "phone": "+1-555-999-8888"
}'

test_endpoint "PUT" "$API_URL/$CUSTOMER_ID" "$UPDATE_DATA" "200" "Update customer"
echo ""

echo "========================================="
echo "5. GET UPDATED CUSTOMER"
echo "========================================="

test_endpoint "GET" "$API_URL/$CUSTOMER_ID" "" "200" "Verify customer was updated"
echo ""

echo "========================================="
echo "6. ERROR HANDLING TESTS"
echo "========================================="

# Test 404
echo "Testing 404 (Non-existent customer)..."
test_endpoint "GET" "$API_URL/00000000-0000-0000-0000-000000000000" "" "404" "Get non-existent customer"
echo ""

# Test validation error (invalid email)
echo "Testing validation error (invalid email)..."
INVALID_DATA='{
  "name": "Test",
  "email": "invalid-email",
  "address": null,
  "phone": null
}'
test_endpoint "POST" "$API_URL" "$INVALID_DATA" "400" "Create customer with invalid email"
echo ""

# Test duplicate email
echo "Testing duplicate email..."
DUPLICATE_DATA='{
  "name": "Another Customer",
  "email": "john.updated@example.com",
  "address": null,
  "phone": null
}'
test_endpoint "POST" "$API_URL" "$DUPLICATE_DATA" "400" "Create customer with duplicate email"
echo ""

echo "========================================="
echo "7. DELETE CUSTOMER"
echo "========================================="

test_endpoint "DELETE" "$API_URL/$CUSTOMER_ID" "" "204" "Delete customer"
echo ""

# Verify deletion
echo "Verifying customer was deleted..."
test_endpoint "GET" "$API_URL/$CUSTOMER_ID" "" "404" "Verify customer was deleted"
echo ""

echo "========================================="
echo "TEST SUMMARY"
echo "========================================="
echo -e "Tests Passed: ${GREEN}$TESTS_PASSED${NC}"
echo -e "Tests Failed: ${RED}$TESTS_FAILED${NC}"
if [ $PERFORMANCE_FAILED -gt 0 ]; then
    echo -e "Performance Warnings: ${YELLOW}$PERFORMANCE_FAILED${NC}"
fi
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed!${NC}"
    exit 1
fi

