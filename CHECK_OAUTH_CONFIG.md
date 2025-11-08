# Quick OAuth 400 Error Checklist

Since you have the redirect URI configured, let's verify these other critical items:

## ✅ Quick Checks

### 1. Test User (MOST COMMON FIX)

If your app is in **Testing** mode, you MUST add yourself as a test user:

1. Go to Google Cloud Console → **Google Auth Platform** → **Audience**
2. Scroll to **Test users** section
3. Click **"+ ADD USERS"**
4. Add **your exact Google account email** (the one you're trying to sign in with)
5. Click **"ADD"**
6. **Save** if prompted
7. Wait 10-30 seconds
8. Try again

**This is the #1 cause of 400 errors when redirect URI is correct!**

### 2. Verify .env File Format

Your `.env` file should look EXACTLY like this (no quotes, no spaces around =):

```env
GOOGLE_CLIENT_ID=896566428459-p0qjrd9kd64hvfvb5svpc33i7ifmhtpk.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-your-secret-here
```

**Common mistakes to check:**
- ❌ Quotes: `GOOGLE_CLIENT_ID="value"` 
- ❌ Spaces: `GOOGLE_CLIENT_ID = value`
- ❌ Missing `.apps.googleusercontent.com` suffix
- ❌ Extra characters or line breaks

### 3. Restart Backend After .env Changes

After modifying `.env`:
1. Stop backend (Ctrl+C)
2. Start again: `mvn spring-boot:run`
3. Wait for full startup
4. Try OAuth again

### 4. Check OAuth Consent Screen Status

1. Go to Google Cloud Console → **Google Auth Platform** → **Overview**
2. Check if it says "Testing" or "In production"
3. If "Testing", make sure you're added as a test user (see #1)

### 5. Verify Client ID Matches

The Client ID in your `.env` should match EXACTLY what's shown in:
- Google Cloud Console → Clients → Your Client → Client ID

No extra spaces, no missing characters.

## 🔍 Debug Steps

### Test Direct URL

Open this in your browser directly:
```
http://localhost:8080/oauth2/authorization/google
```

If this also gives a 400, it's definitely a backend/Google Console configuration issue.

### Check Backend Logs

When you click "Sign in with Google", watch the backend console for:
- Any error messages
- OAuth2 configuration warnings
- Client ID/Secret loading errors

## 🎯 Most Likely Fix

**If redirect URI is correct, 90% of the time it's:**
1. **Missing test user** (if app is in Testing mode) ← Try this first!
2. **Wrong Client ID/Secret** in .env file
3. **Backend not restarted** after .env changes

Try adding yourself as a test user first - that's the most common cause!

