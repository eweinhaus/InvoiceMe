# PRD 03: Customer Feature - Frontend

## Overview
**Priority**: 🟢 Can run in parallel with PRDs 02, 04-07  
**Dependencies**: PRD 01 (Foundation) - **PRD 02 NOT required to start** (can use mock data)  
**Enables**: None (standalone feature)  
**Note**: Can start immediately after PRD 01 using mock API responses. PRD 02 completion enables final integration.

This PRD implements the complete frontend for Customer management following MVVM architecture pattern.

## Objectives
1. Create Customer page with list, create, edit, delete functionality
2. Implement Customer ViewModel using custom hooks
3. Integrate with Customer API endpoints
4. Build reusable Customer components
5. Implement form validation and error handling

## Feature Structure

### Directory Layout
```
features/customers/
├── components/
│   ├── CustomerList.tsx
│   ├── CustomerForm.tsx
│   ├── CustomerCard.tsx
│   └── CustomerDeleteDialog.tsx
├── hooks/
│   ├── useCustomers.ts
│   ├── useCustomerMutations.ts
│   └── useCustomerViewModel.ts
├── types/
│   └── customer.types.ts
└── pages/
    └── CustomersPage.tsx
```

## Components

### CustomerList Component
- [ ] Display customers in a table (shadcn/ui Table)
- [ ] Show columns: Name, Email, Phone, Address, Actions
- [ ] Pagination controls (reuse shared Pagination component)
- [ ] Loading state (skeleton or spinner)
- [ ] Empty state (no customers message)
- [ ] Actions: Edit, Delete buttons per row

### CustomerForm Component
- [ ] Create/Edit form using React Hook Form
- [ ] Fields: Name, Email, Address, Phone
- [ ] Zod schema validation:
  - Name: required, min 2 characters
  - Email: required, valid email format
  - Address: optional
  - Phone: optional, valid format
- [ ] Form modes: "create" or "edit"
- [ ] Submit button with loading state
- [ ] Cancel button
- [ ] Error display for validation and API errors

### CustomerCard Component (Optional)
- [ ] Card view for customer details
- [ ] Can be used in list or detail view
- [ ] Shows all customer information

### CustomerDeleteDialog Component
- [ ] Confirmation dialog (shadcn/ui Dialog)
- [ ] Shows customer name in confirmation message
- [ ] Delete and Cancel buttons
- [ ] Loading state during deletion

## Hooks (React Query)

### useCustomers Hook
- [ ] Query hook for fetching customers
- [ ] Uses `useQuery` with pagination support
- [ ] Query key: `['customers', page, size]`
- [ ] Fetches from `GET /api/customers?page={page}&size={size}`
- [ ] Returns: `{ data, isLoading, error, refetch }`

### useCustomerMutations Hook
- [ ] Mutation hooks for create, update, delete
- [ ] `useCreateCustomer`: POST `/api/customers`
- [ ] `useUpdateCustomer`: PUT `/api/customers/{id}`
- [ ] `useDeleteCustomer`: DELETE `/api/customers/{id}`
- [ ] Optimistic updates for instant UI feedback
- [ ] Error handling with toast notifications
- [ ] Invalidate queries on success

### useCustomerViewModel Hook (ViewModel)
- [ ] Encapsulates business logic and state
- [ ] Uses `useCustomers` and `useCustomerMutations`
- [ ] Transforms data for UI (sorting, filtering if needed)
- [ ] Handles form state and validation
- [ ] Returns: `{ customers, isLoading, createCustomer, updateCustomer, deleteCustomer, ... }`
- [ ] This is the MVVM ViewModel layer

## Page Component

### CustomersPage
- [ ] Main page component at route `/customers`
- [ ] Uses `useCustomerViewModel` hook
- [ ] Layout: Header with "Create Customer" button, CustomerList below
- [ ] Modal/Dialog for CustomerForm (create/edit)
- [ ] Integrates CustomerDeleteDialog
- [ ] Handles routing and navigation
- [ ] Error boundary for component errors

## Types

### customer.types.ts
- [ ] Use auto-generated types from OpenAPI (preferred)
- [ ] Or define manually:
  - `Customer` interface
  - `CustomerRequest` interface
  - `CustomerResponse` interface
  - `Page<Customer>` interface

## API Integration

### API Client Methods
- [ ] `getCustomers(page, size)` - GET request
- [ ] `getCustomerById(id)` - GET request
- [ ] `createCustomer(data)` - POST request
- [ ] `updateCustomer(id, data)` - PUT request
- [ ] `deleteCustomer(id)` - DELETE request
- [ ] All methods use Axios instance from foundation
- [ ] Error handling via interceptors

## UI/UX Requirements

### User Experience
- [ ] Optimistic updates: UI updates immediately, rolls back on error
- [ ] Loading states: Spinners during API calls
- [ ] Success feedback: Toast notification on create/update/delete
- [ ] Error feedback: Toast notification with error message
- [ ] Form validation: Real-time validation with clear error messages
- [ ] Responsive design: Works on mobile and desktop

### Accessibility
- [ ] ARIA labels on buttons and form fields
- [ ] Keyboard navigation support
- [ ] Focus management in modals
- [ ] Screen reader friendly

## Success Criteria
- [ ] Can create, read, update, delete customers
- [ ] Pagination works correctly
- [ ] Form validation prevents invalid submissions
- [ ] Optimistic updates provide instant feedback
- [ ] Error handling shows user-friendly messages
- [ ] Loading states prevent user confusion
- [ ] Responsive design works on all screen sizes
- [ ] MVVM pattern is clear (ViewModel separate from View)

## Dependencies
- **Requires**: PRD 01 (Foundation)
- **Does NOT require**: PRD 02 (Customer Backend) - can use mock data to start
- **Can run parallel with**: PRDs 02, 04, 05, 06, 07
- **Final integration requires**: PRD 02 completion

## Timeline Estimate
**0.5-1 day** (can be done in parallel with other features)

## Notes
- Can start development with mock data if backend isn't ready
- Use generated TypeScript types for type safety
- Follow MVVM pattern strictly (ViewModel in hooks, View in components)
- Reuse shared components from foundation (Pagination, LoadingSpinner, etc.)

