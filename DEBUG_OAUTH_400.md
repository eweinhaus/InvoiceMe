# Debugging Google OAuth 400 Error

Since you already have the redirect URI configured correctly, let's check other common causes:

## Step 1: Verify Client ID Format

The Client ID should:
- End with `.apps.googleusercontent.com`
- Be exactly as shown in Google Cloud Console (no extra spaces or characters)
- Match exactly what's in your `.env` file

**Check your `.env` file**:
```bash
cd backend
cat .env | grep GOOGLE_CLIENT_ID
```

The format should be:
```
GOOGLE_CLIENT_ID=123456789-abcdefghijklmnop.apps.googleusercontent.com
```

**Common mistakes**:
- ❌ Extra spaces: `GOOGLE_CLIENT_ID = value`
- ❌ Quotes: `GOOGLE_CLIENT_ID="value"`
- ❌ Missing `.apps.googleusercontent.com` suffix
- ❌ Copy-paste errors (extra characters)

## Step 2: Verify Client Secret

The Client Secret should:
- Be exactly as shown in Google Cloud Console
- Match exactly what's in your `.env` file
- No quotes or extra spaces

**Check your `.env` file**:
```bash
cd backend
cat .env | grep GOOGLE_CLIENT_SECRET
```

## Step 3: Check OAuth Consent Screen

If your app is in **Testing** mode:

1. Go to Google Cloud Console → **Google Auth Platform** → **Audience**
2. Scroll down to **Test users**
3. Make sure **your Google account email** is listed
4. If not, click **"+ ADD USERS"** and add your email
5. **Save** and wait a few seconds

## Step 4: Verify Redirect URI (Double-Check)

Even though you have it, let's verify it's exactly right:

1. Go to Google Cloud Console → **Google Auth Platform** → **Clients**
2. Click on your OAuth client
3. Look at **Authorized redirect URIs**
4. Check for:
   - ✅ Exactly: `http://localhost:8080/login/oauth2/code/google`
   - ❌ No trailing slash
   - ❌ No extra spaces
   - ❌ No `https://` instead of `http://`
   - ❌ No `127.0.0.1` instead of `localhost`

**If you see multiple URIs**, make sure none have typos that might be causing issues.

## Step 5: Check Authorized JavaScript Origins

1. In the same client configuration
2. Under **Authorized JavaScript origins**
3. Should have: `http://localhost:8080`
4. No trailing slash
5. Should be `http://` not `https://`

## Step 6: Restart Backend

After checking/updating `.env` file:

1. Stop the backend (Ctrl+C or kill the process)
2. Restart it: `cd backend && mvn spring-boot:run`
3. Wait for it to fully start
4. Try again

## Step 7: Check Backend Logs

When you click "Sign in with Google", check the backend console for:
- Any error messages
- OAuth2 configuration errors
- Client ID/Secret loading errors

## Step 8: Try Direct URL

Test the OAuth endpoint directly in your browser:

1. Open a new tab
2. Go to: `http://localhost:8080/oauth2/authorization/google`
3. You should be redirected to Google
4. If you get a 400 here too, it's definitely a configuration issue

## Step 9: Verify .env File Location

Make sure the `.env` file is:
- In the `backend/` directory
- Same level as `pom.xml`
- Named exactly `.env` (not `.env.txt` or `env.txt`)

## Step 10: Check for Hidden Characters

Sometimes copy-paste can introduce hidden characters. Try:
1. Manually typing the Client ID and Secret (don't copy-paste)
2. Or copy from Google Console in a plain text editor first
3. Then paste into `.env` file

## Still Not Working?

Share:
1. The first few characters of your Client ID (e.g., `896566428459-...`)
2. Whether your app is in "Testing" or "Production" mode
3. Any error messages from backend logs
4. Whether the direct URL test works (`http://localhost:8080/oauth2/authorization/google`)

