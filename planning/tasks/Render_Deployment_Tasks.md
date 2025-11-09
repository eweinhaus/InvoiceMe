# Task List: Render Deployment

## Overview
**Priority**: 🟢 Deployment Task  
**Estimated Time**: 2-3 hours  
**Dependencies**: None (app works locally)  
**Goal**: Deploy InvoiceMe application to Render platform, replacing AWS deployment configuration

This task list breaks down the Render deployment into actionable steps, including disabling gcloud auth checks and using Render MCP for service configuration.

---

## Pre-Deployment Preparation

### 0. Set Up Render MCP in Cursor

#### 0.1 Install Render MCP Server
- [ ] Open Cursor Settings (Cmd/Ctrl + ,)
- [ ] Navigate to Features > Model Context Protocol (MCP)
- [ ] Add new MCP server configuration
- [ ] Configure Render MCP server:
  - [ ] Server name: `render`
  - [ ] Command: Check Render MCP documentation for installation command
  - [ ] Environment variables: Add `RENDER_API_KEY` (get from Render dashboard)
- [ ] Save MCP server configuration
- [ ] Restart Cursor to activate MCP server

#### 0.2 Verify Render MCP Connection
- [ ] Verify Render MCP server is connected in Cursor
- [ ] Test MCP connection by listing available Render resources
- [ ] Confirm MCP tools are available (create service, list services, etc.)
- [ ] Document Render API key location for future reference

#### 0.3 Get Render API Key
- [ ] Log in to Render dashboard (https://dashboard.render.com)
- [ ] Navigate to Account Settings > API Keys
- [ ] Generate new API key (or use existing)
- [ ] Copy API key securely
- [ ] Add to Cursor MCP server configuration as `RENDER_API_KEY` environment variable

---

## Pre-Deployment Preparation

### 1. Disable gcloud Authentication Checks

#### 1.1 Update verify-oauth-setup.sh
- [ ] Remove or comment out gcloud CLI checks (lines 10-33)
- [ ] Keep environment variable checks (GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, APP_AUTH_DEV_MODE)
- [ ] Update script to focus only on OAuth environment variables
- [ ] Remove references to gcloud commands and project configuration
- [ ] Update documentation comments to reflect Render deployment

#### 1.2 Remove gcloud Dependencies
- [ ] Verify no other scripts or files reference gcloud
- [ ] Check for any gcloud-related configuration in deployment scripts
- [ ] Document that gcloud is no longer required for this deployment

---

## Render Service Configuration

### 2. Set Up Render PostgreSQL Database

#### 2.1 Create Database via Render MCP
- [ ] Use Render MCP to create PostgreSQL database service
- [ ] Configure database name: `invoiceme`
- [ ] Note the database connection details (host, port, database name, username, password)
- [ ] Save internal database URL for backend service configuration
- [ ] Save external database URL if needed for local testing

#### 2.2 Database Configuration
- [ ] Verify database is accessible
- [ ] Note that Flyway migrations will run automatically on backend startup
- [ ] Ensure database has sufficient resources for production workload

---

### 3. Set Up Backend Web Service

#### 3.1 Create Backend Service via Render MCP
- [ ] Use Render MCP to create Web Service for backend
- [ ] Configure service name: `invoiceme-backend`
- [ ] Set root directory: `backend`
- [ ] Set build command: `mvn clean package -DskipTests`
- [ ] Set start command: `java -jar target/invoiceme-backend-1.0.0.jar`
- [ ] Configure environment: `Java` or `Docker` (if using Dockerfile)
- [ ] Set instance type (recommended: Starter or Standard)

#### 3.2 Backend Environment Variables
- [ ] Set `SPRING_PROFILES_ACTIVE=prod`
- [ ] Set `DB_HOST` (from PostgreSQL service internal hostname)
- [ ] Set `DB_PORT=5432` (or from PostgreSQL service)
- [ ] Set `DB_NAME=invoiceme` (or from PostgreSQL service)
- [ ] Set `DB_USERNAME` (from PostgreSQL service)
- [ ] Set `DB_PASSWORD` (from PostgreSQL service - use Render secrets)
- [ ] Set `GOOGLE_CLIENT_ID` (from Google Cloud Console)
- [ ] Set `GOOGLE_CLIENT_SECRET` (from Google Cloud Console - use Render secrets)
- [ ] Set `APP_AUTH_DEV_MODE=false` (to enable OAuth2)
- [ ] Set `OAUTH_REDIRECT_URI` (will be set after getting backend URL)
- [ ] Set `PORT` (Render will provide this, but backend uses 8080 by default - may need adjustment)

#### 3.3 Update Backend Configuration
- [ ] Update `SecurityConfig.java` OAuth2 success/failure URLs to use Render frontend URL
  - [ ] Change `defaultSuccessUrl` from `http://localhost:5173/customers` to Render frontend URL
  - [ ] Change `failureUrl` from `http://localhost:5173/login?error=true` to Render frontend URL
- [ ] Update `CorsConfig.java` to allow Render frontend origin
  - [ ] Add Render frontend URL to allowed origins
  - [ ] Keep localhost for local development if needed
- [ ] Verify `application-prod.yml` uses environment variables correctly
- [ ] Ensure Flyway migrations will run on startup (already configured)

#### 3.4 Build Configuration
- [ ] Verify `pom.xml` has correct Java version (17)
- [ ] Ensure Dockerfile exists and is correct (if using Docker deployment)
- [ ] Test build command locally: `mvn clean package -DskipTests`
- [ ] Verify JAR file is created: `target/invoiceme-backend-1.0.0.jar`

---

### 4. Set Up Frontend Static Site

#### 4.1 Create Frontend Service via Render MCP
- [ ] Use Render MCP to create Static Site for frontend
- [ ] Configure service name: `invoiceme-frontend`
- [ ] Set root directory: `frontend`
- [ ] Set build command: `npm ci && npm run build`
- [ ] Set publish directory: `dist`
- [ ] Configure environment variables for build

#### 4.2 Frontend Environment Variables
- [ ] Set `VITE_API_URL` to Render backend URL (e.g., `https://invoiceme-backend.onrender.com/api`)
- [ ] Set `VITE_GOOGLE_CLIENT_ID` (same as backend GOOGLE_CLIENT_ID)
- [ ] Note: These are build-time variables (Vite requires VITE_ prefix)

#### 4.3 Update Frontend Configuration
- [ ] Verify `vite.config.ts` is configured correctly
- [ ] Check that API client uses `VITE_API_URL` environment variable
- [ ] Ensure frontend builds successfully: `npm run build`
- [ ] Verify `dist/` folder is created with all assets

---

## OAuth Configuration

### 5. Update Google OAuth Settings

#### 5.1 Google Cloud Console Configuration
- [ ] Log in to Google Cloud Console
- [ ] Navigate to APIs & Services > Credentials
- [ ] Edit the OAuth 2.0 Client ID used for InvoiceMe
- [ ] Add authorized redirect URI: `https://[backend-url].onrender.com/login/oauth2/code/google`
  - [ ] Replace `[backend-url]` with actual Render backend URL
- [ ] Add authorized JavaScript origins: `https://[frontend-url].onrender.com`
  - [ ] Replace `[frontend-url]` with actual Render frontend URL
- [ ] Save changes

#### 5.2 Update Backend OAuth Redirect URI
- [ ] After backend service is created, get the Render URL
- [ ] Update `OAUTH_REDIRECT_URI` environment variable in Render backend service
- [ ] Format: `https://[backend-url].onrender.com/login/oauth2/code/google`
- [ ] Verify `application-prod.yml` uses this variable correctly

---

## Deployment Execution

### 6. Deploy Backend Service

#### 6.1 Initial Deployment
- [ ] Trigger backend service deployment via Render MCP or dashboard
- [ ] Monitor build logs for errors
- [ ] Verify Maven build completes successfully
- [ ] Check that JAR file is found and executed
- [ ] Verify application starts without errors

#### 6.2 Database Migration Verification
- [ ] Check backend logs for Flyway migration execution
- [ ] Verify all migrations (V1, V2, V3, V4) run successfully
- [ ] Confirm database tables are created
- [ ] Test database connection via health endpoint (if available)

#### 6.3 Backend Health Check
- [ ] Access backend health endpoint: `https://[backend-url].onrender.com/actuator/health`
- [ ] Verify backend is responding
- [ ] Test API endpoint: `https://[backend-url].onrender.com/api/customers`
- [ ] Check CORS headers are correct

---

### 7. Deploy Frontend Service

#### 7.1 Initial Deployment
- [ ] Trigger frontend service deployment via Render MCP or dashboard
- [ ] Monitor build logs for errors
- [ ] Verify npm install completes successfully
- [ ] Check that `npm run build` completes
- [ ] Verify `dist/` folder is published correctly

#### 7.2 Frontend Verification
- [ ] Access frontend URL: `https://[frontend-url].onrender.com`
- [ ] Verify frontend loads without errors
- [ ] Check browser console for API connection errors
- [ ] Verify `VITE_API_URL` is correctly set in built assets
- [ ] Test that frontend can communicate with backend

---

## Integration Testing

### 8. End-to-End Testing

#### 8.1 Authentication Flow
- [ ] Navigate to frontend URL
- [ ] Click "Sign in with Google"
- [ ] Complete OAuth flow
- [ ] Verify redirect to dashboard after successful login
- [ ] Check that user info is displayed in header
- [ ] Test logout functionality

#### 8.2 Feature Testing
- [ ] Test Customer CRUD operations
  - [ ] Create customer
  - [ ] List customers
  - [ ] Update customer
  - [ ] Delete customer
- [ ] Test Invoice CRUD operations
  - [ ] Create invoice with line items
  - [ ] List invoices
  - [ ] Update invoice (if draft)
  - [ ] Mark invoice as sent
- [ ] Test Payment operations
  - [ ] Record payment
  - [ ] Verify invoice balance updates
  - [ ] Check payment history

#### 8.3 Error Handling
- [ ] Test 401 errors (unauthorized access)
- [ ] Test 404 errors (not found)
- [ ] Test validation errors (400)
- [ ] Verify error messages display correctly

---

## Post-Deployment Configuration

### 9. Final Configuration Updates

#### 9.1 Update Documentation
- [ ] Update `README.md` with Render deployment instructions
- [ ] Document Render service URLs
- [ ] Update environment variable documentation
- [ ] Remove AWS-specific deployment documentation (or mark as deprecated)

#### 9.2 Clean Up AWS Configuration (Optional)
- [ ] Archive AWS deployment scripts (if not needed)
- [ ] Document that deployment is now on Render
- [ ] Update any CI/CD workflows if applicable

#### 9.3 Monitoring Setup
- [ ] Configure Render health checks
- [ ] Set up monitoring alerts (if needed)
- [ ] Document how to view logs in Render dashboard

---

## Verification Checklist

### 10. Final Verification

- [ ] Backend service is running and accessible
- [ ] Frontend service is running and accessible
- [ ] Database is connected and migrations ran successfully
- [ ] OAuth authentication works end-to-end
- [ ] All CRUD operations work (Customers, Invoices, Payments)
- [ ] CORS is configured correctly
- [ ] Environment variables are set correctly
- [ ] No gcloud dependencies remain
- [ ] Application performs as expected (< 200ms API response times)
- [ ] All features work as in local development

---

## Rollback Plan

### 11. Rollback Procedures

- [ ] Document how to rollback to previous deployment version in Render
- [ ] Keep local development environment working
- [ ] Document how to switch back to AWS if needed (optional)

---

## Notes

- **Render MCP**: Use Render MCP tools to create and configure services programmatically
- **Environment Variables**: All sensitive values should use Render's secret management
- **Database**: Render PostgreSQL automatically handles backups and maintenance
- **Build Time**: First deployment may take 5-10 minutes for build and startup
- **Custom Domains**: Can be configured later if needed
- **Auto-Deploy**: Render can auto-deploy on git push if repository is connected

---

## Success Criteria

✅ Backend deployed and accessible on Render  
✅ Frontend deployed and accessible on Render  
✅ Database provisioned and migrations successful  
✅ OAuth authentication working  
✅ All features functional  
✅ No gcloud dependencies  
✅ Performance requirements met (< 200ms API response times)

