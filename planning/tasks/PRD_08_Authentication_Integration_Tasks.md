# Task List: PRD 08 - Authentication & Integration

## Overview
**Priority**: 🟡 Can run in parallel with PRDs 02-07, but needed for final integration  
**Estimated Time**: 1 day  
**Dependencies**: PRD 01 (Foundation)  
**Enables**: Complete application integration, E2E testing

This task list breaks down PRD 08 into actionable, checkable tasks for implementing Google OAuth2 authentication and completing end-to-end integration testing across all features.

---

## Backend Tasks

### 1. Google OAuth2 Configuration

#### 1.1 Environment Setup
- [ ] Create Google Cloud Project (if not already created)
  - [ ] Go to Google Cloud Console
  - [ ] Create new project or select existing project
  - [ ] Note project ID for reference

- [ ] Enable Google+ API (or Google Identity API)
  - [ ] Navigate to APIs & Services > Library
  - [ ] Search for "Google+ API" or "Google Identity API"
  - [ ] Enable the API

- [ ] Create OAuth 2.0 Credentials
  - [ ] Navigate to APIs & Services > Credentials
  - [ ] Click "Create Credentials" > "OAuth client ID"
  - [ ] Select "Web application" as application type
  - [ ] Set name: "InvoiceMe Development"
  - [ ] Add authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`
  - [ ] Save and note Client ID and Client Secret

- [ ] Set environment variables
  - [ ] Export `GOOGLE_CLIENT_ID` environment variable
  - [ ] Export `GOOGLE_CLIENT_SECRET` environment variable
  - [ ] Verify variables are accessible to Spring Boot application
  - [ ] **Note**: For production, use secure secret management

#### 1.2 Application Configuration
- [ ] Configure OAuth2 client in `application.yml`
  - [ ] Open `src/main/resources/application.yml`
  - [ ] Add `spring.security.oauth2.client.registration.google` section
  - [ ] Set `client-id: ${GOOGLE_CLIENT_ID}`
  - [ ] Set `client-secret: ${GOOGLE_CLIENT_SECRET}`
  - [ ] Set `scope: openid,profile,email`
  - [ ] Set `redirect-uri: http://localhost:8080/login/oauth2/code/google`
  - [ ] Set `authorization-grant-type: authorization_code`
  - [ ] Set `client-name: Google`

- [ ] Configure OAuth2 provider in `application.yml`
  - [ ] Add `spring.security.oauth2.client.provider.google` section
  - [ ] Set `issuer-uri: https://accounts.google.com`
  - [ ] Verify configuration matches Google OAuth2 provider settings

- [ ] Verify OAuth2 configuration
  - [ ] Start application
  - [ ] Check logs for OAuth2 configuration errors
  - [ ] Verify no configuration exceptions on startup

### 2. Security Configuration

#### 2.1 Update SecurityConfig
- [ ] Open `SecurityConfig.java` (in `infrastructure/security/` package)
  - [ ] Locate existing security configuration
  - [ ] Review current security setup from PRD 01

- [ ] Enable OAuth2 login
  - [ ] Add `.oauth2Login()` to security filter chain
  - [ ] Configure OAuth2 login endpoint: `.loginPage("/oauth2/authorization/google")`
  - [ ] Configure default success URL (optional, for redirect after login)
  - [ ] Configure failure URL (optional, for error handling)

- [ ] Configure OAuth2 callback
  - [ ] Verify callback URL: `/login/oauth2/code/google`
  - [ ] Ensure Spring Security handles callback automatically
  - [ ] Configure success handler if custom redirect needed

- [ ] Set up session management
  - [ ] Configure `.sessionManagement()` in security chain
  - [ ] Set session creation policy: `SessionCreationPolicy.IF_REQUIRED`
  - [ ] Configure session timeout (e.g., 30 minutes)
  - [ ] Enable httpOnly cookies (default in Spring Security)
  - [ ] Configure secure cookie flags (for production: `secure: true`)

- [ ] Configure CORS for authentication
  - [ ] Update `CorsConfig` or add CORS configuration to `SecurityConfig`
  - [ ] Ensure CORS allows credentials: `.allowCredentials(true)`
  - [ ] Set allowed origins: `http://localhost:5173` (frontend)
  - [ ] Set allowed methods: GET, POST, PUT, DELETE, OPTIONS
  - [ ] Set allowed headers: Authorization, Content-Type, etc.
  - [ ] Verify CORS works with OAuth2 flow

#### 2.2 Endpoint Protection
- [ ] Protect all `/api/*` endpoints
  - [ ] Add `.requestMatchers("/api/**").authenticated()` to security chain
  - [ ] Verify all API endpoints require authentication

- [ ] Allow public access to auth endpoints
  - [ ] Add `.requestMatchers("/api/auth/**").permitAll()` to security chain
  - [ ] Verify auth endpoints are accessible without authentication

- [ ] Allow public access to OAuth2 endpoints
  - [ ] Add `.requestMatchers("/oauth2/**").permitAll()` to security chain
  - [ ] Add `.requestMatchers("/login/**").permitAll()` to security chain
  - [ ] Verify OAuth2 flow works end-to-end

- [ ] Configure Swagger/OpenAPI access (optional, for development)
  - [ ] Add `.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()` (optional)
  - [ ] Or require authentication for Swagger (more secure)
  - [ ] Document decision in code comments

- [ ] Verify security configuration
  - [ ] Test unauthenticated access to `/api/customers` → should redirect to OAuth2 login
  - [ ] Test authenticated access to `/api/customers` → should work
  - [ ] Test access to `/api/auth/user` → should be public (or authenticated, depending on design)

### 3. Auth Controller

#### 3.1 Create AuthController
- [ ] Create `AuthController.java` in `presentation/rest/` package
  - [ ] Add package declaration: `package com.invoiceme.presentation.rest;`
  - [ ] Add `@RestController` annotation
  - [ ] Add `@RequestMapping("/api/auth")` annotation
  - [ ] Add `@Tag(name = "Authentication")` for OpenAPI documentation

- [ ] Add OpenAPI documentation
  - [ ] Add `@Operation` annotations to endpoints
  - [ ] Add `@ApiResponse` annotations for success/error cases
  - [ ] Document response schemas

#### 3.2 Implement User Info Endpoint
- [ ] Create `GET /api/auth/user` endpoint
  - [ ] Add method: `@GetMapping("/user")`
  - [ ] Add parameter: `@AuthenticationPrincipal OAuth2User oauth2User`
  - [ ] Extract user info from `OAuth2User`:
    - [ ] Get `id` from `oauth2User.getAttribute("sub")` or `oauth2User.getName()`
    - [ ] Get `name` from `oauth2User.getAttribute("name")`
    - [ ] Get `email` from `oauth2User.getAttribute("email")`
    - [ ] Get `picture` from `oauth2User.getAttribute("picture")` (optional)
  - [ ] Create response DTO or return Map with user info
  - [ ] Return `{ id, name, email, picture }` structure
  - [ ] Handle case where `oauth2User` is null (return 401)

- [ ] Test user info endpoint
  - [ ] Authenticate via OAuth2
  - [ ] Call `GET /api/auth/user`
  - [ ] Verify response contains user information
  - [ ] Test unauthenticated access → should return 401

#### 3.3 Implement Logout Endpoint (Optional)
- [ ] Create `POST /api/auth/logout` endpoint
  - [ ] Add method: `@PostMapping("/logout")`
  - [ ] Invalidate session using `HttpServletRequest.logout()`
  - [ ] Return success response
  - [ ] Or use Spring Security's built-in logout endpoint

- [ ] Test logout endpoint
  - [ ] Authenticate via OAuth2
  - [ ] Call `POST /api/auth/logout`
  - [ ] Verify session is invalidated
  - [ ] Verify subsequent API calls return 401

### 4. User Entity (Optional - Skip if Session-Based Auth Only)

#### 4.1 Decide on User Persistence
- [ ] Review requirements: Is user persistence needed?
  - [ ] **Option A**: Session-based auth only (no persistence)
    - [ ] Skip user entity creation
    - [ ] Use OAuth2User directly from session
    - [ ] No database storage needed
  - [ ] **Option B**: User persistence required
    - [ ] Continue with user entity creation
    - [ ] Store user info in database
    - [ ] Link to domain entities if user ownership needed

#### 4.2 Create User Entity (If Needed)
- [ ] Create `User` entity class
  - [ ] Create `src/main/java/com/invoiceme/domain/user/User.java`
  - [ ] Add `@Entity` and `@Table(name = "users")` annotations
  - [ ] Add fields: `id` (UUID), `oauth2Id` (String, unique), `email` (String, unique), `name` (String), `picture` (String, nullable)
  - [ ] Add timestamps: `createdAt`, `updatedAt`
  - [ ] Implement timestamp handling (`@PrePersist`, `@PreUpdate`)

- [ ] Create UserRepository
  - [ ] Create `UserRepository.java` in `infrastructure/persistence/`
  - [ ] Extend `JpaRepository<User, UUID>`
  - [ ] Add `findByOauth2Id(String oauth2Id)` method
  - [ ] Add `findByEmail(String email)` method

- [ ] Create UserService (If Needed)
  - [ ] Create `UserService` to handle user creation/retrieval
  - [ ] Implement `findOrCreateUser(OAuth2User oauth2User)` method
  - [ ] Update `AuthController` to use `UserService`

- [ ] Create Flyway migration (If Needed)
  - [ ] Create `V5__create_users_table.sql`
  - [ ] Define users table schema
  - [ ] Add indexes on `oauth2_id` and `email`

- [ ] **Note**: For this project, session-based auth may not require user persistence. Skip this section if not needed.

---

## Frontend Tasks

### 5. Authentication UI

#### 5.1 Create LoginPage Component
- [ ] Create `LoginPage.tsx` component
  - [ ] Create `src/pages/LoginPage.tsx` or `src/features/auth/pages/LoginPage.tsx`
  - [ ] Add React component structure
  - [ ] Import necessary dependencies (React, React Router, shadcn/ui components)

- [ ] Implement login UI
  - [ ] Add centered container with max-width
  - [ ] Add heading: "Sign in to InvoiceMe"
  - [ ] Add "Sign in with Google" button
    - [ ] Use shadcn/ui Button component
    - [ ] Style appropriately (primary button style)
    - [ ] Add Google icon (SVG, not emoji)
    - [ ] Make button full-width on mobile, centered on desktop

- [ ] Implement OAuth redirect
  - [ ] Add `onClick` handler to button
  - [ ] Redirect to: `http://localhost:8080/oauth2/authorization/google`
  - [ ] Use `window.location.href` for full page redirect
  - [ ] Or use `window.open()` for popup (if preferred)

- [ ] Add loading state
  - [ ] Show loading spinner while redirecting
  - [ ] Disable button during redirect
  - [ ] Display "Redirecting to Google..." message

- [ ] Handle OAuth callback
  - [ ] After OAuth flow, backend redirects back to frontend
  - [ ] Check for OAuth success/error in URL params or session
  - [ ] Redirect to `/customers` or `/dashboard` on success
  - [ ] Display error message on failure
  - [ ] Clear URL params after handling

- [ ] Style login page
  - [ ] Use Tailwind CSS for styling
  - [ ] Center content vertically and horizontally
  - [ ] Add appropriate spacing and padding
  - [ ] Make responsive for mobile devices
  - [ ] Ensure accessibility (ARIA labels, keyboard navigation)

#### 5.2 Add Login Route
- [ ] Update routing configuration
  - [ ] Open `src/routes/index.tsx`
  - [ ] Add route: `<Route path="/login" element={<LoginPage />} />`
  - [ ] Ensure route is accessible without authentication

- [ ] Test login page
  - [ ] Navigate to `/login`
  - [ ] Verify page loads correctly
  - [ ] Click "Sign in with Google" button
  - [ ] Verify redirect to OAuth2 endpoint
  - [ ] Complete OAuth flow
  - [ ] Verify redirect back to frontend

### 6. Auth Hook

#### 6.1 Update useAuth Hook
- [ ] Open `src/lib/hooks/useAuth.ts` (created in PRD 01)
  - [ ] Review existing placeholder implementation
  - [ ] Replace with real authentication logic

- [ ] Implement user query
  - [ ] Use React Query to fetch user info
  - [ ] Create query: `GET /api/auth/user`
  - [ ] Use `useQuery` hook from React Query
  - [ ] Set query key: `['auth', 'user']`
  - [ ] Configure query options:
    - [ ] Set `retry: false` (don't retry on 401)
    - [ ] Set `refetchOnWindowFocus: false` (optional)
    - [ ] Set `staleTime: 5 minutes` (cache user info)

- [ ] Handle authentication state
  - [ ] Extract `user` from query data
  - [ ] Set `isAuthenticated: boolean` based on user presence
  - [ ] Set `isLoading: boolean` from query `isLoading` state
  - [ ] Return: `{ user, isAuthenticated, isLoading }`

- [ ] Handle 401 errors
  - [ ] Check for 401 status in query error
  - [ ] Redirect to `/login` on 401
  - [ ] Use `useNavigate` from React Router
  - [ ] Clear any cached user data

- [ ] Cache user info
  - [ ] Use React Query caching for user info
  - [ ] Invalidate cache on logout (if logout implemented)
  - [ ] Refetch user info when needed

- [ ] Test useAuth hook
  - [ ] Use hook in a component
  - [ ] Verify `isLoading` is true initially
  - [ ] Verify `isAuthenticated` is false when not logged in
  - [ ] Verify `isAuthenticated` is true after OAuth login
  - [ ] Verify `user` contains user information

### 7. Protected Routes

#### 7.1 Create ProtectedRoute Component
- [ ] Create `ProtectedRoute.tsx` component
  - [ ] Create `src/components/common/ProtectedRoute.tsx`
  - [ ] Add React component structure
  - [ ] Import `useAuth` hook
  - [ ] Import `Navigate` from React Router

- [ ] Implement route protection logic
  - [ ] Accept `children` prop (the route component to protect)
  - [ ] Use `useAuth()` hook to get `{ isAuthenticated, isLoading }`
  - [ ] If `isLoading` is true:
    - [ ] Return loading spinner (use `LoadingSpinner` component)
    - [ ] Prevent flash of content
  - [ ] If `isAuthenticated` is false:
    - [ ] Redirect to `/login` using `<Navigate to="/login" replace />`
    - [ ] Optionally save intended destination for redirect after login
  - [ ] If `isAuthenticated` is true:
    - [ ] Return `{children}` (render protected route)

- [ ] Handle redirect after login
  - [ ] Save current location before redirecting to login
  - [ ] Use `location.state` or sessionStorage to store intended destination
  - [ ] After successful login, redirect to saved location or default to `/customers`

- [ ] Test ProtectedRoute component
  - [ ] Wrap a route with `<ProtectedRoute>`
  - [ ] Test unauthenticated access → should redirect to `/login`
  - [ ] Test authenticated access → should render route
  - [ ] Test loading state → should show spinner

#### 7.2 Protect Feature Routes
- [ ] Update routing configuration
  - [ ] Open `src/routes/index.tsx`
  - [ ] Wrap customer route with `ProtectedRoute`:
    - [ ] Change `<Route path="/customers" element={<CustomersPage />} />`
    - [ ] To `<Route path="/customers" element={<ProtectedRoute><CustomersPage /></ProtectedRoute>} />`
  - [ ] Wrap invoice route with `ProtectedRoute`:
    - [ ] Change `<Route path="/invoices" element={<InvoicesPage />} />`
    - [ ] To `<Route path="/invoices" element={<ProtectedRoute><InvoicesPage /></ProtectedRoute>} />`
  - [ ] Wrap payment route with `ProtectedRoute`:
    - [ ] Change `<Route path="/payments" element={<PaymentsPage />} />`
    - [ ] To `<Route path="/payments" element={<ProtectedRoute><PaymentsPage /></ProtectedRoute>} />`

- [ ] Keep login route public
  - [ ] Ensure `<Route path="/login" element={<LoginPage />} />` is NOT wrapped in `ProtectedRoute`
  - [ ] Verify login page is accessible without authentication

- [ ] Redirect authenticated users away from login
  - [ ] Update `LoginPage` component
  - [ ] Use `useAuth()` hook
  - [ ] If `isAuthenticated` is true:
    - [ ] Redirect to `/customers` or `/dashboard` using `<Navigate>`
    - [ ] Prevent authenticated users from seeing login page

- [ ] Test route protection
  - [ ] Test unauthenticated access to `/customers` → should redirect to `/login`
  - [ ] Test unauthenticated access to `/invoices` → should redirect to `/login`
  - [ ] Test unauthenticated access to `/payments` → should redirect to `/login`
  - [ ] Test authenticated access → should work normally
  - [ ] Test authenticated access to `/login` → should redirect to `/customers`

### 8. User Display

#### 8.1 Update Header Component
- [ ] Open `Layout.tsx` or `Header.tsx` component
  - [ ] Locate header/navigation component
  - [ ] Review existing header structure

- [ ] Add user info display
  - [ ] Use `useAuth()` hook to get user info
  - [ ] Display user name or email
    - [ ] Show user name if available, fallback to email
    - [ ] Style appropriately (small text, right-aligned)
  - [ ] Display user avatar/picture (if available)
    - [ ] Use `<img>` tag with user picture URL
    - [ ] Add fallback if picture not available (initials or icon)
    - [ ] Style as circular avatar
    - [ ] Add appropriate sizing (e.g., 32x32px or 40x40px)

- [ ] Add logout button (optional)
  - [ ] Add "Logout" button or link
  - [ ] Implement logout handler:
    - [ ] Call `POST /api/auth/logout` (if implemented)
    - [ ] Or clear session by redirecting to backend logout endpoint
    - [ ] Clear React Query cache
    - [ ] Redirect to `/login`
  - [ ] Style logout button appropriately

- [ ] Style user display
  - [ ] Position user info in header (right side)
  - [ ] Add appropriate spacing and padding
  - [ ] Make responsive for mobile (stack or hide on small screens)
  - [ ] Ensure accessibility (ARIA labels, keyboard navigation)

- [ ] Test user display
  - [ ] Verify user info appears after login
  - [ ] Verify avatar displays correctly (if available)
  - [ ] Test logout functionality (if implemented)
  - [ ] Test responsive behavior on mobile

---

## Integration Testing

### 9. End-to-End Test Scenarios

#### 9.1 Complete Customer Flow Test
- [ ] Create E2E test: `e2e/customer-flow.spec.ts`
  - [ ] Use Playwright (already configured in frontend)
  - [ ] Or create backend integration test with mocked OAuth2

- [ ] Implement test steps:
  - [ ] Authenticate user (mock OAuth2 or use test security config)
  - [ ] Create customer via API: `POST /api/customers`
  - [ ] Verify customer created successfully
  - [ ] List customers: `GET /api/customers`
  - [ ] Verify created customer appears in list
  - [ ] Update customer: `PUT /api/customers/{id}`
  - [ ] Verify customer updated successfully
  - [ ] Delete customer: `DELETE /api/customers/{id}`
  - [ ] Verify customer deleted successfully

- [ ] Run test and verify all steps pass

#### 9.2 Complete Invoice Flow Test
- [ ] Create E2E test: `e2e/invoice-flow.spec.ts`
  - [ ] Use Playwright or backend integration test

- [ ] Implement test steps:
  - [ ] Authenticate user
  - [ ] Create customer (prerequisite)
  - [ ] Create invoice: `POST /api/invoices`
  - [ ] Verify invoice created with DRAFT status
  - [ ] Add line items (via update or during creation)
  - [ ] Verify line items added correctly
  - [ ] Mark invoice as sent: `POST /api/invoices/{id}/send`
  - [ ] Verify invoice status changed to SENT
  - [ ] View invoice: `GET /api/invoices/{id}`
  - [ ] Verify invoice details correct

- [ ] Run test and verify all steps pass

#### 9.3 Complete Payment Flow Test
- [ ] Create E2E test: `e2e/payment-flow.spec.ts`
  - [ ] Use Playwright or backend integration test

- [ ] Implement test steps:
  - [ ] Authenticate user
  - [ ] Create customer: `POST /api/customers`
  - [ ] Create invoice: `POST /api/invoices` (with line items, total > 0)
  - [ ] Mark invoice as sent: `POST /api/invoices/{id}/send`
  - [ ] Record payment: `POST /api/payments` (amount < invoice total)
  - [ ] Verify payment recorded successfully
  - [ ] Verify invoice balance updated: `GET /api/invoices/{id}`
  - [ ] Verify invoice status still SENT (not PAID yet)
  - [ ] Record second payment to cover remaining balance
  - [ ] Verify invoice balance is 0
  - [ ] Verify invoice status changed to PAID

- [ ] Run test and verify all steps pass

#### 9.4 Invoice Lifecycle Test
- [ ] Create E2E test: `e2e/invoice-lifecycle.spec.ts`

- [ ] Implement test steps:
  - [ ] Authenticate user
  - [ ] Create customer
  - [ ] Create draft invoice: `POST /api/invoices`
  - [ ] Verify invoice is DRAFT
  - [ ] Edit draft invoice: `PUT /api/invoices/{id}` (update line items)
  - [ ] Verify draft can be edited
  - [ ] Mark invoice as sent: `POST /api/invoices/{id}/send`
  - [ ] Verify invoice is SENT
  - [ ] Attempt to edit sent invoice: `PUT /api/invoices/{id}`
  - [ ] Verify edit fails (business rule: cannot edit SENT invoices)
  - [ ] Record payment: `POST /api/payments`
  - [ ] Verify payment applied
  - [ ] Record payment to cover full balance
  - [ ] Verify invoice status is PAID

- [ ] Run test and verify all steps pass

#### 9.5 Payment Validation Test
- [ ] Create E2E test: `e2e/payment-validation.spec.ts`

- [ ] Implement test steps:
  - [ ] Authenticate user
  - [ ] Create customer
  - [ ] Create invoice with total: $100
  - [ ] Mark invoice as sent
  - [ ] Attempt to record payment exceeding balance: `POST /api/payments` (amount: $150)
  - [ ] Verify error response (422 or 400)
  - [ ] Verify error message indicates amount exceeds balance
  - [ ] Verify invoice balance unchanged
  - [ ] Record valid payment: `POST /api/payments` (amount: $50)
  - [ ] Verify payment succeeds
  - [ ] Attempt to record payment exceeding remaining balance: `POST /api/payments` (amount: $60, remaining: $50)
  - [ ] Verify error response

- [ ] Run test and verify all steps pass

#### 9.6 Authentication Flow Test
- [ ] Create E2E test: `e2e/authentication-flow.spec.ts`
  - [ ] Use Playwright for frontend E2E test

- [ ] Implement test steps:
  - [ ] Navigate to protected route: `/customers`
  - [ ] Verify redirect to `/login`
  - [ ] Click "Sign in with Google" button
  - [ ] Complete OAuth flow (may need to mock or use test credentials)
  - [ ] Verify redirect back to frontend
  - [ ] Verify redirect to intended destination (`/customers`)
  - [ ] Verify user info displayed in header
  - [ ] Verify protected routes are accessible
  - [ ] Test logout (if implemented)
  - [ ] Verify redirect to `/login` after logout

- [ ] Run test and verify all steps pass

### 10. Performance Testing

#### 10.1 API Performance Verification
- [ ] Create performance test script or use existing test scripts
  - [ ] Review `test-endpoints.sh` or create new performance test
  - [ ] Ensure tests run with authentication enabled

- [ ] Test Customer CRUD operations
  - [ ] Measure `POST /api/customers` response time
  - [ ] Measure `GET /api/customers/{id}` response time
  - [ ] Measure `GET /api/customers` (list) response time
  - [ ] Measure `PUT /api/customers/{id}` response time
  - [ ] Measure `DELETE /api/customers/{id}` response time
  - [ ] Verify all endpoints < 200ms

- [ ] Test Invoice CRUD operations
  - [ ] Measure `POST /api/invoices` response time
  - [ ] Measure `GET /api/invoices/{id}` response time
  - [ ] Measure `GET /api/invoices` (list) response time
  - [ ] Measure `PUT /api/invoices/{id}` response time
  - [ ] Measure `POST /api/invoices/{id}/send` response time
  - [ ] Verify all endpoints < 200ms

- [ ] Test Payment operations
  - [ ] Measure `POST /api/payments` response time
  - [ ] Measure `GET /api/payments/{id}` response time
  - [ ] Measure `GET /api/payments` (list) response time
  - [ ] Verify all endpoints < 200ms

- [ ] Test with realistic data volumes
  - [ ] Create 100+ customers
  - [ ] Test pagination performance: `GET /api/customers?page=0&size=20`
  - [ ] Create 100+ invoices
  - [ ] Test filtering performance: `GET /api/invoices?status=SENT`
  - [ ] Verify performance remains < 200ms with larger datasets

- [ ] Document performance results
  - [ ] Record response times for each endpoint
  - [ ] Note any endpoints that exceed 200ms
  - [ ] Document optimization steps if needed
  - [ ] Create `PERFORMANCE_RESULTS.md` or update existing documentation

### 11. Integration Test Suite

#### 11.1 Create Comprehensive Integration Test
- [ ] Create `AuthenticationIntegrationTest.java`
  - [ ] Create `src/test/java/com/invoiceme/AuthenticationIntegrationTest.java`
  - [ ] Extend base test class (with Testcontainers setup)
  - [ ] Use `@SpringBootTest` annotation

- [ ] Configure test security
  - [ ] Option A: Mock OAuth2 authentication
    - [ ] Use `@WithMockUser` or `@WithOAuth2User` annotations
    - [ ] Configure test security context
  - [ ] Option B: Use test security configuration
    - [ ] Create `TestSecurityConfig` class
    - [ ] Disable OAuth2 for tests, use simple authentication
  - [ ] Choose approach based on testing needs

- [ ] Test complete user flows
  - [ ] Test authenticated customer CRUD flow
  - [ ] Test authenticated invoice CRUD flow
  - [ ] Test authenticated payment flow
  - [ ] Test unauthenticated access (should fail)
  - [ ] Test authentication endpoint accessibility

- [ ] Run integration test suite
  - [ ] Execute all integration tests
  - [ ] Verify all tests pass
  - [ ] Document any test failures
  - [ ] Fix any issues found

---

## Error Handling

### 12. Authentication Error Handling

#### 12.1 OAuth2 Error Handling
- [ ] Handle OAuth2 errors in frontend
  - [ ] Check for error parameters in OAuth callback URL
  - [ ] Display user-friendly error messages:
    - [ ] "Authentication cancelled" if user cancels
    - [ ] "Authentication failed" for other errors
    - [ ] "Please try again" with retry option
  - [ ] Log errors for debugging (console or error tracking)

- [ ] Handle OAuth2 errors in backend
  - [ ] Configure OAuth2 failure handler in `SecurityConfig`
  - [ ] Redirect to frontend with error information
  - [ ] Return appropriate error responses

#### 12.2 Session Expiration Handling
- [ ] Handle expired sessions in frontend
  - [ ] Detect 401 errors from API calls
  - [ ] Clear user data from React Query cache
  - [ ] Redirect to `/login` with message: "Session expired. Please sign in again."
  - [ ] Optionally show toast notification

- [ ] Handle expired sessions in backend
  - [ ] Configure session timeout in `SecurityConfig`
  - [ ] Return 401 status for expired sessions
  - [ ] Clear session data appropriately

#### 12.3 401 Error Handling
- [ ] Update Axios interceptor (from PRD 01)
  - [ ] Open `src/lib/api/client.ts` or similar
  - [ ] Review existing 401 interceptor
  - [ ] Ensure redirect to `/login` on 401
  - [ ] Clear React Query cache on 401
  - [ ] Prevent infinite redirect loops

- [ ] Test error handling
  - [ ] Test expired session → should redirect to login
  - [ ] Test invalid token → should redirect to login
  - [ ] Test OAuth cancellation → should show error message
  - [ ] Verify error messages are user-friendly

---

## Final Verification

### 13. Success Criteria Verification

#### 13.1 Authentication Verification
- [ ] Google OAuth2 authentication works end-to-end
  - [ ] User can click "Sign in with Google"
  - [ ] OAuth flow completes successfully
  - [ ] User is redirected back to frontend
  - [ ] User info is available in frontend

- [ ] All API endpoints are protected
  - [ ] Unauthenticated access to `/api/customers` → redirects to OAuth2 login
  - [ ] Unauthenticated access to `/api/invoices` → redirects to OAuth2 login
  - [ ] Unauthenticated access to `/api/payments` → redirects to OAuth2 login
  - [ ] Auth endpoints (`/api/auth/**`) are accessible

- [ ] Frontend protected routes redirect to login
  - [ ] Unauthenticated access to `/customers` → redirects to `/login`
  - [ ] Unauthenticated access to `/invoices` → redirects to `/login`
  - [ ] Unauthenticated access to `/payments` → redirects to `/login`

- [ ] Session management works correctly
  - [ ] Session persists across page refreshes
  - [ ] Session expires after timeout
  - [ ] Logout clears session

- [ ] User info is displayed in UI
  - [ ] User name/email appears in header
  - [ ] User avatar appears (if available)
  - [ ] User info updates correctly

#### 13.2 Integration Testing Verification
- [ ] All E2E test scenarios pass
  - [ ] Complete Customer Flow test passes
  - [ ] Complete Invoice Flow test passes
  - [ ] Complete Payment Flow test passes
  - [ ] Invoice Lifecycle test passes
  - [ ] Payment Validation test passes
  - [ ] Authentication Flow test passes

- [ ] All API endpoints respond in < 200ms
  - [ ] Customer endpoints < 200ms
  - [ ] Invoice endpoints < 200ms
  - [ ] Payment endpoints < 200ms
  - [ ] Performance documented

- [ ] Error handling is user-friendly
  - [ ] OAuth errors display helpful messages
  - [ ] Session expiration handled gracefully
  - [ ] 401 errors redirect appropriately

#### 13.3 Documentation
- [ ] Update README with authentication setup
  - [ ] Document Google OAuth2 setup steps
  - [ ] Document environment variables needed
  - [ ] Document how to run with authentication
  - [ ] Update setup instructions

- [ ] Document any known issues or limitations
  - [ ] Note any OAuth2 configuration requirements
  - [ ] Document test authentication approach
  - [ ] Note any performance considerations

---

## Notes

### OAuth2 Configuration Tips
- Redirect URI must match exactly: `http://localhost:8080/login/oauth2/code/google`
- Client ID and Secret must be set as environment variables
- Google Cloud Console setup is required before development

### Testing Considerations
- OAuth2 in tests can be complex; consider mocking or test security config
- E2E tests may need test Google OAuth credentials or mocking
- Performance tests should run with authentication enabled

### Security Considerations
- Use secure cookies in production (`secure: true`)
- Configure CORS properly for production
- Store OAuth2 credentials securely (not in code)
- Consider rate limiting for production

### Performance Considerations
- Authentication adds overhead; verify < 200ms requirement still met
- Session storage may impact performance at scale
- Consider caching strategies if needed

---

## Checklist Summary

### Backend
- [ ] Google OAuth2 configured
- [ ] Security configuration updated
- [ ] AuthController implemented
- [ ] All endpoints protected
- [ ] CORS configured for authentication

### Frontend
- [ ] LoginPage created
- [ ] useAuth hook implemented
- [ ] ProtectedRoute component created
- [ ] All feature routes protected
- [ ] User display in header

### Testing
- [ ] E2E test scenarios implemented
- [ ] Performance tests passing
- [ ] Integration test suite complete
- [ ] Error handling tested

### Verification
- [ ] All success criteria met
- [ ] Documentation updated
- [ ] Ready for final integration

