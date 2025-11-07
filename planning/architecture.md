# InvoiceMe Architecture & Tech Stack

## Overview

Full-stack ERP invoicing system implementing **Domain-Driven Design (DDD)**, **Command Query Responsibility Segregation (CQRS)**, **Vertical Slice Architecture (VSA)**, and **Clean Architecture**.

**Design Philosophy**: Pragmatic implementation that demonstrates architectural principles without over-engineering. Optimized for 5-7 day development timeline.

## Technology Stack

### Backend
- **Spring Boot**: `3.3.11` (Java 17, Maven)
- **Spring Data JPA**: Data persistence
- **Spring Security**: Session-based authentication with Google OAuth2
- **Database**: H2 (dev) / PostgreSQL (test/prod)
- **SpringDoc OpenAPI**: API documentation (`/swagger-ui.html`)

### Frontend
- **React**: `^18.3.1` (TypeScript `^5.5.0`)
- **Vite**: `^5.4.0` (build tool)
- **shadcn/ui + Tailwind CSS**: UI components
- **React Query (TanStack Query)**: Server state management
- **React Hook Form + Zod**: Form handling & validation
- **Axios**: HTTP client
- **Package Manager**: npm

### Infrastructure (Optional - Document Only)
- **AWS S3 + CloudFront**: Frontend hosting
- **AWS Elastic Beanstalk**: Backend hosting
- **AWS RDS PostgreSQL**: Production database
- **Note**: Focus on local development; deployment is documented but not required

## Architecture Principles

### 1. Domain-Driven Design (DDD)
- **Rich domain models** with business logic in entities
- Domain methods: `calculateTotal()`, `applyPayment()`, `markAsSent()`, `canBeMarkedAsSent()`
- **No anemic domain models** - business rules live in domain layer
- Value objects where appropriate (e.g., `Money`, `Email`)

### 2. Command Query Responsibility Segregation (CQRS)
- **Simplified CQRS**: Service-level separation, not individual handlers
- **Command Services**: Handle write operations (Create, Update, Delete)
- **Query Services**: Handle read operations (Get, List)
- Clear separation allows independent optimization
- **Pragmatic approach**: No event sourcing or separate read models

### 3. Vertical Slice Architecture (VSA)
- Feature-based organization (Customers, Invoices, Payments)
- Each feature contains: Controller → Service → Domain → Repository
- Self-contained slices enable parallel development
- Shared infrastructure (security, config) in separate package

### 4. Clean Architecture (Simplified)
- **Domain Layer**: Entities with business logic (no dependencies)
- **Application Layer**: Services (CQRS), DTOs, mappers
- **Infrastructure Layer**: JPA repositories, security config
- **Presentation Layer**: REST controllers, exception handling
- Dependency rule: outer layers depend on inner layers

## Backend Architecture

### Simplified Layer Structure
```
src/main/java/com/invoiceme/
├── domain/                    # Domain Layer (DDD)
│   ├── customer/
│   │   └── Customer.java      # Rich domain entity
│   ├── invoice/
│   │   ├── Invoice.java       # Rich domain entity
│   │   ├── InvoiceStatus.java # Enum: DRAFT, SENT, PAID
│   │   └── LineItem.java      # Value object (@Embeddable)
│   └── payment/
│       └── Payment.java       # Rich domain entity
│
├── application/               # Application Layer (CQRS Services)
│   ├── customer/
│   │   ├── CustomerCommandService.java  # Write operations
│   │   ├── CustomerQueryService.java    # Read operations
│   │   └── dto/
│   │       ├── CustomerRequest.java
│   │       └── CustomerResponse.java
│   ├── invoice/
│   │   ├── InvoiceCommandService.java
│   │   ├── InvoiceQueryService.java
│   │   └── dto/
│   ├── payment/
│   │   ├── PaymentCommandService.java
│   │   ├── PaymentQueryService.java
│   │   └── dto/
│   └── mapper/                # DTOs ↔ Domain mappers
│
├── infrastructure/            # Infrastructure Layer
│   ├── persistence/
│   │   ├── CustomerRepository.java
│   │   ├── InvoiceRepository.java
│   │   └── PaymentRepository.java
│   ├── security/
│   │   └── SecurityConfig.java
│   └── config/
│       └── OpenApiConfig.java
│
└── presentation/              # Presentation Layer
    ├── rest/
    │   ├── CustomerController.java
    │   ├── InvoiceController.java
    │   └── PaymentController.java
    └── exception/
        └── GlobalExceptionHandler.java
```

### Domain Entities (Rich Models)

**Customer Entity**
```java
@Entity
public class Customer {
    @Id @GeneratedValue
    private UUID id;
    private String name;
    private String email;
    private String address;
    private String phone;
    
    // Business logic methods
    public void validate() { /* validation rules */ }
    public void updateDetails(String name, String email, ...) { /* update logic */ }
}
```

**Invoice Entity** (Most Complex)
```java
@Entity
public class Invoice {
    @Id @GeneratedValue
    private UUID id;
    @ManyToOne
    private Customer customer;
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;
    @ElementCollection
    private List<LineItem> lineItems;
    private BigDecimal totalAmount;
    private BigDecimal balance;
    
    // Business logic methods (core DDD)
    public BigDecimal calculateTotal() { /* sum line items */ }
    public BigDecimal calculateBalance() { /* total - payments */ }
    public void addLineItem(String desc, int qty, BigDecimal price) { /* add item */ }
    public boolean canBeMarkedAsSent() { /* validation */ }
    public void markAsSent() { /* state transition */ }
    public void applyPayment(BigDecimal amount) { /* update balance */ }
}
```

**Payment Entity**
```java
@Entity
public class Payment {
    @Id @GeneratedValue
    private UUID id;
    @ManyToOne
    private Invoice invoice;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    
    // Business logic methods
    public void validate(Invoice invoice) { /* cannot exceed balance */ }
}
```

**LineItem** (Value Object)
```java
@Embeddable
public class LineItem {
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    
    public BigDecimal calculateSubtotal() { 
        return unitPrice.multiply(BigDecimal.valueOf(quantity)); 
    }
}
```

### Key Domain Rules
- **Invoice lifecycle**: DRAFT → SENT → PAID (enforced in domain)
- **Balance calculation**: `balance = totalAmount - sum(payments)`
- **Payment validation**: Payments cannot exceed invoice balance
- **Status transitions**: Only valid transitions allowed (enforced by domain methods)
- **Line items**: Stored as `@ElementCollection` (embedded in Invoice table)

## Frontend Architecture

### MVVM Pattern (React-Style)
- **View**: React Components (presentational, minimal logic)
- **ViewModel**: Custom hooks encapsulating business logic
- **Model**: React Query (server state) + Axios (HTTP client)

**Implementation**:
```typescript
// ViewModel = Custom Hook
function useCustomerViewModel() {
  const { data, isLoading } = useCustomers();      // Model (React Query)
  const { mutate } = useCreateCustomer();          // Model (mutations)
  
  // ViewModel logic: transformation, validation, business rules
  const transformedData = useMemo(() => /* transform */, [data]);
  
  return { customers: transformedData, isLoading, createCustomer: mutate };
}

// View = Component
function CustomerList() {
  const vm = useCustomerViewModel();  // Use ViewModel
  return <div>{/* render vm.customers */}</div>;
}
```

### Feature Organization (VSA)
```
features/customers/
  ├── components/        # UI components
  ├── hooks/            # ViewModels (useCustomerViewModel)
  ├── types/            # TypeScript types
  └── pages/            # Page components
```

## API Endpoints

### Customers
- `POST /api/customers` - Create
- `GET /api/customers/{id}` - Get by ID
- `GET /api/customers` - List all
- `PUT /api/customers/{id}` - Update
- `DELETE /api/customers/{id}` - Delete

### Invoices
- `POST /api/invoices` - Create (DRAFT)
- `GET /api/invoices/{id}` - Get by ID
- `GET /api/invoices?status={status}` - List by status
- `GET /api/invoices?customerId={id}` - List by customer
- `PUT /api/invoices/{id}` - Update (only if DRAFT)
- `POST /api/invoices/{id}/send` - Mark as SENT
- `POST /api/invoices/{id}/payments` - Record payment

### Payments
- `POST /api/payments` - Record payment
- `GET /api/payments/{id}` - Get by ID
- `GET /api/payments?invoiceId={id}` - List by invoice

## Authentication

### Google OAuth2 with Session-Based Auth

**Flow**:
1. User clicks "Sign in with Google" in React app
2. Frontend redirects to: `http://localhost:8080/oauth2/authorization/google`
3. Spring Security handles OAuth flow with Google
4. On success, creates server-side session (JSESSIONID cookie)
5. Redirects to frontend: `http://localhost:5173/dashboard`
6. Frontend uses session cookie for all API requests (httpOnly, secure)

**Backend Configuration** (`application.yml`):
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: [openid, profile, email]
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
```

**Frontend Auth Check**:
```typescript
// Check if user is authenticated
const { data: user, isLoading } = useQuery({
  queryKey: ['auth', 'user'],
  queryFn: () => axios.get('/api/auth/user'),
  retry: false,
});
```

**Why Session-Based?**
- Simpler than JWT for this project
- httpOnly cookies prevent XSS attacks
- No token refresh logic needed
- Spring Security handles everything

## Database Schema

### Tables & Relationships
```sql
-- Customers
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    address TEXT,
    phone VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Invoices
CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'SENT', 'PAID')),
    total_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    balance DECIMAL(19,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status)
);

-- Line Items (embedded via @ElementCollection)
CREATE TABLE invoice_line_items (
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description VARCHAR(500) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(19,2) NOT NULL CHECK (unit_price >= 0),
    line_order INTEGER NOT NULL,  -- Preserve order
    PRIMARY KEY (invoice_id, line_order)
);

-- Payments
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE RESTRICT,
    amount DECIMAL(19,2) NOT NULL CHECK (amount > 0),
    payment_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_invoice_id (invoice_id)
);
```

### Database Strategy
- **Development**: H2 in-memory (auto-configured, zero setup)
- **Testing**: PostgreSQL via Testcontainers (real DB for integration tests)
- **Production**: PostgreSQL on AWS RDS (documented, optional to implement)

### JPA Configuration
- Use `spring.jpa.hibernate.ddl-auto=update` for dev (H2)
- Use `spring.jpa.hibernate.ddl-auto=validate` for prod (PostgreSQL)
- Consider Flyway/Liquibase for production migrations (optional)

## Deployment (AWS) - Document Only

**Note**: Focus on local development. Deployment is documented but not required for 5-7 day timeline.

- **Frontend**: S3 + CloudFront (Vite build: `npm run build`)
- **Backend**: Elastic Beanstalk (executable JAR: `mvn package`)
- **Database**: RDS PostgreSQL
- **Security**: IAM roles, Secrets Manager for OAuth credentials

**Deployment Steps** (documented in README):
1. Build frontend: `npm run build` → upload `dist/` to S3
2. Build backend: `mvn package` → deploy JAR to Elastic Beanstalk
3. Configure environment variables in Elastic Beanstalk
4. Set up RDS PostgreSQL instance
5. Update OAuth redirect URIs in Google Cloud Console

## Testing Strategy

### Integration Tests (Required)
Use `@SpringBootTest` with Testcontainers for real PostgreSQL testing.

**Key Test Flows**:
1. **Complete Payment Flow**: Create Customer → Create Invoice → Record Payment → Verify Balance
2. **Invoice Lifecycle**: Draft → Sent → Paid (verify state transitions)
3. **Payment Validation**: Attempt to exceed invoice balance (should fail)
4. **Domain Logic**: Test `calculateTotal()`, `calculateBalance()`, `applyPayment()`

**Example Test Structure**:
```java
@SpringBootTest
@Testcontainers
class InvoicePaymentFlowIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Test
    void shouldCompleteFullPaymentFlow() {
        // Create customer, invoice, payment
        // Verify balance calculations
        // Verify status transitions
    }
}
```

### Performance Verification
- **API Latency**: Verify < 200ms for CRUD operations (measure in tests)
- **UI Responsiveness**: Manual testing, use Chrome DevTools
- Use Spring Boot Actuator metrics for monitoring

## Quick Wins & Optimizations

### Backend Quick Wins

**1. MapStruct + Lombok for DTO Mapping**
- **Why**: Eliminates 80% of boilerplate mapper code
- **Impact**: Saves 4-6 hours of manual mapping implementation
- **Setup**: Add MapStruct annotation processor + Lombok
- **Benefit**: Auto-generated mappers, cleaner code, fewer bugs

**2. Flyway for Database Migrations**
- **Why**: Repeatable, version-controlled schema changes
- **Impact**: Prevents "works on my machine" database issues
- **Setup**: Add Flyway dependency, create migration files
- **Benefit**: Consistent database state across dev/test/prod

**3. Consistent Error Envelope**
- **Why**: Standardized API error responses improve frontend error handling
- **Impact**: Better UX, easier debugging
- **Setup**: `@RestControllerAdvice` with structured error DTO
- **Benefit**: Frontend can handle errors uniformly

**4. Pagination from Day 1**
- **Why**: Adding pagination later is painful; doing it upfront is trivial
- **Impact**: Prevents performance issues as data grows
- **Setup**: Spring Data's `Pageable` interface
- **Benefit**: Scalable list endpoints from the start

**5. OpenAPI Code Generation**
- **Why**: Auto-generate TypeScript types from backend OpenAPI spec
- **Impact**: Eliminates manual type definitions, ensures type safety
- **Setup**: Generate OpenAPI spec, use `openapi-typescript` or `swagger-typescript-api`
- **Benefit**: Frontend types always match backend API

### Frontend Quick Wins

**1. React Query Optimistic Updates**
- **Why**: Instant UI feedback makes app feel snappy
- **Impact**: Better perceived performance
- **Setup**: Use `onMutate` in React Query mutations
- **Benefit**: Professional UX without extra complexity

**2. OpenAPI Type Generation**
- **Why**: TypeScript types generated from backend spec
- **Impact**: Zero manual type maintenance, guaranteed type safety
- **Setup**: Generate types during build process
- **Benefit**: Types always match backend, catches breaking changes

**3. Pagination Components**
- **Why**: Reusable pagination UI component
- **Impact**: Consistent UX across all list pages
- **Setup**: Create generic `Pagination` component with shadcn/ui
- **Benefit**: One component, multiple uses

### Testing Quick Wins

**1. Performance Test Automation**
- **Why**: Verify < 200ms requirement automatically
- **Impact**: Catches performance regressions early
- **Setup**: Simple Spring Boot test with `StopWatch` or use k6
- **Benefit**: Confidence in performance requirements

**2. Postman Collection**
- **Why**: Reusable API tests, easy demo preparation
- **Impact**: Faster manual testing, better demo flow
- **Setup**: Export Postman collection with all endpoints
- **Benefit**: Professional API testing setup

### Implementation Priority

**Must Have (Day 1)**:
- MapStruct + Lombok
- OpenAPI spec generation
- Consistent error envelope
- Pagination support

**Should Have (Day 2-3)**:
- Flyway migrations
- OpenAPI type generation (frontend)
- React Query optimistic updates

**Nice to Have (Day 4-5)**:
- Performance test automation
- Postman collection export

## Development Strategy

### Incremental Development (Realistic Timeline)

**Day 1: Foundation & API Contract**
- Set up Spring Boot project with dependencies
- Set up React + Vite project with shadcn/ui
- Define API contract (OpenAPI spec)
- Configure H2 database
- Basic security setup (can defer OAuth to Day 5)

**Day 2: Customer Feature (Full Stack)**
- Backend: Customer entity, service, controller, repository
- Frontend: Customer page, list, form, API integration
- Test: Customer CRUD operations
- **Deliverable**: Working customer management

**Day 3: Invoice Feature (Backend Heavy)**
- Backend: Invoice entity with line items, service, controller
- Backend: Implement domain logic (calculateTotal, lifecycle)
- Frontend: Basic invoice list and create form
- Test: Invoice creation and status transitions
- **Deliverable**: Can create and manage invoices

**Day 4: Payment Feature & Integration**
- Backend: Payment entity, service, controller
- Backend: Payment application logic (applyPayment)
- Frontend: Payment form and list
- Test: Full payment flow integration test
- **Deliverable**: Complete invoice → payment flow

**Day 5: Polish & Authentication**
- Implement Google OAuth (or mock if time-constrained)
- UI polish: loading states, error handling, validation
- Frontend: Invoice details view, filters
- Test: Additional edge cases
- **Deliverable**: Feature-complete application

**Day 6: Testing & Documentation**
- Run all integration tests
- Performance verification (< 200ms API)
- Write technical writeup (architecture decisions)
- Record demo video
- **Deliverable**: Tested, documented application

**Day 7: Buffer & Final Polish**
- Fix any remaining bugs
- Code cleanup and comments
- README with setup instructions
- Prepare for presentation
- **Deliverable**: Submission-ready project

### Development Tools
- **Postman/Insomnia**: Backend API testing
- **H2 Console**: Database inspection (`/h2-console`)
- **Swagger UI**: API documentation (`/swagger-ui.html`)
- **React DevTools**: Frontend debugging
- **Chrome DevTools**: Network/performance monitoring

## Success Criteria

✅ Rich domain models (not anemic)  
✅ CQRS separation (Commands vs Queries)  
✅ Vertical slices by feature  
✅ Integration tests passing  
✅ API < 200ms response time  
✅ Google OAuth working  
✅ Invoice lifecycle enforced  
✅ Payment balance calculations correct  
✅ MVVM pattern in frontend  
✅ Responsive, accessible UI  

