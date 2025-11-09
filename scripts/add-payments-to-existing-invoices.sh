#!/bin/bash

# Script to add payments to existing invoices in InvoiceMe deployed application
# This script fetches existing invoices and adds payments to demonstrate the payment feature

set -e

# Configuration
BACKEND_URL="${BACKEND_URL:-https://invoiceme-backend.onrender.com}"
API_URL="${BACKEND_URL}/api"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "======================================"
echo "Adding Payments to Existing Invoices"
echo "======================================"
echo ""
echo -e "${BLUE}Backend URL: ${API_URL}${NC}"
echo ""

# Function to extract ID from JSON response
extract_id() {
    echo "$1" | grep -o '"id":"[^"]*' | cut -d'"' -f4 || echo ""
}

# Function to extract field from JSON
extract_field() {
    local json=$1
    local field=$2
    echo "$json" | grep -o "\"${field}\":\"[^\"]*" | cut -d'"' -f4 || echo ""
}

# Function to make API call and handle errors
api_call() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$endpoint")
    fi
    
    http_code=$(echo "$response" | tail -1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo "$body"
        return 0
    else
        echo -e "${RED}✗ Failed: $description${NC}" >&2
        echo "  HTTP Code: $http_code" >&2
        echo "  Response: $body" >&2
        return 1
    fi
}

# Get current date in ISO format for payment dates
CURRENT_DATE=$(date -u +"%Y-%m-%dT%H:%M:%S")
PAYMENT_DATE_1=$(date -u -v-10d +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || date -u -d "10 days ago" +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || echo "$CURRENT_DATE")
PAYMENT_DATE_2=$(date -u -v-5d +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || date -u -d "5 days ago" +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || echo "$CURRENT_DATE")
PAYMENT_DATE_3=$(date -u -v-2d +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || date -u -d "2 days ago" +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || echo "$CURRENT_DATE")

echo -e "${BLUE}=== Fetching Existing Invoices ===${NC}"
echo ""

# Get all invoices
echo -e "${YELLOW}Fetching invoices...${NC}"
invoices_response=$(api_call "GET" "${API_URL}/invoices?page=0&size=20" "" "Get invoices")

if [ $? -ne 0 ]; then
    echo -e "${RED}Failed to fetch invoices${NC}"
    exit 1
fi

# Extract invoice IDs and statuses from the response
# The response is a paginated response, so we need to extract from the content array
invoice_ids=$(echo "$invoices_response" | grep -o '"id":"[^"]*' | cut -d'"' -f4)
invoice_statuses=$(echo "$invoices_response" | grep -o '"status":"[^"]*' | cut -d'"' -f4)

# Convert to arrays
declare -a INVOICE_ID_ARRAY
declare -a INVOICE_STATUS_ARRAY
declare -a INVOICE_TOTAL_ARRAY

index=0
for id in $invoice_ids; do
    INVOICE_ID_ARRAY[$index]=$id
    index=$((index + 1))
done

index=0
for status in $invoice_statuses; do
    INVOICE_STATUS_ARRAY[$index]=$status
    index=$((index + 1))
done

# Get totals
totals=$(echo "$invoices_response" | grep -o '"totalAmount":[0-9.]*' | cut -d':' -f2)
index=0
for total in $totals; do
    INVOICE_TOTAL_ARRAY[$index]=$total
    index=$((index + 1))
done

invoice_count=${#INVOICE_ID_ARRAY[@]}

if [ $invoice_count -eq 0 ]; then
    echo -e "${RED}No invoices found. Please create invoices first.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Found $invoice_count invoice(s)${NC}"
echo ""

# Find SENT invoices to add payments to
echo -e "${BLUE}=== Adding Payments ===${NC}"
echo ""

payment_count=0
for i in "${!INVOICE_ID_ARRAY[@]}"; do
    invoice_id="${INVOICE_ID_ARRAY[$i]}"
    status="${INVOICE_STATUS_ARRAY[$i]}"
    total="${INVOICE_TOTAL_ARRAY[$i]}"
    
    # Only add payments to SENT invoices
    if [ "$status" = "SENT" ]; then
        echo -e "${YELLOW}Adding payment to invoice ${invoice_id:0:8}... (Status: $status, Total: \$$total)${NC}"
        
        # Calculate payment amount (partial payment: 60% of total)
        payment_amount=$(echo "scale=2; $total * 0.6" | bc)
        
        # Use appropriate date based on payment count
        if [ $payment_count -eq 0 ]; then
            payment_date="$PAYMENT_DATE_1"
        elif [ $payment_count -eq 1 ]; then
            payment_date="$PAYMENT_DATE_2"
        else
            payment_date="$PAYMENT_DATE_3"
        fi
        
        response=$(api_call "POST" "${API_URL}/payments" \
            "{
                \"invoiceId\": \"${invoice_id}\",
                \"amount\": ${payment_amount},
                \"paymentDate\": \"${payment_date}\"
            }" \
            "Record payment for invoice ${invoice_id:0:8}")
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ Recorded payment of \$${payment_amount}${NC}"
            payment_count=$((payment_count + 1))
        else
            echo -e "${YELLOW}⚠ Payment failed (may already exist), continuing...${NC}"
        fi
        echo ""
        
        # Limit to 3 payments
        if [ $payment_count -ge 3 ]; then
            break
        fi
    fi
done

if [ $payment_count -eq 0 ]; then
    echo -e "${YELLOW}No SENT invoices found to add payments to.${NC}"
    echo -e "${YELLOW}You may need to mark some invoices as SENT first.${NC}"
else
    echo "======================================"
    echo -e "${GREEN}Payment addition completed!${NC}"
    echo "======================================"
    echo ""
    echo "Summary:"
    echo "  • $payment_count payment(s) added"
    echo ""
fi

echo "You can view the updated data at:"
echo "  Frontend: https://invoiceme-frontend.onrender.com"
echo "  Backend API: ${API_URL}"
echo ""

