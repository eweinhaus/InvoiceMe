# InvoiceMe Backend

Spring Boot 3.3.11 backend for InvoiceMe invoice management system.

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker (for Testcontainers integration tests)

## Setup

1. Build the project:
```bash
mvn clean install
```

2. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Documentation

Once the application is running:

- **OpenAPI Spec**: `http://localhost:8080/v3/api-docs`
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`

## Database

### Development (H2)
- H2 in-memory database is used by default
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:invoiceme`
  - Username: `sa`
  - Password: (empty)

### Testing (PostgreSQL via Testcontainers)
- Tests use PostgreSQL via Testcontainers
- Requires Docker to be running
- Database is automatically created and destroyed per test

## Project Structure

```
src/main/java/com/invoiceme/
├── domain/              # Domain layer (DDD entities)
├── application/         # Application layer (CQRS services, DTOs)
├── infrastructure/      # Infrastructure layer (repositories, config, security)
└── presentation/        # Presentation layer (REST controllers, exception handling)
```

## Key Technologies

- **Spring Boot 3.3.11**: Core framework
- **Spring Data JPA**: Data persistence
- **Spring Security**: Authentication and authorization
- **MapStruct**: DTO mapping
- **Lombok**: Boilerplate reduction
- **Flyway**: Database migrations
- **SpringDoc OpenAPI**: API documentation
- **Testcontainers**: Integration testing

## Running Tests

```bash
mvn test
```

Integration tests use Testcontainers with PostgreSQL. Ensure Docker is running.

## Configuration

Configuration is in `src/main/resources/application.yml`

- Database: H2 for dev, PostgreSQL for tests
- Port: 8080
- CORS: Configured for `http://localhost:5173` (frontend)

