# Task List: PRD 06 - Payment Feature Backend

## Overview
**Priority**: 🟢 Can run in parallel with PRDs 02, 03, 04, 05, 07  
**Estimated Time**: 0.5-1 day (simpler than Invoice, but requires integration)  
**Dependencies**: PRD 01 (Foundation), PRD 04 (Invoice Backend) - for invoice reference and balance calculation  
**Enables**: PRD 07 (Payment Frontend) - *Note: PRD 07 can start independently using mock data*  
**Completes**: Invoice balance calculation from PRD 04

This task list breaks down PRD 06 into actionable, checkable tasks for implementing the complete Payment backend feature following DDD, CQRS, and Vertical Slice Architecture principles. This feature completes the Invoice payment flow and integrates payment application with Invoice balance calculation.

---

## Domain Layer Tasks

### 1. Payment Entity
- [ ] Create `Payment` entity class:
  - [ ] Create `src/main/java/com/invoiceme/domain/payment/Payment.java`
  - [ ] Add package declaration: `package com.invoiceme.domain.payment;`

- [ ] Add JPA entity annotations:
  - [ ] Add `@Entity` annotation
  - [ ] Add `@Table(name = "payments")` annotation
  - [ ] Verify entity is in domain package (not infrastructure)

- [ ] Implement entity properties:
  - [ ] Add `id` field: `UUID` type with `@Id` and `@GeneratedValue(strategy = GenerationType.UUID)`
  - [ ] Add `invoice` field: `Invoice` type with `@ManyToOne` and `@JoinColumn(name = "invoice_id")`
    - [ ] Import Invoice entity: `import com.invoiceme.domain.invoice.Invoice;`
    - [ ] Add `@NotNull` validation
  - [ ] Add `amount` field: `BigDecimal` type with `@Column(nullable = false, precision = 19, scale = 2)`
  - [ ] Add `paymentDate` field: `LocalDateTime` type with `@Column(nullable = false)`
  - [ ] Add `createdAt` field: `LocalDateTime` type

- [ ] Implement timestamp handling:
  - [ ] Option A: Use JPA Auditing (requires `@EnableJpaAuditing` in config):
    - [ ] Add `@EntityListeners(AuditingEntityListener.class)`
    - [ ] Add `@CreatedDate` to `createdAt`
  - [ ] Option B: Manual timestamp handling (simpler, consistent with Invoice):
    - [ ] Add `@PrePersist` method to set `createdAt` on create

- [ ] Implement rich domain methods (NOT anemic):
  - [ ] Create `validateAmount(Invoice invoice)` method:
    - [ ] Return type: `void` (throws exception if invalid)
    - [ ] Validation logic:
      - [ ] Validate `amount > 0` (throw `IllegalArgumentException` if invalid)
      - [ ] Validate `amount <= invoice.balance` (throw `IllegalArgumentException` if exceeds balance)
      - [ ] Ensure invoice is not null
    - [ ] Throw `BusinessRuleViolationException` (or `IllegalArgumentException`) with descriptive message
    - [ ] Note: This method validates payment amount before applying to invoice

  - [ ] Create `applyToInvoice()` method:
    - [ ] Return type: `void`
    - [ ] Ensure invoice is not null
    - [ ] Call `invoice.applyPayment(this.amount)` to update invoice balance
    - [ ] Note: This method delegates to Invoice's `applyPayment()` method
    - [ ] Note: Invoice's `applyPayment()` will transition status to PAID if balance reaches 0

- [ ] Add constructor(s):
  - [ ] Add no-args constructor (required by JPA)
  - [ ] Add constructor for required fields (`invoice`, `amount`, `paymentDate`) that:
    - [ ] Validates invoice is not null
    - [ ] Validates amount > 0
    - [ ] Sets paymentDate (can be null, defaults to now if not provided)

- [ ] Verify domain entity follows DDD principles:
  - [ ] Business logic is in entity methods (not in services)
  - [ ] Entity is self-validating
  - [ ] No anemic domain model (has behavior, not just data)
  - [ ] Payment validation is enforced in domain methods

---

## Application Layer Tasks

### 2. DTOs (Data Transfer Objects)
- [ ] Create `PaymentRequest` DTO:
  - [ ] Create `src/main/java/com/invoiceme/application/payment/dto/PaymentRequest.java`
  - [ ] Add package declaration: `package com.invoiceme.application.payment.dto;`
  - [ ] Add fields:
    - [ ] `invoiceId`: `UUID` with `@NotNull`
    - [ ] `amount`: `BigDecimal` with `@NotNull` and `@DecimalMin("0.01")` (minimum 0.01)
    - [ ] `paymentDate`: `LocalDateTime` with `@NotNull`
  - [ ] Add Lombok annotations: `@Data` or `@Getter`/`@Setter`
  - [ ] Add no-args constructor (Lombok handles this)

- [ ] Create `PaymentResponse` DTO:
  - [ ] Create `src/main/java/com/invoiceme/application/payment/dto/PaymentResponse.java`
  - [ ] Add package declaration: `package com.invoiceme.application.payment.dto;`
  - [ ] Add fields:
    - [ ] `id`: `UUID`
    - [ ] `invoiceId`: `UUID`
    - [ ] `invoiceNumber`: `String` (for convenience, included in response)
    - [ ] `amount`: `BigDecimal`
    - [ ] `paymentDate`: `LocalDateTime`
    - [ ] `createdAt`: `LocalDateTime`
  - [ ] Add Lombok annotations: `@Data` or `@Getter`/`@Setter`
  - [ ] Add no-args constructor (Lombok handles this)

- [ ] Verify DTOs are in application layer:
  - [ ] Check package structure: `application/payment/dto/`
  - [ ] Verify DTOs have no domain logic (just data transfer)
  - [ ] Verify validation annotations are present

### 3. MapStruct Mapper
- [ ] Create `PaymentMapper` interface:
  - [ ] Create `src/main/java/com/invoiceme/application/payment/PaymentMapper.java`
  - [ ] Add package declaration: `package com.invoiceme.application.payment;`
  - [ ] Add `@Mapper(componentModel = "spring")` annotation
  - [ ] Import MapStruct: `import org.mapstruct.Mapper;`

- [ ] Implement mapper methods:
  - [ ] Add `toResponse(Payment payment)` method:
    - [ ] Return type: `PaymentResponse`
    - [ ] Map all fields from `Payment` to `PaymentResponse`
    - [ ] Map `invoice` to `invoiceId` and `invoiceNumber`
    - [ ] Handle null values appropriately
    - [ ] Use `@Mapping` annotations if field names differ

  - [ ] Add `toEntity(PaymentRequest request)` method:
    - [ ] Return type: `Payment`
    - [ ] Map `invoiceId` to `invoice` (requires Invoice lookup - handle in service)
    - [ ] Map `amount` and `paymentDate`
    - [ ] Note: `invoice` reference must be set in service (not in mapper)
    - [ ] Use `@Mapping(target = "invoice", ignore = true)` and set in service

- [ ] Verify MapStruct annotation processor is configured:
  - [ ] Check `pom.xml` has MapStruct dependency
  - [ ] Check annotation processor order (Lombok → MapStruct)
  - [ ] Build project and verify mapper implementation is generated
  - [ ] Check `target/generated-sources/annotations/` for generated mapper

### 4. Command Service (CQRS - Write Operations)
- [ ] Create `PaymentCommandService`:
  - [ ] Create `src/main/java/com/invoiceme/application/payment/PaymentCommandService.java`
  - [ ] Add package declaration: `package com.invoiceme.application.payment;`
  - [ ] Add `@Service` annotation
  - [ ] Add `@Transactional` annotation (for write operations)

- [ ] Inject dependencies:
  - [ ] Inject `PaymentRepository` via constructor injection
  - [ ] Inject `InvoiceRepository` via constructor injection (for invoice lookup)
  - [ ] Inject `PaymentMapper` via constructor injection
  - [ ] Use `@RequiredArgsConstructor` (Lombok) or manual constructor

- [ ] Implement `recordPayment(PaymentRequest request)` method:
  - [ ] Return type: `PaymentResponse`
  - [ ] Find invoice by ID: `invoiceRepository.findById(request.getInvoiceId())`
  - [ ] Throw `EntityNotFoundException` if invoice not found
  - [ ] Use mapper to convert `PaymentRequest` to `Payment` entity
  - [ ] Set `invoice` reference on payment entity
  - [ ] Validate payment amount: Call entity's `validateAmount(invoice)` method
  - [ ] Apply payment to invoice: Call entity's `applyToInvoice()` method
    - [ ] This will call `invoice.applyPayment(amount)` which updates balance and may transition to PAID
  - [ ] Save payment entity: `paymentRepository.save(payment)`
  - [ ] Save invoice entity: `invoiceRepository.save(invoice)` (balance and status may have changed)
  - [ ] Map saved payment to `PaymentResponse` using mapper
  - [ ] Return `PaymentResponse`

- [ ] Create `BusinessRuleViolationException` (if not exists):
  - [ ] Check if exception exists from PRD 04 (Invoice Backend)
  - [ ] If not exists, create `src/main/java/com/invoiceme/application/common/exception/BusinessRuleViolationException.java`
  - [ ] Extend `RuntimeException` or create custom exception
  - [ ] Add constructor with message
  - [ ] Or use existing exception from foundation (if created in PRD 01)

- [ ] Verify CQRS separation:
  - [ ] Command service only handles write operations (record payment)
  - [ ] Command service uses `@Transactional` (not `readOnly = true`)
  - [ ] Business logic delegates to domain entity methods
  - [ ] Service validates business rules before calling domain methods

### 5. Query Service (CQRS - Read Operations)
- [ ] Create `PaymentQueryService`:
  - [ ] Create `src/main/java/com/invoiceme/application/payment/PaymentQueryService.java`
  - [ ] Add package declaration: `package com.invoiceme.application.payment;`
  - [ ] Add `@Service` annotation
  - [ ] Add `@Transactional(readOnly = true)` annotation (for read operations)

- [ ] Inject dependencies:
  - [ ] Inject `PaymentRepository` via constructor injection
  - [ ] Inject `PaymentMapper` via constructor injection
  - [ ] Use `@RequiredArgsConstructor` (Lombok) or manual constructor

- [ ] Implement `getById(UUID id)` method:
  - [ ] Return type: `PaymentResponse`
  - [ ] Find payment by ID: `paymentRepository.findById(id)`
  - [ ] Throw `EntityNotFoundException` if payment not found
  - [ ] Map entity to `PaymentResponse` using mapper
  - [ ] Return `PaymentResponse`

- [ ] Implement `getByInvoiceId(UUID invoiceId, Pageable pageable)` method:
  - [ ] Return type: `Page<PaymentResponse>`
  - [ ] Find payments by invoice: `paymentRepository.findByInvoiceId(invoiceId, pageable)`
  - [ ] Map `Page<Payment>` to `Page<PaymentResponse>`:
    - [ ] Use `Page.map()` to transform each entity to response DTO
  - [ ] Return `Page<PaymentResponse>`

- [ ] Implement `getAll(Pageable pageable)` method:
  - [ ] Return type: `Page<PaymentResponse>`
  - [ ] Find all payments: `paymentRepository.findAll(pageable)`
  - [ ] Map `Page<Payment>` to `Page<PaymentResponse>`:
    - [ ] Use `Page.map()` to transform each entity to response DTO
  - [ ] Return `Page<PaymentResponse>`

- [ ] Verify CQRS separation:
  - [ ] Query service only handles read operations (get by ID, list by invoice, list all)
  - [ ] Query service uses `@Transactional(readOnly = true)`
  - [ ] No write operations in query service

---

## Infrastructure Layer Tasks

### 6. Repository
- [ ] Create `PaymentRepository` interface:
  - [ ] Create `src/main/java/com/invoiceme/infrastructure/persistence/PaymentRepository.java`
  - [ ] Add package declaration: `package com.invoiceme.infrastructure.persistence;`
  - [ ] Extend `JpaRepository<Payment, UUID>`
  - [ ] Import: `import org.springframework.data.jpa.repository.JpaRepository;`
  - [ ] Import domain entity: `import com.invoiceme.domain.payment.Payment;`

- [ ] Add custom query methods:
  - [ ] Add `Page<Payment> findByInvoiceId(UUID invoiceId, Pageable pageable);`
    - [ ] Import: `import org.springframework.data.domain.Page;`
    - [ ] Import: `import org.springframework.data.domain.Pageable;`
    - [ ] Import: `import java.util.UUID;`

- [ ] Verify repository interface:
  - [ ] Repository is in infrastructure layer (not domain)
  - [ ] Repository extends `JpaRepository` with correct generic types
  - [ ] Custom query methods follow Spring Data JPA naming conventions
  - [ ] Query methods return `Page<Payment>` for pagination support

---

## Presentation Layer Tasks

### 7. Controller
- [ ] Create `PaymentController`:
  - [ ] Create `src/main/java/com/invoiceme/presentation/rest/PaymentController.java`
  - [ ] Add package declaration: `package com.invoiceme.presentation.rest;`
  - [ ] Add `@RestController` annotation
  - [ ] Add `@RequestMapping("/api/payments")` annotation

- [ ] Inject dependencies:
  - [ ] Inject `PaymentCommandService` via constructor injection
  - [ ] Inject `PaymentQueryService` via constructor injection
  - [ ] Use `@RequiredArgsConstructor` (Lombok) or manual constructor

- [ ] Implement `POST /api/payments` endpoint (Record Payment):
  - [ ] Add `@PostMapping` annotation
  - [ ] Add `@RequestBody @Valid PaymentRequest request` parameter
  - [ ] Add `@Operation` annotation for OpenAPI documentation
  - [ ] Add `@ApiResponse` annotations (201, 400, 404, 422)
  - [ ] Call `commandService.recordPayment(request)`
  - [ ] Return `ResponseEntity<PaymentResponse>` with status `HttpStatus.CREATED` (201)
  - [ ] Verify validation errors are handled by `GlobalExceptionHandler` (from PRD 01)

- [ ] Implement `GET /api/payments/{id}` endpoint (Get by ID):
  - [ ] Add `@GetMapping("/{id}")` annotation
  - [ ] Add `@PathVariable UUID id` parameter
  - [ ] Add `@Operation` annotation for OpenAPI documentation
  - [ ] Add `@ApiResponse` annotations (200, 404)
  - [ ] Call `queryService.getById(id)`
  - [ ] Return `ResponseEntity<PaymentResponse>` with status `HttpStatus.OK` (200)
  - [ ] Verify 404 is handled by `GlobalExceptionHandler` (throws `EntityNotFoundException`)

- [ ] Implement `GET /api/payments` endpoint (List all with pagination):
  - [ ] Add `@GetMapping` annotation
  - [ ] Add `@PageableDefault(size = 20, sort = "paymentDate", direction = Sort.Direction.DESC) Pageable pageable` parameter
  - [ ] Add `@Operation` annotation for OpenAPI documentation
  - [ ] Add `@ApiResponse` annotations (200)
  - [ ] Call `queryService.getAll(pageable)`
  - [ ] Return `ResponseEntity<Page<PaymentResponse>>` with status `HttpStatus.OK` (200)
  - [ ] Verify pagination parameters work: `?page=0&size=20&sort=paymentDate,desc`

- [ ] Implement `GET /api/payments?invoiceId={id}` endpoint (List by invoice with pagination):
  - [ ] Add `@GetMapping` annotation
  - [ ] Add `@RequestParam(required = false) UUID invoiceId` parameter
  - [ ] Add `@PageableDefault(size = 20, sort = "paymentDate", direction = Sort.Direction.DESC) Pageable pageable` parameter
  - [ ] Add `@Operation` annotation for OpenAPI documentation
  - [ ] Add `@ApiResponse` annotations (200, 404)
  - [ ] If `invoiceId` is provided, call `queryService.getByInvoiceId(invoiceId, pageable)`
  - [ ] If `invoiceId` is null, call `queryService.getAll(pageable)`
  - [ ] Return `ResponseEntity<Page<PaymentResponse>>` with status `HttpStatus.OK` (200)
  - [ ] Verify filtering works: `?invoiceId={uuid}&page=0&size=20`

- [ ] Optional: Implement `POST /api/invoices/{id}/payments` endpoint (Convenience endpoint):
  - [ ] Add `@PostMapping("/api/invoices/{invoiceId}/payments")` annotation
  - [ ] Add `@PathVariable UUID invoiceId` parameter
  - [ ] Add `@RequestBody @Valid PaymentRequest request` parameter (without invoiceId, it's in path)
  - [ ] Create `PaymentRequest` with `invoiceId` from path
  - [ ] Delegate to `PaymentController.recordPayment()` or call `commandService.recordPayment()`
  - [ ] Add `@Operation` annotation for OpenAPI documentation
  - [ ] Add `@ApiResponse` annotations (201, 400, 404, 422)
  - [ ] Return `ResponseEntity<PaymentResponse>` with status `HttpStatus.CREATED` (201)
  - [ ] Note: This is a convenience endpoint mentioned in PRD, can be in InvoiceController or PaymentController

- [ ] Verify OpenAPI documentation:
  - [ ] Start application and access Swagger UI
  - [ ] Verify all endpoints are documented
  - [ ] Verify request/response schemas are correct
  - [ ] Verify status codes are documented
  - [ ] Verify query parameters are documented (invoiceId, pagination)

---

## Database Tasks

### 8. Flyway Migration
- [ ] Create Flyway migration file:
  - [ ] Create `src/main/resources/db/migration/V4__create_payments_table.sql`
  - [ ] Follow Flyway naming convention: `V{version}__{description}.sql`
  - [ ] Ensure version number is sequential (V4 comes after V3 for invoices)

- [ ] Implement database schema:
  - [ ] Create `payments` table:
    ```sql
    CREATE TABLE payments (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE RESTRICT,
        amount DECIMAL(19,2) NOT NULL CHECK (amount > 0),
        payment_date TIMESTAMP NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    ```
  - [ ] Create index:
    ```sql
    CREATE INDEX idx_payments_invoice_id ON payments(invoice_id);
    ```
  - [ ] Verify SQL syntax is correct for PostgreSQL (used by Testcontainers)
  - [ ] Verify foreign key constraints are correct
  - [ ] Verify CHECK constraints enforce business rules (amount > 0)

- [ ] Verify migration runs successfully:
  - [ ] Start application
  - [ ] Check Flyway logs for successful migration
  - [ ] Verify `payments` table exists in database
  - [ ] Verify index exists on `invoice_id` column
  - [ ] Verify foreign key constraints are enforced

- [ ] Test migration with H2 (development):
  - [ ] Ensure H2 supports UUID and gen_random_uuid() (or use alternative)
  - [ ] If H2 doesn't support, use `RANDOM_UUID()` or `java.util.UUID.randomUUID()`
  - [ ] Or create separate migration for H2 (not recommended, use PostgreSQL-compatible SQL)

---

## Integration with Invoice Tasks

### 9. Invoice Balance Calculation Integration
- [ ] Update `Invoice.calculateBalance()` method (if needed):
  - [ ] Review current implementation in `Invoice.java`
  - [ ] If method currently returns `totalAmount` (stub), update to query payments:
    - [ ] Option A: Inject `PaymentRepository` in Invoice entity (not recommended, breaks DDD)
    - [ ] Option B: Calculate balance in `InvoiceQueryService` (recommended)
    - [ ] Option C: Keep `calculateBalance()` as-is and calculate in service layer
  - [ ] Note: PRD 04 may have left this as a stub, decide on approach

- [ ] Ensure `Invoice.applyPayment()` method works correctly:
  - [ ] Verify method exists in `Invoice.java` (should be from PRD 04)
  - [ ] Verify method updates balance correctly
  - [ ] Verify method transitions status to PAID when balance reaches 0
  - [ ] Test method with various payment amounts

- [ ] Verify transaction boundaries:
  - [ ] Ensure payment and invoice updates are in same transaction
  - [ ] Verify `@Transactional` on `PaymentCommandService.recordPayment()`
  - [ ] Test rollback if payment save fails after invoice update

---

## Testing Tasks

### 10. Integration Tests
- [ ] Create `PaymentIntegrationTest` class:
  - [ ] Create `src/test/java/com/invoiceme/PaymentIntegrationTest.java`
  - [ ] Extend base test class (if created in PRD 01) or use `@SpringBootTest`
  - [ ] Add `@Testcontainers` annotation (if using Testcontainers)
  - [ ] Add `@AutoConfigureMockMvc` or use `@WebMvcTest` (if testing controller layer)
  - [ ] Add `@Transactional` annotation for test cleanup

- [ ] Set up test dependencies:
  - [ ] Inject `MockMvc` (for controller tests) or use `TestRestTemplate`
  - [ ] Inject `PaymentRepository` (for direct repository tests)
  - [ ] Inject `InvoiceRepository` (for creating test invoices)
  - [ ] Inject `CustomerRepository` (for creating test customers)
  - [ ] Inject `PaymentCommandService` and `PaymentQueryService` (for service tests)

- [ ] Test: Record payment successfully:
  - [ ] Create test method: `testRecordPayment_Success()`
  - [ ] Create and save customer in database
  - [ ] Create and save invoice in database (SENT status, with line items, total > 0)
  - [ ] Create `PaymentRequest` with invoice ID, amount, and payment date
  - [ ] Call POST endpoint or command service
  - [ ] Verify response status is 201 (or 200 for service)
  - [ ] Verify response contains payment data
  - [ ] Verify payment is saved in database
  - [ ] Verify invoice balance is updated correctly
  - [ ] Verify invoice status remains SENT (if balance > 0)

- [ ] Test: Get payment by ID:
  - [ ] Create test method: `testGetPaymentById_Success()`
  - [ ] Create and save payment in database
  - [ ] Call GET endpoint or query service with payment ID
  - [ ] Verify response status is 200
  - [ ] Verify response contains correct payment data

- [ ] Test: List payments with pagination:
  - [ ] Create test method: `testGetAllPayments_WithPagination()`
  - [ ] Create multiple payments in database (at least 3)
  - [ ] Call GET endpoint with pagination: `?page=0&size=2`
  - [ ] Verify response status is 200
  - [ ] Verify response contains `Page` with correct size
  - [ ] Verify pagination metadata (totalElements, totalPages, etc.)
  - [ ] Test sorting: `?sort=paymentDate,desc` and `?sort=paymentDate,asc`

- [ ] Test: List payments by invoice:
  - [ ] Create test method: `testGetPaymentsByInvoiceId_Success()`
  - [ ] Create multiple invoices
  - [ ] Create payments for different invoices
  - [ ] Call GET endpoint with invoice filter: `?invoiceId={uuid}`
  - [ ] Verify response status is 200
  - [ ] Verify response contains only payments for specified invoice

- [ ] Test: Payment updates invoice balance correctly:
  - [ ] Create test method: `testRecordPayment_UpdatesInvoiceBalance()`
  - [ ] Create invoice with total amount = 1000.00
  - [ ] Record payment of 300.00
  - [ ] Verify invoice balance = 700.00
  - [ ] Record another payment of 500.00
  - [ ] Verify invoice balance = 200.00
  - [ ] Verify invoice status remains SENT (balance > 0)

- [ ] Test: Payment transitions invoice to PAID when balance reaches 0:
  - [ ] Create test method: `testRecordPayment_TransitionsInvoiceToPaid()`
  - [ ] Create invoice with total amount = 1000.00
  - [ ] Record payment of 1000.00 (full payment)
  - [ ] Verify invoice balance = 0.00
  - [ ] Verify invoice status = PAID
  - [ ] Test with partial payments that sum to total: Record 600.00, then 400.00

- [ ] Test: Payment validation - amount exceeds balance:
  - [ ] Create test method: `testRecordPayment_AmountExceedsBalance_Fails()`
  - [ ] Create invoice with total amount = 1000.00
  - [ ] Create `PaymentRequest` with amount = 1500.00
  - [ ] Call POST endpoint or command service
  - [ ] Verify response status is 422 (or 400) - business rule violation
  - [ ] Verify error message indicates payment amount exceeds balance

- [ ] Test: Payment validation - amount is zero or negative:
  - [ ] Create test method: `testRecordPayment_ZeroOrNegativeAmount_Fails()`
  - [ ] Create invoice
  - [ ] Create `PaymentRequest` with amount = 0.00
  - [ ] Call POST endpoint or command service
  - [ ] Verify response status is 400 (validation error)
  - [ ] Test with negative amount: amount = -100.00
  - [ ] Verify error message indicates invalid amount

- [ ] Test: Payment validation - invoice not found:
  - [ ] Create test method: `testRecordPayment_InvoiceNotFound()`
  - [ ] Create `PaymentRequest` with non-existent invoice ID
  - [ ] Call POST endpoint or command service
  - [ ] Verify response status is 404
  - [ ] Verify error message indicates invoice not found

- [ ] Test: Full Flow Integration Test:
  - [ ] Create test method: `testFullFlow_CreateCustomer_CreateInvoice_RecordPayment_VerifyBalance()`
  - [ ] Create customer
  - [ ] Create invoice for customer (DRAFT status)
  - [ ] Mark invoice as SENT
  - [ ] Record payment (partial)
  - [ ] Verify invoice balance is updated
  - [ ] Record another payment (full payment)
  - [ ] Verify invoice balance = 0
  - [ ] Verify invoice status = PAID
  - [ ] Test complete invoice lifecycle: DRAFT → SENT → PAID

- [ ] Test: Handle not found (404):
  - [ ] Create test method: `testGetPaymentById_NotFound()`
  - [ ] Call GET endpoint with non-existent UUID
  - [ ] Verify response status is 404
  - [ ] Verify error response format matches `ErrorResponse` DTO

- [ ] Test: Validate required fields:
  - [ ] Create test method: `testRecordPayment_MissingRequiredFields()`
  - [ ] Create `PaymentRequest` with null `invoiceId`, null `amount`, or null `paymentDate`
  - [ ] Call POST endpoint
  - [ ] Verify response status is 400 (validation error)
  - [ ] Verify error response contains validation details

- [ ] Test: Domain logic - `validateAmount()`:
  - [ ] Create test method: `testPaymentValidateAmount_DomainLogic()`
  - [ ] Create payment entity directly
  - [ ] Create invoice with balance = 1000.00
  - [ ] Call `validateAmount(invoice)` with amount = 500.00 (valid)
  - [ ] Verify no exception is thrown
  - [ ] Test with amount = 1500.00 (exceeds balance)
  - [ ] Verify exception is thrown
  - [ ] Test with amount = 0.00 (invalid)
  - [ ] Verify exception is thrown

- [ ] Test: Domain logic - `applyToInvoice()`:
  - [ ] Create test method: `testPaymentApplyToInvoice_DomainLogic()`
  - [ ] Create payment entity with amount = 500.00
  - [ ] Create invoice with balance = 1000.00
  - [ ] Call `applyToInvoice()` method
  - [ ] Verify invoice balance = 500.00
  - [ ] Test with amount = 1000.00 (full payment)
  - [ ] Verify invoice balance = 0.00
  - [ ] Verify invoice status = PAID

- [ ] Test: Performance (response time < 200ms):
  - [ ] Create test method: `testRecordPayment_Performance()`
  - [ ] Create and save invoice
  - [ ] Measure response time for POST request
  - [ ] Verify response time is < 200ms
  - [ ] Repeat for other endpoints (get by ID, list, list by invoice)

- [ ] Verify all tests pass:
  - [ ] Run all integration tests: `mvn test`
  - [ ] Verify no test failures
  - [ ] Verify test coverage is reasonable
  - [ ] Verify domain logic tests pass

---

## Verification Tasks

### 11. End-to-End Verification
- [ ] Backend starts without errors:
  - [ ] Run `mvn spring-boot:run` or start from IDE
  - [ ] Verify no compilation errors
  - [ ] Verify application starts on port 8080
  - [ ] Check logs for any warnings or errors

- [ ] Database migration runs successfully:
  - [ ] Check Flyway logs on startup
  - [ ] Verify `V4__create_payments_table.sql` is executed
  - [ ] Verify `payments` table exists in database
  - [ ] Verify index exists on `invoice_id` column
  - [ ] Verify foreign key constraints are enforced

- [ ] OpenAPI documentation is accurate:
  - [ ] Access `http://localhost:8080/swagger-ui.html`
  - [ ] Verify Payment endpoints are documented
  - [ ] Verify request/response schemas are correct
  - [ ] Verify status codes are documented
  - [ ] Verify query parameters are documented (invoiceId, pagination)

- [ ] All CRUD operations work:
  - [ ] Test POST `/api/payments` (record payment)
  - [ ] Test GET `/api/payments/{id}` (get by ID)
  - [ ] Test GET `/api/payments?page=0&size=20&sort=paymentDate,desc` (list with pagination)
  - [ ] Test GET `/api/payments?invoiceId={uuid}&page=0&size=20` (list by invoice)
  - [ ] Test POST `/api/invoices/{id}/payments` (convenience endpoint, if implemented)

- [ ] Error handling works correctly:
  - [ ] Test 404 for non-existent payment
  - [ ] Test 404 for non-existent invoice (when recording payment)
  - [ ] Test 400 for invalid request data
  - [ ] Test 422 for business rule violations (e.g., payment amount exceeds balance)
  - [ ] Verify error responses match `ErrorResponse` format

- [ ] Domain entity has rich behavior:
  - [ ] Verify `Payment` entity has `validateAmount()` method
  - [ ] Verify `Payment` entity has `applyToInvoice()` method
  - [ ] Verify business logic is in entity (not in services)
  - [ ] Verify entity is not anemic (has behavior, not just data)

- [ ] CQRS separation is clear:
  - [ ] Verify `PaymentCommandService` only handles writes (record payment)
  - [ ] Verify `PaymentQueryService` only handles reads (get by ID, list by invoice, list all)
  - [ ] Verify command service uses `@Transactional` (not read-only)
  - [ ] Verify query service uses `@Transactional(readOnly = true)`

- [ ] Payment validation prevents invalid amounts:
  - [ ] Test payment with amount = 0 (should fail)
  - [ ] Test payment with negative amount (should fail)
  - [ ] Test payment with amount > invoice balance (should fail)
  - [ ] Test payment with valid amount (should succeed)

- [ ] Payment correctly updates invoice balance:
  - [ ] Create invoice with total = 1000.00
  - [ ] Record payment of 300.00
  - [ ] Verify invoice balance = 700.00
  - [ ] Record another payment of 500.00
  - [ ] Verify invoice balance = 200.00

- [ ] Payment transitions invoice to PAID when appropriate:
  - [ ] Create invoice with total = 1000.00
  - [ ] Record payment of 1000.00
  - [ ] Verify invoice balance = 0.00
  - [ ] Verify invoice status = PAID
  - [ ] Test with partial payments that sum to total

- [ ] Pagination works correctly:
  - [ ] Test pagination with different page sizes
  - [ ] Test sorting by different fields (paymentDate, amount)
  - [ ] Test pagination metadata (totalElements, totalPages, etc.)
  - [ ] Test filtering with pagination (invoiceId + pagination)

- [ ] Integration tests pass:
  - [ ] Run all integration tests
  - [ ] Verify all test scenarios pass
  - [ ] Verify no test failures
  - [ ] Verify domain logic tests pass
  - [ ] Verify full flow integration test passes

- [ ] API response times < 200ms:
  - [ ] Measure response time for each endpoint
  - [ ] Verify all endpoints respond in < 200ms
  - [ ] Document any exceptions (if any)

- [ ] Invoice balance calculation is complete:
  - [ ] Verify `Invoice.calculateBalance()` works correctly with payments
  - [ ] Test balance calculation with multiple payments
  - [ ] Verify balance is updated when payments are applied
  - [ ] This completes the Invoice balance calculation from PRD 04

---

## Notes

- **Rich Domain Model**: Ensure `Payment` entity has business logic methods (`validateAmount()`, `applyToInvoice()`), not just getters/setters
- **CQRS Separation**: Keep Command and Query services separate - Command handles writes, Query handles reads
- **Invoice Integration**: Payment must integrate with Invoice's `applyPayment()` method. Ensure both payment and invoice are saved in same transaction
- **Balance Calculation**: Complete the Invoice balance calculation from PRD 04. Decide whether to update `Invoice.calculateBalance()` or calculate in QueryService
- **Transaction Boundaries**: Payment creation updates both Payment and Invoice. Ensure both are in the same transaction to avoid inconsistencies
- **Validation Order**: Validate payment amount before applying to invoice. Use domain method `Payment.validateAmount(Invoice)` for validation
- **Status Transition**: When balance reaches zero, `Invoice.applyPayment()` transitions to PAID. Ensure this is tested and works correctly
- **MapStruct Configuration**: Ensure Lombok processes before MapStruct (processor order in Maven)
- **Database Migration**: Ensure `V4__create_payments_table.sql` follows Flyway naming and uses PostgreSQL-compatible SQL
- **Error Handling**: Use `EntityNotFoundException` for 404s, `BusinessRuleViolationException` for invalid amounts (handled by `GlobalExceptionHandler` from PRD 01)
- **Pagination**: Verify `Pageable` is correctly passed from Controller → QueryService → Repository
- **Transaction Boundaries**: Command service should be `@Transactional`, Query service should be `@Transactional(readOnly = true)`
- **Invoice Reference**: Payment requires valid invoice. Handle invoice not found and foreign key constraints
- **Full Flow Test**: The full flow integration test (Customer → Invoice → Payment) is a key deliverable
- **Convenience Endpoint**: Consider implementing `POST /api/invoices/{id}/payments` for convenience, but main endpoint is `POST /api/payments`

---

## Completion Checklist

Before marking PRD 06 as complete, verify:

- [x] All domain layer tasks completed ✅
- [x] All application layer tasks completed ✅
- [x] All infrastructure layer tasks completed ✅
- [x] All presentation layer tasks completed ✅
- [x] All database tasks completed ✅
- [x] All integration with Invoice tasks completed ✅
- [x] All testing tasks completed ✅
- [x] All verification tasks passed ✅
- [x] Payment CRUD operations work end-to-end ✅ (verified via endpoint testing)
- [x] Payment validation prevents invalid amounts ✅ (tested: overpayment rejected)
- [x] Payment correctly updates invoice balance ✅ (tested: 1000 - 600 = 400, then 400 - 400 = 0)
- [x] Payment transitions invoice to PAID when appropriate ✅ (tested: status changed to PAID)
- [x] Domain entity has rich behavior (not anemic) ✅ (validateAmount(), applyToInvoice() methods)
- [x] CQRS separation is clear (Command vs Query services) ✅
- [x] Integration tests created ✅ (PaymentIntegrationTest with 13 scenarios)
- [x] Endpoint tests pass ✅ (all endpoints verified working - see TEST_SUMMARY_PRD_06.md)
- [x] API response times < 200ms ✅ (14-16ms average, well under requirement)
- [x] OpenAPI documentation is accurate ✅ (Swagger UI accessible)
- [x] Invoice balance calculation is complete (from PRD 04) ✅ (InvoiceQueryService updated)
- [x] Ready for PRD 07 (Payment Frontend) to begin ✅ (endpoints fully functional)

---

**Status**: ✅ Completed  
**Last Updated**: November 7, 2024

