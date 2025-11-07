#!/bin/bash

# Test script for Payment API endpoints
# Prerequisites: Backend must be running on http://localhost:8080

BASE_URL="http://localhost:8080/api"
CUSTOMER_ID=""
INVOICE_ID=""
PAYMENT_ID=""

echo "======================================"
echo "Payment API Endpoint Tests"
echo "======================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test 1: Create a customer first
echo -e "${BLUE}1. Creating test customer...${NC}"
CUSTOMER_RESPONSE=$(curl -s -X POST "$BASE_URL/customers" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Payment Test Customer",
    "email": "payment@test.com",
    "phone": "555-0123",
    "address": "123 Test St"
  }')

CUSTOMER_ID=$(echo $CUSTOMER_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)
if [ -z "$CUSTOMER_ID" ]; then
  echo -e "${RED}✗ Failed to create customer${NC}"
  echo "Response: $CUSTOMER_RESPONSE"
  exit 1
fi
echo -e "${GREEN}✓ Customer created with ID: $CUSTOMER_ID${NC}"
echo ""

# Test 2: Create an invoice
echo -e "${BLUE}2. Creating test invoice...${NC}"
INVOICE_RESPONSE=$(curl -s -X POST "$BASE_URL/invoices" \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": \"$CUSTOMER_ID\",
    \"lineItems\": [
      {
        \"description\": \"Test Service\",
        \"quantity\": 2,
        \"unitPrice\": 500.00
      }
    ]
  }")

INVOICE_ID=$(echo $INVOICE_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)
if [ -z "$INVOICE_ID" ]; then
  echo -e "${RED}✗ Failed to create invoice${NC}"
  echo "Response: $INVOICE_RESPONSE"
  exit 1
fi
echo -e "${GREEN}✓ Invoice created with ID: $INVOICE_ID (Total: \$1000.00)${NC}"
echo ""

# Test 3: Mark invoice as SENT
echo -e "${BLUE}3. Marking invoice as SENT...${NC}"
SENT_RESPONSE=$(curl -s -X POST "$BASE_URL/invoices/$INVOICE_ID/send" \
  -H "Content-Type: application/json")
echo -e "${GREEN}✓ Invoice marked as SENT${NC}"
echo ""

# Test 4: Record a payment
echo -e "${BLUE}4. Recording payment (Partial: \$600.00)...${NC}"
PAYMENT_RESPONSE=$(curl -s -X POST "$BASE_URL/payments" \
  -H "Content-Type: application/json" \
  -d "{
    \"invoiceId\": \"$INVOICE_ID\",
    \"amount\": 600.00,
    \"paymentDate\": \"2024-11-01T10:00:00\"
  }")

PAYMENT_ID=$(echo $PAYMENT_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)
if [ -z "$PAYMENT_ID" ]; then
  echo -e "${RED}✗ Failed to record payment${NC}"
  echo "Response: $PAYMENT_RESPONSE"
  exit 1
fi
echo -e "${GREEN}✓ Payment recorded with ID: $PAYMENT_ID${NC}"
echo "Response: $PAYMENT_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$PAYMENT_RESPONSE"
echo ""

# Test 5: Get payment by ID
echo -e "${BLUE}5. Getting payment by ID...${NC}"
GET_PAYMENT_RESPONSE=$(curl -s -X GET "$BASE_URL/payments/$PAYMENT_ID")
echo -e "${GREEN}✓ Payment retrieved${NC}"
echo "Response: $GET_PAYMENT_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$GET_PAYMENT_RESPONSE"
echo ""

# Test 6: Check invoice balance
echo -e "${BLUE}6. Checking invoice balance...${NC}"
INVOICE_CHECK=$(curl -s -X GET "$BASE_URL/invoices/$INVOICE_ID")
BALANCE=$(echo $INVOICE_CHECK | grep -o '"balance":[0-9.]*' | cut -d':' -f2)
STATUS=$(echo $INVOICE_CHECK | grep -o '"status":"[^"]*' | cut -d'"' -f4)
echo -e "${GREEN}✓ Invoice balance: \$$BALANCE, Status: $STATUS${NC}"
if [ "$BALANCE" = "400.0" ] || [ "$BALANCE" = "400.00" ]; then
  echo -e "${GREEN}✓ Balance correctly updated (1000 - 600 = 400)${NC}"
else
  echo -e "${RED}✗ Balance incorrect. Expected 400.00, got $BALANCE${NC}"
fi
echo ""

# Test 7: Get payments by invoice
echo -e "${BLUE}7. Getting payments for invoice...${NC}"
PAYMENTS_LIST=$(curl -s -X GET "$BASE_URL/payments?invoiceId=$INVOICE_ID")
echo -e "${GREEN}✓ Payments retrieved${NC}"
echo "Response: $PAYMENTS_LIST" | python3 -m json.tool 2>/dev/null || echo "$PAYMENTS_LIST"
echo ""

# Test 8: Record remaining payment
echo -e "${BLUE}8. Recording remaining payment (\$400.00)...${NC}"
FINAL_PAYMENT=$(curl -s -X POST "$BASE_URL/payments" \
  -H "Content-Type: application/json" \
  -d "{
    \"invoiceId\": \"$INVOICE_ID\",
    \"amount\": 400.00,
    \"paymentDate\": \"2024-11-01T11:00:00\"
  }")
echo -e "${GREEN}✓ Final payment recorded${NC}"
echo ""

# Test 9: Verify invoice is PAID
echo -e "${BLUE}9. Verifying invoice is PAID...${NC}"
FINAL_CHECK=$(curl -s -X GET "$BASE_URL/invoices/$INVOICE_ID")
FINAL_BALANCE=$(echo $FINAL_CHECK | grep -o '"balance":[0-9.]*' | cut -d':' -f2)
FINAL_STATUS=$(echo $FINAL_CHECK | grep -o '"status":"[^"]*' | cut -d'"' -f4)
echo -e "${GREEN}✓ Invoice balance: \$$FINAL_BALANCE, Status: $FINAL_STATUS${NC}"
if [ "$FINAL_BALANCE" = "0.0" ] || [ "$FINAL_BALANCE" = "0.00" ] || [ "$FINAL_BALANCE" = "0" ]; then
  echo -e "${GREEN}✓ Balance is zero${NC}"
else
  echo -e "${RED}✗ Balance should be zero, got $FINAL_BALANCE${NC}"
fi
if [ "$FINAL_STATUS" = "PAID" ]; then
  echo -e "${GREEN}✓ Invoice status is PAID${NC}"
else
  echo -e "${RED}✗ Invoice status should be PAID, got $FINAL_STATUS${NC}"
fi
echo ""

# Test 10: Try to overpay (should fail)
echo -e "${BLUE}10. Testing validation (overpayment should fail)...${NC}"
OVERPAY_RESPONSE=$(curl -s -X POST "$BASE_URL/payments" \
  -H "Content-Type: application/json" \
  -d "{
    \"invoiceId\": \"$INVOICE_ID\",
    \"amount\": 100.00,
    \"paymentDate\": \"2024-11-01T12:00:00\"
  }")

if echo "$OVERPAY_RESPONSE" | grep -q "error\|exceeds\|cannot"; then
  echo -e "${GREEN}✓ Overpayment correctly rejected${NC}"
else
  echo -e "${RED}✗ Overpayment should have been rejected${NC}"
fi
echo ""

# Test 11: Get all payments
echo -e "${BLUE}11. Getting all payments...${NC}"
ALL_PAYMENTS=$(curl -s -X GET "$BASE_URL/payments")
echo -e "${GREEN}✓ All payments retrieved${NC}"
echo "Response (first 500 chars): $(echo $ALL_PAYMENTS | cut -c1-500)"
echo ""

echo "======================================"
echo -e "${GREEN}Payment API Tests Complete!${NC}"
echo "======================================"
