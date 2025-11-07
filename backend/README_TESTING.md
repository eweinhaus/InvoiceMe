# Testing Instructions for PRD 02 - Customer Backend

## Prerequisites

### Java 17 Installation

The project requires Java 17. If you don't have it installed:

**macOS (using Homebrew):**
```bash
brew install openjdk@17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

**Or using SDKMAN:**
```bash
sdk install java 17.0.9-tem
sdk use java 17.0.9-tem
```

**Verify Java version:**
```bash
java -version
# Should show: openjdk version "17.x.x"
```

## Compilation and Testing

### 1. Compile the Project

```bash
cd backend
mvn clean compile
```

This will:
- Compile all Java source files
- Generate MapStruct mapper implementations
- Process Lombok annotations

### 2. Run Integration Tests

```bash
mvn test
```

This will:
- Start PostgreSQL container via Testcontainers
- Run all integration tests
- Verify CRUD operations
- Test error handling
- Validate domain entity behavior

### 3. Start the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Run Endpoint Tests

Once the application is running, use the provided test script:

```bash
./test-endpoints.sh
```

Or manually test using curl:

```bash
# Create customer
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "address": "123 Main St",
    "phone": "+1-555-123-4567"
  }'

# Get customer by ID (replace {id} with actual UUID)
curl http://localhost:8080/api/customers/{id}

# List all customers
curl "http://localhost:8080/api/customers?page=0&size=20&sort=name,asc"

# Update customer
curl -X PUT http://localhost:8080/api/customers/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Updated",
    "email": "john.updated@example.com"
  }'

# Delete customer
curl -X DELETE http://localhost:8080/api/customers/{id}
```

## Performance Validation

### Using the Test Script

The `test-endpoints.sh` script automatically measures response times and validates they are < 200ms.

### Manual Performance Testing

Use curl with timing:

```bash
curl -w "\nTime: %{time_total}s\n" -o /dev/null -s \
  http://localhost:8080/api/customers
```

Or use Apache Bench:

```bash
ab -n 100 -c 10 http://localhost:8080/api/customers
```

### Expected Performance

All endpoints should respond in < 200ms:
- POST /api/customers: < 200ms
- GET /api/customers/{id}: < 200ms
- GET /api/customers: < 200ms
- PUT /api/customers/{id}: < 200ms
- DELETE /api/customers/{id}: < 200ms

## Using Docker (Alternative)

If you can't install Java 17 locally, use Docker:

```bash
# Build and test using Docker
docker build -f Dockerfile.test -t invoiceme-test .

# Or use a Java 17 container to compile
docker run --rm -v $(pwd):/app -w /app \
  maven:3.9-eclipse-temurin-17 \
  mvn clean compile test
```

## Swagger UI Testing

Access Swagger UI for interactive testing:

```
http://localhost:8080/swagger-ui/index.html
```

## Test Coverage

The integration tests cover:
- ✅ Create customer successfully
- ✅ Get customer by ID
- ✅ List customers with pagination
- ✅ Update customer
- ✅ Delete customer
- ✅ Handle 404 errors
- ✅ Validate email format
- ✅ Validate required fields
- ✅ Handle duplicate emails
- ✅ Domain entity validation
- ✅ Pagination and sorting

## Troubleshooting

### Compilation Errors

If you see annotation processor errors:
1. Ensure Java 17 is being used: `java -version`
2. Clean and rebuild: `mvn clean compile`
3. Check Lombok version compatibility

### Test Failures

If tests fail:
1. Ensure Docker is running (for Testcontainers)
2. Check database connection
3. Verify Flyway migrations ran successfully

### Performance Issues

If response times exceed 200ms:
1. Check database indexes are created
2. Verify `@Transactional(readOnly = true)` on query service
3. Check for N+1 query problems
4. Monitor database connection pool

