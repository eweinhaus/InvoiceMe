# PRD 06: Payment Feature - Backend

## Overview
**Priority**: 🟢 Can run in parallel with PRDs 02, 03, 04, 05, 07  
**Dependencies**: PRD 01 (Foundation), PRD 04 (Invoice Backend) - for invoice reference  
**Enables**: PRD 07 (Payment Frontend), completes Invoice balance calculation

This PRD implements the complete backend for Payment management with payment application logic and balance updates.

## Objectives
1. Implement Payment domain entity with validation logic
2. Create Payment Command and Query services (CQRS)
3. Implement Payment REST API endpoints
4. Integrate payment application with Invoice balance calculation
5. Set up database persistence for Payments
6. Create integration tests for Payment operations and balance calculations

## Domain Layer

### Payment Entity
- [ ] Create `Payment` entity in `domain/payment/` package
- [ ] Properties:
  - `id` (UUID)
  - `invoice` (`@ManyToOne` to Invoice)
  - `amount` (BigDecimal)
  - `paymentDate` (LocalDateTime)
  - `createdAt` (timestamp)
- [ ] Rich domain methods:
  - `validateAmount(Invoice invoice)` - validates payment doesn't exceed invoice balance
    - Throws `BusinessRuleViolationException` if amount > invoice.balance
    - Throws `BusinessRuleViolationException` if amount <= 0
  - `applyToInvoice()` - calls `invoice.applyPayment(amount)`
- [ ] JPA annotations: `@Entity`, `@Id`, `@GeneratedValue`, `@ManyToOne`, `@Column`

## Application Layer

### DTOs
- [ ] `PaymentRequest` - for payment creation
  - Fields: `invoiceId`, `amount`, `paymentDate`
  - Validation: `@NotNull`, `@DecimalMin("0.01")` for amount, `@NotNull` for invoiceId and paymentDate
- [ ] `PaymentResponse` - for API responses
  - Fields: `id`, `invoiceId`, `invoiceNumber`, `amount`, `paymentDate`, `createdAt`
  - Include invoice number for convenience

### Mapper
- [ ] Create `PaymentMapper` interface with MapStruct
- [ ] Methods:
  - `toResponse(Payment)` - maps payment to response DTO
  - `toEntity(PaymentRequest)` - maps request to domain entity
- [ ] Handle invoice reference mapping

### Command Service
- [ ] Create `PaymentCommandService` in `application/payment/`
- [ ] Annotate with `@Service` and `@Transactional`
- [ ] Methods:
  - `recordPayment(PaymentRequest)` → `PaymentResponse`
    - Loads invoice by ID
    - Creates payment entity
    - Validates payment amount via `payment.validateAmount(invoice)`
    - Applies payment to invoice via `payment.applyToInvoice()`
    - Saves payment
    - Returns payment response with updated invoice balance
- [ ] Business logic: Delegate to domain entity methods
- [ ] Error handling:
  - Throw `EntityNotFoundException` if invoice not found
  - Throw `BusinessRuleViolationException` for validation failures

### Query Service
- [ ] Create `PaymentQueryService` in `application/payment/`
- [ ] Annotate with `@Service` and `@Transactional(readOnly = true)`
- [ ] Methods:
  - `getById(UUID id)` → `PaymentResponse`
  - `getByInvoiceId(UUID invoiceId, Pageable)` → `Page<PaymentResponse>`
  - `getAll(Pageable)` → `Page<PaymentResponse>`
- [ ] Location: `infrastructure/persistence/`

## Infrastructure Layer

### Repository
- [ ] Create `PaymentRepository` interface extending `JpaRepository<Payment, UUID>`
- [ ] Custom query methods:
  - `findByInvoiceId(UUID invoiceId, Pageable)`
- [ ] Location: `infrastructure/persistence/`

## Presentation Layer

### Controller
- [ ] Create `PaymentController` in `presentation/rest/`
- [ ] Base path: `/api/payments`
- [ ] Endpoints:
  - `POST /api/payments` - Record payment
  - `GET /api/payments/{id}` - Get by ID
  - `GET /api/payments?page=0&size=20` - List all (paginated)
  - `GET /api/payments?invoiceId={id}&page=0&size=20` - List by invoice (paginated)
- [ ] Alternative endpoint (for convenience):
  - `POST /api/invoices/{id}/payments` - Record payment for specific invoice (delegates to PaymentController)
- [ ] Use `@PageableDefault(size = 20, sort = "paymentDate,desc")` for list endpoints
- [ ] OpenAPI annotations for Swagger documentation
- [ ] Proper HTTP status codes: 200, 201, 400, 404, 422

## Database

### Migration
- [ ] Create Flyway migration: `V4__create_payments_table.sql`
- [ ] Schema:
  ```sql
  CREATE TABLE payments (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE RESTRICT,
      amount DECIMAL(19,2) NOT NULL CHECK (amount > 0),
      payment_date TIMESTAMP NOT NULL,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  );
  CREATE INDEX idx_payments_invoice_id ON payments(invoice_id);
  ```

## Integration with Invoice

### Invoice Balance Calculation
- [ ] Ensure `Invoice.calculateBalance()` method queries payments correctly
- [ ] Update `Invoice.applyPayment(amount)` method:
  - Subtracts amount from balance
  - Transitions status to PAID if balance reaches 0
- [ ] This completes the Invoice domain logic from PRD 04

## Testing

### Integration Tests
- [ ] Create `PaymentIntegrationTest` with `@SpringBootTest` and Testcontainers
- [ ] Test scenarios:
  - Record payment successfully
  - Get payment by ID
  - List payments with pagination
  - List payments by invoice
  - Payment updates invoice balance correctly
  - Payment transitions invoice to PAID when balance reaches 0
  - Payment validation: amount exceeds balance → should fail (422)
  - Payment validation: amount is zero or negative → should fail (422)
  - Payment validation: invoice not found → should fail (404)
- [ ] **Full Flow Integration Test**:
  - Create Customer → Create Invoice → Record Payment → Verify Balance
  - Test complete invoice lifecycle: DRAFT → SENT → PAID
- [ ] Verify response times < 200ms
- [ ] Test domain logic: `validateAmount()`, `applyToInvoice()`

## Success Criteria
- [ ] Payment CRUD operations work end-to-end
- [ ] Payment validation prevents invalid amounts
- [ ] Payment correctly updates invoice balance
- [ ] Payment transitions invoice to PAID when appropriate
- [ ] Domain entity has rich behavior (not anemic)
- [ ] CQRS separation is clear
- [ ] Integration tests pass (including full flow test)
- [ ] API response times < 200ms
- [ ] OpenAPI documentation is accurate

## Dependencies
- **Requires**: PRD 01 (Foundation)
- **Requires**: PRD 04 (Invoice Backend) - for invoice reference and balance calculation
- **Enables**: PRD 07 (Payment Frontend)
- **Completes**: Invoice balance calculation from PRD 04
- **Can run parallel with**: PRDs 02, 03, 05, 07

## Timeline Estimate
**0.5-1 day** (simpler than Invoice, but requires integration)

## Notes
- This feature completes the Invoice payment flow
- Payment validation is critical business logic
- Full flow integration test is a key deliverable
- Balance calculation should be tested thoroughly



