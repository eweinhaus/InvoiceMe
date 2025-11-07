# PRD 01: Foundation & API Contract

## Overview
**Priority**: 🔴 Critical Path (Must be completed first)  
**Dependencies**: None  
**Enables**: All other PRDs

This PRD establishes the foundational infrastructure for both backend and frontend, including project setup, API contract definition, and shared infrastructure components.

## Objectives
1. Set up backend and frontend project structures
2. Define and generate API contract (OpenAPI spec)
3. Configure shared infrastructure (database, security basics, CORS)
4. Establish development tooling and type generation pipeline
5. Create reusable error handling and validation patterns

## Backend Tasks

### Project Setup
- [ ] Initialize Spring Boot 3.3.11 project with Maven
- [ ] Configure all dependencies (Spring Boot, JPA, Security, OAuth2, MapStruct, Lombok, Flyway, OpenAPI)
- [ ] Set up package structure following Clean Architecture layers
- [ ] Configure Maven compiler plugin for MapStruct + Lombok annotation processors

### Database Configuration
- [ ] Configure H2 database for development
- [ ] Configure PostgreSQL for testing (Testcontainers)
- [ ] Set up Flyway migration structure (`db/migration/`)
- [ ] Create initial migration placeholder (V1__init.sql)

### API Contract Definition
- [ ] Configure SpringDoc OpenAPI (Swagger)
- [ ] Create OpenAPI configuration class
- [ ] Define base API structure (`/api/*` prefix)
- [ ] Set up OpenAPI spec generation endpoint (`/v3/api-docs`)

### Infrastructure Components
- [ ] Create `GlobalExceptionHandler` with standardized `ErrorResponse` DTO
- [ ] Implement error codes: 400, 401, 403, 404, 422, 500
- [ ] Configure CORS for frontend origin
- [ ] Set up basic security configuration (defer OAuth to PRD 08)
- [ ] Create base configuration classes

### Testing Infrastructure
- [ ] Set up Testcontainers configuration
- [ ] Create base integration test class
- [ ] Configure test database setup

## Frontend Tasks

### Project Setup
- [ ] Initialize React + Vite project with TypeScript
- [ ] Configure Tailwind CSS
- [ ] Set up shadcn/ui CLI and install base components
- [ ] Configure ESLint and Prettier
- [ ] Set up environment configuration (`.env`)

### API Client Setup
- [ ] Install and configure Axios
- [ ] Create Axios instance with base URL from env
- [ ] Configure `withCredentials: true` for session cookies
- [ ] Set up request/response interceptors
- [ ] Implement 401 redirect to login

### Type Generation Pipeline
- [ ] Install `openapi-typescript` or `swagger-typescript-api`
- [ ] Create npm script: `generate:types`
- [ ] Configure type generation from backend OpenAPI spec
- [ ] Set up type generation workflow (can run against stubbed backend)

### React Query Setup
- [ ] Install and configure React Query (TanStack Query)
- [ ] Set up QueryClient with default options
- [ ] Configure React Query DevTools (development only)

### Routing Setup
- [ ] Install and configure React Router
- [ ] Set up base route structure
- [ ] Create placeholder routes for features (to be implemented in feature PRDs)

### Shared Components
- [ ] Create `Layout` component (Header, Sidebar structure)
- [ ] Create `LoadingSpinner` component
- [ ] Create `ErrorMessage` component
- [ ] Create `Pagination` component (reusable)
- [ ] Install base shadcn/ui components: button, table, form, input, dialog, select, badge, card

### Utilities
- [ ] Create `lib/utils/cn.ts` (className utility)
- [ ] Create `lib/utils/formatters.ts` (date, currency formatting)
- [ ] Create `lib/hooks/useAuth.ts` (placeholder, to be implemented in PRD 08)

## API Contract Specification

### Base Structure
- All endpoints under `/api/*`
- Standardized error response format
- Pagination support for all list endpoints
- Consistent DTO structure

### Endpoint Stubs (for type generation)
Create controller stubs with OpenAPI annotations for:
- Customer endpoints (full CRUD)
- Invoice endpoints (full CRUD + lifecycle)
- Payment endpoints (create + list)
- Auth endpoint (`GET /api/auth/user`)

**Note**: These can be stubbed initially to generate OpenAPI spec. Implementation happens in feature PRDs.

## Deliverables

### Backend
- ✅ Working Spring Boot application (starts successfully)
- ✅ OpenAPI spec available at `/v3/api-docs`
- ✅ Swagger UI available at `/swagger-ui.html`
- ✅ Global exception handler with error envelope
- ✅ CORS configured
- ✅ H2 database accessible
- ✅ Flyway migration structure ready

### Frontend
- ✅ Working React application (starts successfully)
- ✅ TypeScript types generated from OpenAPI spec
- ✅ Axios client configured
- ✅ React Query configured
- ✅ Base routing structure
- ✅ Shared components library
- ✅ Development environment ready

## Success Criteria
- [ ] Backend starts without errors
- [ ] Frontend starts without errors
- [ ] OpenAPI spec is generated and accessible
- [ ] TypeScript types can be generated from OpenAPI spec
- [ ] CORS allows frontend to call backend
- [ ] Error handling returns standardized format
- [ ] Both projects follow architectural patterns

## Dependencies
- **Blocks**: All feature PRDs (02-07)
- **Enables**: Parallel development of all features

## Timeline Estimate
**1 day** (Day 1 of development)

## Notes
- This PRD must be completed before any feature work begins
- API contract stubs enable frontend to generate types and start development
- OAuth implementation is deferred to PRD 08 (can use mock auth initially)
- Database schema migrations will be created in feature PRDs as needed

