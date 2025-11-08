# Fixing Google OAuth 400 Error

## The Problem
You're getting a **400 error** from Google OAuth. This almost always means the **redirect URI doesn't match** what's configured in Google Cloud Console.

## The Solution

### Step 1: Verify Redirect URI in Google Cloud Console

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Navigate to **Google Auth Platform** → **Clients**
3. Click on your OAuth client (InvoiceMe Development)
4. Check the **Authorized redirect URIs** section

### Step 2: Required Redirect URI

The redirect URI **MUST** be exactly:
```
http://localhost:8080/login/oauth2/code/google
```

**Important**:
- ✅ Must start with `http://` (not `https://`)
- ✅ Must be `localhost:8080` (not `127.0.0.1`)
- ✅ Must include `/login/oauth2/code/google` (exact path)
- ✅ No trailing slash
- ✅ No typos or extra characters

### Step 3: Add/Update Redirect URI

If the redirect URI is missing or incorrect:

1. In Google Cloud Console → Clients → Your Client
2. Under **Authorized redirect URIs**, click **"+ ADD URI"** or edit existing
3. Add exactly: `http://localhost:8080/login/oauth2/code/google`
4. Click **"SAVE"** or **"UPDATE"**

### Step 4: Also Check Authorized JavaScript Origins

Make sure you have:
- **Authorized JavaScript origins**: `http://localhost:8080`

### Step 5: Wait a Few Seconds

Google's changes can take a few seconds to propagate. Wait 10-30 seconds after saving.

### Step 6: Try Again

1. Clear your browser cache/cookies (or use incognito mode)
2. Go to `http://localhost:5173/login`
3. Click "Sign in with Google" again

## Common Mistakes

❌ **Wrong**: `http://localhost:8080/login/oauth2/code/Google` (capital G)
❌ **Wrong**: `http://localhost:8080/login/oauth2/code/google` (missing `/oauth2/`)
❌ **Wrong**: `http://127.0.0.1:8080/login/oauth2/code/google` (should be localhost)
❌ **Wrong**: `https://localhost:8080/login/oauth2/code/google` (should be http)
❌ **Wrong**: `http://localhost:8080/login/oauth2/code/google/` (trailing slash)

✅ **Correct**: `http://localhost:8080/login/oauth2/code/google`

## Verify Your Configuration

Your backend `application.yml` should have:
```yaml
redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
```

This resolves to: `http://localhost:8080/login/oauth2/code/google`

## Still Getting 400?

1. **Double-check the redirect URI** in Google Cloud Console matches exactly
2. **Check your Client ID** - make sure it matches what's in your `.env` file
3. **Check your Client Secret** - make sure it matches what's in your `.env` file
4. **Restart the backend** after changing `.env` file
5. **Try in incognito mode** to rule out browser cache issues

## Need Help?

If you're still getting the error, share:
- The exact redirect URI you have in Google Cloud Console
- A screenshot of your OAuth client configuration (with sensitive info redacted)

