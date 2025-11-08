# OAuth Consent Screen - New Google Auth Platform Interface

Google has updated their interface! Here's how to configure OAuth in the **new Google Auth Platform** interface.

## Current Location
You should be seeing:
- **Left sidebar**: "Google Auth Platform" with sections like Overview, Branding, Audience, Clients, etc.
- **Main area**: "OAuth Overview" page

## Step-by-Step Configuration

### Step 1: Configure Audience (User Type)

1. In the left sidebar, click **"Audience"**
2. You'll see options for **"User type"**
3. **Select: "External"** (allows any Google user to sign in)
   - This is required for allowing users outside your organization
4. Click **"Save"** or **"Continue"**

### Step 2: Configure Branding (App Information)

1. In the left sidebar, click **"Branding"**
2. Fill in the required fields:
   - **App name**: `InvoiceMe`
   - **User support email**: 
     - Click the dropdown
     - Select **your email address** (the one you're logged in with)
   - **App logo**: (Optional - you can skip this)
   - **App domain**: (Optional - can skip for localhost development)
3. Scroll down and look for **"Developer contact information"**
   - Enter **your email address** here
4. Click **"Save"** at the bottom

### Step 3: Add Test Users (If Needed)

1. Go back to **"Audience"** in the left sidebar
2. Scroll down to find **"Test users"** section
3. Click **"+ ADD USERS"** or **"Add test users"**
4. Enter **your Google account email**
5. Click **"Add"** or **"Save"**

### Step 4: Create OAuth Client (Credentials)

1. In the left sidebar, click **"Clients"**
2. Click **"+ CREATE CLIENT"** or **"Create OAuth client ID"**
3. Select **"Web application"** as the application type
4. Fill in:
   - **Name**: `InvoiceMe Development`
   - **Authorized JavaScript origins**: 
     - Click **"+ ADD URI"**
     - Enter: `http://localhost:8080`
   - **Authorized redirect URIs**:
     - Click **"+ ADD URI"**
     - Enter: `http://localhost:8080/login/oauth2/code/google`
5. Click **"CREATE"** or **"Save"**
6. **IMPORTANT**: Copy the **Client ID** and **Client Secret** immediately - you'll need these!

## Quick Navigation Reference

**New Interface Structure:**
- **Overview** → Metrics and token grant rates (where you are now)
- **Branding** → App name, logo, support email, developer contact
- **Audience** → User type (External/Internal), test users
- **Clients** → Create and manage OAuth client IDs
- **Data Access** → Scopes and permissions
- **Settings** → Additional configuration

## What to Fill In (Minimum Required)

✅ **Audience**:
- User type: **External**

✅ **Branding**:
- App name: **InvoiceMe**
- User support email: **Your email**
- Developer contact: **Your email**

✅ **Clients**:
- Create a Web application client
- Redirect URI: `http://localhost:8080/login/oauth2/code/google`

## Troubleshooting

### Can't find "Audience" or "Branding"
- Make sure you're in the **"Google Auth Platform"** section (not "APIs & Services")
- The left sidebar should show: Overview, Branding, Audience, Clients, etc.

### "External" option is grayed out
- You might need to verify your domain first
- For development, try selecting "Internal" if available, or check if there's a verification step

### Can't create OAuth client
- Make sure you've completed the Audience and Branding steps first
- Some projects require the consent screen to be configured before creating clients

## Next Steps

After completing these steps:
1. Copy your **Client ID** and **Client Secret** from the Clients page
2. Set them as environment variables:
   ```bash
   export GOOGLE_CLIENT_ID="your-client-id-here"
   export GOOGLE_CLIENT_SECRET="your-client-secret-here"
   export APP_AUTH_DEV_MODE=false
   ```
3. Restart your backend
4. Test the OAuth flow!

