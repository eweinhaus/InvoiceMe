# Frontend PRD: InvoiceMe Web Portal

## Overview
React-based single-page application (SPA) for InvoiceMe invoicing system. Implements MVVM architecture pattern for UI logic separation, built with TypeScript, React, and shadcn/ui components.

**Design Philosophy**: Pragmatic React patterns using custom hooks as ViewModels. Optimized for 5-7 day development timeline.

## Technical Stack

### Core Framework
- **React**: `^18.3.1` (stable, widely supported)
- **TypeScript**: `^5.5.0` (type safety, better AI tooling)
- **Build Tool**: Vite (faster than Create React App, better DX)
- **Package Manager**: npm (user preference, acceptable for this project size)

### UI Library
- **shadcn/ui**: Copy-paste component library (AI-friendly, customizable)
- **Tailwind CSS**: `^3.4.0` (utility-first CSS, excellent AI support)
- **Radix UI**: Primitives used by shadcn/ui (accessible, unstyled)

### Routing & State Management
- **React Router**: `^6.26.0` (client-side routing)
- **React Query (TanStack Query)**: `^5.56.0` (server state management, caching)
- **Zustand** (optional): `^4.5.0` (lightweight client state if needed)

### Form Handling
- **React Hook Form**: `^7.53.0` (performant forms, AI-friendly)
- **Zod**: `^3.23.0` (schema validation, works with React Hook Form)

### HTTP Client
- **Axios**: `^1.7.0` (REST API client)
- **React Query** for data fetching and caching

### Type Generation
- **openapi-typescript**: `^7.0.0` (generate TypeScript types from OpenAPI spec)
- **or swagger-typescript-api**: Alternative for type generation
- Auto-generate types from backend OpenAPI spec during build

### Development Tools
- **ESLint**: Code quality
- **Prettier**: Code formatting
- **TypeScript**: Type checking

## Project Structure

### Simplified Directory Layout
```
frontend/
├── src/
│   ├── features/              # Vertical Slice Architecture
│   │   ├── customers/
│   │   │   ├── components/
│   │   │   │   ├── CustomerList.tsx
│   │   │   │   ├── CustomerForm.tsx
│   │   │   │   └── CustomerCard.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useCustomers.ts         # React Query queries
│   │   │   │   ├── useCustomerMutations.ts # React Query mutations
│   │   │   │   └── useCustomerViewModel.ts # ViewModel (business logic)
│   │   │   ├── types/
│   │   │   │   └── customer.types.ts
│   │   │   └── pages/
│   │   │       └── CustomersPage.tsx
│   │   ├── invoices/
│   │   │   ├── components/
│   │   │   │   ├── InvoiceList.tsx
│   │   │   │   ├── InvoiceForm.tsx
│   │   │   │   ├── InvoiceCard.tsx
│   │   │   │   └── LineItemForm.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useInvoices.ts
│   │   │   │   ├── useInvoiceMutations.ts
│   │   │   │   └── useInvoiceViewModel.ts  # ViewModel
│   │   │   ├── types/
│   │   │   │   └── invoice.types.ts
│   │   │   └── pages/
│   │   │       └── InvoicesPage.tsx
│   │   └── payments/
│   │       ├── components/
│   │       │   ├── PaymentList.tsx
│   │       │   └── PaymentForm.tsx
│   │       ├── hooks/
│   │       │   ├── usePayments.ts
│   │       │   ├── usePaymentMutations.ts
│   │       │   └── usePaymentViewModel.ts  # ViewModel
│   │       ├── types/
│   │       │   └── payment.types.ts
│   │       └── pages/
│   │           └── PaymentsPage.tsx
│   │
│   ├── components/           # Shared UI Components
│   │   ├── ui/               # shadcn/ui components (auto-generated)
│   │   │   ├── button.tsx
│   │   │   ├── table.tsx
│   │   │   ├── form.tsx
│   │   │   ├── input.tsx
│   │   │   ├── dialog.tsx
│   │   │   └── ...
│   │   ├── layout/
│   │   │   ├── Header.tsx
│   │   │   ├── Sidebar.tsx
│   │   │   └── Layout.tsx
│   │   └── common/
│   │       ├── LoadingSpinner.tsx
│   │       └── ErrorMessage.tsx
│   │
│   ├── lib/                  # Utilities
│   │   ├── api/
│   │   │   └── client.ts     # Axios instance with auth interceptor
│   │   ├── utils/
│   │   │   ├── formatters.ts # Date, currency formatting
│   │   │   └── cn.ts         # shadcn/ui className utility
│   │   └── hooks/
│   │       └── useAuth.ts    # Authentication hook
│   │
│   ├── types/                # Shared TypeScript types
│   │   └── api.types.ts
│   │
│   ├── App.tsx               # Root component with routing
│   ├── main.tsx              # Entry point
│   └── index.css             # Global styles (Tailwind)
│
├── public/
├── package.json
├── tsconfig.json
├── vite.config.ts
└── tailwind.config.js
```

## MVVM Architecture (React-Style)

### ViewModel Pattern Using Custom Hooks

**Key Concept**: In React, ViewModels are implemented as custom hooks that encapsulate business logic, state management, and data transformation.

**Example**: Custom hook (`useCustomerViewModel`) encapsulates React Query hooks, business logic (sorting, transformations), and mutation handlers. Component uses hook and renders data. Benefits: Separation of concerns, reusable logic, testable hooks.

**Benefits**:
- Clear separation of concerns (View vs ViewModel)
- Reusable business logic
- Easy to test (test hooks independently)
- Idiomatic React patterns

## Page Requirements

### 1. Customers Page (`/customers`)
**Functionality:**
- Display list of all customers in a table
- Create new customer (modal or inline form)
- Edit existing customer (inline or modal)
- Delete customer (with confirmation dialog)
- View customer details (optional expandable row)

**Components:**
- `CustomerList` - Table with customer data
- `CustomerForm` - Create/Edit form
- `CustomerDeleteDialog` - Confirmation dialog

**API Integration:**
- `GET /api/customers?page=0&size=20` - List all (paginated)
- `GET /api/customers/{id}` - Get by ID
- `POST /api/customers` - Create
- `PUT /api/customers/{id}` - Update
- `DELETE /api/customers/{id}` - Delete

**Pagination Component**: Reusable `Pagination` component using shadcn/ui

### 2. Invoices Page (`/invoices`)
**Functionality:**
- Display list of invoices with filters:
  - Filter by status (Draft, Sent, Paid)
  - Filter by customer
- Create new invoice (Draft status)
  - Add multiple line items dynamically
  - Each line item: description, quantity, unit price
  - Auto-calculate subtotals and total
- Edit invoice (only if Draft status)
- Mark invoice as Sent (button/action)
- View invoice details:
  - Line items breakdown
  - Current balance
  - Applied payments
  - Status badge

**Components:**
- `InvoiceList` - Table with filters
- `InvoiceForm` - Create/Edit form with line items
- `LineItemForm` - Individual line item input
- `InvoiceStatusBadge` - Status indicator
- `InvoiceDetails` - Detailed view

**API Integration:**
- `GET /api/invoices?page=0&size=20` - List all (paginated)
- `GET /api/invoices?status={status}&page=0&size=20` - Filter by status (paginated)
- `GET /api/invoices?customerId={id}&page=0&size=20` - Filter by customer (paginated)
- `GET /api/invoices/{id}` - Get by ID
- `POST /api/invoices` - Create (Draft)
- `PUT /api/invoices/{id}` - Update (only Draft)
- `POST /api/invoices/{id}/send` - Mark as Sent

### 3. Payments Page (`/payments`)
**Functionality:**
- Display list of all payments
- Record new payment:
  - Select invoice (dropdown/search)
  - Enter amount (validate against invoice balance)
  - Enter payment date
- View payments for specific invoice
- Display payment confirmation/status

**Components:**
- `PaymentList` - Table of payments
- `PaymentForm` - Record payment form
- `InvoiceSelector` - Invoice selection component

**API Integration:**
- `GET /api/payments?page=0&size=20` - List all (paginated)
- `GET /api/payments?invoiceId={id}&page=0&size=20` - List by invoice (paginated)
- `GET /api/payments/{id}` - Get by ID
- `POST /api/payments` - Record payment

### 4. Authentication

**Login Page (`/login`)**:
- "Sign in with Google" button
- Redirects to: `http://localhost:8080/oauth2/authorization/google`
- Backend handles OAuth flow
- On success, redirects back to: `http://localhost:5173/dashboard`
- Session cookie (JSESSIONID) automatically included in requests

**Auth Hook**: `useAuth()` uses React Query to fetch `/api/auth/user`, returns `{user, isAuthenticated, isLoading}`. **Protected Routes**: `ProtectedRoute` component checks auth and redirects to `/login` if not authenticated.

**Session-Based Auth Benefits**:
- No token management needed
- httpOnly cookies prevent XSS
- Backend handles everything
- Simple implementation

## UI/UX Requirements

### Design System
- Use shadcn/ui components for consistency
- Tailwind CSS for styling
- Responsive design (mobile-friendly)
- Accessible (ARIA labels, keyboard navigation)

### Key Components Needed
Install via shadcn/ui CLI:
```bash
npx shadcn-ui@latest add button
npx shadcn-ui@latest add table
npx shadcn-ui@latest add form
npx shadcn-ui@latest add input
npx shadcn-ui@latest add dialog
npx shadcn-ui@latest add select
npx shadcn-ui@latest add badge
npx shadcn-ui@latest add card
npx shadcn-ui@latest add dropdown-menu
```

### Layout
- Header with navigation (Customers, Invoices, Payments)
- Sidebar (optional, for larger screens)
- Main content area
- Footer (optional)

### User Feedback
- Loading states (spinners, skeletons)
- Error messages (toast notifications)
- Success confirmations
- Form validation errors

## State Management

### Server State (React Query)
- All API data fetching
- Caching and invalidation
- **Optimistic updates** for mutations (instant UI feedback)
- Automatic refetching on window focus
- Stale-while-revalidate pattern

### Client State (React State / Zustand)
- UI state (modals, filters, form state)
- Local preferences

## API Integration

### API Client Setup
Axios instance with `baseURL` from env, `withCredentials: true` for session cookies, response interceptor redirects to `/login` on 401.

### React Query Hooks

**Query Hook**: `useQuery` with `queryKey: ['customers', page, size]` and `queryFn` calling paginated endpoint. **Mutation Hook**: `useMutation` with `onMutate` for optimistic updates, `onError` for rollback, `onSettled` for invalidation. Benefits: Instant UI feedback, automatic rollback, better UX.

## Form Handling

### React Hook Form + Zod
Define Zod schema, infer TypeScript type, use `useForm` with `zodResolver`. Example: `z.object({name, email})` → `useForm({resolver: zodResolver(schema)})`.

## TypeScript Types

### Auto-Generated Types from OpenAPI

**Setup** (`package.json`):
```json
{
  "scripts": {
    "generate:types": "openapi-typescript http://localhost:8080/v3/api-docs -o src/types/api.generated.ts"
  }
}
```

**Usage**: Run `npm run generate:types` after backend starts to generate types.

**Manual Types** (fallback): `Customer`, `Invoice`, `LineItem`, `Payment`, `Page<T>`, `ErrorResponse` interfaces matching backend DTOs.

## Performance Requirements
- Smooth UI interactions (< 100ms perceived latency)
- Lazy loading for routes
- Optimistic updates for better UX
- Proper loading states

## Error Handling

### Consistent Error Handling
**API Errors**: Interceptor extracts `ErrorResponse`, shows toast with `message`, redirects on 401. **Error Boundary**: React `ErrorBoundary` catches component errors. Features: Toast notifications, user-friendly messages, automatic retry (React Query), consistent display.

## Environment Configuration
```env
VITE_API_URL=http://localhost:8080/api
VITE_GOOGLE_CLIENT_ID=your-client-id
```

## Deployment (AWS) - Document Only

**Note**: Focus on local development. Deployment is documented but not required for 5-7 day timeline.

**Steps**:
1. Build: `npm run build` (creates `dist/` folder)
2. Upload `dist/` to S3 bucket
3. Configure CloudFront distribution pointing to S3
4. Set environment variables:
   - `VITE_API_URL=https://api.yourdomain.com/api`
5. Update CORS configuration in backend to allow frontend domain

## Success Criteria
- ✅ All three pages functional (Customers, Invoices, Payments)
- ✅ MVVM pattern implemented (ViewModels separate from Views)
- ✅ Forms with validation
- ✅ API integration working
- ✅ Google OAuth authentication
- ✅ Responsive design
- ✅ Loading and error states
- ✅ TypeScript types throughout (ideally auto-generated from OpenAPI)
- ✅ Smooth user experience with optimistic updates
- ✅ Pagination on all list endpoints
- ✅ Consistent error handling

