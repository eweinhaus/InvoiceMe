# PRD 04: Invoice Feature - Backend

## Overview
**Priority**: 🟢 Can run in parallel with PRDs 02, 03, 05, 06, 07  
**Dependencies**: PRD 01 (Foundation), PRD 02 (Customer Backend) - for customer reference  
**Enables**: PRD 05 (Invoice Frontend)

This PRD implements the complete backend for Invoice management with rich domain logic, lifecycle management, and line items support.

## Objectives
1. Implement Invoice domain entity with rich business logic
2. Create Invoice Command and Query services (CQRS)
3. Implement Invoice REST API endpoints with lifecycle management
4. Set up database persistence for Invoices and Line Items
5. Create integration tests for Invoice operations and lifecycle

## Domain Layer

### InvoiceStatus Enum
- [ ] Create `InvoiceStatus` enum: `DRAFT`, `SENT`, `PAID`
- [ ] Location: `domain/invoice/`

### LineItem Value Object
- [ ] Create `LineItem` as `@Embeddable` class
- [ ] Properties: `description`, `quantity`, `unitPrice`
- [ ] Rich domain method: `calculateSubtotal()` → `quantity * unitPrice`
- [ ] Validation: `quantity > 0`, `unitPrice >= 0`

### Invoice Entity
- [ ] Create `Invoice` entity in `domain/invoice/` package
- [ ] Properties:
  - `id` (UUID)
  - `customer` (`@ManyToOne` to Customer)
  - `status` (InvoiceStatus enum)
  - `lineItems` (`@ElementCollection` of LineItem)
  - `totalAmount` (BigDecimal)
  - `balance` (BigDecimal)
  - `createdAt`, `updatedAt` (timestamps)
- [ ] Rich domain methods:
  - `calculateTotal()` - sums all line item subtotals
  - `calculateBalance()` - `totalAmount - sum(payments)` (requires payment query)
  - `addLineItem(description, quantity, unitPrice)` - adds line item, recalculates total
  - `canBeMarkedAsSent()` - validates: has line items, total > 0, status is DRAFT
  - `markAsSent()` - transitions status DRAFT → SENT (with validation)
  - `applyPayment(amount)` - updates balance, may transition to PAID if balance = 0
  - `canBeEdited()` - returns true if status is DRAFT
- [ ] JPA configuration:
  - `@ElementCollection` with `@CollectionTable(name = "invoice_line_items")`
  - `@OrderColumn(name = "line_order")` to preserve line item order

## Application Layer

### DTOs
- [ ] `LineItemRequest` - for line item input
  - Fields: `description`, `quantity`, `unitPrice`
  - Validation: `@NotNull`, `@Min(1)` for quantity, `@DecimalMin("0")` for price
- [ ] `CreateInvoiceRequest` - for invoice creation
  - Fields: `customerId`, `lineItems` (List<LineItemRequest>)
  - Validation: at least one line item required
- [ ] `UpdateInvoiceRequest` - for invoice updates (only if DRAFT)
  - Fields: `lineItems` (List<LineItemRequest>)
- [ ] `InvoiceResponse` - for API responses
  - Fields: `id`, `customerId`, `customerName`, `status`, `lineItems`, `totalAmount`, `balance`, `createdAt`, `updatedAt`
  - Include customer name for convenience

### Mapper
- [ ] Create `InvoiceMapper` interface with MapStruct
- [ ] Methods:
  - `toResponse(Invoice)` - maps invoice to response DTO
  - `toEntity(CreateInvoiceRequest)` - maps request to domain entity
  - `updateEntity(UpdateInvoiceRequest, Invoice)` - updates existing entity
- [ ] Handle line items mapping
- [ ] Handle customer reference mapping

### Command Service
- [ ] Create `InvoiceCommandService` in `application/invoice/`
- [ ] Annotate with `@Service` and `@Transactional`
- [ ] Methods:
  - `createInvoice(CreateInvoiceRequest)` → `InvoiceResponse`
    - Creates invoice in DRAFT status
    - Adds line items via domain method
    - Calculates total via domain method
  - `updateInvoice(UUID id, UpdateInvoiceRequest)` → `InvoiceResponse`
    - Validates invoice is DRAFT (via `canBeEdited()`)
    - Updates line items
    - Recalculates total
  - `markAsSent(UUID id)` → `InvoiceResponse`
    - Uses domain method `canBeMarkedAsSent()` for validation
    - Uses domain method `markAsSent()` for state transition
- [ ] Business logic: Delegate to domain entity methods
- [ ] Error handling: Throw `BusinessRuleViolationException` for invalid transitions

### Query Service
- [ ] Create `InvoiceQueryService` in `application/invoice/`
- [ ] Annotate with `@Service` and `@Transactional(readOnly = true)`
- [ ] Methods:
  - `getById(UUID id)` → `InvoiceResponse`
  - `getByStatus(InvoiceStatus status, Pageable)` → `Page<InvoiceResponse>`
  - `getByCustomerId(UUID customerId, Pageable)` → `Page<InvoiceResponse>`
  - `getAll(Pageable)` → `Page<InvoiceResponse>`
- [ ] Calculate balance by querying payments (delegates to PaymentQueryService or repository)

## Infrastructure Layer

### Repository
- [ ] Create `InvoiceRepository` interface extending `JpaRepository<Invoice, UUID>`
- [ ] Custom query methods:
  - `findByStatus(InvoiceStatus status, Pageable)`
  - `findByCustomerId(UUID customerId, Pageable)`
- [ ] Location: `infrastructure/persistence/`

## Presentation Layer

### Controller
- [ ] Create `InvoiceController` in `presentation/rest/`
- [ ] Base path: `/api/invoices`
- [ ] Endpoints:
  - `POST /api/invoices` - Create invoice (DRAFT)
  - `GET /api/invoices/{id}` - Get by ID
  - `GET /api/invoices?page=0&size=20` - List all (paginated)
  - `GET /api/invoices?status={status}&page=0&size=20` - Filter by status
  - `GET /api/invoices?customerId={id}&page=0&size=20` - Filter by customer
  - `PUT /api/invoices/{id}` - Update invoice (only if DRAFT)
  - `POST /api/invoices/{id}/send` - Mark invoice as SENT
- [ ] Use `@PageableDefault(size = 20, sort = "createdAt,desc")` for list endpoints
- [ ] OpenAPI annotations for Swagger documentation
- [ ] Proper HTTP status codes: 200, 201, 400, 404, 422

## Database

### Migration
- [ ] Create Flyway migration: `V3__create_invoices_table.sql`
- [ ] Schema:
  ```sql
  CREATE TABLE invoices (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
      status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'SENT', 'PAID')),
      total_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
      balance DECIMAL(19,2) NOT NULL DEFAULT 0,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  );
  CREATE INDEX idx_invoices_customer_id ON invoices(customer_id);
  CREATE INDEX idx_invoices_status ON invoices(status);
  
  CREATE TABLE invoice_line_items (
      invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
      description VARCHAR(500) NOT NULL,
      quantity INTEGER NOT NULL CHECK (quantity > 0),
      unit_price DECIMAL(19,2) NOT NULL CHECK (unit_price >= 0),
      line_order INTEGER NOT NULL,
      PRIMARY KEY (invoice_id, line_order)
  );
  ```

## Testing

### Integration Tests
- [ ] Create `InvoiceIntegrationTest` with `@SpringBootTest` and Testcontainers
- [ ] Test scenarios:
  - Create invoice with line items (DRAFT status)
  - Get invoice by ID
  - List invoices with pagination
  - Filter invoices by status
  - Filter invoices by customer
  - Update invoice (only if DRAFT)
  - Mark invoice as SENT (valid transition)
  - Mark invoice as SENT (invalid: no line items) → should fail
  - Mark invoice as SENT (invalid: already SENT) → should fail
  - Calculate total correctly
  - Calculate balance correctly (requires payments, can be tested in PRD 06)
- [ ] Verify response times < 200ms
- [ ] Test domain logic: `calculateTotal()`, `canBeMarkedAsSent()`, `markAsSent()`

## Success Criteria
- [ ] Invoice CRUD operations work end-to-end
- [ ] Domain entity has rich behavior (not anemic)
- [ ] Invoice lifecycle (DRAFT → SENT) is enforced
- [ ] Line items are stored and calculated correctly
- [ ] Total amount calculation is correct
- [ ] CQRS separation is clear
- [ ] Integration tests pass
- [ ] API response times < 200ms
- [ ] OpenAPI documentation is accurate

## Dependencies
- **Requires**: PRD 01 (Foundation)
- **Requires**: PRD 02 (Customer Backend) - for customer reference
- **Enables**: PRD 05 (Invoice Frontend)
- **Can run parallel with**: PRDs 03, 06, 07
- **Note**: Balance calculation may require PRD 06 (Payment Backend) for full testing

## Timeline Estimate
**1-1.5 days** (most complex feature, backend-heavy)

## Notes
- This is the most complex feature due to domain logic
- Rich domain methods demonstrate DDD principles
- Lifecycle management is a key architectural requirement
- Balance calculation can be stubbed initially, fully tested with PRD 06


