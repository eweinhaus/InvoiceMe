# Task List: PRD 07 - Payment Feature Frontend

## Overview
**Priority**: 🟢 Can run in parallel with PRDs 02, 03, 04, 05, 06  
**Estimated Time**: 0.5-1 day  
**Dependencies**: PRD 01 (Foundation) - **PRD 06 NOT required to start** (can use mock data)  
**Enables**: None (standalone feature)  
**Note**: Can start immediately after PRD 01 using mock API responses. PRD 06 completion enables final integration. Simpler than Invoice frontend, but requires invoice integration for invoice selector.

This task list breaks down PRD 07 into actionable, checkable tasks for implementing the Payment frontend feature following MVVM architecture pattern.

---

## Feature Structure Setup

### 1. Create Feature Directory Structure
- [ ] Create `src/features/payments/` directory
- [ ] Create `src/features/payments/components/` directory
- [ ] Create `src/features/payments/hooks/` directory
- [ ] Create `src/features/payments/types/` directory
- [ ] Create `src/features/payments/pages/` directory
- [ ] Verify directory structure matches PRD specification

---

## Types

### 2. Define Payment Types
- [ ] Check if OpenAPI types are available in `src/types/api.ts`
  - [ ] If available, verify `PaymentResponse`, `PaymentRequest` types exist
  - [ ] If not available, proceed to manual type definition

- [ ] Create `src/features/payments/types/payment.types.ts`:
  - [ ] Define `Payment` interface:
    - [ ] `id: string` (UUID)
    - [ ] `invoiceId: string` (UUID)
    - [ ] `invoiceNumber?: string` (optional, for display)
    - [ ] `amount: number`
    - [ ] `paymentDate: string` (ISO date string)
    - [ ] `createdAt: string` (ISO date string)
  - [ ] Define `PaymentRequest` interface:
    - [ ] `invoiceId: string` (UUID, required)
    - [ ] `amount: number` (required, > 0)
    - [ ] `paymentDate: string` (ISO date string, required)
  - [ ] Define `PaymentResponse` interface (if different from Payment):
    - [ ] Same as Payment or extend Payment
  - [ ] Define `Page<Payment>` interface:
    - [ ] `content: Payment[]`
    - [ ] `totalElements: number`
    - [ ] `totalPages: number`
    - [ ] `size: number`
    - [ ] `number: number` (current page, 0-indexed)
  - [ ] Export all types

- [ ] If using OpenAPI types, create type aliases:
  - [ ] Import types from `src/types/api.ts`
  - [ ] Create aliases for easier use: `type Payment = ...`, `type PaymentRequest = ...`
  - [ ] Map OpenAPI types to feature types if needed
  - [ ] Export aliases

---

## API Client Methods

### 3. Create Payment API Client Methods
- [ ] Create `src/lib/api/payments.ts` file (or add to existing API client structure)
- [ ] Import `apiClient` from `src/lib/api/client.ts`
- [ ] Import payment types from `src/features/payments/types/payment.types.ts`

- [ ] Implement `getPayments(page, size)`:
  - [ ] Function signature: `(page: number, size: number) => Promise<Page<Payment>>`
  - [ ] Use `apiClient.get('/payments', { params: { page, size } })`
  - [ ] Return typed response data
  - [ ] Handle errors (will be caught by interceptors)

- [ ] Implement `getPaymentsByInvoice(invoiceId, page, size)`:
  - [ ] Function signature: `(invoiceId: string, page: number, size: number) => Promise<Page<Payment>>`
  - [ ] Use `apiClient.get('/payments', { params: { invoiceId, page, size } })`
  - [ ] Return typed response data

- [ ] Implement `getPaymentById(id)`:
  - [ ] Function signature: `(id: string) => Promise<Payment>`
  - [ ] Use `apiClient.get(`/payments/${id}`)`
  - [ ] Return typed response data

- [ ] Implement `recordPayment(data)`:
  - [ ] Function signature: `(data: PaymentRequest) => Promise<Payment>`
  - [ ] Use `apiClient.post('/payments', data)`
  - [ ] Return typed response data

- [ ] Export all API methods

---

## React Query Hooks

### 4. Create usePayments Hook (Query)
- [ ] Create `src/features/payments/hooks/usePayments.ts`
- [ ] Import `useQuery` from `@tanstack/react-query`
- [ ] Import payment API client methods
- [ ] Import payment types

- [ ] Implement `usePayments` hook:
  - [ ] Accept parameters: `page: number = 0`, `size: number = 20`
  - [ ] Use `useQuery` with:
    - [ ] Query key: `['payments', 'all', page, size]`
    - [ ] Query function: `() => getPayments(page, size)`
    - [ ] Configure staleTime (e.g., 30 seconds)
    - [ ] Configure cacheTime (e.g., 5 minutes)
  - [ ] Return: `{ data, isLoading, error, refetch }`
  - [ ] Type return value properly

- [ ] Implement `usePaymentsByInvoice` hook:
  - [ ] Accept parameters: `invoiceId: string`, `page: number = 0`, `size: number = 20`
  - [ ] Use `useQuery` with:
    - [ ] Query key: `['payments', 'invoice', invoiceId, page, size]`
    - [ ] Query function: `() => getPaymentsByInvoice(invoiceId, page, size)`
    - [ ] Configure `enabled: !!invoiceId` (only fetch if invoiceId exists)
    - [ ] Configure staleTime and cacheTime
  - [ ] Return: `{ data, isLoading, error, refetch }`

- [ ] Implement `usePayment` hook:
  - [ ] Accept parameter: `id: string`
  - [ ] Use `useQuery` with:
    - [ ] Query key: `['payments', id]`
    - [ ] Query function: `() => getPaymentById(id)`
    - [ ] Configure `enabled: !!id` (only fetch if id exists)
  - [ ] Return: `{ data, isLoading, error, refetch }`

- [ ] Export all query hooks

---

### 5. Create usePaymentMutations Hook
- [ ] Create `src/features/payments/hooks/usePaymentMutations.ts`
- [ ] Import `useMutation`, `useQueryClient` from `@tanstack/react-query`
- [ ] Import API client methods
- [ ] Import payment types
- [ ] Import toast notification library (if available, or use console for now)

- [ ] Implement `useRecordPayment` mutation:
  - [ ] Use `useMutation` with:
    - [ ] Mutation function: `(data: PaymentRequest) => recordPayment(data)`
    - [ ] On success: Invalidate `['payments']` queries
    - [ ] On success: Invalidate `['invoices']` queries (to update invoice balance)
    - [ ] On success: Invalidate specific invoice query: `['invoices', data.invoiceId]`
    - [ ] On success: Show success toast notification with updated balance
    - [ ] On error: Show error toast notification
  - [ ] Return mutation object: `{ mutate, mutateAsync, isLoading, error }`

- [ ] **Optional: Implement Optimistic Updates**:
  - [ ] For `useRecordPayment`: Optimistically add payment to cache
  - [ ] Optimistically update invoice balance in cache
  - [ ] Rollback on error using `onError` callback

- [ ] Export mutation hook

---

### 6. Create usePaymentViewModel Hook (MVVM ViewModel)
- [ ] Create `src/features/payments/hooks/usePaymentViewModel.ts`
- [ ] Import `useState` from React
- [ ] Import `usePayments`, `usePaymentsByInvoice` hooks
- [ ] Import `usePaymentMutations` hooks
- [ ] Import payment types

- [ ] Implement `usePaymentViewModel` hook:
  - [ ] Manage filter state:
    - [ ] `invoiceFilter: string | null` (default: null)
  - [ ] Manage pagination state:
    - [ ] `page: number` (default: 0)
    - [ ] `size: number` (default: 20)
  - [ ] Conditionally call appropriate query hook based on filters:
    - [ ] If `invoiceFilter`: Use `usePaymentsByInvoice`
    - [ ] Otherwise: Use `usePayments`
  - [ ] Call mutation hook: `useRecordPayment()`
  - [ ] Transform data if needed (sorting, formatting)
  - [ ] Validate payment amount against invoice balance (helper function):
    - [ ] Function: `validatePaymentAmount(amount: number, invoiceBalance: number): boolean`
    - [ ] Returns true if amount <= invoiceBalance and amount > 0
  - [ ] Return object with:
    - [ ] `payments: Payment[]` (from data.content)
    - [ ] `pagination: { page, size, totalElements, totalPages }`
    - [ ] `filters: { invoiceId: string | null }`
    - [ ] `isLoading: boolean`
    - [ ] `error: Error | null`
    - [ ] `recordPayment: (data: PaymentRequest) => void`
    - [ ] `setInvoiceFilter: (invoiceId: string | null) => void`
    - [ ] `setPage: (page: number) => void`
    - [ ] `setSize: (size: number) => void`
    - [ ] `refetch: () => void`
    - [ ] `validatePaymentAmount: (amount: number, invoiceBalance: number) => boolean`
  - [ ] Reset page to 0 when filter changes

- [ ] This hook encapsulates all business logic and state management
- [ ] Components should use this hook, not the individual query/mutation hooks directly

---

## Components

### 7. Create InvoiceSelector Component
- [ ] Create `src/features/payments/components/InvoiceSelector.tsx`
- [ ] Import required dependencies:
  - [ ] `useState`, `useEffect` from React
  - [ ] shadcn/ui components: `Select`, `Label`
  - [ ] Invoice types (from `src/features/invoices/types/invoice.types.ts`)
  - [ ] Invoice API hooks (from `src/features/invoices/hooks/useInvoices.ts` or mock data)

- [ ] Define component props:
  - [ ] `value: string | null` (selected invoice ID)
  - [ ] `onChange: (invoiceId: string | null) => void`
  - [ ] `error?: string` (validation error)
  - [ ] `disabled?: boolean`

- [ ] Implement component:
  - [ ] Fetch invoices list (use invoice hooks or mock data):
    - [ ] Filter invoices: Only show SENT or PAID invoices with balance > 0
    - [ ] Or show all invoices and filter in display
  - [ ] Create dropdown/select component:
    - [ ] "Select Invoice" placeholder option
    - [ ] List of invoices showing: Invoice #, Customer name, Balance
    - [ ] Format: "Invoice #123 - Customer Name ($500.00 balance)"
  - [ ] Handle selection change:
    - [ ] Call `onChange` with selected invoice ID
  - [ ] Display selected invoice details (if invoice selected):
    - [ ] Invoice number
    - [ ] Customer name
    - [ ] Current balance (formatted as currency)
  - [ ] Display validation error if provided
  - [ ] Show loading state while fetching invoices
  - [ ] Show empty state if no eligible invoices

- [ ] Add accessibility:
  - [ ] ARIA labels on select
  - [ ] Label association
  - [ ] Error announcements

- [ ] Style with Tailwind CSS
- [ ] Make responsive
- [ ] Export component

---

### 8. Create PaymentForm Component
- [ ] Create `src/features/payments/components/PaymentForm.tsx`
- [ ] Install React Hook Form and Zod if not already installed:
  - [ ] `npm install react-hook-form @hookform/resolvers zod`

- [ ] Import required dependencies:
  - [ ] `useForm` from `react-hook-form`
  - [ ] `zodResolver` from `@hookform/resolvers/zod`
  - [ ] `z` from `zod`
  - [ ] `useState`, `useEffect` from React
  - [ ] shadcn/ui components: `Form`, `Input`, `Label`, `Button`
  - [ ] `InvoiceSelector` component
  - [ ] Payment types
  - [ ] Invoice types (for invoice details display)

- [ ] Create Zod validation schema:
  - [ ] `invoiceId`: `z.string().uuid("Invalid invoice ID").min(1, "Invoice is required")`
  - [ ] `amount`: `z.number().min(0.01, "Amount must be greater than 0")`
    - [ ] Add custom validation: `.refine((val, ctx) => { ... }, { message: "Amount cannot exceed invoice balance" })`
  - [ ] `paymentDate`: `z.string().min(1, "Payment date is required")` or `z.date()`

- [ ] Define component props:
  - [ ] `onSubmit: (data: PaymentRequest) => void | Promise<void>`
  - [ ] `onCancel: () => void`
  - [ ] `isLoading?: boolean`
  - [ ] `selectedInvoice?: Invoice | null` (for displaying invoice details)

- [ ] Implement form component:
  - [ ] Use `useForm` with zodResolver
  - [ ] Manage selected invoice state:
    - [ ] `selectedInvoice: Invoice | null`
    - [ ] Fetch invoice details when invoice is selected
  - [ ] Create form fields:
    - [ ] Invoice selector (required, use `InvoiceSelector` component)
    - [ ] Amount input (number, required, min: 0.01)
    - [ ] Payment Date input (date picker or date input, required)
  - [ ] Display selected invoice details when invoice is selected:
    - [ ] Invoice number
    - [ ] Customer name
    - [ ] Current balance (formatted as currency)
    - [ ] Remaining balance after payment (calculated: balance - amount, updated in real-time)
  - [ ] Real-time balance validation:
    - [ ] Watch amount field
    - [ ] Calculate remaining balance as user types
    - [ ] Show error if amount exceeds balance
    - [ ] Update validation schema dynamically based on selected invoice balance
  - [ ] Display validation errors for each field
  - [ ] Submit button with loading state
  - [ ] Cancel button
  - [ ] Handle form submission
  - [ ] Set default payment date to today's date

- [ ] Add accessibility:
  - [ ] ARIA labels on form fields
  - [ ] Proper label associations
  - [ ] Error announcements for screen readers
  - [ ] Focus management

- [ ] Style with Tailwind CSS
- [ ] Make responsive
- [ ] Export component

---

### 9. Create PaymentList Component
- [ ] Create `src/features/payments/components/PaymentList.tsx`
- [ ] Import required dependencies:
  - [ ] shadcn/ui components: `Table`, `Button`, `Select`
  - [ ] Shared components: `LoadingSpinner`, `ErrorMessage`, `Pagination`
  - [ ] Payment types

- [ ] Define component props:
  - [ ] `payments: Payment[]`
  - [ ] `isLoading: boolean`
  - [ ] `error: Error | null`
  - [ ] `pagination: { page, size, totalElements, totalPages }`
  - [ ] `onViewDetails?: (payment: Payment) => void` (optional)
  - [ ] `onPageChange: (page: number) => void`
  - [ ] `onSizeChange: (size: number) => void`

- [ ] Implement component:
  - [ ] Show loading spinner when `isLoading` is true
  - [ ] Show error message when `error` exists
  - [ ] Show empty state when `payments.length === 0` and not loading
  - [ ] Create table with columns:
    - [ ] Payment # (truncated ID or formatted number)
    - [ ] Invoice # (invoice number or ID)
    - [ ] Customer (customer name, if available)
    - [ ] Amount (formatted as currency)
    - [ ] Payment Date (formatted date)
    - [ ] Created (formatted date)
    - [ ] Actions (View Details, if implemented)
  - [ ] Add Pagination component at bottom
  - [ ] Handle pagination changes

- [ ] Add accessibility:
  - [ ] ARIA labels on table
  - [ ] ARIA labels on action buttons
  - [ ] Keyboard navigation support
  - [ ] Screen reader friendly table structure

- [ ] Style with Tailwind CSS
- [ ] Make responsive (mobile-friendly table or card view)
- [ ] Export component

---

## Page Component

### 10. Create PaymentsPage Component
- [ ] Create `src/features/payments/pages/PaymentsPage.tsx`
- [ ] Import required dependencies:
  - [ ] `useState` from React
  - [ ] `usePaymentViewModel` hook
  - [ ] `PaymentList` component
  - [ ] `PaymentForm` component
  - [ ] shadcn/ui components: `Dialog`, `Button`, `Select`
  - [ ] Payment types
  - [ ] Invoice types (for invoice filter dropdown)

- [ ] Implement page component:
  - [ ] Use `usePaymentViewModel` hook with pagination and filter state
  - [ ] Manage dialog state for payment form:
    - [ ] `isFormOpen: boolean`
  - [ ] Fetch invoices list (for invoice filter dropdown):
    - [ ] Use invoice API or mock data
    - [ ] Store in state: `invoices: Invoice[]`
  - [ ] Create header section:
    - [ ] Title: "Payments"
    - [ ] Filter controls:
      - [ ] Invoice filter dropdown (All Invoices, then list of invoices)
    - [ ] "Record Payment" button
  - [ ] Render `PaymentList` component with props
  - [ ] Render `PaymentForm` in Dialog
  - [ ] Handle form submission:
    - [ ] Call `recordPayment` from ViewModel
    - [ ] Close dialog on success
    - [ ] Show success message with updated invoice balance
  - [ ] Handle filter changes:
    - [ ] Update ViewModel invoice filter
    - [ ] Reset pagination to page 0

- [ ] Add error boundary (optional, can use React error boundary)
- [ ] Style with Tailwind CSS
- [ ] Make responsive
- [ ] Export component

---

## Routing Integration

### 11. Add Payments Route
- [ ] Open `src/routes/index.tsx` (or routing file)
- [ ] Import `PaymentsPage` component
- [ ] Add route:
  - [ ] Path: `/payments`
  - [ ] Component: `<PaymentsPage />`
- [ ] Verify route is accessible
- [ ] Test navigation to `/payments`

---

## Integration with Invoice Feature

### 12. Update InvoiceDetails Component (Optional Enhancement)
- [ ] Open `src/features/invoices/components/InvoiceDetails.tsx`
- [ ] Import payment hooks:
  - [ ] `usePaymentsByInvoice` from `src/features/payments/hooks/usePayments.ts`
- [ ] Import payment types

- [ ] Add payment history section:
  - [ ] Fetch payments for the invoice using `usePaymentsByInvoice(invoice.id)`
  - [ ] Display payments list:
    - [ ] Payment date
    - [ ] Payment amount
    - [ ] Total payments count
  - [ ] Show "No payments yet" if empty

- [ ] Add "Record Payment" button:
  - [ ] Only show if invoice is not fully paid (balance > 0)
  - [ ] Open PaymentForm dialog when clicked
  - [ ] Handle payment recording
  - [ ] Refresh invoice data after payment is recorded

- [ ] Update invoice balance display:
  - [ ] Ensure balance updates after payment is recorded
  - [ ] May need to refetch invoice query

- [ ] Test integration:
  - [ ] Verify payment history displays correctly
  - [ ] Verify "Record Payment" button works
  - [ ] Verify balance updates after payment

---

## Mock Data Setup (If Backend Not Ready)

### 13. Set Up Mock Data (Optional - Only if PRD 06 not complete)
- [ ] Decide on mock strategy:
  - [ ] Option A: MSW (Mock Service Worker)
  - [ ] Option B: Simple stub functions in API client
  - [ ] Option C: React Query mock implementation

- [ ] If using MSW:
  - [ ] Install: `npm install -D msw`
  - [ ] Create `src/mocks/handlers.ts` with payment handlers
  - [ ] Create `src/mocks/browser.ts` for browser setup
  - [ ] Set up MSW in `src/main.tsx` (development only)
  - [ ] Create mock payment data
  - [ ] Create mock invoice data (for invoice selector)

- [ ] If using simple stubs:
  - [ ] Modify API client methods to return mock data
  - [ ] Add delay simulation (setTimeout) for realistic behavior
  - [ ] Create mock payment array
  - [ ] Create mock invoice array (for invoice selector)
  - [ ] Implement mock filtering logic
  - [ ] Implement mock payment recording (updates invoice balance)

- [ ] Verify mock data works:
  - [ ] List payments shows mock data
  - [ ] Filter by invoice works
  - [ ] Record payment adds to mock array
  - [ ] Invoice balance updates correctly
  - [ ] Invoice selector shows mock invoices

- [ ] Document how to switch from mocks to real API

---

## UI/UX Polish

### 14. Add Loading States
- [ ] Verify loading spinners appear during API calls
- [ ] Add skeleton loaders for table rows (optional, better UX)
- [ ] Ensure buttons show loading state during mutations
- [ ] Show loading state in form when submitting
- [ ] Show loading state in invoice selector when fetching invoices

### 15. Add Error Handling
- [ ] Verify error messages display correctly
- [ ] Test error scenarios:
  - [ ] Network errors
  - [ ] Validation errors (e.g., amount exceeds balance)
  - [ ] 404 errors (invoice not found)
  - [ ] 422 errors (business rule violations)
  - [ ] 500 errors
- [ ] Ensure toast notifications work (or console logs if toast not set up)
- [ ] Show specific error messages for validation failures
- [ ] Show error if invoice balance is insufficient

### 16. Add Success Feedback
- [ ] Verify success toast notifications appear on payment recording
- [ ] Test optimistic updates (if implemented)
- [ ] Verify UI updates immediately after successful operations
- [ ] Show confirmation message with updated invoice balance
- [ ] Close form dialog on success

### 17. Form Validation
- [ ] Test real-time validation:
  - [ ] Invoice required
  - [ ] Amount required
  - [ ] Amount > 0
  - [ ] Amount cannot exceed invoice balance
  - [ ] Payment date required
- [ ] Verify error messages are clear and helpful
- [ ] Test form submission prevention when invalid
- [ ] Test real-time balance calculation as amount is entered
- [ ] Verify remaining balance updates correctly

### 18. Invoice Selector Functionality
- [ ] Test invoice selector:
  - [ ] Fetches invoices correctly
  - [ ] Shows only eligible invoices (SENT or PAID with balance > 0)
  - [ ] Displays invoice information correctly
  - [ ] Handles selection change
  - [ ] Shows invoice details when selected
- [ ] Test with empty invoice list
- [ ] Test loading state

### 19. Real-time Balance Calculation
- [ ] Test balance calculation:
  - [ ] Current balance displays correctly
  - [ ] Remaining balance calculates correctly (balance - amount)
  - [ ] Updates in real-time as amount is entered
  - [ ] Shows error if amount exceeds balance
  - [ ] Currency formatting is correct

### 20. Filtering Functionality
- [ ] Test invoice filtering:
  - [ ] "All Invoices" shows all payments
  - [ ] Selecting invoice shows only payments for that invoice
  - [ ] Pagination resets when filter changes
- [ ] Test filter persistence (optional):
  - [ ] Filters persist in URL query params (if implemented)
  - [ ] Filters restore on page reload

### 21. Responsive Design
- [ ] Test on mobile viewport (< 768px):
  - [ ] Table is scrollable or switches to card view
  - [ ] Form is usable
  - [ ] Invoice selector is accessible
  - [ ] Filters are accessible
  - [ ] Buttons are accessible
- [ ] Test on tablet viewport (768px - 1024px)
- [ ] Test on desktop viewport (> 1024px)
- [ ] Verify all breakpoints work correctly

### 22. Accessibility
- [ ] Test keyboard navigation:
  - [ ] Tab through form fields
  - [ ] Tab through invoice selector
  - [ ] Enter to submit form
  - [ ] Escape to close dialogs
  - [ ] Arrow keys in dropdowns
- [ ] Test with screen reader (optional, but recommended)
- [ ] Verify ARIA labels are present
- [ ] Test focus management in modals
- [ ] Verify table structure is accessible

---

## Integration Testing

### 23. Manual Testing Checklist
- [ ] **Record Payment**:
  - [ ] Click "Record Payment" button
  - [ ] Select invoice from dropdown
  - [ ] Verify invoice details display (number, customer, balance)
  - [ ] Enter payment amount
  - [ ] Verify remaining balance calculates correctly
  - [ ] Enter payment date
  - [ ] Submit form
  - [ ] Verify payment appears in list
  - [ ] Verify success message with updated balance
  - [ ] Verify invoice balance updates (if viewing invoice details)

- [ ] **Read Payments**:
  - [ ] Verify payments load on page load
  - [ ] Verify pagination works
  - [ ] Verify empty state shows when no payments
  - [ ] Verify payment details are correct

- [ ] **Filter by Invoice**:
  - [ ] Select an invoice from filter dropdown
  - [ ] Verify only payments for that invoice show
  - [ ] Select "All Invoices"
  - [ ] Verify all payments show
  - [ ] Verify pagination resets when filter changes

- [ ] **Form Validation**:
  - [ ] Try to submit empty form
  - [ ] Try to submit without invoice
  - [ ] Try to submit without amount
  - [ ] Try to submit with amount = 0
  - [ ] Try to submit with amount < 0
  - [ ] Try to submit with amount exceeding invoice balance
  - [ ] Try to submit without payment date
  - [ ] Verify validation errors appear
  - [ ] Verify real-time validation works

- [ ] **Real-time Balance Calculation**:
  - [ ] Select an invoice
  - [ ] Enter payment amount
  - [ ] Verify remaining balance updates as amount changes
  - [ ] Enter amount exceeding balance
  - [ ] Verify error message appears
  - [ ] Reduce amount below balance
  - [ ] Verify error disappears

- [ ] **Invoice Selector**:
  - [ ] Verify only eligible invoices show (SENT or PAID with balance > 0)
  - [ ] Verify invoice information displays correctly
  - [ ] Verify selection works
  - [ ] Verify invoice details display when selected

- [ ] **Error Handling**:
  - [ ] Disconnect network (or stop backend)
  - [ ] Try to record payment
  - [ ] Verify error message appears
  - [ ] Reconnect network
  - [ ] Verify recovery works

- [ ] **Pagination**:
  - [ ] Create more than 20 payments (or adjust size)
  - [ ] Verify pagination controls appear
  - [ ] Click next page
  - [ ] Verify correct payments appear
  - [ ] Change page size
  - [ ] Verify list updates
  - [ ] Verify filters persist across pages

- [ ] **Invoice Integration** (if InvoiceDetails updated):
  - [ ] Open invoice details
  - [ ] Verify payment history displays
  - [ ] Click "Record Payment" button
  - [ ] Record payment
  - [ ] Verify payment appears in history
  - [ ] Verify invoice balance updates

---

## Final Verification

### 24. MVVM Pattern Verification
- [ ] Verify ViewModel (`usePaymentViewModel`) contains business logic
- [ ] Verify components (View) are mostly presentational
- [ ] Verify components use ViewModel, not individual query/mutation hooks
- [ ] Verify separation of concerns is clear
- [ ] Verify filter logic is in ViewModel, not components
- [ ] Verify validation logic is in ViewModel or form component

### 25. Code Quality
- [ ] Run ESLint and fix any errors
- [ ] Run Prettier and format code
- [ ] Verify TypeScript compiles without errors
- [ ] Check for any console.log statements (remove or replace with proper logging)
- [ ] Verify no unused imports
- [ ] Verify proper error handling throughout

### 26. Documentation
- [ ] Add JSDoc comments to hooks (optional, but recommended)
- [ ] Add comments for complex logic (balance calculations, validation)
- [ ] Verify code is self-documenting
- [ ] Document any workarounds or known issues

---

## Success Criteria Checklist

- [ ] Can record payments successfully
- [ ] Can list payments with pagination
- [ ] Can filter payments by invoice
- [ ] Payment amount validation prevents exceeding balance
- [ ] Invoice balance updates correctly after payment
- [ ] Real-time balance calculation works as amount is entered
- [ ] Optimistic updates provide instant feedback (if implemented)
- [ ] Error handling shows user-friendly messages
- [ ] Invoice selector works correctly (shows eligible invoices)
- [ ] Loading states prevent user confusion
- [ ] Form validation prevents invalid submissions
- [ ] Responsive design works on all screen sizes
- [ ] MVVM pattern is clear (ViewModel separate from View)
- [ ] Invoice integration works (payment history in InvoiceDetails, if implemented)
- [ ] All manual tests pass
- [ ] Code quality is good (no linting errors, TypeScript compiles)

---

## Notes

- **Mock Data**: If backend (PRD 06) is not ready, use mock data to enable parallel development
- **Type Generation**: Prefer using auto-generated types from OpenAPI spec if available
- **MVVM Pattern**: Strictly follow MVVM - ViewModel in hooks, View in components
- **Reuse Components**: Use shared components from foundation (Pagination, LoadingSpinner, ErrorMessage)
- **Toast Notifications**: If toast library not set up, use console.log for now (can be improved in PRD 08)
- **Invoice Selector**: Must fetch invoices - use Invoice API from PRD 04 or mock data
- **Balance Validation**: Critical - must prevent payments exceeding invoice balance
- **Real-time Updates**: Balance calculation should update as user types amount
- **Query Invalidation**: Must invalidate both payment and invoice queries after recording payment
- **Invoice Integration**: Updating InvoiceDetails is optional but recommended for better UX
- **Date Picker**: May need to add date picker component if not available in shadcn/ui
- **Testing**: Manual testing is sufficient for this PRD. Unit tests can be added later if needed.

---

## Next Steps After Completion

1. If using mock data, switch to real API when PRD 06 is complete
2. Test integration with real backend
3. Verify all features work end-to-end
4. Test with real invoice data (when PRD 04 is complete)
5. Test payment integration with invoice feature
6. Move to next feature PRD (PRD 08: Authentication & Integration or continue with other features)

