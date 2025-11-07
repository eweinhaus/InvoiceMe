# Task List: PRD 01 - Foundation & API Contract

## Overview
**Priority**: 🔴 Critical Path (Must be completed first)  
**Estimated Time**: 1 day  
**Dependencies**: None  
**Enables**: All other PRDs (02-07)

This task list breaks down PRD 01 into actionable, checkable tasks for both backend and frontend foundation setup.

---

## Backend Tasks

### 1. Project Setup
- [ ] Initialize Spring Boot 3.3.11 project with Maven
  - [ ] Use Spring Initializr or create manually
  - [ ] Set groupId: `com.invoiceme`
  - [ ] Set artifactId: `invoiceme-backend`
  - [ ] Set Java version: 17
  - [ ] Set packaging: `jar`

- [ ] Configure Maven `pom.xml` with all required dependencies:
  - [ ] Spring Boot Starter Web
  - [ ] Spring Boot Starter Data JPA
  - [ ] Spring Boot Starter Security
  - [ ] Spring Boot Starter OAuth2 Client
  - [ ] Spring Boot Starter Validation
  - [ ] H2 Database (runtime scope)
  - [ ] PostgreSQL Driver (runtime scope)
  - [ ] Flyway Core
  - [ ] MapStruct (annotation processor)
  - [ ] Lombok (annotation processor)
  - [ ] SpringDoc OpenAPI (Swagger UI + OpenAPI 3)

- [ ] Configure Maven compiler plugin for annotation processors:
  - [ ] Set Java source/target: 17
  - [ ] Configure MapStruct annotation processor
  - [ ] Configure Lombok annotation processor
  - [ ] Ensure correct processor order (Lombok → MapStruct)

- [ ] Set up package structure following Clean Architecture:
  - [ ] Create `com.invoiceme.domain` package
  - [ ] Create `com.invoiceme.application` package
  - [ ] Create `com.invoiceme.infrastructure` package
  - [ ] Create `com.invoiceme.presentation` package
  - [ ] Create sub-packages for each layer (customer, invoice, payment)

- [ ] Create main application class:
  - [ ] `InvoiceMeApplication.java` with `@SpringBootApplication`
  - [ ] Verify application starts successfully

### 2. Database Configuration
- [ ] Configure H2 database for development:
  - [ ] Add H2 configuration to `application.yml`
  - [ ] Set `spring.datasource.url: jdbc:h2:mem:invoiceme`
  - [ ] Set `spring.datasource.driver-class-name: org.h2.Driver`
  - [ ] Enable H2 console: `spring.h2.console.enabled: true`
  - [ ] Set `spring.jpa.hibernate.ddl-auto: update` (for dev)

- [ ] Configure PostgreSQL for testing (Testcontainers):
  - [ ] Add Testcontainers PostgreSQL dependency
  - [ ] Create test profile (`application-test.yml`)
  - [ ] Configure Testcontainers to use PostgreSQL
  - [ ] Set `spring.jpa.hibernate.ddl-auto: validate` (for test)

- [ ] Set up Flyway migration structure:
  - [ ] Create `src/main/resources/db/migration/` directory
  - [ ] Configure Flyway in `application.yml`
  - [ ] Set `spring.flyway.enabled: true`
  - [ ] Set `spring.flyway.locations: classpath:db/migration`

- [ ] Create initial migration placeholder:
  - [ ] Create `V1__init.sql` (empty or with comments)
  - [ ] Verify Flyway runs on application startup

### 3. API Contract Definition
- [ ] Configure SpringDoc OpenAPI:
  - [ ] Add SpringDoc OpenAPI dependency (already in pom.xml)
  - [ ] Create `OpenApiConfig.java` configuration class
  - [ ] Configure API info (title, version, description)
  - [ ] Set base API path: `/api/*`

- [ ] Create OpenAPI configuration class:
  - [ ] `@Configuration` class in infrastructure/config package
  - [ ] Define `OpenAPI` bean with API metadata
  - [ ] Configure server URLs (dev: `http://localhost:8080`)
  - [ ] Set API base path: `/api`

- [ ] Verify OpenAPI spec generation:
  - [ ] Start application
  - [ ] Access `/v3/api-docs` endpoint
  - [ ] Verify JSON spec is generated
  - [ ] Access `/swagger-ui.html` (or `/swagger-ui/index.html`)
  - [ ] Verify Swagger UI loads correctly

- [ ] Create controller stubs with OpenAPI annotations:
  - [ ] `CustomerController` with CRUD endpoint stubs
  - [ ] `InvoiceController` with CRUD + lifecycle endpoint stubs
  - [ ] `PaymentController` with create + list endpoint stubs
  - [ ] `AuthController` with `GET /api/auth/user` endpoint stub
  - [ ] Add `@Operation`, `@ApiResponse` annotations to all endpoints
  - [ ] Define request/response DTOs as OpenAPI schemas

### 4. Infrastructure Components
- [ ] Create `GlobalExceptionHandler`:
  - [ ] Create `@RestControllerAdvice` class
  - [ ] Handle common exceptions:
    - [ ] `MethodArgumentNotValidException` (400)
    - [ ] `EntityNotFoundException` (404)
    - [ ] `IllegalArgumentException` (400)
    - [ ] `IllegalStateException` (422)
    - [ ] `AccessDeniedException` (403)
    - [ ] `AuthenticationException` (401)
    - [ ] Generic `Exception` (500)

- [ ] Create standardized `ErrorResponse` DTO:
  - [ ] Fields: `timestamp`, `status`, `error`, `message`, `path`
  - [ ] Use consistent error codes: 400, 401, 403, 404, 422, 500
  - [ ] Add validation error details for 400 responses

- [ ] Configure CORS for frontend origin:
  - [ ] Create `CorsConfig.java` configuration class
  - [ ] Allow frontend origin: `http://localhost:5173` (Vite default)
  - [ ] Allow credentials: `true`
  - [ ] Allow methods: GET, POST, PUT, DELETE, OPTIONS
  - [ ] Allow headers: Content-Type, Authorization
  - [ ] Test CORS with frontend request

- [ ] Set up basic security configuration:
  - [ ] Create `SecurityConfig.java` (defer OAuth to PRD 08)
  - [ ] Configure basic security (can be permissive for now)
  - [ ] Allow `/api/**` endpoints (or configure properly)
  - [ ] Allow `/v3/api-docs/**` and `/swagger-ui/**` (for dev)
  - [ ] Allow `/h2-console/**` (for dev, if needed)

- [ ] Create base configuration classes:
  - [ ] Verify all config classes are in `infrastructure/config` package
  - [ ] Ensure proper `@Configuration` annotations

### 5. Testing Infrastructure
- [ ] Set up Testcontainers configuration:
  - [ ] Add Testcontainers dependencies (if not already added)
  - [ ] Create base test configuration class
  - [ ] Configure PostgreSQL container for tests
  - [ ] Set up container lifecycle management

- [ ] Create base integration test class:
  - [ ] `@SpringBootTest` base class
  - [ ] Configure Testcontainers PostgreSQL
  - [ ] Set up test database connection
  - [ ] Add `@Transactional` support for test cleanup

- [ ] Configure test database setup:
  - [ ] Ensure Flyway runs in tests
  - [ ] Verify test database is isolated
  - [ ] Test basic database connectivity

---

## Frontend Tasks

### 1. Project Setup
- [ ] Initialize React + Vite project with TypeScript:
  - [ ] Run `npm create vite@latest invoiceme-frontend -- --template react-ts`
  - [ ] Or use Vite CLI: `npm create vite@latest`
  - [ ] Select React + TypeScript template
  - [ ] Verify project structure is created

- [ ] Configure Tailwind CSS:
  - [ ] Install Tailwind CSS: `npm install -D tailwindcss postcss autoprefixer`
  - [ ] Initialize Tailwind: `npx tailwindcss init -p`
  - [ ] Configure `tailwind.config.js` with content paths
  - [ ] Add Tailwind directives to `src/index.css`
  - [ ] Verify Tailwind classes work

- [ ] Set up shadcn/ui CLI and install base components:
  - [ ] Initialize shadcn/ui: `npx shadcn-ui@latest init`
  - [ ] Configure `components.json`
  - [ ] Install base components:
    - [ ] `button`
    - [ ] `table`
    - [ ] `form`
    - [ ] `input`
    - [ ] `dialog`
    - [ ] `select`
    - [ ] `badge`
    - [ ] `card`
  - [ ] Verify components are installed in `src/components/ui/`

- [ ] Configure ESLint and Prettier:
  - [ ] Install ESLint: `npm install -D eslint`
  - [ ] Install Prettier: `npm install -D prettier`
  - [ ] Create `.eslintrc.json` configuration
  - [ ] Create `.prettierrc` configuration
  - [ ] Add format scripts to `package.json`

- [ ] Set up environment configuration:
  - [ ] Create `.env` file
  - [ ] Create `.env.example` file
  - [ ] Add `VITE_API_URL=http://localhost:8080/api`
  - [ ] Add `VITE_GOOGLE_CLIENT_ID=` (placeholder for PRD 08)
  - [ ] Verify environment variables are accessible

### 2. API Client Setup
- [ ] Install and configure Axios:
  - [ ] Install Axios: `npm install axios`
  - [ ] Create `src/lib/api/client.ts` file
  - [ ] Create Axios instance with base URL from env

- [ ] Create Axios instance with base URL from env:
  - [ ] Use `import.meta.env.VITE_API_URL` for base URL
  - [ ] Set default timeout
  - [ ] Configure default headers

- [ ] Configure `withCredentials: true` for session cookies:
  - [ ] Set `withCredentials: true` on Axios instance
  - [ ] Verify cookies will be sent with requests

- [ ] Set up request/response interceptors:
  - [ ] Request interceptor: Add auth headers if needed (for PRD 08)
  - [ ] Response interceptor: Handle errors globally
  - [ ] Error interceptor: Transform error responses

- [ ] Implement 401 redirect to login:
  - [ ] In response interceptor, check for 401 status
  - [ ] Redirect to `/login` on 401
  - [ ] Store redirect path for post-login navigation

### 3. Type Generation Pipeline
- [ ] Install type generation tool:
  - [ ] Option A: Install `openapi-typescript`: `npm install -D openapi-typescript`
  - [ ] Option B: Install `swagger-typescript-api`: `npm install -D swagger-typescript-api`
  - [ ] Choose one approach (recommend `openapi-typescript`)

- [ ] Create npm script for type generation:
  - [ ] Add script to `package.json`: `"generate:types": "openapi-typescript http://localhost:8080/v3/api-docs -o src/types/api.ts"`
  - [ ] Or use `swagger-typescript-api` equivalent command
  - [ ] Test script runs successfully

- [ ] Configure type generation from backend OpenAPI spec:
  - [ ] Ensure backend is running (or use stubbed spec)
  - [ ] Run type generation script
  - [ ] Verify `src/types/api.ts` (or equivalent) is created
  - [ ] Verify types are correct

- [ ] Set up type generation workflow:
  - [ ] Document how to run type generation
  - [ ] Add note about backend needing to be running
  - [ ] Consider adding pre-build script (optional)

### 4. React Query Setup
- [ ] Install and configure React Query (TanStack Query):
  - [ ] Install: `npm install @tanstack/react-query`
  - [ ] Create `src/lib/react-query/queryClient.ts`
  - [ ] Create QueryClient instance

- [ ] Set up QueryClient with default options:
  - [ ] Configure default query options (staleTime, cacheTime)
  - [ ] Configure default mutation options
  - [ ] Set retry logic
  - [ ] Configure error handling

- [ ] Configure React Query DevTools (development only):
  - [ ] Install: `npm install -D @tanstack/react-query-devtools`
  - [ ] Add `<ReactQueryDevtools />` to App (dev only)
  - [ ] Verify DevTools appear in browser

- [ ] Wrap app with QueryClientProvider:
  - [ ] Update `src/main.tsx` (or `src/index.tsx`)
  - [ ] Wrap app with `<QueryClientProvider>`
  - [ ] Verify React Query works

### 5. Routing Setup
- [ ] Install and configure React Router:
  - [ ] Install: `npm install react-router-dom`
  - [ ] Create `src/routes/index.tsx` or `src/App.tsx` with routing

- [ ] Set up base route structure:
  - [ ] Create route for `/` (home/dashboard)
  - [ ] Create route for `/login` (placeholder for PRD 08)
  - [ ] Create placeholder routes for features:
    - [ ] `/customers` (placeholder)
    - [ ] `/invoices` (placeholder)
    - [ ] `/payments` (placeholder)
  - [ ] Add `<Outlet />` or route components

- [ ] Create placeholder routes for features:
  - [ ] Create placeholder components for each route
  - [ ] Add basic "Coming soon" or feature name display
  - [ ] Verify routing works

### 6. Shared Components
- [ ] Create `Layout` component:
  - [ ] Create `src/components/layout/Layout.tsx`
  - [ ] Include Header structure
  - [ ] Include Sidebar structure (if needed)
  - [ ] Include main content area with `<Outlet />`
  - [ ] Add basic styling with Tailwind

- [ ] Create `LoadingSpinner` component:
  - [ ] Create `src/components/common/LoadingSpinner.tsx`
  - [ ] Use shadcn/ui spinner or create custom
  - [ ] Make it reusable with size variants

- [ ] Create `ErrorMessage` component:
  - [ ] Create `src/components/common/ErrorMessage.tsx`
  - [ ] Display error message with styling
  - [ ] Support different error types

- [ ] Create `Pagination` component:
  - [ ] Create `src/components/common/Pagination.tsx`
  - [ ] Accept props: `currentPage`, `totalPages`, `onPageChange`
  - [ ] Use shadcn/ui button components
  - [ ] Make it reusable for all list pages

- [ ] Verify base shadcn/ui components are installed:
  - [ ] Check `src/components/ui/` directory
  - [ ] Verify all required components exist:
    - [ ] `button.tsx`
    - [ ] `table.tsx`
    - [ ] `form.tsx`
    - [ ] `input.tsx`
    - [ ] `dialog.tsx`
    - [ ] `select.tsx`
    - [ ] `badge.tsx`
    - [ ] `card.tsx`

### 7. Utilities
- [ ] Create `lib/utils/cn.ts` (className utility):
  - [ ] Install `clsx` and `tailwind-merge`: `npm install clsx tailwind-merge`
  - [ ] Create `cn()` utility function
  - [ ] Export from `src/lib/utils/cn.ts`
  - [ ] This is typically auto-generated by shadcn/ui, verify it exists

- [ ] Create `lib/utils/formatters.ts` (date, currency formatting):
  - [ ] Create `src/lib/utils/formatters.ts`
  - [ ] Add `formatCurrency()` function
  - [ ] Add `formatDate()` function
  - [ ] Export functions

- [ ] Create `lib/hooks/useAuth.ts` (placeholder):
  - [ ] Create `src/lib/hooks/useAuth.ts`
  - [ ] Add placeholder hook structure
  - [ ] Return mock user data for now
  - [ ] Add TODO comment for PRD 08 implementation

---

## Verification Tasks

### Backend Verification
- [ ] Backend starts without errors:
  - [ ] Run `mvn spring-boot:run` or start from IDE
  - [ ] Verify no compilation errors
  - [ ] Verify application starts on port 8080
  - [ ] Check logs for any warnings

- [ ] OpenAPI spec is accessible:
  - [ ] Access `http://localhost:8080/v3/api-docs`
  - [ ] Verify JSON spec is returned
  - [ ] Verify all controller stubs are in spec

- [ ] Swagger UI is accessible:
  - [ ] Access `http://localhost:8080/swagger-ui.html` (or `/swagger-ui/index.html`)
  - [ ] Verify Swagger UI loads
  - [ ] Verify all endpoints are visible

- [ ] Global exception handler works:
  - [ ] Test invalid request (should return 400)
  - [ ] Test non-existent endpoint (should return 404)
  - [ ] Verify error response format matches `ErrorResponse` DTO

- [ ] CORS is configured:
  - [ ] Start frontend (if ready)
  - [ ] Make test API call from frontend
  - [ ] Verify no CORS errors in browser console

- [ ] H2 database is accessible:
  - [ ] Access `http://localhost:8080/h2-console`
  - [ ] Connect to database
  - [ ] Verify connection works

- [ ] Flyway migration structure is ready:
  - [ ] Check Flyway logs on startup
  - [ ] Verify `V1__init.sql` is recognized
  - [ ] Verify no migration errors

### Frontend Verification
- [ ] Frontend starts without errors:
  - [ ] Run `npm run dev`
  - [ ] Verify no compilation errors
  - [ ] Verify application starts on port 5173 (or configured port)
  - [ ] Check browser console for errors

- [ ] TypeScript types can be generated:
  - [ ] Ensure backend is running
  - [ ] Run `npm run generate:types`
  - [ ] Verify types file is created
  - [ ] Verify no type generation errors

- [ ] Axios client is configured:
  - [ ] Verify Axios instance is created
  - [ ] Verify base URL is correct
  - [ ] Verify `withCredentials` is set

- [ ] React Query is configured:
  - [ ] Verify QueryClientProvider wraps app
  - [ ] Verify React Query DevTools appear (in dev)
  - [ ] Test a simple query (can be mock)

- [ ] Base routing structure works:
  - [ ] Navigate to `/`
  - [ ] Navigate to `/customers`
  - [ ] Navigate to `/invoices`
  - [ ] Navigate to `/payments`
  - [ ] Verify routes work without errors

- [ ] Shared components are created:
  - [ ] Verify `Layout` component exists
  - [ ] Verify `LoadingSpinner` component exists
  - [ ] Verify `ErrorMessage` component exists
  - [ ] Verify `Pagination` component exists
  - [ ] Test components render without errors

### Integration Verification
- [ ] CORS allows frontend to call backend:
  - [ ] Start both backend and frontend
  - [ ] Make test API call from frontend
  - [ ] Verify request succeeds (or returns proper error, not CORS error)
  - [ ] Check browser Network tab for CORS headers

- [ ] Error handling returns standardized format:
  - [ ] Trigger error from frontend (e.g., 404)
  - [ ] Verify error response matches `ErrorResponse` structure
  - [ ] Verify frontend can parse error response

- [ ] Both projects follow architectural patterns:
  - [ ] Backend: Verify Clean Architecture package structure
  - [ ] Frontend: Verify feature-based structure (if started)
  - [ ] Verify code organization matches architecture docs

---

## Notes

- **MapStruct + Lombok**: Ensure annotation processors are configured correctly in Maven
- **OpenAPI Stubs**: Controller stubs can return mock data initially; implementation happens in feature PRDs
- **Type Generation**: Backend must be running for type generation (or use static OpenAPI spec file)
- **CORS**: Test early to avoid integration issues
- **OAuth**: Deferred to PRD 08; basic security config is sufficient for now
- **Database**: H2 for dev is fine; PostgreSQL via Testcontainers for tests

---

## Completion Checklist

Before marking PRD 01 as complete, verify:

- [ ] All backend tasks completed
- [ ] All frontend tasks completed
- [ ] All verification tasks passed
- [ ] Backend starts successfully
- [ ] Frontend starts successfully
- [ ] OpenAPI spec is generated and accessible
- [ ] TypeScript types can be generated from OpenAPI spec
- [ ] CORS is configured and working
- [ ] Error handling returns standardized format
- [ ] Both projects follow architectural patterns
- [ ] Ready for feature PRDs (02-07) to begin

---

**Status**: ✅ Complete  
**Last Updated**: November 7, 2024

