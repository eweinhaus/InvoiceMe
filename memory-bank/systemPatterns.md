# System Patterns: InvoiceMe

## Architecture Overview

InvoiceMe implements a multi-layered architecture following DDD, CQRS, VSA, and Clean Architecture principles.

## Backend Architecture

### Layer Structure
```
Domain Layer (Inner) → Application Layer → Infrastructure Layer → Presentation Layer (Outer)
```

### Domain Layer (DDD)
**Location**: `src/main/java/com/invoiceme/domain/`

**Purpose**: Core business logic, rich domain models

**Entities**:
- ✅ `Customer`: Rich domain entity with validation logic (`validate()`, `updateDetails()`) - **IMPLEMENTED**
- ✅ `Invoice`: Complex entity with business methods (`calculateTotal()`, `applyPayment()`, `markAsSent()`, `canBeEdited()`, `updateLineItems()`) - **IMPLEMENTED**
- `Payment`: Entity with payment validation logic
- ✅ `LineItem`: Value object (`@Embeddable`) with `calculateSubtotal()` - **IMPLEMENTED**

**Key Pattern**: Rich domain models with business logic methods, not anemic data containers

### Application Layer (CQRS)
**Location**: `src/main/java/com/invoiceme/application/`

**Pattern**: Simplified CQRS with service-level separation

**Command Services** (Write Operations):
- ✅ `CustomerCommandService` - Create, Update, Delete - **IMPLEMENTED**
- ✅ `InvoiceCommandService` - Create, Update, Mark as Sent - **IMPLEMENTED**
- `PaymentCommandService` - Record Payment

**Query Services** (Read Operations):
- ✅ `CustomerQueryService` - Get by ID, List all - **IMPLEMENTED**
- ✅ `InvoiceQueryService` - Get by ID, List by status/customer - **IMPLEMENTED**
- `PaymentQueryService` - Get by ID, List by invoice

**DTOs**: Request/Response DTOs for boundary crossing
**Mappers**: MapStruct interfaces for DTO ↔ Domain conversion

### Infrastructure Layer
**Location**: `src/main/java/com/invoiceme/infrastructure/`

**Components**:
- **Persistence**: ✅ `CustomerRepository` (JPA) - **IMPLEMENTED**
- **Persistence**: ✅ `InvoiceRepository` (JPA with custom query methods) - **IMPLEMENTED**
- **Persistence**: `PaymentRepository` (to be implemented)
- **Security**: `SecurityConfig` for OAuth2 and session management (✅ CORS configured, OAuth2 ready), `DevAuthConfig` for dev mode authentication (✅ implemented)
- **Config**: `OpenApiConfig` (✅ configured), `CorsConfig` (✅ configured)

### Presentation Layer
**Location**: `src/main/java/com/invoiceme/presentation/`

**Components**:
- **REST Controllers**: ✅ `CustomerController` (fully implemented with all 5 CRUD endpoints) - **COMPLETE**
- **REST Controllers**: ✅ `InvoiceController` (fully implemented with all 5 endpoints: create, get, list, update, mark as sent) - **COMPLETE**
- **REST Controllers**: ✅ `PaymentController` (fully implemented) - **COMPLETE**
- **REST Controllers**: ✅ `AuthController` (fully implemented with login, logout, user info endpoints) - **COMPLETE**
- **Exception Handling**: `GlobalExceptionHandler` with standardized `ErrorResponse` DTO (✅ implemented)

## Frontend Architecture

### MVVM Pattern (React-Style)
**View**: React Components (presentational, minimal logic)
**ViewModel**: Custom hooks encapsulating business logic
**Model**: React Query (server state) + Axios (HTTP client)

**Example Structure**:
```typescript
// ViewModel = Custom Hook
useCustomerViewModel() {
  - Uses React Query hooks (useCustomers, useCreateCustomer)
  - Encapsulates business logic (sorting, transformations)
  - Returns data and actions for View
}

useInvoiceViewModel() {
  - Uses React Query hooks (useInvoices, useInvoiceMutations)
  - Manages filter state (status, customer)
  - Manages pagination state
  - Conditionally calls query hooks based on filters
  - Returns data, filters, actions, and setters
}

// View = Component
CustomerList() {
  - Uses useCustomerViewModel()
  - Renders UI based on ViewModel data
}

InvoicesPage() {
  - Uses useInvoiceViewModel()
  - Manages dialog state
  - Renders InvoiceList, InvoiceForm, InvoiceDetails
  - Handles filter changes and actions
}
```

### Vertical Slice Architecture
**Location**: `src/features/`

**Structure** (per feature):
```
features/customers/
  ├── components/     # UI components
  ├── hooks/         # ViewModels (useCustomerViewModel)
  ├── types/         # TypeScript types
  └── pages/         # Page components
```

**Features**:
- ✅ `customers/` - Customer management (Backend + Frontend COMPLETE)
- ✅ `invoices/` - Invoice management (Backend + Frontend COMPLETE)
- `payments/` - Payment management (to be implemented)

### Shared Components
**Location**: `src/components/`

- `ui/` - shadcn/ui components (Badge, Button, Card, Dialog, Form, Input, Label, Select, Table)
- `layout/` - Layout component with Header and Navigation (✅ implemented with active state)
- `common/` - LoadingSpinner (✅), ErrorMessage (✅), Pagination (✅)

## Key Design Patterns

### 1. CQRS (Simplified)
- **Command Services**: `@Transactional`, modify state, return DTOs
- **Query Services**: `@Transactional(readOnly = true)`, read-only, return DTOs
- **Rationale**: Demonstrates CQRS principle without over-engineering individual handlers

### 2. DDD Rich Domain Models
- Business logic in domain entities
- Methods like `calculateTotal()`, `applyPayment()`, `canBeMarkedAsSent()`
- No anemic domain models

### 3. Vertical Slice Architecture
- Feature-based organization
- Each slice: Controller → Service → Domain → Repository
- Self-contained, enables parallel development
- **Parallel Development Strategy**: Frontend and backend for same feature can be developed completely independently using mock data

### 4. Dependency Inversion
- Outer layers depend on inner layers
- Domain layer has no dependencies
- Application layer depends on domain interfaces
- Infrastructure implements domain interfaces

### 5. DTO Pattern
- DTOs for all API boundaries
- MapStruct for automatic mapping
- Prevents domain entities from leaking to presentation layer

## Component Relationships

### Backend Flow
```
Controller → Command/Query Service → Domain Entity → Repository → Database
```

### Frontend Flow
```
Page Component → ViewModel Hook → React Query → Axios → API
```

## Data Flow

### Create Invoice Flow
1. Frontend: User fills form → `InvoiceForm` component
2. ViewModel: `useInvoiceViewModel` validates and transforms data
3. React Query: `useCreateInvoice` mutation
4. Axios: POST `/api/invoices`
5. Backend Controller: `InvoiceController.create()`
6. Command Service: `InvoiceCommandService.create()`
7. Domain: `Invoice` entity with business logic
8. Repository: Persist to database
9. Response: Invoice DTO returned
10. Frontend: Optimistic update, then refetch

### Payment Application Flow
1. Frontend: User records payment → `PaymentForm`
2. ViewModel: `usePaymentViewModel` validates amount
3. React Query: `useRecordPayment` mutation
4. Axios: POST `/api/payments`
5. Backend Controller: `PaymentController.create()`
6. Command Service: `PaymentCommandService.recordPayment()`
7. Domain: `Payment.validateAmount()`, `Invoice.applyPayment()`
8. Repository: Persist payment, update invoice
9. Response: Payment DTO returned
10. Frontend: Update invoice balance, show success

## State Management

### Backend State
- **Database**: PostgreSQL/H2 for persistence
- **Session**: Spring Security session store for authentication

### Frontend State
- **Server State**: React Query (caching, invalidation, optimistic updates)
- **Client State**: React state / Zustand (UI state, modals, filters)
- **Authentication State**: React Query via `useAuth` hook (user info, authentication status)

## Error Handling Pattern

### Backend
- `GlobalExceptionHandler` with `@RestControllerAdvice`
- Standardized `ErrorResponse` DTO
- HTTP status codes: 400, 401, 403, 404, 422, 500

### Frontend
- Axios interceptor for 401 → redirect to login (clears React Query cache)
- React Query error handling with toast notifications
- Error Boundary for component errors
- Authentication error handling (OAuth errors, session expiration)

## Parallel Development Patterns

### Contract-First Development
- **API Contract**: Defined in PRD 01 via OpenAPI spec
- **Type Generation**: Frontend generates TypeScript types from OpenAPI immediately
- **Enables**: Frontend can start development before backend is ready

### Mock Data Strategy
- **Frontend Independence**: Frontend PRDs (03, 05, 07) can start immediately after PRD 01
- **Mock Implementation**: Use MSW, React Query mocks, or simple stubs for API responses
- **Seamless Transition**: Switch from mocks to real API when backend PRDs (02, 04, 06) complete
- **Enables**: All 6 feature PRDs can run in parallel after PRD 01

### Dependency Minimization
- **No Frontend-Backend Blocking**: Frontend does NOT wait for corresponding backend
- **Feature Independence**: Each feature (Customer, Invoice, Payment) is self-contained
- **Cross-Feature Dependencies**: Handled via mock data (e.g., Invoice frontend uses mock customer list)
- **Final Integration**: PRD 08 requires all features complete for E2E testing

### PRD Structure for Parallelization
- **PRD 01**: Foundation (blocks all, must be first)
- **PRDs 02-07**: Feature PRDs (can all run in parallel after PRD 01)
  - Backend PRDs: 02 (Customer), 04 (Invoice), 06 (Payment)
  - Frontend PRDs: 03 (Customer), 05 (Invoice), 07 (Payment)
- **PRD 08**: Integration (requires all features complete)

**Maximum Parallelism**: With 6 developers, all feature PRDs can run simultaneously after PRD 01, reducing timeline from 7 days to 3-4 days.

