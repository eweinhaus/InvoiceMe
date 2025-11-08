#!/bin/bash

# Verify Google OAuth Setup Script
# This script helps verify that your OAuth setup is complete

echo "🔍 Google OAuth Setup Verification"
echo "=================================="
echo ""

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    echo "⚠️  gcloud CLI is not installed."
    echo "   Install it from: https://cloud.google.com/sdk/docs/install"
    echo ""
else
    echo "✅ gcloud CLI is installed"
    
    # Check if authenticated
    if gcloud auth list --filter=status:ACTIVE --format="value(account)" &> /dev/null; then
        ACTIVE_ACCOUNT=$(gcloud auth list --filter=status:ACTIVE --format="value(account)" | head -n 1)
        echo "✅ Authenticated as: $ACTIVE_ACCOUNT"
    else
        echo "⚠️  Not authenticated. Run: gcloud auth login"
    fi
    
    # Check if project is set
    PROJECT=$(gcloud config get-value project 2>/dev/null)
    if [ -n "$PROJECT" ]; then
        echo "✅ Project set to: $PROJECT"
    else
        echo "⚠️  No project set. Run: gcloud config set project PROJECT_ID"
    fi
    echo ""
fi

# Check environment variables
echo "📋 Environment Variables Check:"
echo "-------------------------------"

if [ -n "$GOOGLE_CLIENT_ID" ]; then
    echo "✅ GOOGLE_CLIENT_ID is set"
    echo "   Value: ${GOOGLE_CLIENT_ID:0:20}..."
else
    echo "❌ GOOGLE_CLIENT_ID is not set"
    echo "   Set it with: export GOOGLE_CLIENT_ID='your-client-id'"
fi

if [ -n "$GOOGLE_CLIENT_SECRET" ]; then
    echo "✅ GOOGLE_CLIENT_SECRET is set"
    echo "   Value: ${GOOGLE_CLIENT_SECRET:0:10}..."
else
    echo "❌ GOOGLE_CLIENT_SECRET is not set"
    echo "   Set it with: export GOOGLE_CLIENT_SECRET='your-client-secret'"
fi

if [ -n "$APP_AUTH_DEV_MODE" ]; then
    if [ "$APP_AUTH_DEV_MODE" = "false" ]; then
        echo "✅ APP_AUTH_DEV_MODE is set to false (OAuth2 enabled)"
    else
        echo "⚠️  APP_AUTH_DEV_MODE is set to: $APP_AUTH_DEV_MODE"
        echo "   Set to 'false' to enable OAuth2: export APP_AUTH_DEV_MODE=false"
    fi
else
    echo "⚠️  APP_AUTH_DEV_MODE is not set (defaults to dev mode)"
    echo "   Set to 'false' to enable OAuth2: export APP_AUTH_DEV_MODE=false"
fi

echo ""
echo "📝 Next Steps:"
echo "--------------"

if [ -z "$GOOGLE_CLIENT_ID" ] || [ -z "$GOOGLE_CLIENT_SECRET" ]; then
    echo "1. Follow OAUTH_CONSENT_SCREEN_SIMPLE.md to configure consent screen"
    echo "2. Create OAuth credentials in Google Cloud Console"
    echo "3. Set GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET environment variables"
else
    echo "✅ OAuth credentials are configured!"
    echo ""
    echo "To test:"
    echo "1. Make sure backend is running with OAuth2 enabled"
    echo "2. Navigate to http://localhost:5173/login"
    echo "3. Click 'Sign in with Google'"
fi

echo ""
echo "📚 Documentation:"
echo "   - OAUTH_CONSENT_SCREEN_SIMPLE.md (consent screen setup)"
echo "   - GOOGLE_OAUTH_SETUP.md (full setup guide)"
echo "   - GOOGLE_OAUTH_QUICK_START.md (quick reference)"

