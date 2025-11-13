#!/bin/bash
set -e

# InvoiceMe Frontend Deployment Script
# Deploys frontend to AWS S3 + CloudFront

REGION=${AWS_REGION:-us-east-1}
ENVIRONMENT=${ENVIRONMENT:-prod}
BUCKET_NAME="${ENVIRONMENT}-invoiceme-frontend"
CLOUDFRONT_DIST_ID=${CLOUDFRONT_DIST_ID:-""}
BACKEND_API_URL=${BACKEND_API_URL:-""}

echo "🚀 Starting InvoiceMe Frontend Deployment"
echo "Region: $REGION"
echo "Environment: $ENVIRONMENT"
echo "Bucket: $BUCKET_NAME"
echo "Backend API URL: $BACKEND_API_URL"

# Step 1: Build frontend
echo ""
echo "📦 Building frontend..."
cd "$(dirname "$0")/../frontend"

# Create production .env file
if [ -n "$BACKEND_API_URL" ]; then
    echo "VITE_API_URL=$BACKEND_API_URL" > .env.production
    echo "Created .env.production with API URL: $BACKEND_API_URL"
fi

npm ci
npm run build

if [ ! -d "dist" ]; then
    echo "❌ Error: dist directory not found after build"
    exit 1
fi

# Step 2: Upload to S3
echo ""
echo "☁️  Uploading to S3..."
aws s3 sync dist/ s3://$BUCKET_NAME/ \
    --delete \
    --region $REGION \
    --cache-control "public, max-age=31536000, immutable" \
    --exclude "*.html" \
    --exclude "*.json"

# Upload HTML files with no cache
aws s3 sync dist/ s3://$BUCKET_NAME/ \
    --delete \
    --region $REGION \
    --cache-control "no-cache, no-store, must-revalidate" \
    --include "*.html" \
    --include "*.json"

# Step 3: Invalidate CloudFront cache (if distribution ID provided)
if [ -n "$CLOUDFRONT_DIST_ID" ]; then
    echo ""
    echo "🔄 Invalidating CloudFront cache..."
    INVALIDATION_ID=$(aws cloudfront create-invalidation \
        --distribution-id $CLOUDFRONT_DIST_ID \
        --paths "/*" \
        --region $REGION \
        --query 'Invalidation.Id' \
        --output text)
    
    echo "CloudFront invalidation created: $INVALIDATION_ID"
    echo "Cache invalidation in progress. This may take a few minutes."
fi

echo ""
echo "✅ Frontend deployment complete!"
echo "Bucket: s3://$BUCKET_NAME"
if [ -n "$CLOUDFRONT_DIST_ID" ]; then
    echo "CloudFront Distribution: $CLOUDFRONT_DIST_ID"
fi



