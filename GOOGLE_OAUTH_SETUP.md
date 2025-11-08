# Google OAuth Setup Guide

This guide will help you set up Google OAuth2 authentication for InvoiceMe.

## Prerequisites

- A Google account
- Access to Google Cloud Console (https://console.cloud.google.com)

## Step 1: Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Click on the project dropdown at the top
3. Click "New Project"
4. Enter project name: `InvoiceMe` (or your preferred name)
5. Click "Create"
6. Wait for the project to be created and select it

## Step 2: Configure OAuth Consent Screen

**⚠️ IMPORTANT**: The consent screen must be configured via the web console - there's no CLI command for this.

**For a simplified guide, see `OAUTH_CONSENT_SCREEN_SIMPLE.md`**

### Quick Steps:
1. In the left sidebar, go to **APIs & Services** > **OAuth consent screen**
2. Select **External** (unless you have a Google Workspace account)
3. Click **Create**
4. Fill in the required information:
   - **App name**: `InvoiceMe`
   - **User support email**: Your email address (select from dropdown)
   - **Developer contact information**: Your email address
5. Click **Save and Continue**
6. On the **Scopes** page, click **Save and Continue** (no need to add scopes manually)
7. On the **Test users** page (if in testing mode):
   - Click **Add Users**
   - Add your Google account email
   - Click **Save and Continue**
8. On the **Summary** page, click **Back to Dashboard**

**Note**: If you're having trouble with this step, see `OAUTH_CONSENT_SCREEN_SIMPLE.md` for a more detailed walkthrough.

## Step 3: Create OAuth 2.0 Credentials

1. In the left sidebar, go to **APIs & Services** > **Credentials**
2. Click **Create Credentials** > **OAuth client ID**
3. Select **Web application** as the application type
4. Fill in the details:
   - **Name**: `InvoiceMe Development`
   - **Authorized JavaScript origins**: 
     - `http://localhost:8080`
   - **Authorized redirect URIs**:
     - `http://localhost:8080/login/oauth2/code/google`
5. Click **Create**
6. **IMPORTANT**: Copy the **Client ID** and **Client Secret** - you'll need these in the next step

## Step 4: Set Environment Variables

### Option A: Export in Terminal (Temporary - for current session)

```bash
export GOOGLE_CLIENT_ID="your-client-id-here"
export GOOGLE_CLIENT_SECRET="your-client-secret-here"
```

### Option B: Add to Shell Profile (Permanent)

Add to your `~/.zshrc` (or `~/.bashrc` if using bash):

```bash
export GOOGLE_CLIENT_ID="your-client-id-here"
export GOOGLE_CLIENT_SECRET="your-client-secret-here"
```

Then reload:
```bash
source ~/.zshrc
```

### Option C: Create `.env` file (Backend)

If your backend supports `.env` files, create one in the backend root directory:

```env
GOOGLE_CLIENT_ID=your-client-id-here
GOOGLE_CLIENT_SECRET=your-client-secret-here
```

## Step 5: Configure Backend

### Verify `application.yml` Configuration

Ensure your backend `application.yml` has the OAuth2 configuration:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - openid
              - profile
              - email
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            authorization-grant-type: authorization_code
            client-name: Google
        provider:
          google:
            issuer-uri: https://accounts.google.com
```

### Disable Dev Mode

To enable Google OAuth, ensure dev mode is disabled in your backend configuration:

```yaml
app:
  auth:
    dev-mode: false  # Set to false to enable OAuth2
```

Or set the environment variable:
```bash
export APP_AUTH_DEV_MODE=false
```

## Step 6: Verify SecurityConfig

Ensure your `SecurityConfig.java` has OAuth2 login enabled when dev mode is false. The configuration should include:

```java
.oauth2Login()
  .loginPage("/oauth2/authorization/google")
  .defaultSuccessUrl("http://localhost:5173/customers", true)
  .failureUrl("http://localhost:5173/login?error=true")
```

And allow public access to OAuth2 endpoints:

```java
.requestMatchers("/oauth2/**", "/login/**").permitAll()
```

## Step 7: Test the Flow

1. **Start the backend**:
   ```bash
   # In backend directory
   mvn spring-boot:run
   ```

2. **Start the frontend**:
   ```bash
   # In frontend directory
   npm run dev
   ```

3. **Test the login**:
   - Navigate to `http://localhost:5173/login`
   - Click "Sign in with Google"
   - You should be redirected to Google's login page
   - After logging in with your Google account, you'll be redirected back to the app
   - You should be authenticated and redirected to `/customers`

## Troubleshooting

### Issue: "redirect_uri_mismatch" Error

**Solution**: 
- Verify the redirect URI in Google Cloud Console exactly matches: `http://localhost:8080/login/oauth2/code/google`
- Make sure there are no trailing slashes or typos

### Issue: "OAuth2 configuration not found"

**Solution**:
- Verify environment variables are set: `echo $GOOGLE_CLIENT_ID`
- Restart the backend after setting environment variables
- Check backend logs for OAuth2 configuration errors

### Issue: "Access blocked: This app's request is invalid"

**Solution**:
- If your app is in "Testing" mode, make sure your Google account is added as a test user
- Go to OAuth consent screen > Test users and add your email

### Issue: Frontend redirects but backend doesn't authenticate

**Solution**:
- Check CORS configuration allows credentials
- Verify session cookies are being set (check browser DevTools > Application > Cookies)
- Ensure `credentials: 'include'` is set in frontend API calls

### Issue: Backend redirects to wrong URL after OAuth

**Solution**:
- Update `defaultSuccessUrl` in SecurityConfig to point to your frontend URL
- Example: `http://localhost:5173/customers`

## Production Considerations

For production deployment:

1. **Update OAuth Credentials**:
   - Create a new OAuth client ID for production
   - Add production redirect URIs (e.g., `https://yourdomain.com/login/oauth2/code/google`)

2. **Secure Environment Variables**:
   - Use a secrets management service (AWS Secrets Manager, Azure Key Vault, etc.)
   - Never commit credentials to version control

3. **OAuth Consent Screen**:
   - Submit your app for verification if you want to make it public
   - Add production domains to authorized domains

4. **HTTPS Required**:
   - OAuth requires HTTPS in production
   - Update redirect URIs to use `https://`

## Additional Resources

- [Google OAuth 2.0 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [Spring Security OAuth2 Client](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
- [Google Cloud Console](https://console.cloud.google.com)

