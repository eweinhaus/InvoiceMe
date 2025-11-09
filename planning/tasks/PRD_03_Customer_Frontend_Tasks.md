# Task List: PRD 03 - Customer Feature Frontend

## Overview
**Priority**: 🟢 Can run in parallel with PRDs 02, 04-07  
**Estimated Time**: 0.5-1 day  
**Dependencies**: PRD 01 (Foundation) - **PRD 02 NOT required to start** (can use mock data)  
**Enables**: None (standalone feature)  
**Note**: Can start immediately after PRD 01 using mock API responses. PRD 02 completion enables final integration.

This task list breaks down PRD 03 into actionable, checkable tasks for implementing the Customer frontend feature following MVVM architecture pattern.

---

## Feature Structure Setup

### 1. Create Feature Directory Structure
- [ ] Create `src/features/customers/` directory
- [ ] Create `src/features/customers/components/` directory
- [ ] Create `src/features/customers/hooks/` directory
- [ ] Create `src/features/customers/types/` directory
- [ ] Create `src/features/customers/pages/` directory
- [ ] Verify directory structure matches PRD specification

---

## Types

### 2. Define Customer Types
- [ ] Check if OpenAPI types are available in `src/types/api.ts`
  - [ ] If available, verify `Customer`, `CustomerRequest`, `CustomerResponse` types exist
  - [ ] If not available, proceed to manual type definition

- [ ] Create `src/features/customers/types/customer.types.ts`:
  - [ ] Define `Customer` interface:
    - [ ] `id: string` (UUID)
    - [ ] `name: string`
    - [ ] `email: string`
    - [ ] `address?: string` (optional)
    - [ ] `phone?: string` (optional)
    - [ ] `createdAt: string` (ISO date string)
    - [ ] `updatedAt: string` (ISO date string)
  - [ ] Define `CustomerRequest` interface:
    - [ ] `name: string`
    - [ ] `email: string`
    - [ ] `address?: string`
    - [ ] `phone?: string`
  - [ ] Define `CustomerResponse` interface (same as Customer, or use Customer)
  - [ ] Define `Page<Customer>` interface:
    - [ ] `content: Customer[]`
    - [ ] `totalElements: number`
    - [ ] `totalPages: number`
    - [ ] `size: number`
    - [ ] `number: number` (current page, 0-indexed)
  - [ ] Export all types

- [ ] If using OpenAPI types, create type aliases:
  - [ ] Import types from `src/types/api.ts`
  - [ ] Create aliases for easier use: `type Customer = ...`
  - [ ] Export aliases

---

## API Client Methods

### 3. Create Customer API Client Methods
- [ ] Create `src/lib/api/customers.ts` file (or add to existing API client structure)
- [ ] Import `apiClient` from `src/lib/api/client.ts`
- [ ] Import customer types from `src/features/customers/types/customer.types.ts`

- [ ] Implement `getCustomers(page, size)`:
  - [ ] Function signature: `(page: number, size: number) => Promise<Page<Customer>>`
  - [ ] Use `apiClient.get('/customers', { params: { page, size } })`
  - [ ] Return typed response data
  - [ ] Handle errors (will be caught by interceptors)

- [ ] Implement `getCustomerById(id)`:
  - [ ] Function signature: `(id: string) => Promise<Customer>`
  - [ ] Use `apiClient.get(`/customers/${id}`)`
  - [ ] Return typed response data

- [ ] Implement `createCustomer(data)`:
  - [ ] Function signature: `(data: CustomerRequest) => Promise<Customer>`
  - [ ] Use `apiClient.post('/customers', data)`
  - [ ] Return typed response data

- [ ] Implement `updateCustomer(id, data)`:
  - [ ] Function signature: `(id: string, data: CustomerRequest) => Promise<Customer>`
  - [ ] Use `apiClient.put(`/customers/${id}`, data)`
  - [ ] Return typed response data

- [ ] Implement `deleteCustomer(id)`:
  - [ ] Function signature: `(id: string) => Promise<void>`
  - [ ] Use `apiClient.delete(`/customers/${id}`)`
  - [ ] Return void

- [ ] Export all API methods

---

## React Query Hooks

### 4. Create useCustomers Hook (Query)
- [ ] Create `src/features/customers/hooks/useCustomers.ts`
- [ ] Import `useQuery` from `@tanstack/react-query`
- [ ] Import `getCustomers` from API client
- [ ] Import customer types

- [ ] Implement `useCustomers` hook:
  - [ ] Accept parameters: `page: number = 0`, `size: number = 20`
  - [ ] Use `useQuery` with:
    - [ ] Query key: `['customers', page, size]`
    - [ ] Query function: `() => getCustomers(page, size)`
    - [ ] Configure staleTime (e.g., 30 seconds)
    - [ ] Configure cacheTime (e.g., 5 minutes)
  - [ ] Return: `{ data, isLoading, error, refetch }`
  - [ ] Type return value properly

- [ ] Test hook in isolation (optional, for development)

---

### 5. Create useCustomerMutations Hook
- [ ] Create `src/features/customers/hooks/useCustomerMutations.ts`
- [ ] Import `useMutation`, `useQueryClient` from `@tanstack/react-query`
- [ ] Import API client methods
- [ ] Import customer types
- [ ] Import toast notification library (if available, or use console for now)

- [ ] Implement `useCreateCustomer` mutation:
  - [ ] Use `useMutation` with:
    - [ ] Mutation function: `(data: CustomerRequest) => createCustomer(data)`
    - [ ] On success: Invalidate `['customers']` queries
    - [ ] On success: Show success toast notification
    - [ ] On error: Show error toast notification
  - [ ] Return mutation object: `{ mutate, mutateAsync, isLoading, error }`

- [ ] Implement `useUpdateCustomer` mutation:
  - [ ] Use `useMutation` with:
    - [ ] Mutation function: `({ id, data }: { id: string, data: CustomerRequest }) => updateCustomer(id, data)`
    - [ ] On success: Invalidate `['customers']` queries
    - [ ] On success: Show success toast notification
    - [ ] On error: Show error toast notification
  - [ ] Return mutation object

- [ ] Implement `useDeleteCustomer` mutation:
  - [ ] Use `useMutation` with:
    - [ ] Mutation function: `(id: string) => deleteCustomer(id)`
    - [ ] On success: Invalidate `['customers']` queries
    - [ ] On success: Show success toast notification
    - [ ] On error: Show error toast notification
  - [ ] Return mutation object

- [ ] Export all mutation hooks

- [ ] **Optional: Implement Optimistic Updates**:
  - [ ] For `useCreateCustomer`: Optimistically add customer to cache
  - [ ] For `useUpdateCustomer`: Optimistically update customer in cache
  - [ ] For `useDeleteCustomer`: Optimistically remove customer from cache
  - [ ] Rollback on error using `onError` callback

---

### 6. Create useCustomerViewModel Hook (MVVM ViewModel)
- [ ] Create `src/features/customers/hooks/useCustomerViewModel.ts`
- [ ] Import `useCustomers` and `useCustomerMutations` hooks
- [ ] Import customer types

- [ ] Implement `useCustomerViewModel` hook:
  - [ ] Accept parameters: `page: number = 0`, `size: number = 20`
  - [ ] Call `useCustomers(page, size)` to get data
  - [ ] Call `useCreateCustomer()`, `useUpdateCustomer()`, `useDeleteCustomer()` mutations
  - [ ] Transform data if needed (sorting, filtering)
  - [ ] Handle pagination state
  - [ ] Return object with:
    - [ ] `customers: Customer[]` (from data.content)
    - [ ] `pagination: { page, size, totalElements, totalPages }`
    - [ ] `isLoading: boolean`
    - [ ] `error: Error | null`
    - [ ] `createCustomer: (data: CustomerRequest) => void`
    - [ ] `updateCustomer: (id: string, data: CustomerRequest) => void`
    - [ ] `deleteCustomer: (id: string) => void`
    - [ ] `refetch: () => void`
    - [ ] `setPage: (page: number) => void`
    - [ ] `setSize: (size: number) => void`

- [ ] This hook encapsulates all business logic and state management
- [ ] Components should use this hook, not the individual query/mutation hooks directly

---

## Components

### 7. Create CustomerForm Component
- [ ] Create `src/features/customers/components/CustomerForm.tsx`
- [ ] Install React Hook Form and Zod if not already installed:
  - [ ] `npm install react-hook-form @hookform/resolvers zod`

- [ ] Import required dependencies:
  - [ ] `useForm` from `react-hook-form`
  - [ ] `zodResolver` from `@hookform/resolvers/zod`
  - [ ] `z` from `zod`
  - [ ] shadcn/ui components: `Form`, `Input`, `Label`, `Button`
  - [ ] Customer types

- [ ] Create Zod validation schema:
  - [ ] `name`: `z.string().min(2, "Name must be at least 2 characters")`
  - [ ] `email`: `z.string().email("Invalid email format")`
  - [ ] `address`: `z.string().optional()`
  - [ ] `phone`: `z.string().optional()` (or add format validation)

- [ ] Define component props:
  - [ ] `mode: 'create' | 'edit'`
  - [ ] `initialData?: Customer` (for edit mode)
  - [ ] `onSubmit: (data: CustomerRequest) => void | Promise<void>`
  - [ ] `onCancel: () => void`
  - [ ] `isLoading?: boolean`

- [ ] Implement form component:
  - [ ] Use `useForm` with zodResolver
  - [ ] Set default values from `initialData` if in edit mode
  - [ ] Create form fields:
    - [ ] Name input (required)
    - [ ] Email input (required)
    - [ ] Address input (optional, textarea)
    - [ ] Phone input (optional)
  - [ ] Display validation errors for each field
  - [ ] Submit button with loading state
  - [ ] Cancel button
  - [ ] Handle form submission

- [ ] Add accessibility:
  - [ ] ARIA labels on form fields
  - [ ] Proper label associations
  - [ ] Error announcements for screen readers

- [ ] Style with Tailwind CSS

---

### 8. Create CustomerList Component
- [ ] Create `src/features/customers/components/CustomerList.tsx`
- [ ] Import required dependencies:
  - [ ] shadcn/ui components: `Table`, `Button`, `Badge`
  - [ ] Shared components: `LoadingSpinner`, `ErrorMessage`, `Pagination`
  - [ ] Customer types

- [ ] Define component props:
  - [ ] `customers: Customer[]`
  - [ ] `isLoading: boolean`
  - [ ] `error: Error | null`
  - [ ] `pagination: { page, size, totalElements, totalPages }`
  - [ ] `onEdit: (customer: Customer) => void`
  - [ ] `onDelete: (customer: Customer) => void`
  - [ ] `onPageChange: (page: number) => void`
  - [ ] `onSizeChange: (size: number) => void`

- [ ] Implement component:
  - [ ] Show loading spinner when `isLoading` is true
  - [ ] Show error message when `error` exists
  - [ ] Show empty state when `customers.length === 0` and not loading
  - [ ] Create table with columns:
    - [ ] Name
    - [ ] Email
    - [ ] Phone (show "N/A" if empty)
    - [ ] Address (truncate if long)
    - [ ] Actions (Edit, Delete buttons)
  - [ ] Add Pagination component at bottom
  - [ ] Handle pagination changes

- [ ] Add accessibility:
  - [ ] ARIA labels on table
  - [ ] ARIA labels on action buttons
  - [ ] Keyboard navigation support

- [ ] Style with Tailwind CSS
- [ ] Make responsive (mobile-friendly table)

---

### 9. Create CustomerDeleteDialog Component
- [ ] Create `src/features/customers/components/CustomerDeleteDialog.tsx`
- [ ] Import required dependencies:
  - [ ] shadcn/ui components: `Dialog`, `Button`
  - [ ] Customer types

- [ ] Define component props:
  - [ ] `open: boolean`
  - [ ] `onOpenChange: (open: boolean) => void`
  - [ ] `customer: Customer | null`
  - [ ] `onConfirm: () => void | Promise<void>`
  - [ ] `isLoading?: boolean`

- [ ] Implement dialog component:
  - [ ] Use shadcn/ui Dialog component
  - [ ] Show customer name in confirmation message
  - [ ] Display: "Are you sure you want to delete {customer.name}?"
  - [ ] Delete button (destructive style, with loading state)
  - [ ] Cancel button
  - [ ] Handle confirm action
  - [ ] Close dialog on cancel or after successful delete

- [ ] Add accessibility:
  - [ ] Focus management (focus on delete button when opened)
  - [ ] ARIA labels
  - [ ] Keyboard support (Escape to close)

- [ ] Style with Tailwind CSS

---

### 10. Create CustomerCard Component (Optional)
- [ ] Create `src/features/customers/components/CustomerCard.tsx`
- [ ] Import required dependencies:
  - [ ] shadcn/ui components: `Card`
  - [ ] Customer types

- [ ] Define component props:
  - [ ] `customer: Customer`
  - [ ] `onEdit?: (customer: Customer) => void`
  - [ ] `onDelete?: (customer: Customer) => void`

- [ ] Implement card component:
  - [ ] Display customer name prominently
  - [ ] Display email, phone, address
  - [ ] Show created/updated dates (formatted)
  - [ ] Add edit/delete buttons if callbacks provided

- [ ] Style with Tailwind CSS
- [ ] Make responsive

---

## Page Component

### 11. Create CustomersPage Component
- [ ] Create `src/features/customers/pages/CustomersPage.tsx`
- [ ] Import required dependencies:
  - [ ] `useState` from React
  - [ ] `useCustomerViewModel` hook
  - [ ] `CustomerForm` component
  - [ ] `CustomerList` component
  - [ ] `CustomerDeleteDialog` component
  - [ ] shadcn/ui components: `Dialog`, `Button`
  - [ ] Customer types

- [ ] Implement page component:
  - [ ] Use `useCustomerViewModel` hook with pagination state
  - [ ] Manage dialog state for create/edit form:
    - [ ] `isFormOpen: boolean`
    - [ ] `formMode: 'create' | 'edit'`
    - [ ] `editingCustomer: Customer | null`
  - [ ] Manage delete dialog state:
    - [ ] `isDeleteDialogOpen: boolean`
    - [ ] `customerToDelete: Customer | null`
  - [ ] Create header section:
    - [ ] Title: "Customers"
    - [ ] "Create Customer" button
  - [ ] Render CustomerList component with props
  - [ ] Render CustomerForm in Dialog (create/edit mode)
  - [ ] Render CustomerDeleteDialog
  - [ ] Handle form submission:
    - [ ] Call `createCustomer` or `updateCustomer` from ViewModel
    - [ ] Close dialog on success
  - [ ] Handle delete confirmation:
    - [ ] Call `deleteCustomer` from ViewModel
    - [ ] Close dialog on success

- [ ] Add error boundary (optional, can use React error boundary)
- [ ] Style with Tailwind CSS
- [ ] Make responsive

---

## Routing Integration

### 12. Add Customers Route
- [ ] Open `src/routes/index.tsx` (or routing file)
- [ ] Import `CustomersPage` component
- [ ] Add route:
  - [ ] Path: `/customers`
  - [ ] Component: `<CustomersPage />`
- [ ] Verify route is accessible
- [ ] Test navigation to `/customers`

---

## Mock Data Setup (If Backend Not Ready)

### 13. Set Up Mock Data (Optional - Only if PRD 02 not complete)
- [ ] Decide on mock strategy:
  - [ ] Option A: MSW (Mock Service Worker)
  - [ ] Option B: Simple stub functions in API client
  - [ ] Option C: React Query mock implementation

- [ ] If using MSW:
  - [ ] Install: `npm install -D msw`
  - [ ] Create `src/mocks/handlers.ts` with customer handlers
  - [ ] Create `src/mocks/browser.ts` for browser setup
  - [ ] Set up MSW in `src/main.tsx` (development only)
  - [ ] Create mock customer data

- [ ] If using simple stubs:
  - [ ] Modify API client methods to return mock data
  - [ ] Add delay simulation (setTimeout) for realistic behavior
  - [ ] Create mock customer array

- [ ] Verify mock data works:
  - [ ] List customers shows mock data
  - [ ] Create customer adds to mock array
  - [ ] Update customer modifies mock array
  - [ ] Delete customer removes from mock array

- [ ] Document how to switch from mocks to real API

---

## UI/UX Polish

### 14. Add Loading States
- [ ] Verify loading spinners appear during API calls
- [ ] Add skeleton loaders for table rows (optional, better UX)
- [ ] Ensure buttons show loading state during mutations

### 15. Add Error Handling
- [ ] Verify error messages display correctly
- [ ] Test error scenarios:
  - [ ] Network errors
  - [ ] Validation errors
  - [ ] 404 errors
  - [ ] 500 errors
- [ ] Ensure toast notifications work (or console logs if toast not set up)

### 16. Add Success Feedback
- [ ] Verify success toast notifications appear on create/update/delete
- [ ] Test optimistic updates (if implemented)
- [ ] Verify UI updates immediately after successful operations

### 17. Form Validation
- [ ] Test real-time validation:
  - [ ] Name too short
  - [ ] Invalid email format
  - [ ] Required fields
- [ ] Verify error messages are clear and helpful
- [ ] Test form submission prevention when invalid

### 18. Responsive Design
- [ ] Test on mobile viewport (< 768px):
  - [ ] Table is scrollable or switches to card view
  - [ ] Form is usable
  - [ ] Buttons are accessible
- [ ] Test on tablet viewport (768px - 1024px)
- [ ] Test on desktop viewport (> 1024px)
- [ ] Verify all breakpoints work correctly

### 19. Accessibility
- [ ] Test keyboard navigation:
  - [ ] Tab through form fields
  - [ ] Enter to submit form
  - [ ] Escape to close dialogs
- [ ] Test with screen reader (optional, but recommended)
- [ ] Verify ARIA labels are present
- [ ] Test focus management in modals

---

## Integration Testing

### 20. Manual Testing Checklist
- [ ] **Create Customer**:
  - [ ] Click "Create Customer" button
  - [ ] Fill form with valid data
  - [ ] Submit form
  - [ ] Verify customer appears in list
  - [ ] Verify success message

- [ ] **Read Customers**:
  - [ ] Verify customers load on page load
  - [ ] Verify pagination works
  - [ ] Verify empty state shows when no customers

- [ ] **Update Customer**:
  - [ ] Click "Edit" button on a customer
  - [ ] Verify form is pre-filled
  - [ ] Modify data
  - [ ] Submit form
  - [ ] Verify customer is updated in list
  - [ ] Verify success message

- [ ] **Delete Customer**:
  - [ ] Click "Delete" button on a customer
  - [ ] Verify confirmation dialog appears
  - [ ] Confirm deletion
  - [ ] Verify customer is removed from list
  - [ ] Verify success message

- [ ] **Form Validation**:
  - [ ] Try to submit empty form
  - [ ] Try to submit with invalid email
  - [ ] Try to submit with name < 2 characters
  - [ ] Verify validation errors appear

- [ ] **Error Handling**:
  - [ ] Disconnect network (or stop backend)
  - [ ] Try to create customer
  - [ ] Verify error message appears
  - [ ] Reconnect network
  - [ ] Verify recovery works

- [ ] **Pagination**:
  - [ ] Create more than 20 customers (or adjust size)
  - [ ] Verify pagination controls appear
  - [ ] Click next page
  - [ ] Verify correct customers appear
  - [ ] Change page size
  - [ ] Verify list updates

---

## Final Verification

### 21. MVVM Pattern Verification
- [ ] Verify ViewModel (`useCustomerViewModel`) contains business logic
- [ ] Verify components (View) are mostly presentational
- [ ] Verify components use ViewModel, not individual query/mutation hooks
- [ ] Verify separation of concerns is clear

### 22. Code Quality
- [ ] Run ESLint and fix any errors
- [ ] Run Prettier and format code
- [ ] Verify TypeScript compiles without errors
- [ ] Check for any console.log statements (remove or replace with proper logging)
- [ ] Verify no unused imports

### 23. Documentation
- [ ] Add JSDoc comments to hooks (optional, but recommended)
- [ ] Add comments for complex logic
- [ ] Verify code is self-documenting

---

## Success Criteria Checklist

- [ ] Can create, read, update, delete customers
- [ ] Pagination works correctly
- [ ] Form validation prevents invalid submissions
- [ ] Optimistic updates provide instant feedback (if implemented)
- [ ] Error handling shows user-friendly messages
- [ ] Loading states prevent user confusion
- [ ] Responsive design works on all screen sizes
- [ ] MVVM pattern is clear (ViewModel separate from View)
- [ ] All manual tests pass
- [ ] Code quality is good (no linting errors, TypeScript compiles)

---

## Notes

- **Mock Data**: If backend (PRD 02) is not ready, use mock data to enable parallel development
- **Type Generation**: Prefer using auto-generated types from OpenAPI spec if available
- **MVVM Pattern**: Strictly follow MVVM - ViewModel in hooks, View in components
- **Reuse Components**: Use shared components from foundation (Pagination, LoadingSpinner, ErrorMessage)
- **Toast Notifications**: If toast library not set up, use console.log for now (can be improved in PRD 08)
- **Testing**: Manual testing is sufficient for this PRD. Unit tests can be added later if needed.

---

## Next Steps After Completion

1. If using mock data, switch to real API when PRD 02 is complete
2. Test integration with real backend
3. Verify all features work end-to-end
4. Move to next feature PRD (PRD 05: Invoice Frontend or PRD 07: Payment Frontend)


