#!/bin/bash

# Deploy InvoiceMe to Render
# This script helps set up Render services using the Render API

set -e

RENDER_API_KEY="${RENDER_API_KEY:-rnd_2haFSXFINxhKEnxVEaaGIhRFL6hf}"
RENDER_API_URL="https://api.render.com/v1"

echo "🚀 InvoiceMe Render Deployment Script"
echo "======================================"
echo ""

# Check if jq is installed (for JSON parsing)
if ! command -v jq &> /dev/null; then
    echo "⚠️  jq is not installed. Installing via brew..."
    if command -v brew &> /dev/null; then
        brew install jq
    else
        echo "❌ Please install jq manually: https://stedolan.github.io/jq/download/"
        exit 1
    fi
fi

echo "✅ Using Render API Key: ${RENDER_API_KEY:0:10}..."
echo ""

# Function to make Render API calls
render_api() {
    local method=$1
    local endpoint=$2
    local data=$3
    
    if [ -n "$data" ]; then
        curl -s -X "$method" \
            -H "Authorization: Bearer $RENDER_API_KEY" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$RENDER_API_URL$endpoint"
    else
        curl -s -X "$method" \
            -H "Authorization: Bearer $RENDER_API_KEY" \
            "$RENDER_API_URL$endpoint"
    fi
}

echo "📋 Available Render Services:"
echo "----------------------------"
render_api GET "/services" | jq -r '.[] | "\(.service.name) - \(.service.type) - \(.service.serviceDetails.url // "N/A")"'
echo ""

echo "📝 Next Steps:"
echo "--------------"
echo "1. Create PostgreSQL database via Render dashboard or API"
echo "2. Create backend web service"
echo "3. Create frontend static site"
echo ""
echo "For detailed instructions, see: planning/tasks/Render_Deployment_Tasks.md"
echo ""
echo "To create services via API, you can use:"
echo "  render_api POST \"/services\" '{\"service\": {...}}'"

