# Task List: PRD 05 - Invoice Feature Frontend

## Overview
**Priority**: 🟢 Can run in parallel with PRDs 02, 03, 04, 06, 07  
**Estimated Time**: 1-1.5 days  
**Dependencies**: PRD 01 (Foundation) - **PRD 04 NOT required to start** (can use mock data)  
**Enables**: None (standalone feature)  
**Note**: Can start immediately after PRD 01 using mock API responses. PRD 04 completion enables final integration. Most complex frontend feature due to line items and filtering.

This task list breaks down PRD 05 into actionable, checkable tasks for implementing the Invoice frontend feature following MVVM architecture pattern.

---

## Feature Structure Setup

### 1. Create Feature Directory Structure
- [ ] Create `src/features/invoices/` directory
- [ ] Create `src/features/invoices/components/` directory
- [ ] Create `src/features/invoices/hooks/` directory
- [ ] Create `src/features/invoices/types/` directory
- [ ] Create `src/features/invoices/pages/` directory
- [ ] Verify directory structure matches PRD specification

---

## Types

### 2. Define Invoice Types
- [ ] Check if OpenAPI types are available in `src/types/api.ts`
  - [ ] If available, verify `InvoiceResponse`, `InvoiceRequest`, `LineItemRequest`, `LineItemResponse` types exist
  - [ ] If not available, proceed to manual type definition

- [ ] Create `src/features/invoices/types/invoice.types.ts`:
  - [ ] Define `InvoiceStatus` enum:
    - [ ] `DRAFT = 'DRAFT'`
    - [ ] `SENT = 'SENT'`
    - [ ] `PAID = 'PAID'`
  - [ ] Define `LineItem` interface:
    - [ ] `description: string`
    - [ ] `quantity: number`
    - [ ] `unitPrice: number`
    - [ ] `subtotal?: number` (calculated, optional in request)
  - [ ] Define `Invoice` interface:
    - [ ] `id: string` (UUID)
    - [ ] `customerId: string` (UUID)
    - [ ] `customerName?: string` (optional, for display)
    - [ ] `status: InvoiceStatus`
    - [ ] `lineItems: LineItem[]`
    - [ ] `totalAmount: number`
    - [ ] `balance: number`
    - [ ] `createdAt: string` (ISO date string)
    - [ ] `updatedAt: string` (ISO date string)
  - [ ] Define `CreateInvoiceRequest` interface:
    - [ ] `customerId: string`
    - [ ] `lineItems: LineItemRequest[]` (at least one required)
  - [ ] Define `UpdateInvoiceRequest` interface:
    - [ ] `lineItems: LineItemRequest[]` (at least one required)
  - [ ] Define `LineItemRequest` interface:
    - [ ] `description: string`
    - [ ] `quantity: number`
    - [ ] `unitPrice: number`
  - [ ] Define `Page<Invoice>` interface:
    - [ ] `content: Invoice[]`
    - [ ] `totalElements: number`
    - [ ] `totalPages: number`
    - [ ] `size: number`
    - [ ] `number: number` (current page, 0-indexed)
  - [ ] Export all types

- [ ] If using OpenAPI types, create type aliases:
  - [ ] Import types from `src/types/api.ts`
  - [ ] Create aliases for easier use: `type Invoice = ...`, `type InvoiceStatus = ...`
  - [ ] Map OpenAPI types to feature types if needed
  - [ ] Export aliases

---

## API Client Methods

### 3. Create Invoice API Client Methods
- [ ] Create `src/lib/api/invoices.ts` file (or add to existing API client structure)
- [ ] Import `apiClient` from `src/lib/api/client.ts`
- [ ] Import invoice types from `src/features/invoices/types/invoice.types.ts`

- [ ] Implement `getInvoices(page, size)`:
  - [ ] Function signature: `(page: number, size: number) => Promise<Page<Invoice>>`
  - [ ] Use `apiClient.get('/invoices', { params: { page, size } })`
  - [ ] Return typed response data
  - [ ] Handle errors (will be caught by interceptors)

- [ ] Implement `getInvoicesByStatus(status, page, size)`:
  - [ ] Function signature: `(status: InvoiceStatus, page: number, size: number) => Promise<Page<Invoice>>`
  - [ ] Use `apiClient.get('/invoices', { params: { status, page, size } })`
  - [ ] Return typed response data

- [ ] Implement `getInvoicesByCustomer(customerId, page, size)`:
  - [ ] Function signature: `(customerId: string, page: number, size: number) => Promise<Page<Invoice>>`
  - [ ] Use `apiClient.get('/invoices', { params: { customerId, page, size } })`
  - [ ] Return typed response data

- [ ] Implement `getInvoiceById(id)`:
  - [ ] Function signature: `(id: string) => Promise<Invoice>`
  - [ ] Use `apiClient.get(`/invoices/${id}`)`
  - [ ] Return typed response data

- [ ] Implement `createInvoice(data)`:
  - [ ] Function signature: `(data: CreateInvoiceRequest) => Promise<Invoice>`
  - [ ] Use `apiClient.post('/invoices', data)`
  - [ ] Return typed response data

- [ ] Implement `updateInvoice(id, data)`:
  - [ ] Function signature: `(id: string, data: UpdateInvoiceRequest) => Promise<Invoice>`
  - [ ] Use `apiClient.put(`/invoices/${id}`, data)`
  - [ ] Return typed response data

- [ ] Implement `markInvoiceAsSent(id)`:
  - [ ] Function signature: `(id: string) => Promise<Invoice>`
  - [ ] Use `apiClient.post(`/invoices/${id}/send`)`
  - [ ] Return typed response data

- [ ] Export all API methods

---

## React Query Hooks

### 4. Create useInvoices Hook (Query)
- [ ] Create `src/features/invoices/hooks/useInvoices.ts`
- [ ] Import `useQuery` from `@tanstack/react-query`
- [ ] Import invoice API client methods
- [ ] Import invoice types

- [ ] Implement `useInvoices` hook:
  - [ ] Accept parameters: `page: number = 0`, `size: number = 20`
  - [ ] Use `useQuery` with:
    - [ ] Query key: `['invoices', 'all', page, size]`
    - [ ] Query function: `() => getInvoices(page, size)`
    - [ ] Configure staleTime (e.g., 30 seconds)
    - [ ] Configure cacheTime (e.g., 5 minutes)
  - [ ] Return: `{ data, isLoading, error, refetch }`
  - [ ] Type return value properly

- [ ] Implement `useInvoicesByStatus` hook:
  - [ ] Accept parameters: `status: InvoiceStatus`, `page: number = 0`, `size: number = 20`
  - [ ] Use `useQuery` with:
    - [ ] Query key: `['invoices', 'status', status, page, size]`
    - [ ] Query function: `() => getInvoicesByStatus(status, page, size)`
    - [ ] Configure staleTime and cacheTime
  - [ ] Return: `{ data, isLoading, error, refetch }`

- [ ] Implement `useInvoicesByCustomer` hook:
  - [ ] Accept parameters: `customerId: string`, `page: number = 0`, `size: number = 20`
  - [ ] Use `useQuery` with:
    - [ ] Query key: `['invoices', 'customer', customerId, page, size]`
    - [ ] Query function: `() => getInvoicesByCustomer(customerId, page, size)`
    - [ ] Configure staleTime and cacheTime
  - [ ] Return: `{ data, isLoading, error, refetch }`

- [ ] Implement `useInvoice` hook:
  - [ ] Accept parameter: `id: string`
  - [ ] Use `useQuery` with:
    - [ ] Query key: `['invoices', id]`
    - [ ] Query function: `() => getInvoiceById(id)`
    - [ ] Configure `enabled: !!id` (only fetch if id exists)
  - [ ] Return: `{ data, isLoading, error, refetch }`

- [ ] Export all query hooks

---

### 5. Create useInvoiceMutations Hook
- [ ] Create `src/features/invoices/hooks/useInvoiceMutations.ts`
- [ ] Import `useMutation`, `useQueryClient` from `@tanstack/react-query`
- [ ] Import API client methods
- [ ] Import invoice types
- [ ] Import toast notification library (if available, or use console for now)

- [ ] Implement `useCreateInvoice` mutation:
  - [ ] Use `useMutation` with:
    - [ ] Mutation function: `(data: CreateInvoiceRequest) => createInvoice(data)`
    - [ ] On success: Invalidate `['invoices']` queries
    - [ ] On success: Show success toast notification
    - [ ] On error: Show error toast notification
  - [ ] Return mutation object: `{ mutate, mutateAsync, isLoading, error }`

- [ ] Implement `useUpdateInvoice` mutation:
  - [ ] Use `useMutation` with:
    - [ ] Mutation function: `({ id, data }: { id: string, data: UpdateInvoiceRequest }) => updateInvoice(id, data)`
    - [ ] On success: Invalidate `['invoices']` queries and specific invoice query
    - [ ] On success: Show success toast notification
    - [ ] On error: Show error toast notification
  - [ ] Return mutation object

- [ ] Implement `useMarkInvoiceAsSent` mutation:
  - [ ] Use `useMutation` with:
    - [ ] Mutation function: `(id: string) => markInvoiceAsSent(id)`
    - [ ] On success: Invalidate `['invoices']` queries and specific invoice query
    - [ ] On success: Show success toast notification
    - [ ] On error: Show error toast notification
  - [ ] Return mutation object

- [ ] Export all mutation hooks

- [ ] **Optional: Implement Optimistic Updates**:
  - [ ] For `useCreateInvoice`: Optimistically add invoice to cache
  - [ ] For `useUpdateInvoice`: Optimistically update invoice in cache
  - [ ] For `useMarkInvoiceAsSent`: Optimistically update invoice status in cache
  - [ ] Rollback on error using `onError` callback

---

### 6. Create useInvoiceViewModel Hook (MVVM ViewModel)
- [ ] Create `src/features/invoices/hooks/useInvoiceViewModel.ts`
- [ ] Import `useState` from React
- [ ] Import `useInvoices`, `useInvoicesByStatus`, `useInvoicesByCustomer` hooks
- [ ] Import `useInvoiceMutations` hooks
- [ ] Import invoice types

- [ ] Implement `useInvoiceViewModel` hook:
  - [ ] Manage filter state:
    - [ ] `statusFilter: InvoiceStatus | 'ALL'` (default: 'ALL')
    - [ ] `customerFilter: string | null` (default: null)
  - [ ] Manage pagination state:
    - [ ] `page: number` (default: 0)
    - [ ] `size: number` (default: 20)
  - [ ] Conditionally call appropriate query hook based on filters:
    - [ ] If `statusFilter !== 'ALL'` and `customerFilter`: Use combined filter (may need custom query)
    - [ ] If `statusFilter !== 'ALL'`: Use `useInvoicesByStatus`
    - [ ] If `customerFilter`: Use `useInvoicesByCustomer`
    - [ ] Otherwise: Use `useInvoices`
  - [ ] Call mutation hooks: `useCreateInvoice()`, `useUpdateInvoice()`, `useMarkInvoiceAsSent()`
  - [ ] Transform data if needed (sorting, calculations)
  - [ ] Calculate totals and balances (client-side validation)
  - [ ] Return object with:
    - [ ] `invoices: Invoice[]` (from data.content)
    - [ ] `pagination: { page, size, totalElements, totalPages }`
    - [ ] `filters: { status: InvoiceStatus | 'ALL', customerId: string | null }`
    - [ ] `isLoading: boolean`
    - [ ] `error: Error | null`
    - [ ] `createInvoice: (data: CreateInvoiceRequest) => void`
    - [ ] `updateInvoice: (id: string, data: UpdateInvoiceRequest) => void`
    - [ ] `markAsSent: (id: string) => void`
    - [ ] `setStatusFilter: (status: InvoiceStatus | 'ALL') => void`
    - [ ] `setCustomerFilter: (customerId: string | null) => void`
    - [ ] `setPage: (page: number) => void`
    - [ ] `setSize: (size: number) => void`
    - [ ] `refetch: () => void`
  - [ ] Reset page to 0 when filters change

- [ ] This hook encapsulates all business logic and state management
- [ ] Components should use this hook, not the individual query/mutation hooks directly

---

## Components

### 7. Create InvoiceStatusBadge Component
- [ ] Create `src/features/invoices/components/InvoiceStatusBadge.tsx`
- [ ] Import shadcn/ui `Badge` component (install if needed: `npx shadcn-ui@latest add badge`)
- [ ] Import `InvoiceStatus` type

- [ ] Define component props:
  - [ ] `status: InvoiceStatus`

- [ ] Implement badge component:
  - [ ] Map status to color:
    - [ ] `DRAFT` → gray variant
    - [ ] `SENT` → blue variant
    - [ ] `PAID` → green variant
  - [ ] Display status text
  - [ ] Use Badge component with appropriate variant

- [ ] Add accessibility:
  - [ ] ARIA label: `aria-label={`Invoice status: ${status}`}`

- [ ] Style with Tailwind CSS
- [ ] Export component

---

### 8. Create LineItemForm Component
- [ ] Create `src/features/invoices/components/LineItemForm.tsx`
- [ ] Import required dependencies:
  - [ ] `useForm` from `react-hook-form`
  - [ ] shadcn/ui components: `Form`, `Input`, `Label`, `Button`
  - [ ] `LineItemRequest` type

- [ ] Define component props:
  - [ ] `lineItem: LineItemRequest`
  - [ ] `index: number`
  - [ ] `onChange: (index: number, lineItem: LineItemRequest) => void`
  - [ ] `onRemove: (index: number) => void`
  - [ ] `errors?: Record<string, string>` (validation errors)

- [ ] Implement component:
  - [ ] Create form fields:
    - [ ] Description input (text, required)
    - [ ] Quantity input (number, min: 1, required)
    - [ ] Unit Price input (number, min: 0, required)
  - [ ] Auto-calculate and display subtotal: `quantity * unitPrice`
  - [ ] Format subtotal as currency
  - [ ] Call `onChange` when any field changes
  - [ ] Remove button that calls `onRemove(index)`
  - [ ] Display validation errors if provided
  - [ ] Use controlled inputs with local state or form library

- [ ] Add accessibility:
  - [ ] ARIA labels on all inputs
  - [ ] Label associations
  - [ ] Error announcements

- [ ] Style with Tailwind CSS
- [ ] Make responsive (stack on mobile)
- [ ] Export component

---

### 9. Create InvoiceForm Component
- [ ] Create `src/features/invoices/components/InvoiceForm.tsx`
- [ ] Install React Hook Form and Zod if not already installed:
  - [ ] `npm install react-hook-form @hookform/resolvers zod`

- [ ] Import required dependencies:
  - [ ] `useForm`, `useFieldArray` from `react-hook-form`
  - [ ] `zodResolver` from `@hookform/resolvers/zod`
  - [ ] `z` from `zod`
  - [ ] shadcn/ui components: `Form`, `Input`, `Label`, `Button`, `Select`
  - [ ] `LineItemForm` component
  - [ ] Invoice types
  - [ ] Customer types (for customer selector)

- [ ] Create Zod validation schema:
  - [ ] `customerId`: `z.string().uuid("Invalid customer ID")`
  - [ ] `lineItems`: `z.array(lineItemSchema).min(1, "At least one line item required")`
  - [ ] Line item schema:
    - [ ] `description`: `z.string().min(1, "Description is required")`
    - [ ] `quantity`: `z.number().min(1, "Quantity must be at least 1")`
    - [ ] `unitPrice`: `z.number().min(0, "Unit price must be >= 0")`

- [ ] Define component props:
  - [ ] `mode: 'create' | 'edit'`
  - [ ] `initialData?: Invoice` (for edit mode)
  - [ ] `customers: Customer[]` (for customer selector)
  - [ ] `onSubmit: (data: CreateInvoiceRequest | UpdateInvoiceRequest) => void | Promise<void>`
  - [ ] `onCancel: () => void`
  - [ ] `isLoading?: boolean`

- [ ] Implement form component:
  - [ ] Use `useForm` with zodResolver
  - [ ] Use `useFieldArray` for line items management
  - [ ] Set default values from `initialData` if in edit mode
  - [ ] Create form fields:
    - [ ] Customer selector (dropdown/select, required)
    - [ ] Line items section:
      - [ ] "Add Line Item" button
      - [ ] Render `LineItemForm` for each line item
      - [ ] "Remove" button for each line item (disable if only one)
  - [ ] Auto-calculate total as line items change
  - [ ] Display total at bottom (formatted as currency)
  - [ ] Display validation errors for each field
  - [ ] Submit button with loading state
  - [ ] Cancel button
  - [ ] Handle form submission
  - [ ] Disable customer selector in edit mode (if business rule)

- [ ] Add accessibility:
  - [ ] ARIA labels on form fields
  - [ ] Proper label associations
  - [ ] Error announcements for screen readers
  - [ ] Focus management when adding/removing line items

- [ ] Style with Tailwind CSS
- [ ] Make responsive
- [ ] Export component

---

### 10. Create InvoiceList Component
- [ ] Create `src/features/invoices/components/InvoiceList.tsx`
- [ ] Import required dependencies:
  - [ ] shadcn/ui components: `Table`, `Button`, `Badge`
  - [ ] Shared components: `LoadingSpinner`, `ErrorMessage`, `Pagination`
  - [ ] `InvoiceStatusBadge` component
  - [ ] Invoice types

- [ ] Define component props:
  - [ ] `invoices: Invoice[]`
  - [ ] `isLoading: boolean`
  - [ ] `error: Error | null`
  - [ ] `pagination: { page, size, totalElements, totalPages }`
  - [ ] `onViewDetails: (invoice: Invoice) => void`
  - [ ] `onEdit: (invoice: Invoice) => void`
  - [ ] `onMarkAsSent: (invoice: Invoice) => void`
  - [ ] `onDelete: (invoice: Invoice) => void` (optional, if delete is allowed)
  - [ ] `onPageChange: (page: number) => void`
  - [ ] `onSizeChange: (size: number) => void`

- [ ] Implement component:
  - [ ] Show loading spinner when `isLoading` is true
  - [ ] Show error message when `error` exists
  - [ ] Show empty state when `invoices.length === 0` and not loading
  - [ ] Create table with columns:
    - [ ] Invoice # (truncated ID or formatted number)
    - [ ] Customer (customer name or ID)
    - [ ] Status (use `InvoiceStatusBadge`)
    - [ ] Total (formatted as currency)
    - [ ] Balance (formatted as currency)
    - [ ] Created (formatted date)
    - [ ] Actions (View Details, Edit if Draft, Mark as Sent if Draft, Delete if Draft)
  - [ ] Conditionally show actions based on invoice status:
    - [ ] Draft: Show Edit, Mark as Sent, Delete
    - [ ] Sent: Show View Details only
    - [ ] Paid: Show View Details only
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

### 11. Create InvoiceDetails Component
- [ ] Create `src/features/invoices/components/InvoiceDetails.tsx`
- [ ] Import required dependencies:
  - [ ] shadcn/ui components: `Dialog`, `Button`, `Table`, `Card`
  - [ ] `InvoiceStatusBadge` component
  - [ ] Invoice types
  - [ ] Customer types (if displaying customer info)
  - [ ] Payment types (if displaying payment history, from PRD 07)

- [ ] Define component props:
  - [ ] `open: boolean`
  - [ ] `onOpenChange: (open: boolean) => void`
  - [ ] `invoice: Invoice | null`
  - [ ] `onMarkAsSent?: (invoice: Invoice) => void`
  - [ ] `onRecordPayment?: (invoice: Invoice) => void` (from PRD 07, optional)
  - [ ] `payments?: Payment[]` (from PRD 07, optional)

- [ ] Implement component:
  - [ ] Use shadcn/ui Dialog component
  - [ ] Show invoice information:
    - [ ] Invoice # (ID or formatted number)
    - [ ] Customer info (name, email, address)
    - [ ] Status (use `InvoiceStatusBadge`)
    - [ ] Created date (formatted)
    - [ ] Updated date (formatted)
  - [ ] Show line items table:
    - [ ] Columns: Description, Quantity, Unit Price, Subtotal
    - [ ] Display all line items
    - [ ] Show total at bottom
  - [ ] Show balance information:
    - [ ] Total Amount
    - [ ] Balance (formatted)
    - [ ] Paid Amount (calculated: totalAmount - balance)
  - [ ] Show payment history (if available):
    - [ ] List of payments with date, amount
    - [ ] Show "No payments yet" if empty
  - [ ] Show actions:
    - [ ] "Mark as Sent" button (if status is DRAFT)
    - [ ] "Record Payment" button (if not PAID, from PRD 07)
    - [ ] Close button
  - [ ] Handle action callbacks
  - [ ] Close dialog on action completion

- [ ] Add accessibility:
  - [ ] Focus management (focus on close button when opened)
  - [ ] ARIA labels
  - [ ] Keyboard support (Escape to close)
  - [ ] Screen reader friendly structure

- [ ] Style with Tailwind CSS
- [ ] Make responsive
- [ ] Export component

---

## Page Component

### 12. Create InvoicesPage Component
- [ ] Create `src/features/invoices/pages/InvoicesPage.tsx`
- [ ] Import required dependencies:
  - [ ] `useState` from React
  - [ ] `useInvoiceViewModel` hook
  - [ ] `InvoiceList` component
  - [ ] `InvoiceForm` component
  - [ ] `InvoiceDetails` component
  - [ ] shadcn/ui components: `Dialog`, `Button`, `Select`
  - [ ] Invoice types
  - [ ] Customer types (for customer filter dropdown)

- [ ] Implement page component:
  - [ ] Use `useInvoiceViewModel` hook with pagination and filter state
  - [ ] Manage dialog state for create/edit form:
    - [ ] `isFormOpen: boolean`
    - [ ] `formMode: 'create' | 'edit'`
    - [ ] `editingInvoice: Invoice | null`
  - [ ] Manage details dialog state:
    - [ ] `isDetailsOpen: boolean`
    - [ ] `selectedInvoice: Invoice | null`
  - [ ] Fetch customers list (for customer filter and form):
    - [ ] Use customer API or mock data
    - [ ] Store in state: `customers: Customer[]`
  - [ ] Create header section:
    - [ ] Title: "Invoices"
    - [ ] Filter controls:
      - [ ] Status filter dropdown (All, Draft, Sent, Paid)
      - [ ] Customer filter dropdown (All Customers, then list of customers)
    - [ ] "Create Invoice" button
  - [ ] Render `InvoiceList` component with props
  - [ ] Render `InvoiceForm` in Dialog (create/edit mode)
  - [ ] Render `InvoiceDetails` in Dialog
  - [ ] Handle form submission:
    - [ ] Call `createInvoice` or `updateInvoice` from ViewModel
    - [ ] Close dialog on success
  - [ ] Handle "Mark as Sent" action:
    - [ ] Call `markAsSent` from ViewModel
    - [ ] Show confirmation if needed
  - [ ] Handle filter changes:
    - [ ] Update ViewModel filters
    - [ ] Reset pagination to page 0

- [ ] Add error boundary (optional, can use React error boundary)
- [ ] Style with Tailwind CSS
- [ ] Make responsive
- [ ] Export component

---

## Routing Integration

### 13. Add Invoices Route
- [ ] Open `src/routes/index.tsx` (or routing file)
- [ ] Import `InvoicesPage` component
- [ ] Add route:
  - [ ] Path: `/invoices`
  - [ ] Component: `<InvoicesPage />`
- [ ] Verify route is accessible
- [ ] Test navigation to `/invoices`

---

## Mock Data Setup (If Backend Not Ready)

### 14. Set Up Mock Data (Optional - Only if PRD 04 not complete)
- [ ] Decide on mock strategy:
  - [ ] Option A: MSW (Mock Service Worker)
  - [ ] Option B: Simple stub functions in API client
  - [ ] Option C: React Query mock implementation

- [ ] If using MSW:
  - [ ] Install: `npm install -D msw`
  - [ ] Create `src/mocks/handlers.ts` with invoice handlers
  - [ ] Create `src/mocks/browser.ts` for browser setup
  - [ ] Set up MSW in `src/main.tsx` (development only)
  - [ ] Create mock invoice data with line items
  - [ ] Create mock customer data (for customer filter)

- [ ] If using simple stubs:
  - [ ] Modify API client methods to return mock data
  - [ ] Add delay simulation (setTimeout) for realistic behavior
  - [ ] Create mock invoice array with various statuses
  - [ ] Create mock customer array
  - [ ] Implement mock filtering logic

- [ ] Verify mock data works:
  - [ ] List invoices shows mock data
  - [ ] Filter by status works
  - [ ] Filter by customer works
  - [ ] Create invoice adds to mock array
  - [ ] Update invoice modifies mock array
  - [ ] Mark as sent updates status
  - [ ] Line items are preserved

- [ ] Document how to switch from mocks to real API

---

## UI/UX Polish

### 15. Add Loading States
- [ ] Verify loading spinners appear during API calls
- [ ] Add skeleton loaders for table rows (optional, better UX)
- [ ] Ensure buttons show loading state during mutations
- [ ] Show loading state in form when submitting

### 16. Add Error Handling
- [ ] Verify error messages display correctly
- [ ] Test error scenarios:
  - [ ] Network errors
  - [ ] Validation errors (e.g., invalid status transition)
  - [ ] 404 errors (invoice not found)
  - [ ] 422 errors (business rule violations)
  - [ ] 500 errors
- [ ] Ensure toast notifications work (or console logs if toast not set up)
- [ ] Show specific error messages for status transition failures

### 17. Add Success Feedback
- [ ] Verify success toast notifications appear on create/update/mark as sent
- [ ] Test optimistic updates (if implemented)
- [ ] Verify UI updates immediately after successful operations
- [ ] Show confirmation message when marking invoice as sent

### 18. Form Validation
- [ ] Test real-time validation:
  - [ ] Customer required
  - [ ] At least one line item required
  - [ ] Line item description required
  - [ ] Line item quantity > 0
  - [ ] Line item unit price >= 0
- [ ] Verify error messages are clear and helpful
- [ ] Test form submission prevention when invalid
- [ ] Validate total calculation matches backend logic

### 19. Line Item Management
- [ ] Test adding line items:
  - [ ] Add button adds new line item
  - [ ] New line item has empty fields
  - [ ] Focus moves to new line item
- [ ] Test removing line items:
  - [ ] Remove button removes line item
  - [ ] Cannot remove if only one line item
  - [ ] Total recalculates after removal
- [ ] Test line item calculations:
  - [ ] Subtotal calculates correctly (quantity * unitPrice)
  - [ ] Total updates as line items change
  - [ ] Calculations handle decimal values correctly
  - [ ] Currency formatting is correct

### 20. Filtering Functionality
- [ ] Test status filtering:
  - [ ] "All" shows all invoices
  - [ ] "Draft" shows only draft invoices
  - [ ] "Sent" shows only sent invoices
  - [ ] "Paid" shows only paid invoices
  - [ ] Pagination resets when filter changes
- [ ] Test customer filtering:
  - [ ] "All Customers" shows all invoices
  - [ ] Selecting customer shows only their invoices
  - [ ] Pagination resets when filter changes
- [ ] Test combined filtering:
  - [ ] Status + Customer filter work together
  - [ ] Results are correct
- [ ] Test filter persistence (optional):
  - [ ] Filters persist in URL query params (if implemented)
  - [ ] Filters restore on page reload

### 21. Status Management
- [ ] Test status transitions:
  - [ ] Draft → Sent (via "Mark as Sent" button)
  - [ ] Verify validation (only Draft can be marked as Sent)
  - [ ] Verify error if trying to mark non-Draft as Sent
- [ ] Test status-based UI:
  - [ ] Edit button only shows for Draft invoices
  - [ ] "Mark as Sent" only shows for Draft invoices
  - [ ] Delete button only shows for Draft invoices (if implemented)
  - [ ] Status badge colors are correct

### 22. Responsive Design
- [ ] Test on mobile viewport (< 768px):
  - [ ] Table is scrollable or switches to card view
  - [ ] Form is usable
  - [ ] Line items are manageable
  - [ ] Filters are accessible
  - [ ] Buttons are accessible
- [ ] Test on tablet viewport (768px - 1024px)
- [ ] Test on desktop viewport (> 1024px)
- [ ] Verify all breakpoints work correctly

### 23. Accessibility
- [ ] Test keyboard navigation:
  - [ ] Tab through form fields
  - [ ] Tab through line items
  - [ ] Enter to submit form
  - [ ] Escape to close dialogs
  - [ ] Arrow keys in dropdowns
- [ ] Test with screen reader (optional, but recommended)
- [ ] Verify ARIA labels are present
- [ ] Test focus management in modals
- [ ] Verify table structure is accessible

---

## Integration Testing

### 24. Manual Testing Checklist
- [ ] **Create Invoice**:
  - [ ] Click "Create Invoice" button
  - [ ] Select customer
  - [ ] Add line items (at least one)
  - [ ] Verify total calculates correctly
  - [ ] Submit form
  - [ ] Verify invoice appears in list with DRAFT status
  - [ ] Verify success message

- [ ] **Read Invoices**:
  - [ ] Verify invoices load on page load
  - [ ] Verify pagination works
  - [ ] Verify empty state shows when no invoices
  - [ ] Verify invoice details are correct

- [ ] **Update Invoice**:
  - [ ] Click "Edit" button on a Draft invoice
  - [ ] Verify form is pre-filled
  - [ ] Modify line items
  - [ ] Add/remove line items
  - [ ] Verify total recalculates
  - [ ] Submit form
  - [ ] Verify invoice is updated in list
  - [ ] Verify success message
  - [ ] Try to edit a Sent invoice (should fail or be disabled)

- [ ] **Mark Invoice as Sent**:
  - [ ] Click "Mark as Sent" on a Draft invoice
  - [ ] Verify status changes to SENT
  - [ ] Verify Edit button disappears
  - [ ] Verify success message
  - [ ] Try to mark a Sent invoice as Sent (should fail)

- [ ] **View Invoice Details**:
  - [ ] Click "View Details" on an invoice
  - [ ] Verify all information displays correctly
  - [ ] Verify line items are shown
  - [ ] Verify total and balance are correct
  - [ ] Verify payment history (if available from PRD 07)

- [ ] **Filter by Status**:
  - [ ] Select "Draft" filter
  - [ ] Verify only Draft invoices show
  - [ ] Select "Sent" filter
  - [ ] Verify only Sent invoices show
  - [ ] Select "All" filter
  - [ ] Verify all invoices show

- [ ] **Filter by Customer**:
  - [ ] Select a customer from dropdown
  - [ ] Verify only that customer's invoices show
  - [ ] Select "All Customers"
  - [ ] Verify all invoices show

- [ ] **Combined Filtering**:
  - [ ] Select status filter and customer filter
  - [ ] Verify results match both filters
  - [ ] Clear filters
  - [ ] Verify all invoices show

- [ ] **Form Validation**:
  - [ ] Try to submit empty form
  - [ ] Try to submit without customer
  - [ ] Try to submit without line items
  - [ ] Try to submit with invalid line item (quantity = 0)
  - [ ] Try to submit with invalid line item (unitPrice < 0)
  - [ ] Verify validation errors appear

- [ ] **Line Item Management**:
  - [ ] Add multiple line items
  - [ ] Remove line items
  - [ ] Verify subtotals calculate correctly
  - [ ] Verify total calculates correctly
  - [ ] Try to remove last line item (should be prevented)

- [ ] **Error Handling**:
  - [ ] Disconnect network (or stop backend)
  - [ ] Try to create invoice
  - [ ] Verify error message appears
  - [ ] Reconnect network
  - [ ] Verify recovery works

- [ ] **Pagination**:
  - [ ] Create more than 20 invoices (or adjust size)
  - [ ] Verify pagination controls appear
  - [ ] Click next page
  - [ ] Verify correct invoices appear
  - [ ] Change page size
  - [ ] Verify list updates
  - [ ] Verify filters persist across pages

---

## Final Verification

### 25. MVVM Pattern Verification
- [ ] Verify ViewModel (`useInvoiceViewModel`) contains business logic
- [ ] Verify components (View) are mostly presentational
- [ ] Verify components use ViewModel, not individual query/mutation hooks
- [ ] Verify separation of concerns is clear
- [ ] Verify filter logic is in ViewModel, not components
- [ ] Verify total calculation logic is in ViewModel or components (not in API layer)

### 26. Code Quality
- [ ] Run ESLint and fix any errors
- [ ] Run Prettier and format code
- [ ] Verify TypeScript compiles without errors
- [ ] Check for any console.log statements (remove or replace with proper logging)
- [ ] Verify no unused imports
- [ ] Verify proper error handling throughout

### 27. Documentation
- [ ] Add JSDoc comments to hooks (optional, but recommended)
- [ ] Add comments for complex logic (line item calculations, filter logic)
- [ ] Verify code is self-documenting
- [ ] Document any workarounds or known issues

---

## Success Criteria Checklist

- [ ] Can create, read, update invoices
- [ ] Can add/remove line items dynamically
- [ ] Total calculation is correct (matches backend logic)
- [ ] Status filtering works (All, Draft, Sent, Paid)
- [ ] Customer filtering works
- [ ] Combined filtering (status + customer) works
- [ ] Can mark invoice as Sent (with validation)
- [ ] Invoice details view shows all information
- [ ] Line items display correctly with subtotals
- [ ] Optimistic updates provide instant feedback (if implemented)
- [ ] Error handling shows user-friendly messages
- [ ] Loading states prevent user confusion
- [ ] Form validation prevents invalid submissions
- [ ] Responsive design works on all screen sizes
- [ ] MVVM pattern is clear (ViewModel separate from View)
- [ ] All manual tests pass
- [ ] Code quality is good (no linting errors, TypeScript compiles)

---

## Notes

- **Mock Data**: If backend (PRD 04) is not ready, use mock data to enable parallel development
- **Type Generation**: Prefer using auto-generated types from OpenAPI spec if available
- **MVVM Pattern**: Strictly follow MVVM - ViewModel in hooks, View in components
- **Reuse Components**: Use shared components from foundation (Pagination, LoadingSpinner, ErrorMessage)
- **Toast Notifications**: If toast library not set up, use console.log for now (can be improved in PRD 08)
- **Line Items**: Use `useFieldArray` from React Hook Form for dynamic line items management
- **Total Calculation**: Ensure client-side calculation matches backend logic for validation
- **Filter Complexity**: Manage filter state carefully - reset pagination when filters change
- **Customer Dependency**: Invoice form needs customer list - use mock data if PRD 02 not complete
- **Payment History**: Invoice details may show payment history from PRD 07 (optional integration)
- **Testing**: Manual testing is sufficient for this PRD. Unit tests can be added later if needed.

---

## Next Steps After Completion

1. If using mock data, switch to real API when PRD 04 is complete
2. Test integration with real backend
3. Verify all features work end-to-end
4. Test with real customer data (when PRD 02 is complete)
5. Test payment integration (when PRD 07 is complete)
6. Move to next feature PRD (PRD 07: Payment Frontend or continue with other features)

