#!/bin/bash
set -e

# InvoiceMe AWS Infrastructure Setup Script
# Creates all necessary AWS resources using CloudFormation

REGION=${AWS_REGION:-us-east-1}
ENVIRONMENT=${ENVIRONMENT:-prod}
STACK_NAME="${ENVIRONMENT}-invoiceme-infrastructure"

echo "🏗️  Setting up InvoiceMe AWS Infrastructure"
echo "Region: $REGION"
echo "Environment: $ENVIRONMENT"
echo "Stack Name: $STACK_NAME"

# Check if required parameters are provided
if [ -z "$DB_PASSWORD" ] || [ -z "$DB_USERNAME" ] || [ -z "$GOOGLE_CLIENT_ID" ] || [ -z "$GOOGLE_CLIENT_SECRET" ]; then
    echo ""
    echo "❌ Error: Required parameters not set"
    echo "Please set the following environment variables:"
    echo "  DB_PASSWORD - PostgreSQL database password (min 8 characters)"
    echo "  DB_USERNAME - PostgreSQL database username"
    echo "  GOOGLE_CLIENT_ID - Google OAuth Client ID"
    echo "  GOOGLE_CLIENT_SECRET - Google OAuth Client Secret"
    echo ""
    echo "Example:"
    echo "  export DB_PASSWORD='your-secure-password'"
    echo "  export DB_USERNAME='invoiceme_admin'"
    echo "  export GOOGLE_CLIENT_ID='your-client-id'"
    echo "  export GOOGLE_CLIENT_SECRET='your-client-secret'"
    exit 1
fi

cd "$(dirname "$0")/../infrastructure"

echo ""
echo "📋 Deploying CloudFormation stack..."

aws cloudformation deploy \
    --template-file cloudformation-infrastructure.yaml \
    --stack-name $STACK_NAME \
    --region $REGION \
    --capabilities CAPABILITY_IAM \
    --parameter-overrides \
        Environment=$ENVIRONMENT \
        DatabaseName=invoiceme \
        DatabaseUsername=$DB_USERNAME \
        DatabasePassword=$DB_PASSWORD \
        GoogleClientId=$GOOGLE_CLIENT_ID \
        GoogleClientSecret=$GOOGLE_CLIENT_SECRET \
    --tags \
        Environment=$ENVIRONMENT \
        Project=InvoiceMe

echo ""
echo "✅ Infrastructure deployment complete!"
echo ""
echo "📊 Stack outputs:"
aws cloudformation describe-stacks \
    --stack-name $STACK_NAME \
    --region $REGION \
    --query 'Stacks[0].Outputs' \
    --output table

echo ""
echo "📝 Next steps:"
echo "1. Update ECS task definition with subnet and security group IDs from outputs"
echo "2. Create ECS service using the cluster name from outputs"
echo "3. Update CloudFront distribution with S3 bucket origin"
echo "4. Configure OAuth redirect URI in Google Cloud Console with ALB URL"

