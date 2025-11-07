# Task List: PRD 04 - Invoice Feature Backend

## Overview
**Priority**: 🟢 Can run in parallel with PRDs 02, 03, 05, 06, 07  
**Estimated Time**: 1-1.5 days (most complex feature, backend-heavy)  
**Dependencies**: PRD 01 (Foundation), PRD 02 (Customer Backend) - for customer reference  
**Enables**: PRD 05 (Invoice Frontend) - *Note: PRD 05 can start independently using mock data*

This task list breaks down PRD 04 into actionable, checkable tasks for implementing the complete Invoice backend feature following DDD, CQRS, and Vertical Slice Architecture principles. This is the most complex feature due to rich domain logic, lifecycle management, and line items support.

---

## Domain Layer Tasks

### 1. InvoiceStatus Enum
- [ ] Create `InvoiceStatus` enum:
  - [ ] Create `src/main/java/com/invoiceme/domain/invoice/InvoiceStatus.java`
  - [ ] Add package declaration: `package com.invoiceme.domain.invoice;`
  - [ ] Add enum values: `DRAFT`, `SENT`, `PAID`
  - [ ] Verify enum is in domain package (not application or infrastructure)

### 2. LineItem Value Object
- [ ] Create `LineItem` embeddable class:
  - [ ] Create `src/main/java/com/invoiceme/domain/invoice/LineItem.java`
  - [ ] Add package declaration: `package com.invoiceme.domain.invoice;`
  - [ ] Add `@Embeddable` annotation (JPA)
  - [ ] Import: `import javax.persistence.Embeddable;` or `jakarta.persistence.Embeddable;`

- [ ] Implement value object properties:
  - [ ] Add `description` field: `String` type with `@Column(nullable = false, length = 500)`
  - [ ] Add `quantity` field: `Integer` type with `@Column(nullable = false)`
  - [ ] Add `unitPrice` field: `BigDecimal` type with `@Column(nullable = false, precision = 19, scale = 2)`
  - [ ] Use `@NotNull` validation annotations on fields

- [ ] Implement rich domain method:
  - [ ] Create `calculateSubtotal()` method:
    - [ ] Return type: `BigDecimal`
    - [ ] Implementation: `quantity * unitPrice`
    - [ ] Handle null values appropriately
    - [ ] Use `BigDecimal` arithmetic (not primitive operations)

- [ ] Implement validation:
  - [ ] Add validation in constructor or setter:
    - [ ] Validate `quantity > 0` (throw `IllegalArgumentException` if invalid)
    - [ ] Validate `unitPrice >= 0` (throw `IllegalArgumentException` if invalid)
  - [ ] Or use `@Min(1)` and `@DecimalMin("0")` annotations (if supported in embeddable)

- [ ] Add constructor(s):
  - [ ] Add no-args constructor (required by JPA)
  - [ ] Add constructor with all fields (`description`, `quantity`, `unitPrice`) that validates

- [ ] Verify value object principles:
  - [ ] LineItem is immutable (or has controlled mutability)
  - [ ] Business logic is in value object methods
  - [ ] No anemic value object (has behavior, not just data)

### 3. Invoice Entity
- [ ] Create `Invoice` entity class:
  - [ ] Create `src/main/java/com/invoiceme/domain/invoice/Invoice.java`
  - [ ] Add package declaration: `package com.invoiceme.domain.invoice;`

- [ ] Add JPA entity annotations:
  - [ ] Add `@Entity` annotation
  - [ ] Add `@Table(name = "invoices")` annotation
  - [ ] Verify entity is in domain package (not infrastructure)

- [ ] Implement entity properties:
  - [ ] Add `id` field: `UUID` type with `@Id` and `@GeneratedValue(strategy = GenerationType.UUID)`
  - [ ] Add `customer` field: `Customer` type with `@ManyToOne` and `@JoinColumn(name = "customer_id")`
    - [ ] Import Customer entity: `import com.invoiceme.domain.customer.Customer;`
    - [ ] Add `@NotNull` validation
  - [ ] Add `status` field: `InvoiceStatus` enum type with `@Enumerated(EnumType.STRING)` and `@Column(nullable = false)`
  - [ ] Add `lineItems` field: `List<LineItem>` type with `@ElementCollection` and `@CollectionTable`
    - [ ] Add `@ElementCollection(fetch = FetchType.EAGER)` annotation
    - [ ] Add `@CollectionTable(name = "invoice_line_items", joinColumns = @JoinColumn(name = "invoice_id"))`
    - [ ] Add `@OrderColumn(name = "line_order")` to preserve order
    - [ ] Initialize as `new ArrayList<>()` to avoid null
  - [ ] Add `totalAmount` field: `BigDecimal` type with `@Column(nullable = false, precision = 19, scale = 2)`
  - [ ] Add `balance` field: `BigDecimal` type with `@Column(nullable = false, precision = 19, scale = 2)`
  - [ ] Add `createdAt` field: `LocalDateTime` type
  - [ ] Add `updatedAt` field: `LocalDateTime` type

- [ ] Implement timestamp handling:
  - [ ] Option A: Use JPA Auditing (requires `@EnableJpaAuditing` in config):
    - [ ] Add `@EntityListeners(AuditingEntityListener.class)`
    - [ ] Add `@CreatedDate` to `createdAt`
    - [ ] Add `@LastModifiedDate` to `updatedAt`
  - [ ] Option B: Manual timestamp handling (simpler):
    - [ ] Add `@PrePersist` method to set `createdAt` and `updatedAt` on create
    - [ ] Add `@PreUpdate` method to set `updatedAt` on update

- [ ] Implement rich domain methods (NOT anemic):
  - [ ] Create `calculateTotal()` method:
    - [ ] Return type: `BigDecimal`
    - [ ] Implementation: Sum all line item subtotals using `calculateSubtotal()`
    - [ ] Handle empty line items (return `BigDecimal.ZERO`)
    - [ ] Use `BigDecimal` arithmetic (not primitive operations)
    - [ ] Update `totalAmount` field and return it

  - [ ] Create `calculateBalance()` method:
    - [ ] Return type: `BigDecimal`
    - [ ] Implementation: `totalAmount - sum(payments)`
    - [ ] Note: Requires payment query (may need to inject PaymentRepository or PaymentQueryService)
    - [ ] For now, can return `totalAmount` if payments not available (stub for PRD 06)
    - [ ] Update `balance` field and return it
    - [ ] Alternative: Calculate balance in QueryService (not in domain) if payments not accessible

  - [ ] Create `addLineItem(String description, Integer quantity, BigDecimal unitPrice)` method:
    - [ ] Return type: `void` or `Invoice` (for method chaining)
    - [ ] Create new `LineItem` with provided parameters
    - [ ] Add to `lineItems` list
    - [ ] Call `calculateTotal()` to recalculate total
    - [ ] Validate line item (quantity > 0, unitPrice >= 0)

  - [ ] Create `canBeMarkedAsSent()` method:
    - [ ] Return type: `boolean`
    - [ ] Validation logic:
      - [ ] Check if invoice has at least one line item
      - [ ] Check if `totalAmount > 0`
      - [ ] Check if `status == InvoiceStatus.DRAFT`
    - [ ] Return `true` only if all conditions are met

  - [ ] Create `markAsSent()` method:
    - [ ] Return type: `void`
    - [ ] Call `canBeMarkedAsSent()` for validation
    - [ ] Throw `IllegalStateException` if validation fails (with descriptive message)
    - [ ] Set `status = InvoiceStatus.SENT`
    - [ ] Update `updatedAt` timestamp

  - [ ] Create `applyPayment(BigDecimal amount)` method:
    - [ ] Return type: `void`
    - [ ] Validate `amount > 0` (throw `IllegalArgumentException` if invalid)
    - [ ] Validate `amount <= balance` (throw `IllegalArgumentException` if exceeds balance)
    - [ ] Update `balance = balance - amount`
    - [ ] If `balance == 0`, set `status = InvoiceStatus.PAID`
    - [ ] Update `updatedAt` timestamp
    - [ ] Note: This method may be called from PaymentCommandService (PRD 06)

  - [ ] Create `canBeEdited()` method:
    - [ ] Return type: `boolean`
    - [ ] Return `true` if `status == InvoiceStatus.DRAFT`
    - [ ] Return `false` otherwise

  - [ ] Create `updateLineItems(List<LineItem> newLineItems)` method (optional helper):
    - [ ] Return type: `void`
    - [ ] Clear existing line items
    - [ ] Add all new line items
    - [ ] Call `calculateTotal()` to recalculate total
    - [ ] Validate all line items

- [ ] Add constructor(s):
  - [ ] Add no-args constructor (required by JPA)
  - [ ] Add constructor for required fields (`customer`) that:
    - [ ] Sets `status = InvoiceStatus.DRAFT`
    - [ ] Initializes `lineItems = new ArrayList<>()`
    - [ ] Sets `totalAmount = BigDecimal.ZERO`
    - [ ] Sets `balance = BigDecimal.ZERO`

- [ ] Verify domain entity follows DDD principles:
  - [ ] Business logic is in entity methods (not in services)
  - [ ] Entity is self-validating
  - [ ] No anemic domain model (has behavior, not just data)
  - [ ] Lifecycle transitions are enforced in domain methods

---

## Application Layer Tasks

### 4. DTOs (Data Transfer Objects)
- [ ] Create `LineItemRequest` DTO:
  - [ ] Create `src/main/java/com/invoiceme/application/invoice/dto/LineItemRequest.java`
  - [ ] Add package declaration: `package com.invoiceme.application.invoice.dto;`
  - [ ] Add fields:
    - [ ] `description`: `String` with `@NotNull` and `@Size(max = 500)`
    - [ ] `quantity`: `Integer` with `@NotNull` and `@Min(1)`
    - [ ] `unitPrice`: `BigDecimal` with `@NotNull` and `@DecimalMin("0")`
  - [ ] Add Lombok annotations: `@Data` or `@Getter`/`@Setter`
  - [ ] Add no-args constructor (Lombok handles this)

- [ ] Create `LineItemResponse` DTO (for response):
  - [ ] Create `src/main/java/com/invoiceme/application/invoice/dto/LineItemResponse.java`
  - [ ] Add package declaration: `package com.invoiceme.application.invoice.dto;`
  - [ ] Add fields:
    - [ ] `description`: `String`
    - [ ] `quantity`: `Integer`
    - [ ] `unitPrice`: `BigDecimal`
    - [ ] `subtotal`: `BigDecimal` (calculated field for convenience)
  - [ ] Add Lombok annotations: `@Data` or `@Getter`/`@Setter`
  - [ ] Add no-args constructor (Lombok handles this)

- [ ] Create `CreateInvoiceRequest` DTO:
  - [ ] Create `src/main/java/com/invoiceme/application/invoice/dto/CreateInvoiceRequest.java`
  - [ ] Add package declaration: `package com.invoiceme.application.invoice.dto;`
  - [ ] Add fields:
    - [ ] `customerId`: `UUID` with `@NotNull`
    - [ ] `lineItems`: `List<LineItemRequest>` with `@NotNull` and `@Size(min = 1)` (at least one line item)
    - [ ] Add `@Valid` annotation on `lineItems` to validate nested objects
  - [ ] Add Lombok annotations: `@Data` or `@Getter`/`@Setter`
  - [ ] Add no-args constructor (Lombok handles this)

- [ ] Create `UpdateInvoiceRequest` DTO:
  - [ ] Create `src/main/java/com/invoiceme/application/invoice/dto/UpdateInvoiceRequest.java`
  - [ ] Add package declaration: `package com.invoiceme.application.invoice.dto;`
  - [ ] Add fields:
    - [ ] `lineItems`: `List<LineItemRequest>` with `@NotNull` and `@Size(min = 1)` (at least one line item)
    - [ ] Add `@Valid` annotation on `lineItems` to validate nested objects
  - [ ] Add Lombok annotations: `@Data` or `@Getter`/`@Setter`
  - [ ] Add no-args constructor (Lombok handles this)

- [ ] Create `InvoiceResponse` DTO:
  - [ ] Create `src/main/java/com/invoiceme/application/invoice/dto/InvoiceResponse.java`
  - [ ] Add package declaration: `package com.invoiceme.application.invoice.dto;`
  - [ ] Add fields:
    - [ ] `id`: `UUID`
    - [ ] `customerId`: `UUID`
    - [ ] `customerName`: `String` (for convenience, included in response)
    - [ ] `status`: `InvoiceStatus` enum (or `String` if preferred)
    - [ ] `lineItems`: `List<LineItemResponse>`
    - [ ] `totalAmount`: `BigDecimal`
    - [ ] `balance`: `BigDecimal`
    - [ ] `createdAt`: `LocalDateTime`
    - [ ] `updatedAt`: `LocalDateTime`
  - [ ] Add Lombok annotations: `@Data` or `@Getter`/`@Setter`
  - [ ] Add no-args constructor (Lombok handles this)

- [ ] Verify DTOs are in application layer:
  - [ ] Check package structure: `application/invoice/dto/`
  - [ ] Verify DTOs have no domain logic (just data transfer)
  - [ ] Verify validation annotations are present

### 5. MapStruct Mapper
- [ ] Create `InvoiceMapper` interface:
  - [ ] Create `src/main/java/com/invoiceme/application/invoice/InvoiceMapper.java`
  - [ ] Add package declaration: `package com.invoiceme.application.invoice;`
  - [ ] Add `@Mapper(componentModel = "spring")` annotation
  - [ ] Import MapStruct: `import org.mapstruct.Mapper;`

- [ ] Implement mapper methods:
  - [ ] Add `toResponse(Invoice invoice)` method:
    - [ ] Return type: `InvoiceResponse`
    - [ ] Map all fields from `Invoice` to `InvoiceResponse`
    - [ ] Map `customer` to `customerId` and `customerName`
    - [ ] Map `lineItems` (List<LineItem>) to `lineItems` (List<LineItemResponse>)
    - [ ] Handle null values appropriately
    - [ ] Use `@Mapping` annotations if field names differ

  - [ ] Add `toEntity(CreateInvoiceRequest request)` method:
    - [ ] Return type: `Invoice`
    - [ ] Map `customerId` to `customer` (requires Customer lookup - handle in service)
    - [ ] Map `lineItems` (List<LineItemRequest>) to `lineItems` (List<LineItem>)
    - [ ] Set `status = InvoiceStatus.DRAFT` (default)
    - [ ] Note: `customer` reference must be set in service (not in mapper)
    - [ ] Use `@Mapping(target = "customer", ignore = true)` and set in service

  - [ ] Add `updateEntity(UpdateInvoiceRequest request, @MappingTarget Invoice invoice)` method:
    - [ ] Return type: `void` (updates existing entity)
    - [ ] Use `@MappingTarget` annotation
    - [ ] Map `lineItems` from request to entity
    - [ ] Note: This updates line items, total will be recalculated in service

  - [ ] Add `lineItemRequestToLineItem(LineItemRequest request)` method:
    - [ ] Return type: `LineItem`
    - [ ] Map all fields from `LineItemRequest` to `LineItem`
    - [ ] Used for mapping line items in lists

  - [ ] Add `lineItemToLineItemResponse(LineItem lineItem)` method:
    - [ ] Return type: `LineItemResponse`
    - [ ] Map all fields from `LineItem` to `LineItemResponse`
    - [ ] Calculate `subtotal` using `lineItem.calculateSubtotal()`
    - [ ] Used for mapping line items in lists

- [ ] Verify MapStruct annotation processor is configured:
  - [ ] Check `pom.xml` has MapStruct dependency
  - [ ] Check annotation processor order (Lombok → MapStruct)
  - [ ] Build project and verify mapper implementation is generated
  - [ ] Check `target/generated-sources/annotations/` for generated mapper

### 6. Command Service (CQRS - Write Operations)
- [ ] Create `InvoiceCommandService`:
  - [ ] Create `src/main/java/com/invoiceme/application/invoice/InvoiceCommandService.java`
  - [ ] Add package declaration: `package com.invoiceme.application.invoice;`
  - [ ] Add `@Service` annotation
  - [ ] Add `@Transactional` annotation (for write operations)

- [ ] Inject dependencies:
  - [ ] Inject `InvoiceRepository` via constructor injection
  - [ ] Inject `CustomerRepository` via constructor injection (for customer lookup)
  - [ ] Inject `InvoiceMapper` via constructor injection
  - [ ] Use `@RequiredArgsConstructor` (Lombok) or manual constructor

- [ ] Implement `createInvoice(CreateInvoiceRequest request)` method:
  - [ ] Return type: `InvoiceResponse`
  - [ ] Find customer by ID: `customerRepository.findById(request.getCustomerId())`
  - [ ] Throw `EntityNotFoundException` if customer not found
  - [ ] Use mapper to convert `CreateInvoiceRequest` to `Invoice` entity
  - [ ] Set `customer` reference on invoice entity
  - [ ] For each line item in request:
    - [ ] Call entity's `addLineItem()` method (adds line item and recalculates total)
  - [ ] Or: Map line items first, then call `calculateTotal()` on entity
  - [ ] Save entity using repository: `invoiceRepository.save(invoice)`
  - [ ] Map saved entity to `InvoiceResponse` using mapper
  - [ ] Return `InvoiceResponse`

- [ ] Implement `updateInvoice(UUID id, UpdateInvoiceRequest request)` method:
  - [ ] Return type: `InvoiceResponse`
  - [ ] Find invoice by ID: `invoiceRepository.findById(id)`
  - [ ] Throw `EntityNotFoundException` if invoice not found
  - [ ] Validate invoice can be edited: Call entity's `canBeEdited()` method
  - [ ] Throw `BusinessRuleViolationException` (or `IllegalStateException`) if invoice cannot be edited
  - [ ] Clear existing line items
  - [ ] For each line item in request:
    - [ ] Call entity's `addLineItem()` method (adds line item and recalculates total)
  - [ ] Or: Use mapper's `updateEntity()` method, then call `calculateTotal()` on entity
  - [ ] Save updated entity: `invoiceRepository.save(invoice)`
  - [ ] Map updated entity to `InvoiceResponse` using mapper
  - [ ] Return `InvoiceResponse`

- [ ] Implement `markAsSent(UUID id)` method:
  - [ ] Return type: `InvoiceResponse`
  - [ ] Find invoice by ID: `invoiceRepository.findById(id)`
  - [ ] Throw `EntityNotFoundException` if invoice not found
  - [ ] Validate invoice can be marked as sent: Call entity's `canBeMarkedAsSent()` method
  - [ ] Throw `BusinessRuleViolationException` (or `IllegalStateException`) if validation fails
  - [ ] Call entity's `markAsSent()` method (transitions status DRAFT → SENT)
  - [ ] Save updated entity: `invoiceRepository.save(invoice)`
  - [ ] Map updated entity to `InvoiceResponse` using mapper
  - [ ] Return `InvoiceResponse`

- [ ] Create `BusinessRuleViolationException` (if not exists):
  - [ ] Create `src/main/java/com/invoiceme/application/invoice/exception/BusinessRuleViolationException.java`
  - [ ] Extend `RuntimeException` or create custom exception
  - [ ] Add constructor with message
  - [ ] Or use existing exception from foundation (if created in PRD 01)

- [ ] Verify CQRS separation:
  - [ ] Command service only handles write operations (create, update, mark as sent)
  - [ ] Command service uses `@Transactional` (not `readOnly = true`)
  - [ ] Business logic delegates to domain entity methods
  - [ ] Service validates business rules before calling domain methods

### 7. Query Service (CQRS - Read Operations)
- [ ] Create `InvoiceQueryService`:
  - [ ] Create `src/main/java/com/invoiceme/application/invoice/InvoiceQueryService.java`
  - [ ] Add package declaration: `package com.invoiceme.application.invoice;`
  - [ ] Add `@Service` annotation
  - [ ] Add `@Transactional(readOnly = true)` annotation (for read operations)

- [ ] Inject dependencies:
  - [ ] Inject `InvoiceRepository` via constructor injection
  - [ ] Inject `InvoiceMapper` via constructor injection
  - [ ] Inject `PaymentRepository` or `PaymentQueryService` (for balance calculation, if PRD 06 complete)
  - [ ] Use `@RequiredArgsConstructor` (Lombok) or manual constructor

- [ ] Implement `getById(UUID id)` method:
  - [ ] Return type: `InvoiceResponse`
  - [ ] Find invoice by ID: `invoiceRepository.findById(id)`
  - [ ] Throw `EntityNotFoundException` if invoice not found
  - [ ] Calculate balance (if PaymentRepository available):
    - [ ] Query payments for invoice: `paymentRepository.findByInvoiceId(id)`
    - [ ] Sum payment amounts
    - [ ] Calculate `balance = totalAmount - sum(payments)`
    - [ ] Update invoice's balance field (or calculate in response mapping)
  - [ ] Or: Calculate balance in domain method `calculateBalance()` (if payments accessible)
  - [ ] Map entity to `InvoiceResponse` using mapper
  - [ ] Return `InvoiceResponse`

- [ ] Implement `getByStatus(InvoiceStatus status, Pageable pageable)` method:
  - [ ] Return type: `Page<InvoiceResponse>`
  - [ ] Find invoices by status: `invoiceRepository.findByStatus(status, pageable)`
  - [ ] Map `Page<Invoice>` to `Page<InvoiceResponse>`:
    - [ ] Use `Page.map()` to transform each entity to response DTO
    - [ ] Calculate balance for each invoice (if payments available)
  - [ ] Return `Page<InvoiceResponse>`

- [ ] Implement `getByCustomerId(UUID customerId, Pageable pageable)` method:
  - [ ] Return type: `Page<InvoiceResponse>`
  - [ ] Find invoices by customer: `invoiceRepository.findByCustomerId(customerId, pageable)`
  - [ ] Map `Page<Invoice>` to `Page<InvoiceResponse>`:
    - [ ] Use `Page.map()` to transform each entity to response DTO
    - [ ] Calculate balance for each invoice (if payments available)
  - [ ] Return `Page<InvoiceResponse>`

- [ ] Implement `getAll(Pageable pageable)` method:
  - [ ] Return type: `Page<InvoiceResponse>`
  - [ ] Find all invoices: `invoiceRepository.findAll(pageable)`
  - [ ] Map `Page<Invoice>` to `Page<InvoiceResponse>`:
    - [ ] Use `Page.map()` to transform each entity to response DTO
    - [ ] Calculate balance for each invoice (if payments available)
  - [ ] Return `Page<InvoiceResponse>`

- [ ] Implement balance calculation helper method (if needed):
  - [ ] Create `calculateBalance(Invoice invoice)` private method
  - [ ] Query payments for invoice
  - [ ] Sum payment amounts
  - [ ] Return `totalAmount - sum(payments)`
  - [ ] Handle case where PaymentRepository is not available (return `totalAmount`)

- [ ] Verify CQRS separation:
  - [ ] Query service only handles read operations (get by ID, list by status/customer, list all)
  - [ ] Query service uses `@Transactional(readOnly = true)`
  - [ ] No write operations in query service
  - [ ] Balance calculation is done in query service (or domain if accessible)

---

## Infrastructure Layer Tasks

### 8. Repository
- [ ] Create `InvoiceRepository` interface:
  - [ ] Create `src/main/java/com/invoiceme/infrastructure/persistence/InvoiceRepository.java`
  - [ ] Add package declaration: `package com.invoiceme.infrastructure.persistence;`
  - [ ] Extend `JpaRepository<Invoice, UUID>`
  - [ ] Import: `import org.springframework.data.jpa.repository.JpaRepository;`
  - [ ] Import domain entity: `import com.invoiceme.domain.invoice.Invoice;`
  - [ ] Import enum: `import com.invoiceme.domain.invoice.InvoiceStatus;`

- [ ] Add custom query methods:
  - [ ] Add `Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);`
    - [ ] Import: `import org.springframework.data.domain.Page;`
    - [ ] Import: `import org.springframework.data.domain.Pageable;`
  - [ ] Add `Page<Invoice> findByCustomerId(UUID customerId, Pageable pageable);`
    - [ ] Import: `import java.util.UUID;`

- [ ] Verify repository interface:
  - [ ] Repository is in infrastructure layer (not domain)
  - [ ] Repository extends `JpaRepository` with correct generic types
  - [ ] Custom query methods follow Spring Data JPA naming conventions
  - [ ] Query methods return `Page<Invoice>` for pagination support

- [ ] Optional: Add additional query methods (if needed):
  - [ ] Example: `List<Invoice> findByCustomerIdAndStatus(UUID customerId, InvoiceStatus status);`
  - [ ] Example: `Optional<Invoice> findByIdAndCustomerId(UUID id, UUID customerId);`
  - [ ] Only add if business requirements need them

---

## Presentation Layer Tasks

### 9. Controller
- [ ] Create `InvoiceController`:
  - [ ] Create `src/main/java/com/invoiceme/presentation/rest/InvoiceController.java`
  - [ ] Add package declaration: `package com.invoiceme.presentation.rest;`
  - [ ] Add `@RestController` annotation
  - [ ] Add `@RequestMapping("/api/invoices")` annotation

- [ ] Inject dependencies:
  - [ ] Inject `InvoiceCommandService` via constructor injection
  - [ ] Inject `InvoiceQueryService` via constructor injection
  - [ ] Use `@RequiredArgsConstructor` (Lombok) or manual constructor

- [ ] Implement `POST /api/invoices` endpoint (Create):
  - [ ] Add `@PostMapping` annotation
  - [ ] Add `@RequestBody @Valid CreateInvoiceRequest request` parameter
  - [ ] Add `@Operation` annotation for OpenAPI documentation
  - [ ] Add `@ApiResponse` annotations (201, 400, 404, 422)
  - [ ] Call `commandService.createInvoice(request)`
  - [ ] Return `ResponseEntity<InvoiceResponse>` with status `HttpStatus.CREATED` (201)
  - [ ] Verify validation errors are handled by `GlobalExceptionHandler` (from PRD 01)

- [ ] Implement `GET /api/invoices/{id}` endpoint (Get by ID):
  - [ ] Add `@GetMapping("/{id}")` annotation
  - [ ] Add `@PathVariable UUID id` parameter
  - [ ] Add `@Operation` annotation for OpenAPI documentation
  - [ ] Add `@ApiResponse` annotations (200, 404)
  - [ ] Call `queryService.getById(id)`
  - [ ] Return `ResponseEntity<InvoiceResponse>` with status `HttpStatus.OK` (200)
  - [ ] Verify 404 is handled by `GlobalExceptionHandler` (throws `EntityNotFoundException`)

- [ ] Implement `GET /api/invoices` endpoint (List all with pagination):
  - [ ] Add `@GetMapping` annotation
  - [ ] Add `@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable` parameter
  - [ ] Add `@Operation` annotation for OpenAPI documentation
  - [ ] Add `@ApiResponse` annotations (200)
  - [ ] Call `queryService.getAll(pageable)`
  - [ ] Return `ResponseEntity<Page<InvoiceResponse>>` with status `HttpStatus.OK` (200)
  - [ ] Verify pagination parameters work: `?page=0&size=20&sort=createdAt,desc`

- [ ] Implement `GET /api/invoices?status={status}` endpoint (Filter by status):
  - [ ] Add `@GetMapping` annotation
  - [ ] Add `@RequestParam(required = false) InvoiceStatus status` parameter
  - [ ] Add `@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable` parameter
  - [ ] Add `@Operation` annotation for OpenAPI documentation
  - [ ] Add `@ApiResponse` annotations (200, 400)
  - [ ] If `status` is provided, call `queryService.getByStatus(status, pageable)`
  - [ ] If `status` is null, call `queryService.getAll(pageable)`
  - [ ] Return `ResponseEntity<Page<InvoiceResponse>>` with status `HttpStatus.OK` (200)
  - [ ] Verify filtering works: `?status=DRAFT&page=0&size=20`

- [ ] Implement `GET /api/invoices?customerId={id}` endpoint (Filter by customer):
  - [ ] Add `@GetMapping` annotation
  - [ ] Add `@RequestParam(required = false) UUID customerId` parameter
  - [ ] Add `@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable` parameter
  - [ ] Add `@Operation` annotation for OpenAPI documentation
  - [ ] Add `@ApiResponse` annotations (200, 404)
  - [ ] If `customerId` is provided, call `queryService.getByCustomerId(customerId, pageable)`
  - [ ] If `customerId` is null, call `queryService.getAll(pageable)`
  - [ ] Return `ResponseEntity<Page<InvoiceResponse>>` with status `HttpStatus.OK` (200)
  - [ ] Verify filtering works: `?customerId={uuid}&page=0&size=20`

- [ ] Implement combined filtering (status + customerId):
  - [ ] Modify `GET /api/invoices` to handle both `status` and `customerId` parameters
  - [ ] If both provided, add custom repository method or filter in service
  - [ ] Or: Create separate endpoint for combined filtering
  - [ ] Document in OpenAPI annotations

- [ ] Implement `PUT /api/invoices/{id}` endpoint (Update):
  - [ ] Add `@PutMapping("/{id}")` annotation
  - [ ] Add `@PathVariable UUID id` parameter
  - [ ] Add `@RequestBody @Valid UpdateInvoiceRequest request` parameter
  - [ ] Add `@Operation` annotation for OpenAPI documentation
  - [ ] Add `@ApiResponse` annotations (200, 400, 404, 422)
  - [ ] Call `commandService.updateInvoice(id, request)`
  - [ ] Return `ResponseEntity<InvoiceResponse>` with status `HttpStatus.OK` (200)
  - [ ] Verify validation and 404/422 errors are handled

- [ ] Implement `POST /api/invoices/{id}/send` endpoint (Mark as Sent):
  - [ ] Add `@PostMapping("/{id}/send")` annotation
  - [ ] Add `@PathVariable UUID id` parameter
  - [ ] Add `@Operation` annotation for OpenAPI documentation
  - [ ] Add `@ApiResponse` annotations (200, 404, 422)
  - [ ] Call `commandService.markAsSent(id)`
  - [ ] Return `ResponseEntity<InvoiceResponse>` with status `HttpStatus.OK` (200)
  - [ ] Verify 404 and 422 (business rule violation) errors are handled

- [ ] Verify OpenAPI documentation:
  - [ ] Start application and access Swagger UI
  - [ ] Verify all endpoints are documented
  - [ ] Verify request/response schemas are correct
  - [ ] Verify status codes are documented
  - [ ] Verify query parameters are documented (status, customerId, pagination)

---

## Database Tasks

### 10. Flyway Migration
- [ ] Create Flyway migration file:
  - [ ] Create `src/main/resources/db/migration/V3__create_invoices_table.sql`
  - [ ] Follow Flyway naming convention: `V{version}__{description}.sql`
  - [ ] Ensure version number is sequential (V3 comes after V2 for customers)

- [ ] Implement database schema:
  - [ ] Create `invoices` table:
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
    ```
  - [ ] Create indexes:
    ```sql
    CREATE INDEX idx_invoices_customer_id ON invoices(customer_id);
    CREATE INDEX idx_invoices_status ON invoices(status);
    ```
  - [ ] Create `invoice_line_items` table:
    ```sql
    CREATE TABLE invoice_line_items (
        invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
        description VARCHAR(500) NOT NULL,
        quantity INTEGER NOT NULL CHECK (quantity > 0),
        unit_price DECIMAL(19,2) NOT NULL CHECK (unit_price >= 0),
        line_order INTEGER NOT NULL,
        PRIMARY KEY (invoice_id, line_order)
    );
    ```
  - [ ] Create index on invoice_id (for faster lookups):
    ```sql
    CREATE INDEX idx_invoice_line_items_invoice_id ON invoice_line_items(invoice_id);
    ```
  - [ ] Verify SQL syntax is correct for PostgreSQL (used by Testcontainers)
  - [ ] Verify foreign key constraints are correct
  - [ ] Verify CHECK constraints enforce business rules

- [ ] Verify migration runs successfully:
  - [ ] Start application
  - [ ] Check Flyway logs for successful migration
  - [ ] Verify `invoices` table exists in database
  - [ ] Verify `invoice_line_items` table exists in database
  - [ ] Verify indexes exist on `customer_id`, `status`, and `invoice_id` columns
  - [ ] Verify foreign key constraints are enforced

- [ ] Test migration with H2 (development):
  - [ ] Ensure H2 supports UUID and gen_random_uuid() (or use alternative)
  - [ ] If H2 doesn't support, use `RANDOM_UUID()` or `java.util.UUID.randomUUID()`
  - [ ] Or create separate migration for H2 (not recommended, use PostgreSQL-compatible SQL)

---

## Testing Tasks

### 11. Integration Tests
- [ ] Create `InvoiceIntegrationTest` class:
  - [ ] Create `src/test/java/com/invoiceme/InvoiceIntegrationTest.java`
  - [ ] Extend base test class (if created in PRD 01) or use `@SpringBootTest`
  - [ ] Add `@Testcontainers` annotation (if using Testcontainers)
  - [ ] Add `@AutoConfigureMockMvc` or use `@WebMvcTest` (if testing controller layer)
  - [ ] Add `@Transactional` annotation for test cleanup

- [ ] Set up test dependencies:
  - [ ] Inject `MockMvc` (for controller tests) or use `TestRestTemplate`
  - [ ] Inject `InvoiceRepository` (for direct repository tests)
  - [ ] Inject `CustomerRepository` (for creating test customers)
  - [ ] Inject `InvoiceCommandService` and `InvoiceQueryService` (for service tests)

- [ ] Test: Create invoice with line items (DRAFT status):
  - [ ] Create test method: `testCreateInvoice_WithLineItems_Success()`
  - [ ] Create and save customer in database
  - [ ] Create `CreateInvoiceRequest` with customer ID and line items
  - [ ] Call POST endpoint or command service
  - [ ] Verify response status is 201 (or 200 for service)
  - [ ] Verify response contains invoice data
  - [ ] Verify invoice is saved in database with DRAFT status
  - [ ] Verify line items are saved correctly
  - [ ] Verify total amount is calculated correctly

- [ ] Test: Get invoice by ID:
  - [ ] Create test method: `testGetInvoiceById_Success()`
  - [ ] Create and save invoice in database
  - [ ] Call GET endpoint or query service with invoice ID
  - [ ] Verify response status is 200
  - [ ] Verify response contains correct invoice data
  - [ ] Verify line items are included in response

- [ ] Test: List invoices with pagination:
  - [ ] Create test method: `testGetAllInvoices_WithPagination()`
  - [ ] Create multiple invoices in database (at least 3)
  - [ ] Call GET endpoint with pagination: `?page=0&size=2`
  - [ ] Verify response status is 200
  - [ ] Verify response contains `Page` with correct size
  - [ ] Verify pagination metadata (totalElements, totalPages, etc.)
  - [ ] Test sorting: `?sort=createdAt,desc` and `?sort=createdAt,asc`

- [ ] Test: Filter invoices by status:
  - [ ] Create test method: `testGetInvoicesByStatus_Success()`
  - [ ] Create invoices with different statuses (DRAFT, SENT)
  - [ ] Call GET endpoint with status filter: `?status=DRAFT`
  - [ ] Verify response status is 200
  - [ ] Verify response contains only invoices with DRAFT status
  - [ ] Test with SENT status: `?status=SENT`

- [ ] Test: Filter invoices by customer:
  - [ ] Create test method: `testGetInvoicesByCustomerId_Success()`
  - [ ] Create multiple customers
  - [ ] Create invoices for different customers
  - [ ] Call GET endpoint with customer filter: `?customerId={uuid}`
  - [ ] Verify response status is 200
  - [ ] Verify response contains only invoices for specified customer

- [ ] Test: Update invoice (only if DRAFT):
  - [ ] Create test method: `testUpdateInvoice_DraftStatus_Success()`
  - [ ] Create and save invoice in DRAFT status
  - [ ] Create `UpdateInvoiceRequest` with new line items
  - [ ] Call PUT endpoint or command service
  - [ ] Verify response status is 200
  - [ ] Verify response contains updated invoice data
  - [ ] Verify invoice is updated in database
  - [ ] Verify total amount is recalculated correctly

- [ ] Test: Update invoice (invalid: not DRAFT):
  - [ ] Create test method: `testUpdateInvoice_NotDraft_Fails()`
  - [ ] Create and save invoice in SENT status
  - [ ] Create `UpdateInvoiceRequest` with new line items
  - [ ] Call PUT endpoint or command service
  - [ ] Verify response status is 422 (or 400) - business rule violation
  - [ ] Verify error message indicates invoice cannot be edited

- [ ] Test: Mark invoice as SENT (valid transition):
  - [ ] Create test method: `testMarkInvoiceAsSent_ValidTransition_Success()`
  - [ ] Create and save invoice in DRAFT status with line items and total > 0
  - [ ] Call POST `/api/invoices/{id}/send` endpoint or command service
  - [ ] Verify response status is 200
  - [ ] Verify response contains invoice with SENT status
  - [ ] Verify invoice status is updated in database to SENT

- [ ] Test: Mark invoice as SENT (invalid: no line items):
  - [ ] Create test method: `testMarkInvoiceAsSent_NoLineItems_Fails()`
  - [ ] Create and save invoice in DRAFT status with no line items
  - [ ] Call POST `/api/invoices/{id}/send` endpoint or command service
  - [ ] Verify response status is 422 (or 400) - business rule violation
  - [ ] Verify error message indicates invoice cannot be marked as sent

- [ ] Test: Mark invoice as SENT (invalid: already SENT):
  - [ ] Create test method: `testMarkInvoiceAsSent_AlreadySent_Fails()`
  - [ ] Create and save invoice in SENT status
  - [ ] Call POST `/api/invoices/{id}/send` endpoint or command service
  - [ ] Verify response status is 422 (or 400) - business rule violation
  - [ ] Verify error message indicates invalid status transition

- [ ] Test: Mark invoice as SENT (invalid: total is zero):
  - [ ] Create test method: `testMarkInvoiceAsSent_ZeroTotal_Fails()`
  - [ ] Create and save invoice in DRAFT status with line items but total = 0
  - [ ] Call POST `/api/invoices/{id}/send` endpoint or command service
  - [ ] Verify response status is 422 (or 400) - business rule violation
  - [ ] Verify error message indicates invoice cannot be marked as sent

- [ ] Test: Calculate total correctly:
  - [ ] Create test method: `testCalculateTotal_Correct()`
  - [ ] Create invoice with multiple line items
  - [ ] Verify total amount = sum of all line item subtotals
  - [ ] Test with different quantities and prices
  - [ ] Test with decimal prices

- [ ] Test: Calculate balance correctly (if payments available):
  - [ ] Create test method: `testCalculateBalance_Correct()`
  - [ ] Create invoice with total amount
  - [ ] Create payments for invoice (if PRD 06 complete)
  - [ ] Verify balance = totalAmount - sum(payments)
  - [ ] Test with partial payments
  - [ ] Test with overpayment (should fail validation)

- [ ] Test: Handle not found (404):
  - [ ] Create test method: `testGetInvoiceById_NotFound()`
  - [ ] Call GET endpoint with non-existent UUID
  - [ ] Verify response status is 404
  - [ ] Verify error response format matches `ErrorResponse` DTO
  - [ ] Repeat for PUT, POST `/send` endpoints

- [ ] Test: Validate required fields:
  - [ ] Create test method: `testCreateInvoice_MissingRequiredFields()`
  - [ ] Create `CreateInvoiceRequest` with null `customerId` or empty `lineItems`
  - [ ] Call POST endpoint
  - [ ] Verify response status is 400 (validation error)
  - [ ] Verify error response contains validation details

- [ ] Test: Validate line item constraints:
  - [ ] Create test method: `testCreateInvoice_InvalidLineItem()`
  - [ ] Create `CreateInvoiceRequest` with line item having quantity = 0 or negative
  - [ ] Create `CreateInvoiceRequest` with line item having negative unit price
  - [ ] Call POST endpoint
  - [ ] Verify response status is 400 (validation error)
  - [ ] Verify error response contains validation details

- [ ] Test: Customer not found:
  - [ ] Create test method: `testCreateInvoice_CustomerNotFound()`
  - [ ] Create `CreateInvoiceRequest` with non-existent customer ID
  - [ ] Call POST endpoint
  - [ ] Verify response status is 404
  - [ ] Verify error message indicates customer not found

- [ ] Test: Domain logic - `calculateTotal()`:
  - [ ] Create test method: `testInvoiceCalculateTotal_DomainLogic()`
  - [ ] Create invoice entity directly
  - [ ] Add line items using `addLineItem()` method
  - [ ] Call `calculateTotal()` method
  - [ ] Verify total is calculated correctly
  - [ ] Verify total is updated in entity

- [ ] Test: Domain logic - `canBeMarkedAsSent()`:
  - [ ] Create test method: `testInvoiceCanBeMarkedAsSent_DomainLogic()`
  - [ ] Create invoice in DRAFT status with line items and total > 0
  - [ ] Call `canBeMarkedAsSent()` method
  - [ ] Verify returns `true`
  - [ ] Test with no line items (should return `false`)
  - [ ] Test with total = 0 (should return `false`)
  - [ ] Test with status = SENT (should return `false`)

- [ ] Test: Domain logic - `markAsSent()`:
  - [ ] Create test method: `testInvoiceMarkAsSent_DomainLogic()`
  - [ ] Create invoice in DRAFT status with valid conditions
  - [ ] Call `markAsSent()` method
  - [ ] Verify status is changed to SENT
  - [ ] Test with invalid conditions (should throw exception)

- [ ] Test: Performance (response time < 200ms):
  - [ ] Create test method: `testGetInvoiceById_Performance()`
  - [ ] Create and save invoice
  - [ ] Measure response time for GET request
  - [ ] Verify response time is < 200ms
  - [ ] Repeat for other endpoints (create, update, list, mark as sent)

- [ ] Verify all tests pass:
  - [ ] Run all integration tests: `mvn test`
  - [ ] Verify no test failures
  - [ ] Verify test coverage is reasonable
  - [ ] Verify domain logic tests pass

---

## Verification Tasks

### 12. End-to-End Verification
- [ ] Backend starts without errors:
  - [ ] Run `mvn spring-boot:run` or start from IDE
  - [ ] Verify no compilation errors
  - [ ] Verify application starts on port 8080
  - [ ] Check logs for any warnings or errors

- [ ] Database migration runs successfully:
  - [ ] Check Flyway logs on startup
  - [ ] Verify `V3__create_invoices_table.sql` is executed
  - [ ] Verify `invoices` table exists in database
  - [ ] Verify `invoice_line_items` table exists in database
  - [ ] Verify indexes exist on columns
  - [ ] Verify foreign key constraints are enforced

- [ ] OpenAPI documentation is accurate:
  - [ ] Access `http://localhost:8080/swagger-ui.html`
  - [ ] Verify Invoice endpoints are documented
  - [ ] Verify request/response schemas are correct
  - [ ] Verify status codes are documented
  - [ ] Verify query parameters are documented (status, customerId, pagination)

- [ ] All CRUD operations work:
  - [ ] Test POST `/api/invoices` (create)
  - [ ] Test GET `/api/invoices/{id}` (get by ID)
  - [ ] Test GET `/api/invoices?page=0&size=20&sort=createdAt,desc` (list with pagination)
  - [ ] Test GET `/api/invoices?status=DRAFT` (filter by status)
  - [ ] Test GET `/api/invoices?customerId={uuid}` (filter by customer)
  - [ ] Test PUT `/api/invoices/{id}` (update)
  - [ ] Test POST `/api/invoices/{id}/send` (mark as sent)

- [ ] Error handling works correctly:
  - [ ] Test 404 for non-existent invoice
  - [ ] Test 404 for non-existent customer (when creating invoice)
  - [ ] Test 400 for invalid request data
  - [ ] Test 422 for business rule violations (e.g., marking non-DRAFT invoice as sent)
  - [ ] Verify error responses match `ErrorResponse` format

- [ ] Domain entity has rich behavior:
  - [ ] Verify `Invoice` entity has `calculateTotal()` method
  - [ ] Verify `Invoice` entity has `canBeMarkedAsSent()` method
  - [ ] Verify `Invoice` entity has `markAsSent()` method
  - [ ] Verify `Invoice` entity has `addLineItem()` method
  - [ ] Verify `Invoice` entity has `canBeEdited()` method
  - [ ] Verify `LineItem` value object has `calculateSubtotal()` method
  - [ ] Verify business logic is in entity (not in services)
  - [ ] Verify entity is not anemic (has behavior, not just data)

- [ ] CQRS separation is clear:
  - [ ] Verify `InvoiceCommandService` only handles writes (create, update, mark as sent)
  - [ ] Verify `InvoiceQueryService` only handles reads (get by ID, list by status/customer, list all)
  - [ ] Verify command service uses `@Transactional` (not read-only)
  - [ ] Verify query service uses `@Transactional(readOnly = true)`

- [ ] Lifecycle management works correctly:
  - [ ] Verify invoices are created in DRAFT status
  - [ ] Verify DRAFT invoices can be edited
  - [ ] Verify SENT invoices cannot be edited
  - [ ] Verify only DRAFT invoices can be marked as SENT
  - [ ] Verify status transitions are enforced (DRAFT → SENT)

- [ ] Line items are stored and calculated correctly:
  - [ ] Verify line items are saved in database with correct order
  - [ ] Verify line item subtotals are calculated correctly
  - [ ] Verify invoice total is sum of all line item subtotals
  - [ ] Verify line items are included in API responses

- [ ] Pagination works correctly:
  - [ ] Test pagination with different page sizes
  - [ ] Test sorting by different fields (createdAt, totalAmount)
  - [ ] Test pagination metadata (totalElements, totalPages, etc.)
  - [ ] Test filtering with pagination (status + pagination, customerId + pagination)

- [ ] Integration tests pass:
  - [ ] Run all integration tests
  - [ ] Verify all test scenarios pass
  - [ ] Verify no test failures
  - [ ] Verify domain logic tests pass

- [ ] API response times < 200ms:
  - [ ] Measure response time for each endpoint
  - [ ] Verify all endpoints respond in < 200ms
  - [ ] Document any exceptions (if any)

- [ ] Balance calculation (if payments available):
  - [ ] Test balance calculation with payments (if PRD 06 complete)
  - [ ] Verify balance = totalAmount - sum(payments)
  - [ ] Verify balance is updated when payments are applied

---

## Notes

- **Rich Domain Model**: Ensure `Invoice` entity has business logic methods (`calculateTotal()`, `canBeMarkedAsSent()`, `markAsSent()`, `addLineItem()`, `canBeEdited()`), not just getters/setters
- **LineItem Value Object**: Ensure `LineItem` has `calculateSubtotal()` method and validation
- **CQRS Separation**: Keep Command and Query services separate - Command handles writes, Query handles reads
- **Lifecycle Management**: Invoice lifecycle (DRAFT → SENT → PAID) must be enforced in domain methods
- **Balance Calculation**: Balance calculation may require PRD 06 (Payment Backend) for full testing. Can stub initially or calculate in QueryService
- **MapStruct Configuration**: Ensure Lombok processes before MapStruct (processor order in Maven)
- **ElementCollection**: Ensure `@OrderColumn` is used to preserve line item order
- **Database Migration**: Ensure `V3__create_invoices_table.sql` follows Flyway naming and uses PostgreSQL-compatible SQL
- **Error Handling**: Use `EntityNotFoundException` for 404s, `BusinessRuleViolationException` for invalid transitions (handled by `GlobalExceptionHandler` from PRD 01)
- **Pagination**: Verify `Pageable` is correctly passed from Controller → QueryService → Repository
- **Transaction Boundaries**: Command service should be `@Transactional`, Query service should be `@Transactional(readOnly = true)`
- **Customer Reference**: Invoice requires valid customer. Handle customer not found and foreign key constraints
- **Edit Restrictions**: Only DRAFT invoices can be edited. Use `canBeEdited()` domain method for validation
- **Status Transitions**: Only valid transitions allowed (DRAFT → SENT). Enforce in domain methods, not just in service layer

---

## Completion Checklist

Before marking PRD 04 as complete, verify:

- [ ] All domain layer tasks completed
- [ ] All application layer tasks completed
- [ ] All infrastructure layer tasks completed
- [ ] All presentation layer tasks completed
- [ ] All database tasks completed
- [ ] All testing tasks completed
- [ ] All verification tasks passed
- [ ] Invoice CRUD operations work end-to-end
- [ ] Domain entity has rich behavior (not anemic)
- [ ] Invoice lifecycle (DRAFT → SENT) is enforced
- [ ] Line items are stored and calculated correctly
- [ ] Total amount calculation is correct
- [ ] CQRS separation is clear (Command vs Query services)
- [ ] Integration tests pass
- [ ] API response times < 200ms
- [ ] OpenAPI documentation is accurate
- [ ] Ready for PRD 05 (Invoice Frontend) to begin (or can start independently with mock data)

---

**Status**: ⏳ Not Started  
**Last Updated**: [Date when last updated]

