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

### Email Configuration

The application supports sending invoices via email. Configure SMTP settings using environment variables.

**📖 For detailed SMTP setup instructions, see: [`md_files/SMTP_SETUP_GUIDE.md`](../md_files/SMTP_SETUP_GUIDE.md)**

**Required Environment Variables:**
- `SMTP_HOST`: SMTP server hostname (e.g., `smtp.gmail.com`, `smtp.sendgrid.net`)
- `SMTP_PORT`: SMTP server port (typically `587` for TLS, `465` for SSL)
- `SMTP_USERNAME`: SMTP username/email address
- `SMTP_PASSWORD`: SMTP password or app password

**Example Configuration:**

For Gmail:
```bash
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
```

For SendGrid:
```bash
SMTP_HOST=smtp.sendgrid.net
SMTP_PORT=587
SMTP_USERNAME=apikey
SMTP_PASSWORD=your-sendgrid-api-key
```

For Mailtrap (testing):
```bash
SMTP_HOST=smtp.mailtrap.io
SMTP_PORT=2525
SMTP_USERNAME=your-mailtrap-username
SMTP_PASSWORD=your-mailtrap-password
```

**Development/Testing:**
- For local testing, you can use MailHog (runs on `localhost:1025`) or Mailtrap
- Test configuration uses `localhost:1025` by default (configured in `application-test.yml`)

**Production:**
- Set environment variables in your deployment platform (e.g., Render dashboard)
- Recommended providers: SendGrid, AWS SES, or Mailgun
- Ensure SMTP credentials are stored securely (use secrets management)

