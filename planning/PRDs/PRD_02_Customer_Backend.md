# PRD 02: Customer Feature - Backend

## Overview
**Priority**: 🟢 Can run in parallel with PRDs 03-07  
**Dependencies**: PRD 01 (Foundation)  
**Enables**: PRD 03 (Customer Frontend) - **but PRD 03 can start without this using mock data**

This PRD implements the complete backend for Customer management following DDD, CQRS, and Vertical Slice Architecture principles.

## Objectives
1. Implement Customer domain entity with rich behavior
2. Create Customer Command and Query services (CQRS)
3. Implement Customer REST API endpoints
4. Set up database persistence for Customers
5. Create integration tests for Customer operations

## Domain Layer

### Customer Entity
- [ ] Create `Customer` entity in `domain/customer/` package
- [ ] Properties: `id`, `name`, `email`, `address`, `phone`, `createdAt`, `updatedAt`
- [ ] Rich domain methods:
  - `validate()` - validates email format, required fields
  - `updateDetails(name, email, address, phone)` - updates customer with validation
- [ ] JPA annotations: `@Entity`, `@Id`, `@GeneratedValue`, `@Column`
- [ ] Timestamp handling: `@CreatedDate`, `@LastModifiedDate` (or manual)

## Application Layer

### DTOs
- [ ] `CustomerRequest` - for create/update operations
  - Fields: `name`, `email`, `address`, `phone`
  - Validation annotations: `@NotNull`, `@Email`, `@Size`
- [ ] `CustomerResponse` - for API responses
  - Fields: `id`, `name`, `email`, `address`, `phone`, `createdAt`, `updatedAt`

### Mapper
- [ ] Create `CustomerMapper` interface with MapStruct
- [ ] Methods: `toResponse(Customer)`, `toEntity(CustomerRequest)`
- [ ] Configure `@Mapper(componentModel = "spring")`

### Command Service
- [ ] Create `CustomerCommandService` in `application/customer/`
- [ ] Annotate with `@Service` and `@Transactional`
- [ ] Methods:
  - `createCustomer(CustomerRequest)` → `CustomerResponse`
  - `updateCustomer(UUID id, CustomerRequest)` → `CustomerResponse`
  - `deleteCustomer(UUID id)` → `void`
- [ ] Business logic: Use domain entity methods for validation
- [ ] Error handling: Throw `EntityNotFoundException` for 404

### Query Service
- [ ] Create `CustomerQueryService` in `application/customer/`
- [ ] Annotate with `@Service` and `@Transactional(readOnly = true)`
- [ ] Methods:
  - `getById(UUID id)` → `CustomerResponse`
  - `getAll(Pageable pageable)` → `Page<CustomerResponse>`
- [ ] Use repository with pagination support

## Infrastructure Layer

### Repository
- [ ] Create `CustomerRepository` interface extending `JpaRepository<Customer, UUID>`
- [ ] Optional: Add custom query methods if needed
- [ ] Location: `infrastructure/persistence/`

## Presentation Layer

### Controller
- [ ] Create `CustomerController` in `presentation/rest/`
- [ ] Base path: `/api/customers`
- [ ] Endpoints:
  - `POST /api/customers` - Create customer
  - `GET /api/customers/{id}` - Get by ID
  - `GET /api/customers?page=0&size=20&sort=name,asc` - List all (paginated)
  - `PUT /api/customers/{id}` - Update customer
  - `DELETE /api/customers/{id}` - Delete customer
- [ ] Use `@PageableDefault(size = 20, sort = "name")` for list endpoint
- [ ] OpenAPI annotations for Swagger documentation
- [ ] Proper HTTP status codes: 200, 201, 204, 404

## Database

### Migration
- [ ] Create Flyway migration: `V2__create_customers_table.sql`
- [ ] Schema:
  ```sql
  CREATE TABLE customers (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      name VARCHAR(255) NOT NULL,
      email VARCHAR(255) UNIQUE NOT NULL,
      address TEXT,
      phone VARCHAR(50),
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  );
  CREATE INDEX idx_customers_email ON customers(email);
  ```

## Testing

### Integration Tests
- [ ] Create `CustomerIntegrationTest` with `@SpringBootTest` and Testcontainers
- [ ] Test scenarios:
  - Create customer successfully
  - Get customer by ID
  - List customers with pagination
  - Update customer
  - Delete customer
  - Handle not found (404)
  - Validate email format
  - Validate required fields
- [ ] Verify response times < 200ms

## Success Criteria
- [ ] Customer CRUD operations work end-to-end
- [ ] Domain entity has rich behavior (not anemic)
- [ ] CQRS separation is clear (Command vs Query services)
- [ ] Pagination works correctly
- [ ] Integration tests pass
- [ ] API response times < 200ms
- [ ] OpenAPI documentation is accurate

## Dependencies
- **Requires**: PRD 01 (Foundation)
- **Enables**: PRD 03 (Customer Frontend) - *Note: PRD 03 can start independently using mock data*
- **Can run parallel with**: PRDs 03, 04, 05, 06, 07

## Timeline Estimate
**0.5-1 day** (can be done in parallel with other features)

## Notes
- This is the simplest feature, good starting point
- Demonstrates all architectural patterns (DDD, CQRS, VSA)
- Frontend can start once this is complete (or use mock data from PRD 01)

