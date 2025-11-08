# Product Context: InvoiceMe

## Why This Project Exists

InvoiceMe is designed as an assessment project to evaluate a candidate's ability to:
1. Design and implement architecturally sound systems
2. Effectively leverage AI tools while maintaining architectural integrity
3. Build production-quality code that demonstrates enterprise-level thinking

## Problems It Solves

### Business Problems
- **Invoice Management**: Track invoices through their lifecycle (Draft → Sent → Paid)
- **Customer Management**: Maintain customer information for invoicing
- **Payment Tracking**: Record and apply payments to invoices with accurate balance calculations
- **Financial Accuracy**: Ensure correct balance calculations and payment validations

### Technical Challenges
- **Architectural Clarity**: Demonstrating DDD, CQRS, and VSA principles in a practical implementation
- **AI-Assisted Development**: Using AI tools effectively without sacrificing architectural quality
- **Performance**: Meeting < 200ms API response time requirements
- **Type Safety**: Maintaining type safety across frontend and backend boundaries

## How It Should Work

### User Flow
1. **Authentication**: User signs in with Google OAuth
2. **Customer Management**: Create and manage customers
3. **Invoice Creation**: Create invoices with multiple line items for customers
4. **Invoice Lifecycle**: 
   - Create invoice in Draft status
   - Mark invoice as Sent when ready
   - Record payments to move invoice to Paid status
5. **Payment Management**: View and track payments applied to invoices

### Core Business Rules
- **Invoice Status Transitions**: Only valid transitions allowed (DRAFT → SENT → PAID)
- **Balance Calculation**: `balance = totalAmount - sum(payments)`
- **Payment Validation**: Payments cannot exceed invoice balance
- **Invoice Editing**: Only Draft invoices can be edited
- **Line Items**: Each invoice must have at least one line item with description, quantity, and unit price

## User Experience Goals

### Frontend Experience
- **Responsive Design**: Works on desktop and mobile devices
- **Fast Interactions**: Optimistic updates for instant UI feedback
- **Clear Feedback**: Loading states, error messages, success confirmations
- **Accessible**: ARIA labels, keyboard navigation
- **Intuitive**: Clear navigation, consistent UI patterns
- **Dashboard**: Home page with statistics, recent activity, and quick actions
- **User Experience**: Personalized welcome message, user avatar in header

### Backend Experience
- **Fast API**: < 200ms response times
- **Consistent Errors**: Standardized error envelope for all API errors
- **Well Documented**: OpenAPI/Swagger documentation available
- **Type Safe**: Strong typing throughout the stack

## Key Features

### Customers
- Create, read, update, delete customers
- List customers with pagination
- Customer details: name, email, address, phone

### Invoices
- Create invoices with multiple line items
- Edit invoices (only if Draft status)
- Mark invoices as Sent
- Filter invoices by status or customer
- View invoice details with line items and payment history
- Automatic total and balance calculations

### Payments
- Record payments against invoices
- Validate payment amounts (cannot exceed balance)
- View payments by invoice
- Automatic balance updates on invoice

### Authentication ✅ COMPLETE
- Dev mode authentication (permissive security for development)
- Google OAuth2 sign-in (ready, can be enabled with credentials)
- Session-based authentication (httpOnly cookies)
- Protected routes requiring authentication
- User display in header with Avatar component
- Login/logout functionality
- Home page dashboard (shows login if not authenticated, dashboard if authenticated)

