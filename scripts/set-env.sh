#!/bin/bash
# Quick script to set deployment environment variables
# Usage: source ./set-env.sh

export AWS_REGION=${AWS_REGION:-us-east-1}
export ENVIRONMENT=${ENVIRONMENT:-prod}

# Check if variables are already set
if [ -z "$DB_PASSWORD" ] || [ -z "$DB_USERNAME" ] || [ -z "$GOOGLE_CLIENT_ID" ] || [ -z "$GOOGLE_CLIENT_SECRET" ]; then
    echo "⚠️  Required environment variables not set."
    echo ""
    echo "Please set the following:"
    echo "  export DB_PASSWORD='your-secure-password'"
    echo "  export DB_USERNAME='invoiceme_admin'"
    echo "  export GOOGLE_CLIENT_ID='your-google-client-id'"
    echo "  export GOOGLE_CLIENT_SECRET='your-google-client-secret'"
    echo ""
    echo "Or run: source ./set-env.sh"
    exit 1
fi

echo "✅ Environment variables configured"
echo "Region: $AWS_REGION"
echo "Environment: $ENVIRONMENT"


