# Invoice Frontend - Manual Testing Checklist

## Prerequisites
- Frontend dev server running: `npm run dev` (http://localhost:5173)
- Backend server running (optional): `mvn spring-boot:run` (http://localhost:8080)
- Browser: Chrome/Firefox/Safari

## Test Scenarios

### 1. Page Load
- [ ] Navigate to http://localhost:5173/invoices
- [ ] Page loads without errors
- [ ] Header shows "Invoices" title
- [ ] "Create Invoice" button is visible
- [ ] Filter dropdowns (Status, Customer) are visible
- [ ] Invoice list area is visible (may be empty)

### 2. Create Invoice
- [ ] Click "Create Invoice" button
- [ ] Dialog opens with invoice form
- [ ] Customer dropdown shows available customers (or "No customers available")
- [ ] "Add Line Item" button is visible
- [ ] At least one line item row is present
- [ ] Fill in line item:
  - [ ] Description field accepts text
  - [ ] Quantity field accepts numbers (min: 1)
  - [ ] Unit Price field accepts numbers (min: 0)
  - [ ] Subtotal calculates automatically (quantity × unit price)
- [ ] Add another line item:
  - [ ] Click "Add Line Item"
  - [ ] New line item row appears
  - [ ] Total updates automatically
- [ ] Remove line item:
  - [ ] Click "Remove" on a line item
  - [ ] Line item is removed
  - [ ] Total updates
  - [ ] Cannot remove if only one line item remains
- [ ] Form validation:
  - [ ] Try to submit without customer → Error appears
  - [ ] Try to submit with empty description → Error appears
  - [ ] Try to submit with quantity = 0 → Error appears
  - [ ] Try to submit with negative unit price → Error appears
- [ ] Submit valid form:
  - [ ] Fill all required fields
  - [ ] Click "Create Invoice"
  - [ ] Form submits (may show error if backend not running)
  - [ ] Dialog closes on success
  - [ ] Invoice appears in list (if backend working)

### 3. View Invoice List
- [ ] Invoices are displayed in a table
- [ ] Columns visible: Invoice #, Customer, Status, Total, Balance, Created, Actions
- [ ] Status badges show correct colors:
  - [ ] DRAFT = gray
  - [ ] SENT = blue
  - [ ] PAID = green
- [ ] Currency amounts are formatted correctly ($X.XX)
- [ ] Dates are formatted correctly
- [ ] Actions column shows appropriate buttons based on status

### 4. Filter by Status
- [ ] Select "Draft" from status filter
- [ ] Only Draft invoices are shown (or empty if none)
- [ ] Select "Sent" from status filter
- [ ] Only Sent invoices are shown
- [ ] Select "All" from status filter
- [ ] All invoices are shown

### 5. Filter by Customer
- [ ] Select a customer from customer filter
- [ ] Only that customer's invoices are shown
- [ ] Select "All Customers"
- [ ] All invoices are shown

### 6. Combined Filtering
- [ ] Select both status (e.g., "Draft") and customer
- [ ] Results match both filters
- [ ] Clear both filters
- [ ] All invoices are shown

### 7. View Invoice Details
- [ ] Click "View" button on an invoice
- [ ] Dialog opens with invoice details
- [ ] Invoice information section shows:
  - [ ] Invoice #
  - [ ] Status badge
  - [ ] Customer
  - [ ] Created date
- [ ] Line items table shows all line items with:
  - [ ] Description
  - [ ] Quantity
  - [ ] Unit Price
  - [ ] Subtotal
- [ ] Total is shown at bottom of line items
- [ ] Balance information shows:
  - [ ] Total Amount
  - [ ] Paid Amount
  - [ ] Balance
- [ ] Close button works

### 8. Edit Invoice (Draft only)
- [ ] Click "Edit" on a Draft invoice
- [ ] Dialog opens with pre-filled form
- [ ] Customer selector is disabled (in edit mode)
- [ ] Line items are pre-filled
- [ ] Modify line items:
  - [ ] Change description
  - [ ] Change quantity
  - [ ] Change unit price
  - [ ] Total recalculates
- [ ] Add/remove line items
- [ ] Submit form
- [ ] Invoice updates in list
- [ ] Try to edit a Sent invoice → Edit button should not be visible

### 9. Mark as Sent
- [ ] Click "Mark as Sent" on a Draft invoice
- [ ] Status changes to SENT (if backend working)
- [ ] Edit button disappears
- [ ] "Mark as Sent" button disappears
- [ ] Try to mark a Sent invoice as Sent → Should fail or button not visible

### 10. Pagination
- [ ] If more than 20 invoices exist:
  - [ ] Pagination controls appear
  - [ ] Click "Next" → Next page loads
  - [ ] Click "Previous" → Previous page loads
  - [ ] Page number updates correctly

### 11. Error Handling
- [ ] Stop backend server (if running)
- [ ] Try to create invoice
- [ ] Error message appears (or console shows error)
- [ ] UI remains functional
- [ ] Restart backend
- [ ] Operations work again

### 12. Loading States
- [ ] While fetching invoices, loading spinner appears
- [ ] While submitting form, button shows "Saving..." or loading state
- [ ] No flickering or layout shifts

### 13. Responsive Design
- [ ] Resize browser to mobile width (< 768px)
- [ ] Table is scrollable or switches to card view
- [ ] Form is usable on mobile
- [ ] Filters stack vertically
- [ ] Buttons are accessible

### 14. Accessibility
- [ ] Tab through form fields → Focus moves correctly
- [ ] Enter key submits form
- [ ] Escape key closes dialogs
- [ ] Screen reader can navigate (if available)
- [ ] ARIA labels are present

## Expected Issues (If Backend Not Running)
- API calls will fail
- Error messages may appear in console
- Invoices won't load
- Create/Update operations will fail

## Success Criteria
✅ All UI components render correctly
✅ Form validation works
✅ Calculations are correct
✅ Filtering works
✅ Status transitions work (if backend available)
✅ No console errors (except expected API errors if backend down)
✅ Responsive design works
✅ Accessibility features work

