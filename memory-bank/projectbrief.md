# Project Brief: InvoiceMe

## Project Overview

InvoiceMe is a full-stack ERP-style invoicing system built as a take-home assessment project for an AI Software Engineer position. The project demonstrates mastery of modern software architecture principles while showcasing effective use of AI-assisted development tools.

## Core Requirements

### Business Domain
The system manages three core entities:
- **Customers**: Client information management
- **Invoices**: Invoice creation with line items, lifecycle management (Draft → Sent → Paid)
- **Payments**: Payment recording and application to invoices

### Architectural Mandates
The project **MUST** demonstrate:
1. **Domain-Driven Design (DDD)**: Rich domain models with business logic in entities
2. **Command Query Responsibility Segregation (CQRS)**: Clear separation of write (Command) and read (Query) operations
3. **Vertical Slice Architecture (VSA)**: Feature-based organization over technical layers
4. **Clean Architecture**: Clear layer boundaries (Domain → Application → Infrastructure → Presentation)

### Technical Stack
- **Backend**: Spring Boot 3.3.11 (Java 17), Maven
- **Frontend**: React 18.3.1, TypeScript 5.5.0, Vite
- **Database**: H2 (dev) / PostgreSQL (test/prod)
- **Authentication**: Google OAuth2 with session-based auth
- **UI Library**: shadcn/ui + Tailwind CSS

### Performance Requirements
- API response times < 200ms for CRUD operations
- Smooth, responsive UI interactions

### Timeline
- **Recommended**: 5-7 days
- **Focus**: Pragmatic implementation demonstrating principles without over-engineering

## Success Criteria

✅ Rich domain models (not anemic)  
✅ CQRS separation (Commands vs Queries)  
✅ Vertical slices by feature  
✅ Integration tests passing  
✅ API < 200ms response time  
✅ Google OAuth working  
✅ Invoice lifecycle enforced  
✅ Payment balance calculations correct  
✅ MVVM pattern in frontend  
✅ Responsive, accessible UI  

## Project Goals

1. **Architectural Excellence**: Prove ability to guide AI tools to produce architecturally sound code
2. **AI Tool Mastery**: Demonstrate intelligent use of AI tools (Cursor, Copilot, etc.) as accelerators
3. **Production Quality**: Code quality suitable for enterprise-level, scalable systems
4. **Complete Functionality**: Full CRUD operations for Customers, Invoices, and Payments

## Deliverables

- Complete, functional code repository
- Demo video/presentation
- Technical writeup (1-2 pages) documenting architecture decisions
- AI tool documentation with example prompts
- Test cases and validation results



