# Active Context: InvoiceMe

## Current Work Focus

**Status**: ✅ PRD 01 Foundation Complete - Ready for Feature Development

PRD 01 (Foundation & API Contract) has been successfully completed. Both backend and frontend projects are set up, running, and verified. The foundation enables all feature PRDs (02-07) to proceed in parallel.

**Current State**:
- ✅ Backend: Spring Boot 3.3.11 running on http://localhost:8080
- ✅ Frontend: React + Vite running on http://localhost:5173
- ✅ OpenAPI spec generated and accessible
- ✅ TypeScript types generated from OpenAPI
- ✅ CORS configured and working
- ✅ All infrastructure components in place

## Recent Changes

### ✅ PRD 01 Foundation - COMPLETED (November 7, 2024)

**Backend Foundation Completed**:
- ✅ Spring Boot 3.3.11 project initialized with Maven
- ✅ All dependencies configured (JPA, Security, OAuth2, MapStruct, Lombok, Flyway, OpenAPI, Testcontainers)
- ✅ H2 database configured for development
- ✅ PostgreSQL via Testcontainers configured for testing
- ✅ Flyway migration structure set up
- ✅ OpenAPI configuration with Swagger UI
- ✅ Controller stubs created with OpenAPI annotations (Customer, Invoice, Payment, Auth)
- ✅ GlobalExceptionHandler with standardized ErrorResponse DTO
- ✅ CORS configuration for frontend origin
- ✅ Basic security configuration (permissive for now, OAuth deferred to PRD 08)
- ✅ Testcontainers base test class created

**Frontend Foundation Completed**:
- ✅ React + Vite + TypeScript project initialized
- ✅ Tailwind CSS configured
- ✅ shadcn/ui structure prepared
- ✅ Axios client configured with interceptors and 401 redirect
- ✅ Type generation pipeline from OpenAPI spec working
- ✅ React Query configured with DevTools
- ✅ React Router with base routes
- ✅ Shared components created (Layout, LoadingSpinner, ErrorMessage, Pagination)
- ✅ Utility functions (cn, formatters, useAuth placeholder)

**Verification Completed**:
- ✅ Backend starts successfully on port 8080
- ✅ Frontend starts successfully on port 5173
- ✅ OpenAPI spec accessible at `/v3/api-docs`
- ✅ Swagger UI accessible at `/swagger-ui/index.html`
- ✅ TypeScript types generated from OpenAPI
- ✅ CORS allows frontend to call backend
- ✅ Error handling returns standardized format
- ✅ Both projects follow architectural patterns

### PRD Structure (8 PRDs for Parallel Development)
- ✅ **PRD 01**: Foundation & API Contract (COMPLETED)
- **PRD 02**: Customer Backend (ready to start)
- **PRD 03**: Customer Frontend (ready to start with mock data)
- **PRD 04**: Invoice Backend (ready to start)
- **PRD 05**: Invoice Frontend (ready to start with mock data)
- **PRD 06**: Payment Backend (ready to start)
- **PRD 07**: Payment Frontend (ready to start with mock data)
- **PRD 08**: Authentication & Integration (requires all features)

**Key Strategy**: Frontend PRDs (03, 05, 07) can start immediately using mock data. They do NOT need to wait for their corresponding backend PRDs (02, 04, 06). This enables true parallel development - all 6 feature PRDs can run simultaneously.

## Next Steps

### Immediate Next Steps - Feature Development

**Ready to Start**: All 6 feature PRDs (02-07) can now run in parallel:

1. **PRD 02: Customer Backend** (0.5-1 day)
   - Implement Customer domain entity (rich domain model)
   - Create CustomerCommandService and CustomerQueryService
   - Implement CustomerRepository
   - Create Flyway migration for customers table
   - Implement controller endpoints (currently stubbed)

2. **PRD 03: Customer Frontend** (0.5-1 day)
   - Can start immediately with mock data
   - Create CustomerList, CustomerForm components
   - Implement useCustomers and useCustomerMutations hooks
   - Create useCustomerViewModel hook
   - Build CustomersPage

3. **PRD 04: Invoice Backend** (1-1.5 days)
   - Implement Invoice domain entity with line items
   - Implement InvoiceStatus enum and lifecycle methods
   - Create InvoiceCommandService and InvoiceQueryService
   - Implement InvoiceRepository
   - Create Flyway migration for invoices and line_items tables

4. **PRD 05: Invoice Frontend** (1-1.5 days)
   - Can start immediately with mock data
   - Create InvoiceList, InvoiceForm, InvoiceDetails components
   - Implement invoice hooks and ViewModel
   - Build InvoicesPage

5. **PRD 06: Payment Backend** (0.5-1 day)
   - Implement Payment domain entity
   - Create PaymentCommandService and PaymentQueryService
   - Implement PaymentRepository
   - Create Flyway migration for payments table

6. **PRD 07: Payment Frontend** (0.5-1 day)
   - Can start immediately with mock data
   - Create PaymentList, PaymentForm components
   - Implement payment hooks and ViewModel
   - Build PaymentsPage

**Note**: Frontend PRDs (03, 05, 07 can start immediately after PRD 01 using mock API responses. They do NOT need to wait for backend PRDs (02, 04, 06).

### Development Roadmap (5-7 Days Sequential, 3-4 Days with Parallelization)

**PRD-Based Development Strategy** (see `planning/PRDs/` for details):

**Phase 1: Foundation (Sequential - Day 1)**
- **PRD 01**: Foundation & API Contract
  - Set up Spring Boot project with dependencies
  - Set up React + Vite project with shadcn/ui
  - Define API contract (OpenAPI spec)
  - Configure H2 database
  - Generate TypeScript types from OpenAPI
  - Basic security setup (OAuth deferred to PRD 08)

**Phase 2: Feature Development (Maximum Parallelism - Days 2-4)**
After PRD 01, all 6 feature PRDs can run in parallel:

- **PRD 02**: Customer Backend (0.5-1 day)
- **PRD 03**: Customer Frontend (0.5-1 day) - *starts with mock data, no wait for PRD 02*
- **PRD 04**: Invoice Backend (1-1.5 days)
- **PRD 05**: Invoice Frontend (1-1.5 days) - *starts with mock data, no wait for PRD 04*
- **PRD 06**: Payment Backend (0.5-1 day)
- **PRD 07**: Payment Frontend (0.5-1 day) - *starts with mock data, no wait for PRD 06*

**Phase 3: Integration (Sequential - Day 5)**
- **PRD 08**: Authentication & Integration
  - Implement Google OAuth
  - Complete E2E integration testing
  - Performance verification (< 200ms API)
  - Final polish

**Days 6-7: Documentation & Presentation**
- Write technical writeup
- Record demo video
- Code cleanup and comments
- README with setup instructions
- Prepare for presentation

**Parallel Development Benefits**:
- With 3-4 developers: Can complete in 3-4 days instead of 7
- Frontend developers can start immediately after PRD 01
- No blocking dependencies between frontend and backend features
- Mock data strategy enables true parallelization

## Active Decisions and Considerations

### Architectural Decisions
- **CQRS Implementation**: Simplified service-level separation (not individual handlers)
- **Domain Events**: Skipped for this project to reduce complexity
- **Database**: H2 for dev, PostgreSQL for test/prod
- **Authentication**: Session-based OAuth2 (not JWT)

### Technical Decisions
- **MapStruct + Lombok**: For DTO mapping (eliminates boilerplate)
- **React Query**: For server state management (optimistic updates)
- **shadcn/ui**: For UI components (copy-paste, customizable)
- **OpenAPI Type Generation**: Auto-generate TypeScript types from backend spec

### Development Priorities
1. **Must Have (PRD 01 - Day 1)**:
   - MapStruct + Lombok
   - OpenAPI spec generation
   - Consistent error envelope
   - Pagination support
   - TypeScript type generation from OpenAPI

2. **Should Have (PRDs 02-07 - Days 2-4)**:
   - Flyway migrations
   - React Query optimistic updates
   - Mock data setup for frontend (enables parallel development)

3. **Nice to Have (PRD 08 - Day 5)**:
   - Performance test automation
   - Postman collection export

### Parallel Development Strategy
- **Contract-First**: PRD 01 defines API contract, frontend generates types immediately
- **Mock Data**: Frontend PRDs use mock API responses until backend is ready
- **No Blocking**: Frontend PRDs (03, 05, 07) do NOT require backend PRDs (02, 04, 06) to start
- **Maximum Parallelism**: After PRD 01, all 6 feature PRDs can run simultaneously
- **Integration**: PRD 08 requires all features complete for final integration testing

## Current State

### What Exists
- ✅ Planning documents (PRDs, architecture docs)
- ✅ **8 PRDs structured for maximum parallel development** (see `planning/PRDs/`)
- ✅ **PRD 01 Foundation COMPLETE** - Backend and frontend projects initialized
- ✅ Backend project structure (Spring Boot with Clean Architecture)
- ✅ Frontend project structure (React + Vite with MVVM pattern)
- ✅ OpenAPI spec with controller stubs for all features
- ✅ TypeScript types generated from OpenAPI
- ✅ Database configuration (H2 for dev, Testcontainers for tests)
- ✅ Flyway migration structure ready
- ✅ Global exception handler and error response format
- ✅ CORS configuration
- ✅ Security configuration (basic, OAuth deferred)
- ✅ Shared frontend components (Layout, LoadingSpinner, ErrorMessage, Pagination)
- ✅ API client setup with interceptors
- ✅ React Query and routing configured
- ✅ Memory bank structure and documentation
- ✅ Clear architectural vision and technical stack
- ✅ Parallel development strategy documented

### What's Missing (Feature Implementation)
- ❌ Domain entities (Customer, Invoice, Payment, LineItem)
- ❌ Application services (Command/Query services)
- ❌ Repositories (JPA repositories)
- ❌ Database schema migrations (V2-V4 for features)
- ❌ Feature UI components (CustomerList, InvoiceForm, etc.)
- ❌ Feature hooks and ViewModels
- ❌ Integration tests
- ❌ OAuth2 authentication (PRD 08)

## Key Considerations

### Performance
- API response times must be < 200ms
- Use `@Transactional(readOnly = true)` for query services
- Implement pagination from day 1
- Consider database indexes on frequently queried fields

### Code Quality
- Rich domain models (not anemic)
- Clear separation of concerns (CQRS)
- Vertical slice organization
- Type safety throughout (TypeScript + Java)

### Testing
- Integration tests with Testcontainers
- Test complete flows (Customer → Invoice → Payment)
- Verify domain logic (calculateTotal, applyPayment)
- Performance verification

### AI Tool Usage
- Use AI tools intelligently as accelerators
- Maintain architectural integrity
- Document AI tool usage and prompts
- Focus on architectural guidance over code generation

