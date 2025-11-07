# PRD 08: Authentication & Integration

## Overview
**Priority**: 🟡 Can run in parallel with PRDs 02-07, but needed for final integration  
**Dependencies**: PRD 01 (Foundation)  
**Enables**: Complete application integration, E2E testing

This PRD implements Google OAuth2 authentication and completes end-to-end integration testing across all features.

## Objectives
1. Implement Google OAuth2 authentication flow
2. Set up session-based authentication
3. Protect API endpoints with authentication
4. Implement frontend authentication UI and protected routes
5. Complete end-to-end integration testing
6. Performance verification (< 200ms API requirement)

## Backend Tasks

### OAuth2 Configuration
- [ ] Configure Google OAuth2 client in `application.yml`
  - Client ID and secret from environment variables
  - Redirect URI configuration
  - Scope: `openid`, `profile`, `email`
- [ ] Update `SecurityConfig` to enable OAuth2 login
- [ ] Configure OAuth2 login endpoint: `/oauth2/authorization/google`
- [ ] Configure OAuth2 callback: `/login/oauth2/code/google`
- [ ] Set up session management (httpOnly cookies)
- [ ] Configure CORS to allow frontend origin

### Security Configuration
- [ ] Protect all `/api/*` endpoints (require authentication)
- [ ] Allow public access to:
  - `/api/auth/**` (auth endpoints)
  - `/oauth2/**` (OAuth2 endpoints)
  - `/swagger-ui.html` (optional, for development)
  - `/v3/api-docs` (optional, for development)
- [ ] Configure session timeout
- [ ] Set secure cookie flags (for production)

### Auth Controller
- [ ] Create `AuthController` in `presentation/rest/`
- [ ] Endpoint: `GET /api/auth/user`
  - Returns current authenticated user info
  - Uses `@AuthenticationPrincipal OAuth2User`
  - Response: `{ id, name, email, picture }`
- [ ] Endpoint: `POST /api/auth/logout` (optional)
  - Invalidates session

### User Entity (Optional)
- [ ] Create `User` entity if user persistence is needed
- [ ] Store OAuth2 user info (id, email, name)
- [ ] Link to domain entities if user ownership is required
- [ ] **Note**: For this project, session-based auth may not require user persistence

## Frontend Tasks

### Authentication UI
- [ ] Create `LoginPage` component at route `/login`
- [ ] "Sign in with Google" button
- [ ] Redirects to: `http://localhost:8080/oauth2/authorization/google`
- [ ] Handles OAuth callback redirect back to frontend
- [ ] Shows loading state during OAuth flow

### Auth Hook
- [ ] Update `useAuth` hook (from PRD 01)
- [ ] Query: `GET /api/auth/user`
- [ ] Returns: `{ user, isAuthenticated, isLoading }`
- [ ] Handles 401 errors (redirects to login)
- [ ] Caches user info in React Query

### Protected Routes
- [ ] Create `ProtectedRoute` component
- [ ] Wraps routes that require authentication
- [ ] Checks `isAuthenticated` from `useAuth`
- [ ] Redirects to `/login` if not authenticated
- [ ] Shows loading state while checking auth

### Route Protection
- [ ] Protect all feature routes:
  - `/customers` → requires auth
  - `/invoices` → requires auth
  - `/payments` → requires auth
- [ ] Allow public access to `/login`
- [ ] Redirect authenticated users away from `/login` to `/dashboard` or `/customers`

### User Display
- [ ] Update `Header` component to show user info
- [ ] Display user name/email
- [ ] Logout button (optional)
- [ ] User avatar/picture (if available from OAuth)

## Integration Testing

### End-to-End Test Scenarios
- [ ] **Complete Customer Flow**:
  - Authenticate → Create Customer → List Customers → Update Customer → Delete Customer
- [ ] **Complete Invoice Flow**:
  - Authenticate → Create Invoice → Add Line Items → Mark as Sent → View Invoice
- [ ] **Complete Payment Flow**:
  - Authenticate → Create Customer → Create Invoice → Record Payment → Verify Balance → Verify Invoice Status (PAID)
- [ ] **Invoice Lifecycle**:
  - Create Draft → Edit Draft → Mark as Sent → Record Payment → Verify Paid
- [ ] **Payment Validation**:
  - Attempt to record payment exceeding balance → Verify error
- [ ] **Authentication Flow**:
  - Access protected route → Redirect to login → OAuth flow → Redirect back → Access granted

### Performance Testing
- [ ] Verify all API endpoints respond in < 200ms
- [ ] Test with realistic data volumes
- [ ] Measure response times for:
  - Customer CRUD operations
  - Invoice CRUD operations
  - Payment recording
  - List queries with pagination
- [ ] Document performance results

### Integration Test Suite
- [ ] Create comprehensive integration test class
- [ ] Use `@SpringBootTest` with Testcontainers
- [ ] Test complete user flows end-to-end
- [ ] Mock OAuth2 authentication for tests (or use test security config)

## Error Handling

### Authentication Errors
- [ ] Handle OAuth2 errors (user cancellation, etc.)
- [ ] Display user-friendly error messages
- [ ] Redirect to login on 401 errors
- [ ] Handle session expiration

## Success Criteria
- [ ] Google OAuth2 authentication works end-to-end
- [ ] All API endpoints are protected (except auth endpoints)
- [ ] Frontend protected routes redirect to login
- [ ] Session management works correctly
- [ ] User info is displayed in UI
- [ ] All E2E test scenarios pass
- [ ] All API endpoints respond in < 200ms
- [ ] Error handling is user-friendly

## Dependencies
- **Requires**: PRD 01 (Foundation)
- **Can run parallel with**: PRDs 02-07 (auth can be implemented independently)
- **Integrates with**: All feature PRDs (adds authentication layer)
- **Final step**: Complete integration testing requires all features (PRDs 02-07)

## Timeline Estimate
**1 day** (OAuth setup + integration testing)

## Notes
- OAuth2 setup requires Google Cloud Console configuration
- Session-based auth is simpler than JWT for this project
- Integration testing is critical for demonstrating complete functionality
- Performance verification is a key requirement
- Can be implemented in parallel with features, but final integration requires all features complete

## Google OAuth Setup Requirements
1. Create Google Cloud Project
2. Enable Google+ API
3. Create OAuth 2.0 credentials
4. Configure authorized redirect URIs:
   - `http://localhost:8080/login/oauth2/code/google`
5. Set environment variables:
   - `GOOGLE_CLIENT_ID`
   - `GOOGLE_CLIENT_SECRET`

