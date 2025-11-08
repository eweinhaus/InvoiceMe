# Google OAuth Quick Start Checklist

## ✅ Frontend Changes (COMPLETED)

- [x] Updated `LoginPage.tsx` to redirect to Google OAuth endpoint
- [x] Added OAuth error handling from URL parameters
- [x] Google sign-in button now functional

## 🔧 Backend Configuration Required

### 1. Set Environment Variables

```bash
export GOOGLE_CLIENT_ID="your-client-id-from-google-cloud-console"
export GOOGLE_CLIENT_SECRET="your-client-secret-from-google-cloud-console"
export APP_AUTH_DEV_MODE=false  # Disable dev mode to enable OAuth2
```

### 2. Verify `application.yml` Configuration

Ensure your backend has:

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

app:
  auth:
    dev-mode: false  # Set to false to enable OAuth2
```

### 3. Verify SecurityConfig

Your `SecurityConfig.java` should:

- Enable OAuth2 login when dev mode is false:
  ```java
  .oauth2Login()
    .loginPage("/oauth2/authorization/google")
    .defaultSuccessUrl("http://localhost:5173/customers", true)
    .failureUrl("http://localhost:5173/login?error=true")
  ```

- Allow public access to OAuth2 endpoints:
  ```java
  .requestMatchers("/oauth2/**", "/login/**").permitAll()
  ```

- Configure CORS to allow credentials:
  ```java
  .cors(cors -> cors.configurationSource(corsConfigurationSource()))
  // Where corsConfigurationSource() allows credentials and frontend origin
  ```

## 📋 Google Cloud Console Setup

1. **Create Project** in [Google Cloud Console](https://console.cloud.google.com)
2. **Configure OAuth Consent Screen**:
   - App name: `InvoiceMe`
   - User support email: Your email
   - Add test users (if in testing mode)
3. **Create OAuth 2.0 Credentials**:
   - Type: Web application
   - Name: `InvoiceMe Development`
   - Authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`
   - Copy Client ID and Client Secret

## 🧪 Testing Steps

1. **Set environment variables** (see above)
2. **Restart backend** to load new environment variables
3. **Start frontend**: `npm run dev` (if not already running)
4. **Navigate to**: `http://localhost:5173/login`
5. **Click**: "Sign in with Google"
6. **Expected flow**:
   - Redirects to Google login
   - After login, redirects back to app
   - Should be authenticated and see `/customers` page

## 🐛 Common Issues

| Issue | Solution |
|-------|----------|
| `redirect_uri_mismatch` | Verify redirect URI in Google Console exactly matches: `http://localhost:8080/login/oauth2/code/google` |
| OAuth not working | Check `APP_AUTH_DEV_MODE=false` is set |
| Environment variables not found | Restart backend after setting variables |
| CORS errors | Verify CORS allows credentials and frontend origin |

## 📚 Full Documentation

See `GOOGLE_OAUTH_SETUP.md` for detailed step-by-step instructions.

