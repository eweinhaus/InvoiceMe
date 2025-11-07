# Active Context: InvoiceMe

## Current Work Focus

**Status**: ✅ Customer & Invoice Complete (Backend + Frontend) - Payment Features In Progress

PRD 01 (Foundation), PRD 02 (Customer Backend), PRD 03 (Customer Frontend), PRD 04 (Invoice Backend), and PRD 05 (Invoice Frontend) have been successfully completed. Both Customer and Invoice features are fully functional end-to-end. Remaining PRDs (06-07) can proceed in parallel.

**Current State**:
- ✅ Backend: Spring Boot 3.3.11 running on http://localhost:8080
- ✅ Frontend: React + Vite running on http://localhost:5173
- ✅ OpenAPI spec generated and accessible
- ✅ TypeScript types generated from OpenAPI
- ✅ CORS configured and working
- ✅ All infrastructure components in place
- ✅ Customer feature fully functional (backend + frontend)
- ✅ Invoice feature fully functional (backend + frontend)
- ✅ Navigation bar added to Layout component

## Recent Changes

### ✅ PRD 04 Invoice Backend - COMPLETED (November 7, 2024)

**Invoice Backend Implementation Completed**:
- ✅ Invoice domain entity (`Invoice.java`) with rich behavior:
  - `calculateTotal()` method for line item aggregation
  - `calculateBalance()` method (placeholder for payment integration)
  - `addLineItem()` method for building invoices
  - `canBeMarkedAsSent()` and `markAsSent()` for lifecycle management
  - `canBeEdited()` and `updateLineItems()` for edit restrictions
  - `applyPayment()` method for payment application
  - Manual timestamp handling via `@PrePersist` and `@PreUpdate`
  - Not anemic - business logic in entity methods
- ✅ LineItem embeddable value object (`LineItem.java`):
  - `calculateSubtotal()` method for line item calculations
  - Validation in constructor (quantity > 0, unitPrice >= 0, description required)
- ✅ InvoiceStatus enum (`DRAFT`, `SENT`, `PAID`)
- ✅ Invoice DTOs:
  - `CreateInvoiceRequest` (customerId, lineItems)
  - `UpdateInvoiceRequest` (lineItems only)
  - `InvoiceResponse` (full invoice details with customer name)
  - `LineItemRequest` and `LineItemResponse` (with calculated subtotal)
- ✅ InvoiceMapper (MapStruct) for DTO ↔ Entity conversion
- ✅ InvoiceCommandService with `@Transactional`:
  - `createInvoice()` - creates invoice with line items, calculates totals
  - `updateInvoice()` - updates line items (only if DRAFT status)
  - `markAsSent()` - transitions invoice from DRAFT to SENT
- ✅ InvoiceQueryService with `@Transactional(readOnly = true)`:
  - `getById()` - throws EntityNotFoundException for 404
  - `getAll()` - pagination support
  - `getByStatus()` - filter by invoice status
  - `getByCustomerId()` - filter by customer
- ✅ InvoiceRepository (JPA) with custom query methods:
  - `findByStatus()` - paginated status filtering
  - `findByCustomer_Id()` - paginated customer filtering
- ✅ InvoiceController fully implemented:
  - POST `/api/invoices` - Create (201)
  - GET `/api/invoices/{id}` - Get by ID (200)
  - GET `/api/invoices` - List with optional filters (status, customerId) (200)
  - PUT `/api/invoices/{id}` - Update (only if DRAFT) (200)
  - POST `/api/invoices/{id}/send` - Mark as SENT (200)
- ✅ Flyway migration V3__create_invoices_table.sql:
  - UUID primary key
  - Foreign key to customers table
  - Status enum constraint
  - Separate invoice_line_items table with order column
  - Indexes on customer_id and status
- ✅ InvoiceIntegrationTest with comprehensive test scenarios:
  - CRUD operations
  - Lifecycle management (DRAFT → SENT transitions)
  - Business rule validation (edit restrictions, status transitions)
  - Calculation validation (totals, subtotals, balance)
  - Error handling (404, validation errors, business rule violations)
  - Pagination and filtering
  - Domain entity behavior validation
- ✅ CustomerIntegrationTest fixes:
  - Fixed timestamp comparison assertion (isAfterOrEqualTo)
  - Added explicit email duplicate checking in CustomerCommandService
  - Added `findByEmail()` method to CustomerRepository
  - All 16 Customer tests passing

**Architecture Validation**:
- ✅ DDD: Rich domain model with business logic in Invoice and LineItem entities
- ✅ CQRS: Clear separation between InvoiceCommandService and InvoiceQueryService
- ✅ Vertical Slice: Invoice feature organized across all layers
- ✅ Clean Architecture: Proper layer boundaries maintained
- ✅ Integration tests: Comprehensive test coverage with Testcontainers

### ✅ PRD 05 Invoice Frontend - COMPLETED (December 2024)

**Invoice Frontend Implementation Completed**:
- ✅ Invoice types defined (`InvoiceStatus` enum, `Invoice`, `LineItem`, request/response types)
- ✅ Invoice API client methods (`src/lib/api/invoices.ts`):
  - `getInvoices()`, `getInvoicesByStatus()`, `getInvoicesByCustomer()`
  - `getInvoicesWithFilters()` (combined status + customer)
  - `getInvoiceById()`, `createInvoice()`, `updateInvoice()`, `markInvoiceAsSent()`
- ✅ React Query hooks (`src/features/invoices/hooks/`):
  - `useInvoices.ts` - 4 query hooks (all, by status, by customer, by ID)
  - `useInvoiceMutations.ts` - 3 mutation hooks (create, update, mark as sent)
  - Query invalidation and error handling implemented
- ✅ ViewModel hook (`useInvoiceViewModel.ts`):
  - Manages filter state (status, customer)
  - Manages pagination state
  - Conditionally calls appropriate query hooks based on filters
  - Encapsulates all business logic and state management
- ✅ UI Components (`src/features/invoices/components/`):
  - `InvoiceStatusBadge` - Color-coded status display (Draft=gray, Sent=blue, Paid=green)
  - `LineItemForm` - Individual line item with real-time subtotal calculation
  - `InvoiceForm` - Create/edit form with React Hook Form + Zod validation
    - Dynamic line items using `useFieldArray`
    - Real-time total calculation
    - Customer selector
  - `InvoiceList` - Table with pagination, status badges, conditional actions
  - `InvoiceDetails` - Modal with full invoice details, line items, balance info
- ✅ Page component (`InvoicesPage.tsx`):
  - Filter controls (status and customer dropdowns)
  - Create Invoice button
  - Dialog management for form and details
  - Customer fetching for form and filters
- ✅ Routing integration (`/invoices` route)
- ✅ Navigation added to Layout component:
  - Navigation bar with Home, Customers, Invoices, Payments links
  - Active state highlighting
  - Quick action buttons on home page

**Key Features Implemented**:
- ✅ Full CRUD operations (Create, Read, Update)
- ✅ Dynamic line items (add/remove with validation)
- ✅ Real-time calculations (subtotals and total)
- ✅ Status filtering (All, Draft, Sent, Paid)
- ✅ Customer filtering (All Customers + specific customer)
- ✅ Combined filtering (status + customer)
- ✅ Status lifecycle (Draft → Sent transition)
- ✅ Form validation (Zod schemas)
- ✅ Responsive design
- ✅ Accessibility (ARIA labels, keyboard navigation)

**Architecture Validation**:
- ✅ MVVM: Clear separation (ViewModel hook, View components)
- ✅ Vertical Slice: Feature organized in `features/invoices/`
- ✅ Type Safety: TypeScript types from OpenAPI
- ✅ React Query: Server state management with caching

### ✅ PRD 02 Customer Backend - COMPLETED (November 7, 2024)

**Customer Backend Implementation Completed**:
- ✅ Customer domain entity (`Customer.java`) with rich behavior:
  - `validate()` method for email format and required fields validation
  - `updateDetails()` method for partial updates with validation
  - Manual timestamp handling via `@PrePersist` and `@PreUpdate`
  - Not anemic - business logic in entity methods
- ✅ Customer DTOs (`CustomerRequest`, `CustomerResponse`) - already existed, verified
- ✅ CustomerMapper (MapStruct) for DTO ↔ Entity conversion
- ✅ CustomerCommandService with `@Transactional`:
  - `createCustomer()` - handles duplicate email exceptions
  - `updateCustomer()` - uses domain entity `updateDetails()` method
  - `deleteCustomer()` - validates existence before deletion
- ✅ CustomerQueryService with `@Transactional(readOnly = true)`:
  - `getById()` - throws EntityNotFoundException for 404
  - `getAll()` - pagination support with `Page<CustomerResponse>`
- ✅ CustomerRepository (JPA) extending `JpaRepository<Customer, UUID>`
- ✅ CustomerController fully implemented:
  - POST `/api/customers` - Create (201)
  - GET `/api/customers/{id}` - Get by ID (200)
  - GET `/api/customers` - List with pagination (200)
  - PUT `/api/customers/{id}` - Update (200)
  - DELETE `/api/customers/{id}` - Delete (204)
- ✅ Flyway migration V2__create_customers_table.sql:
  - UUID primary key
  - Email uniqueness constraint and index
  - Timestamp columns
- ✅ CustomerIntegrationTest with 15 test scenarios:
  - CRUD operations
  - Error handling (404, validation errors, duplicate email)
  - Pagination and sorting
  - Domain entity behavior validation
- ✅ Performance validation: All endpoints < 200ms (tested via Docker with Java 17)
- ✅ Testing infrastructure: Docker setup for Java 17 testing, endpoint test scripts

**Architecture Validation**:
- ✅ DDD: Rich domain model with business logic in entity
- ✅ CQRS: Clear separation between Command and Query services
- ✅ Vertical Slice: Feature organized across all layers
- ✅ Clean Architecture: Proper layer boundaries maintained

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
- ✅ **PRD 02**: Customer Backend (COMPLETED)
- ✅ **PRD 03**: Customer Frontend (COMPLETED)
- ✅ **PRD 04**: Invoice Backend (COMPLETED)
- ✅ **PRD 05**: Invoice Frontend (COMPLETED)
- **PRD 06**: Payment Backend (ready to start)
- **PRD 07**: Payment Frontend (ready to start with mock data)
- **PRD 08**: Authentication & Integration (requires all features)

**Key Strategy**: Frontend PRD (07) can start immediately using mock data. It does NOT need to wait for backend PRD (06). This enables true parallel development - all remaining feature PRDs (04, 06-07) can run simultaneously.

## Next Steps

### Immediate Next Steps - Feature Development

**Ready to Start**: Remaining feature PRDs (04-07) can now run in parallel:

1. ✅ **PRD 02: Customer Backend** (COMPLETED)
   - ✅ Customer domain entity implemented with rich behavior (`validate()`, `updateDetails()`)
   - ✅ CustomerCommandService and CustomerQueryService created (CQRS pattern)
   - ✅ CustomerRepository implemented (JPA)
   - ✅ Flyway migration V2__create_customers_table.sql created
   - ✅ CustomerController endpoints fully implemented (all 5 CRUD operations)
   - ✅ Integration tests created (15 test scenarios)
   - ✅ Performance validated (< 200ms for all endpoints)
   - ✅ Tested with Java 17 via Docker

2. ✅ **PRD 03: Customer Frontend** (COMPLETED)
   - ✅ CustomerList, CustomerForm, CustomerDeleteDialog components created
   - ✅ useCustomers and useCustomerMutations hooks implemented
   - ✅ useCustomerViewModel hook created (MVVM pattern)
   - ✅ CustomersPage built and integrated with routing
   - ✅ Form validation with Zod
   - ✅ React Query with query invalidation
   - ✅ Responsive design and accessibility

3. ✅ **PRD 04: Invoice Backend** (COMPLETED)
   - ✅ Invoice domain entity with line items and rich behavior
   - ✅ InvoiceStatus enum and lifecycle methods (markAsSent, canBeEdited, etc.)
   - ✅ InvoiceCommandService and InvoiceQueryService (CQRS pattern)
   - ✅ InvoiceRepository with custom query methods
   - ✅ Flyway migration V3__create_invoices_table.sql
   - ✅ Comprehensive integration tests

4. ✅ **PRD 05: Invoice Frontend** (COMPLETED)
   - ✅ InvoiceList, InvoiceForm, InvoiceDetails, LineItemForm, InvoiceStatusBadge components
   - ✅ Invoice hooks (useInvoices, useInvoiceMutations, useInvoiceViewModel)
   - ✅ InvoicesPage with filters and dialogs
   - ✅ Navigation bar added to Layout
   - ✅ Full CRUD operations with line items support
   - ✅ Status and customer filtering
   - ✅ Real-time calculations and form validation

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

**Note**: Frontend PRDs (05, 07) can start immediately after PRD 01 using mock API responses. They do NOT need to wait for backend PRDs (04, 06).

### Development Roadmap (5-7 Days Sequential, 3-4 Days with Parallelization)

**PRD-Based Development Strategy** (see `planning/PRDs/` for details):

**Phase 1: Foundation (Sequential - Day 1)**
- ✅ **PRD 01**: Foundation & API Contract (COMPLETED)

**Phase 2: Feature Development (Maximum Parallelism - Days 2-4)**
- ✅ **PRD 02**: Customer Backend (COMPLETED)
- ✅ **PRD 03**: Customer Frontend (COMPLETED)
- **PRD 04**: Invoice Backend (1-1.5 days) - *ready to start*
- ✅ **PRD 05**: Invoice Frontend (COMPLETED)
- **PRD 06**: Payment Backend (0.5-1 day) - *ready to start*
- **PRD 07**: Payment Frontend (0.5-1 day) - *can start with mock data, no wait for PRD 06*

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
- **Timestamp Handling**: Manual via `@PrePersist`/`@PreUpdate` (chosen over JPA Auditing for simplicity)

### Technical Decisions
- **MapStruct + Lombok**: For DTO mapping (eliminates boilerplate) - ✅ CustomerMapper implemented
- **React Query**: For server state management (optimistic updates)
- **shadcn/ui**: For UI components (copy-paste, customizable)
- **OpenAPI Type Generation**: Auto-generate TypeScript types from backend spec
- **Docker for Testing**: Use Docker with Java 17 for consistent testing environment

### Development Priorities
1. **Must Have (PRD 01 - Day 1)**:
   - MapStruct + Lombok ✅
   - OpenAPI spec generation ✅
   - Consistent error envelope ✅
   - Pagination support ✅
   - TypeScript type generation from OpenAPI ✅

2. **Should Have (PRDs 02-07 - Days 2-4)**:
   - Flyway migrations ✅ (V2 complete)
   - React Query optimistic updates
   - Mock data setup for frontend (enables parallel development)
   - Integration tests ✅ (Customer tests complete)

3. **Nice to Have (PRD 08 - Day 5)**:
   - Performance test automation ✅ (endpoint test scripts created)
   - Postman collection export

### Parallel Development Strategy
- **Contract-First**: PRD 01 defines API contract, frontend generates types immediately
- **Mock Data**: Frontend PRDs use mock API responses until backend is ready
- **No Blocking**: Frontend PRDs (05, 07) do NOT require backend PRDs (04, 06) to start
- **Maximum Parallelism**: After PRD 01, all 6 feature PRDs can run simultaneously
- **Integration**: PRD 08 requires all features complete for final integration testing

## Current State

### What Exists
- ✅ Planning documents (PRDs, architecture docs)
- ✅ **8 PRDs structured for maximum parallel development** (see `planning/PRDs/`)
- ✅ **PRD 01 Foundation COMPLETE** - Backend and frontend projects initialized
- ✅ **PRD 02 Customer Backend COMPLETE** - Full Customer backend implementation
- ✅ **PRD 03 Customer Frontend COMPLETE** - Full Customer feature implementation
- ✅ Backend project structure (Spring Boot with Clean Architecture)
- ✅ Frontend project structure (React + Vite with MVVM pattern)
- ✅ OpenAPI spec with all Customer endpoints documented
- ✅ TypeScript types generated from OpenAPI
- ✅ Database configuration (H2 for dev, PostgreSQL for test/prod)
- ✅ Flyway migrations (V1__init.sql, V2__create_customers_table.sql)
- ✅ Global exception handler and error response format
- ✅ CORS configuration
- ✅ Security configuration (basic, OAuth deferred)
- ✅ Shared frontend components (Layout, LoadingSpinner, ErrorMessage, Pagination)
- ✅ API client setup with interceptors
- ✅ React Query and routing configured
- ✅ **Customer Domain Entity** (rich domain model with `validate()`, `updateDetails()`)
- ✅ **Customer DTOs** (CustomerRequest, CustomerResponse)
- ✅ **CustomerMapper** (MapStruct)
- ✅ **CustomerCommandService** (create, update, delete)
- ✅ **CustomerQueryService** (getById, getAll with pagination)
- ✅ **CustomerRepository** (JPA with `findByEmail()`)
- ✅ **CustomerController** (all 5 CRUD endpoints implemented)
- ✅ **Customer Integration Tests** (16 test scenarios, all passing)
- ✅ **Invoice Domain Entity** (rich domain model with `calculateTotal()`, `markAsSent()`, `applyPayment()`, etc.)
- ✅ **LineItem Value Object** (embeddable with `calculateSubtotal()`)
- ✅ **InvoiceStatus Enum** (DRAFT, SENT, PAID)
- ✅ **Invoice DTOs** (CreateInvoiceRequest, UpdateInvoiceRequest, InvoiceResponse, LineItemRequest, LineItemResponse)
- ✅ **InvoiceMapper** (MapStruct)
- ✅ **InvoiceCommandService** (create, update, markAsSent)
- ✅ **InvoiceQueryService** (getById, getAll, getByStatus, getByCustomerId)
- ✅ **InvoiceRepository** (JPA with custom query methods)
- ✅ **InvoiceController** (all 5 endpoints: create, get, list, update, markAsSent)
- ✅ **Invoice Integration Tests** (comprehensive test coverage)
- ✅ **Customer feature components** (CustomerList, CustomerForm, CustomerDeleteDialog)
- ✅ **Customer hooks** (useCustomers, useCustomerMutations, useCustomerViewModel)
- ✅ **Customer API client methods** (getCustomers, createCustomer, updateCustomer, deleteCustomer)
- ✅ **Customer types** (using OpenAPI generated types)
- ✅ **Invoice feature components** (InvoiceList, InvoiceForm, InvoiceDetails, LineItemForm, InvoiceStatusBadge)
- ✅ **Invoice hooks** (useInvoices, useInvoiceMutations, useInvoiceViewModel)
- ✅ **Invoice API client methods** (7 methods: get, filter, create, update, mark as sent)
- ✅ **Invoice types** (InvoiceStatus enum, Invoice, LineItem, request/response types)
- ✅ **Navigation** (Layout component with navigation bar and active state)
- ✅ **Testing Infrastructure** (Docker setup, endpoint test scripts)
- ✅ Memory bank structure and documentation
- ✅ Clear architectural vision and technical stack
- ✅ Parallel development strategy documented

### What's Missing (Feature Implementation)
- ❌ Domain entities (Payment) - *Customer ✅ complete, Invoice ✅ complete*
- ❌ Application services (Payment Command/Query services) - *Customer ✅ complete, Invoice ✅ complete*
- ❌ Repositories (PaymentRepository - CustomerRepository ✅ complete, InvoiceRepository ✅ complete)
- ❌ Database schema migrations (V4 for payments - V2 ✅ complete, V3 ✅ complete)
- ❌ Feature UI components (PaymentForm, etc. - Customer & Invoice components ✅ complete)
- ❌ Feature hooks and ViewModels (Payment - Customer & Invoice ✅ complete)
- ❌ Integration tests (Payment - Customer ✅ complete, Invoice ✅ complete)
- ❌ OAuth2 authentication (PRD 08)

## Key Considerations

### Performance
- API response times must be < 200ms ✅ (Customer endpoints validated)
- Use `@Transactional(readOnly = true)` for query services ✅ (CustomerQueryService implemented)
- Implement pagination from day 1 ✅ (Customer list endpoint has pagination)
- Consider database indexes on frequently queried fields ✅ (email index created)

### Code Quality
- Rich domain models (not anemic) ✅ (Customer entity has business logic)
- Clear separation of concerns (CQRS) ✅ (Command/Query services separated)
- Vertical slice organization ✅ (Customer feature organized across layers)
- Type safety throughout (TypeScript + Java) ✅

### Testing
- Integration tests with Testcontainers ✅ (CustomerIntegrationTest created)
- Test complete flows (Customer → Invoice → Payment)
- Verify domain logic (validate, updateDetails) ✅ (Customer domain logic tested)
- Performance verification ✅ (Customer endpoints < 200ms)

### AI Tool Usage
- Use AI tools intelligently as accelerators
- Maintain architectural integrity
- Document AI tool usage and prompts
- Focus on architectural guidance over code generation
