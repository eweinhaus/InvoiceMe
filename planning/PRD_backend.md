# Backend PRD: InvoiceMe API

## Overview
RESTful API backend for InvoiceMe invoicing system built with Spring Boot, implementing Domain-Driven Design (DDD), Command Query Responsibility Segregation (CQRS), and Vertical Slice Architecture (VSA).

**Design Philosophy**: Pragmatic implementation demonstrating architectural principles without over-engineering. Optimized for 5-7 day development timeline.

## Technical Stack

### Core Framework
- **Spring Boot**: `3.3.11` (LTS, stable, requires Java 17+)
- **Java**: `17` (LTS, widely supported)
- **Build Tool**: Maven (simpler for Spring Boot, better AI tooling support)

### Dependencies
```xml
<!-- Core Starters -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Database -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>

<!-- Code Generation & Utilities -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>

<!-- API Documentation -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

**Maven Compiler Plugin** (for MapStruct + Lombok): Configure annotation processors for MapStruct, Lombok, and `lombok-mapstruct-binding` (v0.2.0).

## Architecture Requirements

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
│   │       ├── CreateInvoiceRequest.java
│   │       ├── InvoiceResponse.java
│   │       └── LineItemRequest.java
│   ├── payment/
│   │   ├── PaymentCommandService.java
│   │   ├── PaymentQueryService.java
│   │   └── dto/
│   │       ├── PaymentRequest.java
│   │       └── PaymentResponse.java
│   └── mapper/                # DTOs ↔ Domain mappers (MapStruct)
│       ├── CustomerMapper.java      # @Mapper interface (auto-generated impl)
│       ├── InvoiceMapper.java       # @Mapper interface
│       └── PaymentMapper.java       # @Mapper interface
│
├── infrastructure/            # Infrastructure Layer
│   ├── persistence/
│   │   ├── CustomerRepository.java
│   │   ├── InvoiceRepository.java
│   │   └── PaymentRepository.java
│   ├── security/
│   │   └── SecurityConfig.java
│   └── config/
│       ├── OpenApiConfig.java
│       └── CorsConfig.java
│
└── presentation/              # Presentation Layer
    ├── rest/
    │   ├── CustomerController.java
    │   ├── InvoiceController.java
    │   ├── PaymentController.java
    │   └── AuthController.java
    └── exception/
        └── GlobalExceptionHandler.java
```

### CQRS Implementation (Simplified)
Instead of individual Command/Query handlers, use service-level separation:

**Command Service** (Write Operations):
```java
@Service
@Transactional
public class CustomerCommandService {
    public CustomerResponse createCustomer(CustomerRequest request) { }
    public CustomerResponse updateCustomer(UUID id, CustomerRequest request) { }
    public void deleteCustomer(UUID id) { }
}
```

**Query Service** (Read Operations):
```java
@Service
@Transactional(readOnly = true)
public class CustomerQueryService {
    public CustomerResponse getById(UUID id) { }
    public List<CustomerResponse> getAll() { }
}
```

**Why This Approach?**
- Demonstrates CQRS principle (separation of concerns)
- Avoids over-engineering with individual handlers
- Faster to implement for 5-7 day timeline
- Still maintains clear boundaries

## Domain Model Requirements

### Customer Domain
- **Rich Domain Model**: Customer entity with business logic
- **Properties**: id, name, email, address, phone, createdAt, updatedAt
- **Behavior**: `validate()`, `updateDetails()`

### Invoice Domain
- **Rich Domain Model**: Invoice entity with business logic
- **Properties**: id, customerId, status, lineItems, totalAmount, balance, createdAt, updatedAt
- **Behavior**: 
  - `calculateTotal()`
  - `calculateBalance()`
  - `addLineItem(description, quantity, unitPrice)`
  - `canBeMarkedAsSent()` (business rule validation)
  - `applyPayment(amount)` (updates balance, may change status to PAID)
- **Line Items**: description, quantity, unitPrice, subtotal
- **Status Transitions**: DRAFT → SENT → PAID (enforce in domain)

### Payment Domain
- **Rich Domain Model**: Payment entity with business logic
- **Properties**: id, invoiceId, amount, paymentDate, createdAt
- **Behavior**: `validateAmount()`, `applyToInvoice()`

## API Endpoints

### Customer Endpoints
- `POST /api/customers` - Create customer
- `GET /api/customers/{id}` - Get customer by ID
- `GET /api/customers?page=0&size=20&sort=name,asc` - List customers (paginated)
- `PUT /api/customers/{id}` - Update customer
- `DELETE /api/customers/{id}` - Delete customer

### Invoice Endpoints
- `POST /api/invoices` - Create invoice (DRAFT)
- `GET /api/invoices/{id}` - Get invoice by ID
- `GET /api/invoices?status={status}&page=0&size=20` - List invoices by status (paginated)
- `GET /api/invoices?customerId={id}&page=0&size=20` - List invoices by customer (paginated)
- `PUT /api/invoices/{id}` - Update invoice (only if DRAFT)
- `POST /api/invoices/{id}/send` - Mark invoice as SENT
- `POST /api/invoices/{id}/payments` - Record payment (applies to invoice)

### Payment Endpoints
- `POST /api/payments` - Record payment
- `GET /api/payments/{id}` - Get payment by ID
- `GET /api/payments?invoiceId={id}&page=0&size=20` - List payments for invoice (paginated)

## CQRS Implementation Details

### Command Services (Write Operations)
- Annotated with `@Service` and `@Transactional`
- Modify state and persist changes
- Return DTOs (not domain entities)
- Encapsulate business logic by calling domain methods
- Example: `createCustomer()`, `updateInvoice()`, `recordPayment()`

### Query Services (Read Operations)
- Annotated with `@Service` and `@Transactional(readOnly = true)`
- Read-only operations, no side effects
- Return DTOs (not domain entities)
- Use repository queries
- Example: `getById()`, `getAll()`, `findByStatus()`

### Domain Events
**Decision**: Skip domain events for this project to reduce complexity. Direct method calls are sufficient for the scope.

## Authentication & Security

### Google OAuth2 with Session-Based Auth

**Authentication Flow**:
1. Frontend redirects to: `http://localhost:8080/oauth2/authorization/google`
2. Spring Security handles OAuth flow with Google
3. On success, creates server-side session (JSESSIONID cookie)
4. Redirects to frontend: `http://localhost:5173/dashboard`
5. All subsequent API requests use session cookie (httpOnly, secure)

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

**Security Config**: `SecurityFilterChain` with OAuth2 login, CORS, and permit `/api/auth/**`. **Auth Endpoint**: `GET /api/auth/user` returns user info from `@AuthenticationPrincipal OAuth2User`.

## Database Schema & Configuration

### Database Schema
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

-- Line Items (via @ElementCollection)
CREATE TABLE invoice_line_items (
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description VARCHAR(500) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(19,2) NOT NULL CHECK (unit_price >= 0),
    line_order INTEGER NOT NULL,
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

### Database Migrations (Flyway)
**Structure**: `src/main/resources/db/migration/V1__*.sql` through `V4__*.sql` for schema versioning. **Config**: Enable Flyway with `baseline-on-migrate: true`. Benefits: Version-controlled, repeatable, works with H2/PostgreSQL.

### MapStruct Mapper Example
**Interface**: `@Mapper(componentModel = "spring")` interface with `toResponse()`, `toEntity()`, `toResponseList()` methods. **Usage**: Inject mapper into service, call `mapper.toResponse(entity)`. **Benefits**: Zero boilerplate, compile-time safety, high performance, works with Lombok.

### Pagination Support
**Service**: Return `Page<CustomerResponse>` using `repository.findAll(pageable).map(mapper::toResponse)`. **Controller**: Use `@PageableDefault(size = 20, sort = "name") Pageable`. **Response**: Spring Data `Page` format with `content`, `pageable`, `totalElements`, `totalPages`, `first`, `last`.

### JPA Entity Configuration
**LineItem**: `@Embeddable` value object with `calculateSubtotal()`. **Invoice**: `@ElementCollection` with `@CollectionTable(name = "invoice_line_items")` and `@OrderColumn(name = "line_order")` for ordered line items.

### Environment Configuration
**Dev (H2)**: `jdbc:h2:mem:invoiceme`, `ddl-auto: update`, H2 console enabled. **Prod (PostgreSQL)**: Use env vars for connection, `ddl-auto: validate`, PostgreSQL dialect.

## Testing Requirements

### Integration Tests
- Use `@SpringBootTest` for full application context
- Use Testcontainers with PostgreSQL for database tests
- Test complete flows:
  1. Create Customer → Create Invoice → Record Payment → Verify Balance
  2. Invoice lifecycle: Draft → Sent → Paid
  3. Payment validation (cannot exceed invoice balance)

### Test Structure
Use `@SpringBootTest` with `@Testcontainers` and `PostgreSQLContainer` for integration tests.

## Performance Requirements
- API response time < 200ms for standard CRUD operations
- Use `@Transactional` appropriately
- Pagination implemented for all list endpoints (prevents performance issues)
- Use `@Transactional(readOnly = true)` for query services
- Consider database indexes on frequently queried fields (customerId, status)

### Performance Testing
Use `StopWatch` in tests to verify endpoints respond in < 200ms.

## Error Handling

### Consistent Error Envelope
All API errors return a standardized format:

```java
public class ErrorResponse {
    private LocalDateTime timestamp;
    private String path;
    private int status;
    private String error;
    private String message;
    private Map<String, Object> details; // Optional validation errors
}
```

**Example**: `{timestamp, path, status, error, message, details?}` - details contains validation errors for 400 responses.

### Global Exception Handler
`@RestControllerAdvice` handles: `EntityNotFoundException` → 404, `MethodArgumentNotValidException` → 400 with details, `BusinessRuleViolationException` → 422.

**Error Codes**:
- `400` - Bad Request (validation errors)
- `401` - Unauthorized (authentication required)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found (resource doesn't exist)
- `422` - Unprocessable Entity (business rule violations)
- `500` - Internal Server Error (unexpected errors)

## API Documentation
- SpringDoc OpenAPI (Swagger) for API documentation
- Accessible at `/swagger-ui.html`

## Deployment (AWS)
- Package as executable JAR
- Deploy to AWS Elastic Beanstalk or ECS Fargate
- Environment variables for configuration
- RDS PostgreSQL for production database

## Success Criteria
- ✅ All domain entities have rich behavior (not anemic)
- ✅ Clear separation between Commands and Queries
- ✅ Vertical slices organized by feature
- ✅ Integration tests pass
- ✅ API response times < 200ms
- ✅ Google OAuth authentication working
- ✅ Invoice lifecycle properly enforced
- ✅ Payment balance calculations correct

