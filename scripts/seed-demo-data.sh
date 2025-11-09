#!/bin/bash

# Script to seed demo data for InvoiceMe deployed application
# This script creates sample customers, invoices, and payments for demonstration purposes

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
echo "InvoiceMe Demo Data Seeding Script"
echo "======================================"
echo ""
echo -e "${BLUE}Backend URL: ${API_URL}${NC}"
echo ""

# Function to extract ID from JSON response
extract_id() {
    echo "$1" | grep -o '"id":"[^"]*' | cut -d'"' -f4 || echo ""
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

# Arrays to store created IDs
declare -a CUSTOMER_IDS
declare -a INVOICE_IDS

echo -e "${BLUE}=== Creating Customers ===${NC}"
echo ""

# Customer 1: Acme Corporation
echo -e "${YELLOW}1. Creating Acme Corporation...${NC}"
response=$(api_call "POST" "${API_URL}/customers" \
    '{
        "name": "Acme Corporation",
        "email": "billing@acme.com",
        "phone": "+1-555-0100",
        "address": "123 Business Park, Suite 100, San Francisco, CA 94105"
    }' \
    "Create Acme Corporation")

if [ $? -eq 0 ]; then
    customer_id=$(extract_id "$response")
    CUSTOMER_IDS+=("$customer_id")
    echo -e "${GREEN}✓ Created Acme Corporation (ID: ${customer_id:0:8}...)${NC}"
else
    echo -e "${RED}Failed to create Acme Corporation${NC}"
    exit 1
fi
echo ""

# Customer 2: TechStart Inc
echo -e "${YELLOW}2. Creating TechStart Inc...${NC}"
response=$(api_call "POST" "${API_URL}/customers" \
    '{
        "name": "TechStart Inc",
        "email": "accounts@techstart.io",
        "phone": "+1-555-0200",
        "address": "456 Innovation Drive, Austin, TX 78701"
    }' \
    "Create TechStart Inc")

if [ $? -eq 0 ]; then
    customer_id=$(extract_id "$response")
    CUSTOMER_IDS+=("$customer_id")
    echo -e "${GREEN}✓ Created TechStart Inc (ID: ${customer_id:0:8}...)${NC}"
else
    echo -e "${RED}Failed to create TechStart Inc${NC}"
    exit 1
fi
echo ""

# Customer 3: Global Services Ltd
echo -e "${YELLOW}3. Creating Global Services Ltd...${NC}"
response=$(api_call "POST" "${API_URL}/customers" \
    '{
        "name": "Global Services Ltd",
        "email": "finance@globalservices.com",
        "phone": "+1-555-0300",
        "address": "789 World Trade Center, New York, NY 10048"
    }' \
    "Create Global Services Ltd")

if [ $? -eq 0 ]; then
    customer_id=$(extract_id "$response")
    CUSTOMER_IDS+=("$customer_id")
    echo -e "${GREEN}✓ Created Global Services Ltd (ID: ${customer_id:0:8}...)${NC}"
else
    echo -e "${RED}Failed to create Global Services Ltd${NC}"
    exit 1
fi
echo ""

# Customer 4: Creative Agency
echo -e "${YELLOW}4. Creating Creative Agency...${NC}"
response=$(api_call "POST" "${API_URL}/customers" \
    '{
        "name": "Creative Agency",
        "email": "billing@creativeagency.com",
        "phone": "+1-555-0400",
        "address": "321 Design Street, Los Angeles, CA 90028"
    }' \
    "Create Creative Agency")

if [ $? -eq 0 ]; then
    customer_id=$(extract_id "$response")
    CUSTOMER_IDS+=("$customer_id")
    echo -e "${GREEN}✓ Created Creative Agency (ID: ${customer_id:0:8}...)${NC}"
else
    echo -e "${RED}Failed to create Creative Agency${NC}"
    exit 1
fi
echo ""

# Customer 5: Manufacturing Co
echo -e "${YELLOW}5. Creating Manufacturing Co...${NC}"
response=$(api_call "POST" "${API_URL}/customers" \
    '{
        "name": "Manufacturing Co",
        "email": "accounts@manufacturing.co",
        "phone": "+1-555-0500",
        "address": "654 Industrial Boulevard, Chicago, IL 60601"
    }' \
    "Create Manufacturing Co")

if [ $? -eq 0 ]; then
    customer_id=$(extract_id "$response")
    CUSTOMER_IDS+=("$customer_id")
    echo -e "${GREEN}✓ Created Manufacturing Co (ID: ${customer_id:0:8}...)${NC}"
else
    echo -e "${RED}Failed to create Manufacturing Co${NC}"
    exit 1
fi
echo ""

echo -e "${BLUE}=== Creating Invoices ===${NC}"
echo ""

# Invoice 1: Acme Corporation - Draft invoice
echo -e "${YELLOW}1. Creating Draft invoice for Acme Corporation...${NC}"
response=$(api_call "POST" "${API_URL}/invoices" \
    "{
        \"customerId\": \"${CUSTOMER_IDS[0]}\",
        \"lineItems\": [
            {
                \"description\": \"Web Development Services - Q1 2024\",
                \"quantity\": 40,
                \"unitPrice\": 150.00
            },
            {
                \"description\": \"UI/UX Design Consultation\",
                \"quantity\": 8,
                \"unitPrice\": 200.00
            }
        ]
    }" \
    "Create invoice for Acme Corporation")

if [ $? -eq 0 ]; then
    invoice_id=$(extract_id "$response")
    INVOICE_IDS+=("$invoice_id")
    echo -e "${GREEN}✓ Created Draft invoice (ID: ${invoice_id:0:8}...) - Total: \$7,600.00${NC}"
else
    echo -e "${RED}Failed to create invoice${NC}"
    exit 1
fi
echo ""

# Invoice 2: TechStart Inc - Sent invoice
echo -e "${YELLOW}2. Creating and sending invoice for TechStart Inc...${NC}"
response=$(api_call "POST" "${API_URL}/invoices" \
    "{
        \"customerId\": \"${CUSTOMER_IDS[1]}\",
        \"lineItems\": [
            {
                \"description\": \"Cloud Infrastructure Setup\",
                \"quantity\": 1,
                \"unitPrice\": 5000.00
            },
            {
                \"description\": \"Database Migration Services\",
                \"quantity\": 1,
                \"unitPrice\": 3000.00
            },
            {
                \"description\": \"Security Audit\",
                \"quantity\": 1,
                \"unitPrice\": 2500.00
            }
        ]
    }" \
    "Create invoice for TechStart Inc")

if [ $? -eq 0 ]; then
    invoice_id=$(extract_id "$response")
    INVOICE_IDS+=("$invoice_id")
    echo -e "${GREEN}✓ Created invoice (ID: ${invoice_id:0:8}...) - Total: \$10,500.00${NC}"
    
    # Mark as sent
    api_call "POST" "${API_URL}/invoices/${invoice_id}/send" "" "Mark invoice as sent" > /dev/null
    echo -e "${GREEN}✓ Invoice marked as SENT${NC}"
else
    echo -e "${RED}Failed to create invoice${NC}"
    exit 1
fi
echo ""

# Invoice 3: Global Services Ltd - Sent invoice (partially paid)
echo -e "${YELLOW}3. Creating and sending invoice for Global Services Ltd...${NC}"
response=$(api_call "POST" "${API_URL}/invoices" \
    "{
        \"customerId\": \"${CUSTOMER_IDS[2]}\",
        \"lineItems\": [
            {
                \"description\": \"Consulting Services - Strategic Planning\",
                \"quantity\": 20,
                \"unitPrice\": 250.00
            },
            {
                \"description\": \"Market Research Analysis\",
                \"quantity\": 1,
                \"unitPrice\": 3500.00
            }
        ]
    }" \
    "Create invoice for Global Services Ltd")

if [ $? -eq 0 ]; then
    invoice_id=$(extract_id "$response")
    INVOICE_IDS+=("$invoice_id")
    echo -e "${GREEN}✓ Created invoice (ID: ${invoice_id:0:8}...) - Total: \$8,500.00${NC}"
    
    # Mark as sent
    api_call "POST" "${API_URL}/invoices/${invoice_id}/send" "" "Mark invoice as sent" > /dev/null
    echo -e "${GREEN}✓ Invoice marked as SENT${NC}"
else
    echo -e "${RED}Failed to create invoice${NC}"
    exit 1
fi
echo ""

# Invoice 4: Creative Agency - Paid invoice
echo -e "${YELLOW}4. Creating invoice for Creative Agency (will be fully paid)...${NC}"
response=$(api_call "POST" "${API_URL}/invoices" \
    "{
        \"customerId\": \"${CUSTOMER_IDS[3]}\",
        \"lineItems\": [
            {
                \"description\": \"Brand Identity Design\",
                \"quantity\": 1,
                \"unitPrice\": 4500.00
            },
            {
                \"description\": \"Marketing Materials Design\",
                \"quantity\": 50,
                \"unitPrice\": 75.00
            }
        ]
    }" \
    "Create invoice for Creative Agency")

if [ $? -eq 0 ]; then
    invoice_id=$(extract_id "$response")
    INVOICE_IDS+=("$invoice_id")
    echo -e "${GREEN}✓ Created invoice (ID: ${invoice_id:0:8}...) - Total: \$8,250.00${NC}"
    
    # Mark as sent
    api_call "POST" "${API_URL}/invoices/${invoice_id}/send" "" "Mark invoice as sent" > /dev/null
    echo -e "${GREEN}✓ Invoice marked as SENT${NC}"
else
    echo -e "${RED}Failed to create invoice${NC}"
    exit 1
fi
echo ""

# Invoice 5: Manufacturing Co - Draft invoice
echo -e "${YELLOW}5. Creating Draft invoice for Manufacturing Co...${NC}"
response=$(api_call "POST" "${API_URL}/invoices" \
    "{
        \"customerId\": \"${CUSTOMER_IDS[4]}\",
        \"lineItems\": [
            {
                \"description\": \"Equipment Maintenance Services\",
                \"quantity\": 12,
                \"unitPrice\": 450.00
            },
            {
                \"description\": \"Technical Support - Annual Contract\",
                \"quantity\": 1,
                \"unitPrice\": 12000.00
            }
        ]
    }" \
    "Create invoice for Manufacturing Co")

if [ $? -eq 0 ]; then
    invoice_id=$(extract_id "$response")
    INVOICE_IDS+=("$invoice_id")
    echo -e "${GREEN}✓ Created Draft invoice (ID: ${invoice_id:0:8}...) - Total: \$17,400.00${NC}"
else
    echo -e "${RED}Failed to create invoice${NC}"
    exit 1
fi
echo ""

echo -e "${BLUE}=== Recording Payments ===${NC}"
echo ""

# Get current date in ISO format for payment dates
CURRENT_DATE=$(date -u +"%Y-%m-%dT%H:%M:%S")
PAYMENT_DATE_1=$(date -u -v-10d +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || date -u -d "10 days ago" +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || echo "$CURRENT_DATE")
PAYMENT_DATE_2=$(date -u -v-5d +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || date -u -d "5 days ago" +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || echo "$CURRENT_DATE")
PAYMENT_DATE_3=$(date -u -v-2d +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || date -u -d "2 days ago" +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || echo "$CURRENT_DATE")

# Payment 1: Partial payment for Global Services Ltd invoice
echo -e "${YELLOW}1. Recording partial payment for Global Services Ltd invoice...${NC}"
response=$(api_call "POST" "${API_URL}/payments" \
    "{
        \"invoiceId\": \"${INVOICE_IDS[2]}\",
        \"amount\": 5000.00,
        \"paymentDate\": \"${PAYMENT_DATE_1}\"
    }" \
    "Record payment for Global Services Ltd")

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Recorded payment of \$5,000.00 (Remaining balance: \$3,500.00)${NC}"
else
    echo -e "${YELLOW}⚠ Payment failed, but continuing...${NC}"
fi
echo ""

# Payment 2: Full payment for Creative Agency invoice
echo -e "${YELLOW}2. Recording full payment for Creative Agency invoice...${NC}"
response=$(api_call "POST" "${API_URL}/payments" \
    "{
        \"invoiceId\": \"${INVOICE_IDS[3]}\",
        \"amount\": 8250.00,
        \"paymentDate\": \"${PAYMENT_DATE_2}\"
    }" \
    "Record payment for Creative Agency")

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Recorded payment of \$8,250.00 (Invoice fully paid)${NC}"
else
    echo -e "${YELLOW}⚠ Payment failed, but continuing...${NC}"
fi
echo ""

# Payment 3: Second partial payment for Global Services Ltd
echo -e "${YELLOW}3. Recording second payment for Global Services Ltd invoice...${NC}"
response=$(api_call "POST" "${API_URL}/payments" \
    "{
        \"invoiceId\": \"${INVOICE_IDS[2]}\",
        \"amount\": 2000.00,
        \"paymentDate\": \"${PAYMENT_DATE_3}\"
    }" \
    "Record payment for Global Services Ltd")

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Recorded payment of \$2,000.00 (Remaining balance: \$1,500.00)${NC}"
else
    echo -e "${YELLOW}⚠ Payment failed, but continuing...${NC}"
fi
echo ""

echo "======================================"
echo -e "${GREEN}Demo data seeding completed!${NC}"
echo "======================================"
echo ""
echo "Summary:"
echo "  • 5 Customers created"
echo "  • 5 Invoices created (2 Draft, 2 Sent, 1 Paid)"
echo "  • 3 Payments recorded"
echo ""
echo "You can now view the demo data at:"
echo "  Frontend: https://invoiceme-frontend.onrender.com"
echo "  Backend API: ${API_URL}"
echo ""

