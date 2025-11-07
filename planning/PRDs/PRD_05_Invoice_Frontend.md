# PRD 05: Invoice Feature - Frontend

## Overview
**Priority**: 🟢 Can run in parallel with PRDs 02, 03, 06, 07  
**Dependencies**: PRD 01 (Foundation), PRD 04 (Invoice Backend) - or use mock data  
**Enables**: None (standalone feature)

This PRD implements the complete frontend for Invoice management with line items, lifecycle management, and filtering capabilities.

## Objectives
1. Create Invoice page with list, create, edit, send functionality
2. Implement Invoice ViewModel using custom hooks
3. Integrate with Invoice API endpoints
4. Build invoice form with dynamic line items
5. Implement status filtering and customer filtering
6. Display invoice details with balance and payment history

## Feature Structure

### Directory Layout
```
features/invoices/
├── components/
│   ├── InvoiceList.tsx
│   ├── InvoiceForm.tsx
│   ├── InvoiceCard.tsx
│   ├── LineItemForm.tsx
│   ├── InvoiceStatusBadge.tsx
│   └── InvoiceDetails.tsx
├── hooks/
│   ├── useInvoices.ts
│   ├── useInvoiceMutations.ts
│   └── useInvoiceViewModel.ts
├── types/
│   └── invoice.types.ts
└── pages/
    └── InvoicesPage.tsx
```

## Components

### InvoiceList Component
- [ ] Display invoices in a table (shadcn/ui Table)
- [ ] Show columns: Invoice #, Customer, Status, Total, Balance, Created, Actions
- [ ] Filter controls:
  - Status filter (dropdown: All, Draft, Sent, Paid)
  - Customer filter (dropdown with customer list)
- [ ] Pagination controls
- [ ] Loading state (skeleton)
- [ ] Empty state
- [ ] Actions: View Details, Edit (if Draft), Mark as Sent (if Draft), Delete (if Draft)

### InvoiceForm Component
- [ ] Create/Edit form using React Hook Form
- [ ] Customer selector (dropdown with customer list)
- [ ] Dynamic line items section:
  - Add/Remove line items buttons
  - Each line item: Description, Quantity, Unit Price
  - Auto-calculate subtotal per line item
  - Auto-calculate total at bottom
- [ ] Form modes: "create" or "edit"
- [ ] Zod schema validation:
  - Customer: required
  - Line items: at least one, each with description, quantity > 0, unitPrice >= 0
- [ ] Submit button with loading state
- [ ] Cancel button
- [ ] Error display

### LineItemForm Component
- [ ] Individual line item input row
- [ ] Fields: Description (text), Quantity (number), Unit Price (number)
- [ ] Auto-calculate and display subtotal
- [ ] Remove button
- [ ] Validation: quantity > 0, unitPrice >= 0

### InvoiceStatusBadge Component
- [ ] Status indicator badge (shadcn/ui Badge)
- [ ] Color coding: Draft (gray), Sent (blue), Paid (green)
- [ ] Shows status text

### InvoiceDetails Component
- [ ] Detailed view of invoice (modal or separate page)
- [ ] Shows: Invoice #, Customer info, Status, Line items table, Total, Balance, Payments list
- [ ] Line items breakdown with subtotals
- [ ] Payment history (if available, from PRD 07)
- [ ] Actions: Mark as Sent (if Draft), Record Payment (if not Paid)

## Hooks (React Query)

### useInvoices Hook
- [ ] Query hooks for fetching invoices
- [ ] `useInvoices(page, size)` - list all
- [ ] `useInvoicesByStatus(status, page, size)` - filter by status
- [ ] `useInvoicesByCustomer(customerId, page, size)` - filter by customer
- [ ] `useInvoice(id)` - get by ID
- [ ] Query keys: `['invoices', filters]`
- [ ] Returns: `{ data, isLoading, error, refetch }`

### useInvoiceMutations Hook
- [ ] Mutation hooks for create, update, mark as sent
- [ ] `useCreateInvoice`: POST `/api/invoices`
- [ ] `useUpdateInvoice`: PUT `/api/invoices/{id}`
- [ ] `useMarkInvoiceAsSent`: POST `/api/invoices/{id}/send`
- [ ] Optimistic updates
- [ ] Error handling with toast notifications
- [ ] Invalidate queries on success

### useInvoiceViewModel Hook (ViewModel)
- [ ] Encapsulates business logic and state
- [ ] Uses `useInvoices` and `useInvoiceMutations`
- [ ] Handles filter state (status, customer)
- [ ] Transforms data for UI
- [ ] Calculates totals and balances (client-side validation)
- [ ] Returns: `{ invoices, filters, isLoading, createInvoice, updateInvoice, markAsSent, ... }`

## Page Component

### InvoicesPage
- [ ] Main page component at route `/invoices`
- [ ] Uses `useInvoiceViewModel` hook
- [ ] Layout: Header with filters and "Create Invoice" button, InvoiceList below
- [ ] Modal/Dialog for InvoiceForm (create/edit)
- [ ] Modal/Dialog for InvoiceDetails
- [ ] Handles routing and navigation
- [ ] Error boundary

## Types

### invoice.types.ts
- [ ] Use auto-generated types from OpenAPI (preferred)
- [ ] Or define manually:
  - `Invoice` interface
  - `LineItem` interface
  - `InvoiceStatus` enum
  - `CreateInvoiceRequest` interface
  - `UpdateInvoiceRequest` interface
  - `InvoiceResponse` interface

## API Integration

### API Client Methods
- [ ] `getInvoices(page, size)` - GET request
- [ ] `getInvoicesByStatus(status, page, size)` - GET with query params
- [ ] `getInvoicesByCustomer(customerId, page, size)` - GET with query params
- [ ] `getInvoiceById(id)` - GET request
- [ ] `createInvoice(data)` - POST request
- [ ] `updateInvoice(id, data)` - PUT request
- [ ] `markInvoiceAsSent(id)` - POST request
- [ ] All methods use Axios instance from foundation

## UI/UX Requirements

### User Experience
- [ ] Optimistic updates for all mutations
- [ ] Real-time total calculation as line items are added/removed
- [ ] Loading states during API calls
- [ ] Success/error toast notifications
- [ ] Form validation with clear error messages
- [ ] Status badge color coding for quick visual recognition
- [ ] Responsive design

### Accessibility
- [ ] ARIA labels on all interactive elements
- [ ] Keyboard navigation support
- [ ] Screen reader friendly table structure
- [ ] Focus management in modals

## Success Criteria
- [ ] Can create, read, update invoices
- [ ] Can add/remove line items dynamically
- [ ] Total calculation is correct
- [ ] Status filtering works
- [ ] Customer filtering works
- [ ] Can mark invoice as Sent (with validation)
- [ ] Invoice details view shows all information
- [ ] Optimistic updates provide instant feedback
- [ ] Error handling shows user-friendly messages
- [ ] MVVM pattern is clear

## Dependencies
- **Requires**: PRD 01 (Foundation)
- **Does NOT require**: PRD 04 (Invoice Backend) - can use mock data to start
- **Does NOT require**: PRD 02 (Customer Backend) - can use mock customer list
- **Can run parallel with**: PRDs 02, 03, 04, 06, 07
- **Final integration requires**: PRD 04 and PRD 02 completion
- **Note**: Payment history display may require PRD 07 (Payment Frontend)

## Timeline Estimate
**1-1.5 days** (complex UI with line items and filters)

## Notes
- Most complex frontend feature due to line items and filtering
- Can start with mock data if backend isn't ready
- Line item management requires careful state handling
- Total calculation should match backend logic for validation

