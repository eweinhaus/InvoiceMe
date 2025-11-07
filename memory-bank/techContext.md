# Technical Context: InvoiceMe

## Technology Stack

### Backend Technologies

#### Core Framework
- **Spring Boot**: `3.3.11` (LTS, stable)
- **Java**: `17` (LTS, widely supported)
- **Build Tool**: Maven
- **Package Manager**: Maven (dependency management)

#### Key Dependencies
- **Spring Boot Web**: REST API endpoints
- **Spring Data JPA**: Data persistence layer
- **Spring Security**: Authentication and authorization
- **Spring OAuth2 Client**: Google OAuth2 integration
- **Spring Validation**: Request validation
- **H2 Database**: In-memory database for development
- **PostgreSQL**: Production database (via Testcontainers for testing)
- **Flyway**: Database migration management
- **MapStruct**: DTO mapping (annotation processor)
- **Lombok**: Boilerplate reduction
- **SpringDoc OpenAPI**: API documentation (Swagger)

#### Development Tools
- **Testcontainers**: Integration testing with real PostgreSQL
- **Spring Boot Test**: Testing framework
- **Spring Security Test**: Security testing utilities

### Frontend Technologies

#### Core Framework
- **React**: `^18.3.1` (stable, widely supported)
- **TypeScript**: `^5.5.0` (type safety, better AI tooling)
- **Build Tool**: Vite `^5.4.0` (faster than Create React App)
- **Package Manager**: npm

#### UI Library
- **shadcn/ui**: Copy-paste component library (AI-friendly, customizable)
- **Tailwind CSS**: `^3.4.0` (utility-first CSS)
- **Radix UI**: Primitives used by shadcn/ui (accessible, unstyled)

#### State Management & Data Fetching
- **React Router**: `^6.26.0` (client-side routing)
- **React Query (TanStack Query)**: `^5.56.0` (server state management, caching)
- **Zustand**: `^4.5.0` (optional, lightweight client state)

#### Form Handling
- **React Hook Form**: `^7.53.0` (performant forms)
- **Zod**: `^3.23.0` (schema validation, works with React Hook Form)

#### HTTP Client
- **Axios**: `^1.7.0` (REST API client)

#### Type Generation
- **openapi-typescript**: `^7.0.0` (generate TypeScript types from OpenAPI spec)
- Alternative: `swagger-typescript-api`

#### Development Tools
- **ESLint**: Code quality
- **Prettier**: Code formatting
- **TypeScript**: Type checking

## Development Setup

### Backend Setup
1. **Java 17+** required
2. **Maven** for dependency management
3. **H2** for local development (in-memory)
4. **PostgreSQL** for testing (via Testcontainers)

### Frontend Setup
1. **Node.js** (LTS version)
2. **npm** for package management
3. **Vite** for development server and build

### Environment Configuration

#### Backend (`application.yml`)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:invoiceme  # Dev
    # url: jdbc:postgresql://localhost:5432/invoiceme  # Prod
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
```

#### Frontend (`.env`)
```env
VITE_API_URL=http://localhost:8080/api
VITE_GOOGLE_CLIENT_ID=your-client-id
```

## Technical Constraints

### Performance Requirements
- **API Latency**: < 200ms for standard CRUD operations
- **UI Responsiveness**: Smooth interactions without noticeable lag

### Database Constraints
- **Development**: H2 in-memory (auto-configured)
- **Testing**: PostgreSQL via Testcontainers
- **Production**: PostgreSQL on AWS RDS (documented, optional)

### Authentication Constraints
- **OAuth2**: Google OAuth2 only
- **Session-Based**: httpOnly cookies (no JWT)
- **CORS**: Must be configured for frontend origin

### Type Safety Constraints
- **Backend**: Java types with validation
- **Frontend**: TypeScript types (ideally auto-generated from OpenAPI)
- **Boundary Crossing**: DTOs required, no domain entities in API responses

## Dependencies and Tools

### Backend Key Dependencies
- MapStruct + Lombok: Eliminate boilerplate mapper code (✅ CustomerMapper implemented)
- Flyway: Version-controlled database migrations (✅ V2 migration created)
- SpringDoc OpenAPI: Auto-generated API documentation (✅ Customer endpoints documented)
- Testcontainers: Real database for integration tests (✅ CustomerIntegrationTest created)

### Frontend Key Dependencies
- React Query: Server state management with optimistic updates
- React Hook Form + Zod: Type-safe form validation
- shadcn/ui: Accessible, customizable UI components
- Axios: HTTP client with interceptors

## Build and Deployment

### Backend Build
```bash
mvn clean package  # Creates executable JAR
```

### Frontend Build
```bash
npm run build  # Creates dist/ folder
```

### Development Servers
- **Backend**: `http://localhost:8080` (✅ running)
- **Frontend**: `http://localhost:5173` (✅ running, Vite default)
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html` (✅ accessible)
- **OpenAPI Spec**: `http://localhost:8080/v3/api-docs` (✅ accessible)
- **H2 Console**: `http://localhost:8080/h2-console` (✅ accessible)

### Testing Infrastructure
- **Docker**: Used for Java 17 testing environment
- **Testcontainers**: PostgreSQL container for integration tests
- **Integration Tests**: CustomerIntegrationTest with 15 test scenarios
- **Performance Testing**: Automated endpoint testing scripts
- **Test Results**: All Customer endpoints validated < 200ms response time

## Development Tools

### Recommended Tools
- **Postman/Insomnia**: Backend API testing
- **H2 Console**: Database inspection (`/h2-console`)
- **Swagger UI**: API documentation (`/swagger-ui.html`)
- **React DevTools**: Frontend debugging
- **Chrome DevTools**: Network/performance monitoring

## Technical Decisions

### Why Session-Based Auth?
- Simpler than JWT for this project
- httpOnly cookies prevent XSS attacks
- No token refresh logic needed
- Spring Security handles everything

### Why MapStruct?
- Eliminates 80% of boilerplate mapper code
- Compile-time safety
- High performance
- Works seamlessly with Lombok

### Why React Query?
- Automatic caching and invalidation
- Optimistic updates for better UX
- Built-in loading and error states
- Reduces boilerplate compared to manual state management

### Why Vite?
- Faster than Create React App
- Better developer experience
- Modern build tooling
- Excellent TypeScript support

### Why shadcn/ui?
- Copy-paste components (not a dependency)
- AI-friendly (easy to modify)
- Accessible (built on Radix UI)
- Customizable with Tailwind CSS

