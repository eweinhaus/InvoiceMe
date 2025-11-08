# OAuth Consent Screen - Simple Step-by-Step Guide

This guide walks you through configuring the OAuth consent screen with minimal steps.

## Prerequisites
- A Google account
- Access to [Google Cloud Console](https://console.cloud.google.com)

## Step 1: Create or Select Project

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Click the **project dropdown** at the top (next to "Google Cloud")
3. Either:
   - **Select existing project** from the list, OR
   - **Click "New Project"** → Enter name: `InvoiceMe` → Click **Create**

## Step 2: Open OAuth Consent Screen (New Google Auth Platform Interface)

**Note**: Google has updated the interface. If you see "Google Auth Platform" in the sidebar, follow these steps:

1. In the left sidebar, you should see **"Google Auth Platform"** (you're already here if you see "OAuth Overview")
2. Click on **"Audience"** in the left sidebar (this is where you configure user type and app info)

## Step 3: Configure Basic Settings (New Interface)

### 3.1 Configure Audience (User Type)
1. Click **"Audience"** in the left sidebar (under Google Auth Platform)
2. You should see options for **"User type"**
3. **Select: "External"** (allows any Google user to sign in)
4. Click **"Save"** or **"Continue"**

### 3.2 Configure Branding (App Information)
1. Click **"Branding"** in the left sidebar
2. Fill in these **required fields**:
   - **App name**: `InvoiceMe`
   - **User support email**: Select your email from the dropdown
   - **App logo**: (Optional - can skip for now)
3. Click **"Save"**

### 3.3 Configure Developer Contact
1. Still in **"Branding"** or check **"Settings"** section
2. Find **"Developer contact information"**
3. Enter **your email address**
4. Click **"Save"**

## Step 4: Skip Optional Steps

You'll see several more pages. For each page:

1. **Scopes**: Click **"Save and Continue"** (no changes needed)
2. **Test users**: Click **"Save and Continue"** (we'll add this later if needed)
3. **Summary**: Click **"Back to Dashboard"**

## Step 5: Add Test User (If Needed)

If you get an error about test users when testing:

1. Go back to **"OAuth consent screen"**
2. Scroll down to **"Test users"** section
3. Click **"+ ADD USERS"**
4. Enter **your Google account email**
5. Click **"Add"**

## That's It!

The consent screen is now configured. You can now proceed to create OAuth credentials.

## Next Steps

After completing this, follow the steps in `GOOGLE_OAUTH_SETUP.md` starting from **Step 3: Create OAuth 2.0 Credentials**.

## Troubleshooting

### "You don't have permission to access this resource"
- Make sure you're logged in with a Google account that has access to the project
- Try creating a new project if you don't have access to the current one

### "App name is required"
- Make sure you filled in the "App name" field (Step 3.2)

### "User support email is required"
- Make sure you selected an email from the dropdown (Step 3.2)

### Can't find "OAuth consent screen"
- Make sure you're in **"APIs & Services"** → **"OAuth consent screen"** (not "Credentials")
- If you don't see it, try refreshing the page

## Quick Reference

**Minimum required fields:**
- ✅ App name: `InvoiceMe`
- ✅ User support email: Your email
- ✅ Developer contact: Your email

**Everything else can be skipped for development/testing!**

