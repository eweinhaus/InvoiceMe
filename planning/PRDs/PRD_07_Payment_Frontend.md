# PRD 07: Payment Feature - Frontend

## Overview
**Priority**: 🟢 Can run in parallel with PRDs 02, 03, 04, 05, 06  
**Dependencies**: PRD 01 (Foundation), PRD 06 (Payment Backend) - or use mock data  
**Enables**: None (standalone feature)

This PRD implements the complete frontend for Payment management with invoice selection and payment recording.

## Objectives
1. Create Payment page with list and create functionality
2. Implement Payment ViewModel using custom hooks
3. Integrate with Payment API endpoints
4. Build payment form with invoice selection
5. Display payment list with filtering by invoice
6. Show payment confirmation and invoice balance updates

## Feature Structure

### Directory Layout
```
features/payments/
├── components/
│   ├── PaymentList.tsx
│   ├── PaymentForm.tsx
│   └── InvoiceSelector.tsx
├── hooks/
│   ├── usePayments.ts
│   ├── usePaymentMutations.ts
│   └── usePaymentViewModel.ts
├── types/
│   └── payment.types.ts
└── pages/
    └── PaymentsPage.tsx
```

## Components

### PaymentList Component
- [ ] Display payments in a table (shadcn/ui Table)
- [ ] Show columns: Payment #, Invoice #, Customer, Amount, Payment Date, Created, Actions
- [ ] Filter controls:
  - Invoice filter (dropdown with invoice list, shows invoice number)
- [ ] Pagination controls
- [ ] Loading state (skeleton)
- [ ] Empty state
- [ ] Actions: View Details (optional)

### PaymentForm Component
- [ ] Create form using React Hook Form
- [ ] Invoice selector (InvoiceSelector component)
- [ ] Fields: Invoice (required), Amount (required, number), Payment Date (required, date picker)
- [ ] Display selected invoice details:
  - Invoice number, Customer name, Current balance
  - Shows remaining balance after payment (calculated)
- [ ] Zod schema validation:
  - Invoice: required
  - Amount: required, > 0, cannot exceed invoice balance
  - Payment Date: required, valid date
- [ ] Submit button with loading state
- [ ] Cancel button
- [ ] Error display for validation and API errors
- [ ] Real-time balance validation as amount is entered

### InvoiceSelector Component
- [ ] Dropdown/select component for choosing invoice
- [ ] Fetches list of invoices (can filter by status: SENT or PAID with balance > 0)
- [ ] Shows: Invoice #, Customer name, Balance
- [ ] Searchable/filterable dropdown
- [ ] Displays invoice details when selected

## Hooks (React Query)

### usePayments Hook
- [ ] Query hooks for fetching payments
- [ ] `usePayments(page, size)` - list all
- [ ] `usePaymentsByInvoice(invoiceId, page, size)` - filter by invoice
- [ ] `usePayment(id)` - get by ID
- [ ] Query keys: `['payments', filters]`
- [ ] Returns: `{ data, isLoading, error, refetch }`

### usePaymentMutations Hook
- [ ] Mutation hook for create
- [ ] `useRecordPayment`: POST `/api/payments`
- [ ] Optimistic updates
- [ ] Error handling with toast notifications
- [ ] Invalidate queries on success
- [ ] Invalidate invoice queries to update balance display

### usePaymentViewModel Hook (ViewModel)
- [ ] Encapsulates business logic and state
- [ ] Uses `usePayments` and `usePaymentMutations`
- [ ] Handles filter state (invoice)
- [ ] Transforms data for UI
- [ ] Validates payment amount against invoice balance
- [ ] Returns: `{ payments, filters, isLoading, recordPayment, ... }`

## Page Component

### PaymentsPage
- [ ] Main page component at route `/payments`
- [ ] Uses `usePaymentViewModel` hook
- [ ] Layout: Header with filter and "Record Payment" button, PaymentList below
- [ ] Modal/Dialog for PaymentForm
- [ ] Handles routing and navigation
- [ ] Error boundary

## Types

### payment.types.ts
- [ ] Use auto-generated types from OpenAPI (preferred)
- [ ] Or define manually:
  - `Payment` interface
  - `PaymentRequest` interface
  - `PaymentResponse` interface

## API Integration

### API Client Methods
- [ ] `getPayments(page, size)` - GET request
- [ ] `getPaymentsByInvoice(invoiceId, page, size)` - GET with query params
- [ ] `getPaymentById(id)` - GET request
- [ ] `recordPayment(data)` - POST request
- [ ] All methods use Axios instance from foundation

## UI/UX Requirements

### User Experience
- [ ] Optimistic updates for payment recording
- [ ] Real-time balance calculation as amount is entered
- [ ] Clear validation: shows error if amount exceeds balance
- [ ] Loading states during API calls
- [ ] Success feedback: Toast notification with updated invoice balance
- [ ] Error feedback: Toast notification with clear error message
- [ ] Invoice selector shows relevant invoices (not fully paid)
- [ ] Responsive design

### Accessibility
- [ ] ARIA labels on all interactive elements
- [ ] Keyboard navigation support
- [ ] Screen reader friendly
- [ ] Focus management in modals

## Integration with Invoice Feature

### Invoice Details Integration
- [ ] Update InvoiceDetails component (from PRD 05) to show payment history
- [ ] Add "Record Payment" button in InvoiceDetails (if invoice not fully paid)
- [ ] Refresh invoice balance after payment is recorded

## Success Criteria
- [ ] Can record payments successfully
- [ ] Can list payments with pagination
- [ ] Can filter payments by invoice
- [ ] Payment amount validation prevents exceeding balance
- [ ] Invoice balance updates correctly after payment
- [ ] Optimistic updates provide instant feedback
- [ ] Error handling shows user-friendly messages
- [ ] Invoice selector works correctly
- [ ] MVVM pattern is clear

## Dependencies
- **Requires**: PRD 01 (Foundation)
- **Does NOT require**: PRD 06 (Payment Backend) - can use mock data to start
- **Does NOT require**: PRD 04 (Invoice Backend) - can use mock invoice list
- **Enhances**: PRD 05 (Invoice Frontend) - adds payment history to invoice details
- **Can run parallel with**: PRDs 02, 03, 04, 05, 06
- **Final integration requires**: PRD 06 and PRD 04 completion

## Timeline Estimate
**0.5-1 day** (simpler than Invoice, but requires invoice integration)

## Notes
- Can start with mock data if backend isn't ready
- Invoice selector requires invoice list API (from PRD 04)
- Payment validation should match backend logic
- Integration with InvoiceDetails is a nice-to-have enhancement

