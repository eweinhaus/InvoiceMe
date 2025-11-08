# Verify OAuth Setup

## Quick Verification Steps

### 1. Check .env File Format

Your `.env` file in the `backend/` directory should look like this:

```env
GOOGLE_CLIENT_ID=your-client-id-here.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-client-secret-here
```

**Important**: 
- No quotes around the values
- No spaces around the `=` sign
- One variable per line

### 2. Verify Application Configuration

Check that `application.yml` has:
- ✅ `dev-mode: false` (line 57)
- ✅ OAuth2 client configuration (lines 29-45)

### 3. Test the Backend

1. **Start the backend**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **Check for errors**:
   - Look for: `Could not resolve placeholder 'GOOGLE_CLIENT_ID'`
   - If you see this, the .env file isn't being loaded correctly

3. **Check logs for OAuth2**:
   - You should see OAuth2 client configuration in the logs
   - No errors about missing OAuth2 configuration

### 4. Test OAuth Flow

1. **Start frontend** (if not already running):
   ```bash
   cd frontend
   npm run dev
   ```

2. **Navigate to login page**:
   - Go to `http://localhost:5173/login`
   - Click "Sign in with Google"

3. **Expected behavior**:
   - Should redirect to Google login page
   - After login, redirects back to `/customers`
   - You should be authenticated

## Troubleshooting

### Error: "Could not resolve placeholder 'GOOGLE_CLIENT_ID'"

**Solution**: The .env file isn't being loaded. Check:
1. `.env` file is in the `backend/` directory (same level as `pom.xml`)
2. File is named exactly `.env` (not `.env.txt` or `env`)
3. Restart the backend after creating/modifying .env file

### Error: "OAuth2 client registration not found"

**Solution**: 
1. Verify `dev-mode: false` in `application.yml`
2. Check that OAuth2 client configuration is present in `application.yml`
3. Restart the backend

### Error: "redirect_uri_mismatch"

**Solution**: 
1. Go to Google Cloud Console → Clients
2. Verify redirect URI is exactly: `http://localhost:8080/login/oauth2/code/google`
3. No trailing slashes or typos

### OAuth button doesn't redirect

**Solution**:
1. Check browser console for errors
2. Verify backend is running on port 8080
3. Check CORS configuration allows frontend origin

## Quick Test Script

Run this to verify environment variables are set:

```bash
cd backend
# Check if .env file exists
ls -la .env

# If using bash/zsh, you can test loading (optional):
# source <(cat .env | sed 's/^/export /')
# echo $GOOGLE_CLIENT_ID
```

## Next Steps

Once OAuth is working:
1. ✅ Test login flow
2. ✅ Verify user info is displayed in header
3. ✅ Test protected routes
4. ✅ Test logout functionality

