# PRD 09: Email Sending Feature - Task List

## Overview
**Priority**: Feature Enhancement  
**Dependencies**: PRD 04 (Invoice Backend), PRD 05 (Invoice Frontend)  
**Enables**: Professional invoice delivery via email

This task list implements email sending functionality to replace "Mark as Sent" with "Send via Email". When users click the button, the system will generate the invoice PDF, send it via email to the customer, and mark the invoice as SENT.

## High-Level Goals

1. Replace "Mark as Sent" button with "Send via Email" button
2. Generate invoice PDF (reuse existing `InvoicePdfService`)
3. Send email to customer with PDF attachment
4. Mark invoice as SENT after successful email send
5. Provide clear user feedback (success/error messages)
6. Handle email failures gracefully

## Success Criteria

- ✅ Button text changed from "Mark as Sent" to "Send via Email"
- ✅ Email sent to customer email address
- ✅ PDF attached to email
- ✅ Invoice status transitions to SENT after successful send
- ✅ User sees clear success/error feedback
- ✅ Email failures are handled gracefully
- ✅ SMTP configuration via environment variables
- ✅ Email template is professional and includes invoice details
- ✅ Integration tests for email sending

## Potential Pitfalls

1. **SMTP Configuration**: Need to set up SMTP provider (Gmail, SendGrid, AWS SES, etc.)
2. **Email Sending Strategy**: Synchronous vs asynchronous (starting with synchronous for simplicity)
3. **Error Handling**: Decide if invoice should be marked as SENT if email fails (recommend: only if email succeeds)
4. **Email Template**: Need professional HTML template with invoice details
5. **Testing**: Need test SMTP server or mock for development/testing
6. **Environment Variables**: SMTP credentials should be in environment variables
7. **PDF Generation**: Ensure customer data (including email) is loaded when generating PDF

## Task Breakdown

### Phase 1: Backend Email Infrastructure

#### Task 1.1: Add Spring Mail Dependency
- [ ] Add `spring-boot-starter-mail` dependency to `backend/pom.xml`
- [ ] Verify dependency is resolved correctly
- [ ] **File**: `backend/pom.xml`
- **Estimated Time**: 5 minutes

#### Task 1.2: Configure Email Settings
- [ ] Add email configuration to `backend/src/main/resources/application.yml`
  - SMTP host, port, username, password
  - Use environment variables: `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`
  - Configure TLS/SSL settings
- [ ] Add email configuration to `backend/src/main/resources/application-prod.yml`
  - Same structure, uses environment variables
- [ ] Add email configuration to `backend/src/main/resources/application-test.yml`
  - Use test SMTP server or mock configuration
- [ ] **Files**: 
  - `backend/src/main/resources/application.yml`
  - `backend/src/main/resources/application-prod.yml`
  - `backend/src/main/resources/application-test.yml`
- **Estimated Time**: 15 minutes

#### Task 1.3: Create Email Service
- [ ] Create `EmailService` interface in `application/invoice/` package
  - Method: `sendInvoiceEmail(Invoice invoice, byte[] pdfBytes) throws EmailException`
- [ ] Create `EmailServiceImpl` in `infrastructure/email/` package
  - Inject `JavaMailSender` from Spring
  - Generate email subject: "Invoice #{invoiceNumber} from InvoiceMe"
  - Generate email body (HTML template)
  - Attach PDF as attachment
  - Send email to customer email address
  - Handle exceptions and wrap in custom `EmailException`
- [ ] Create `EmailException` custom exception class
- [ ] **Files**:
  - `backend/src/main/java/com/invoiceme/application/invoice/EmailService.java`
  - `backend/src/main/java/com/invoiceme/infrastructure/email/EmailServiceImpl.java`
  - `backend/src/main/java/com/invoiceme/infrastructure/email/EmailException.java`
- **Estimated Time**: 1 hour

#### Task 1.4: Create Email Template
- [ ] Create HTML email template in `backend/src/main/resources/templates/`
  - Professional design with invoice details
  - Include: Invoice number, customer name, total amount, due date (if applicable)
  - Include: Line items summary (optional, full details in PDF)
  - Include: Payment instructions or link (optional)
  - Responsive design for email clients
- [ ] Create template service or utility to populate template with invoice data
- [ ] **Files**:
  - `backend/src/main/resources/templates/invoice-email.html`
  - `backend/src/main/java/com/invoiceme/infrastructure/email/EmailTemplateService.java` (optional)
- **Estimated Time**: 1 hour

#### Task 1.5: Update InvoiceCommandService
- [ ] Modify `markAsSent()` method to `sendInvoiceViaEmail(UUID id)`
  - Rename method for clarity
  - Inject `EmailService` and `InvoicePdfService`
  - Load invoice with customer (eager loading)
  - Validate invoice can be marked as sent (existing validation)
  - Generate PDF using `InvoicePdfService`
  - Send email using `EmailService`
  - Only mark as SENT if email sends successfully
  - If email fails, throw exception (don't mark as sent)
  - Return `InvoiceResponse` with updated invoice
- [ ] Update method documentation
- [ ] **File**: `backend/src/main/java/com/invoiceme/application/invoice/InvoiceCommandService.java`
- **Estimated Time**: 30 minutes

#### Task 1.6: Update InvoiceController
- [ ] Update endpoint `POST /api/invoices/{id}/send` 
  - Update OpenAPI documentation to reflect email sending
  - Update operation summary: "Send invoice via email"
  - Update description: "Generates PDF, sends email to customer, and marks invoice as SENT"
  - Add response codes: 200 (success), 500 (email failure)
- [ ] **File**: `backend/src/main/java/com/invoiceme/presentation/rest/InvoiceController.java`
- **Estimated Time**: 10 minutes

#### Task 1.7: Update InvoiceRepository (if needed)
- [ ] Verify `findByIdWithCustomer()` method exists (for eager loading)
  - If not, add method to load invoice with customer relationship
  - Use `@EntityGraph` or `JOIN FETCH` for eager loading
- [ ] **File**: `backend/src/main/java/com/invoiceme/infrastructure/persistence/InvoiceRepository.java`
- **Estimated Time**: 15 minutes (if needed)

### Phase 2: Frontend Updates

#### Task 2.1: Update API Client Method
- [ ] Update `markInvoiceAsSent()` method in `frontend/src/lib/api/invoices.ts`
  - Rename to `sendInvoiceViaEmail(id: string)`
  - Update endpoint path if needed (should remain `/invoices/{id}/send`)
  - Update method documentation
- [ ] **File**: `frontend/src/lib/api/invoices.ts`
- **Estimated Time**: 5 minutes

#### Task 2.2: Update React Query Hook
- [ ] Update `useMarkInvoiceAsSent()` hook in `frontend/src/features/invoices/hooks/useInvoiceMutations.ts`
  - Rename to `useSendInvoiceViaEmail()`
  - Update mutation function to call renamed API method
  - Update success/error messages
  - Keep query invalidation logic
- [ ] **File**: `frontend/src/features/invoices/hooks/useInvoiceMutations.ts`
- **Estimated Time**: 10 minutes

#### Task 2.3: Update InvoiceList Component
- [ ] Update "Mark as Sent" button to "Send via Email"
  - Change button text
  - Update aria-label
  - Update onClick handler to use renamed hook
- [ ] **File**: `frontend/src/features/invoices/components/InvoiceList.tsx`
- **Estimated Time**: 5 minutes

#### Task 2.4: Update InvoiceDetails Component
- [ ] Update "Mark as Sent" button to "Send via Email"
  - Change button text
  - Update icon (use Mail icon instead of Send icon)
  - Update onClick handler to use renamed hook
  - Update aria-label
- [ ] **File**: `frontend/src/features/invoices/components/InvoiceDetails.tsx`
- **Estimated Time**: 10 minutes

#### Task 2.5: Update InvoicesPage Component
- [ ] Update handler function name from `onMarkAsSent` to `onSendViaEmail`
  - Update prop name
  - Update handler implementation
- [ ] **File**: `frontend/src/features/invoices/pages/InvoicesPage.tsx`
- **Estimated Time**: 5 minutes

#### Task 2.6: Update useInvoiceViewModel Hook
- [ ] Update ViewModel hook to use renamed mutation hook
  - Update function name from `markAsSent` to `sendViaEmail`
  - Update implementation to call renamed hook
- [ ] **File**: `frontend/src/features/invoices/hooks/useInvoiceViewModel.ts`
- **Estimated Time**: 5 minutes

### Phase 3: Testing

#### Task 3.1: Create Email Service Unit Tests
- [ ] Create unit tests for `EmailServiceImpl`
  - Mock `JavaMailSender`
  - Test email generation (subject, body, attachment)
  - Test exception handling
- [ ] **File**: `backend/src/test/java/com/invoiceme/infrastructure/email/EmailServiceImplTest.java`
- **Estimated Time**: 30 minutes

#### Task 3.2: Update Invoice Integration Tests
- [ ] Update `InvoiceIntegrationTest` to test email sending
  - Mock email service or use test SMTP server
  - Test successful email send
  - Test email failure handling
  - Verify invoice is only marked as SENT if email succeeds
- [ ] **File**: `backend/src/test/java/com/invoiceme/InvoiceIntegrationTest.java`
- **Estimated Time**: 45 minutes

#### Task 3.3: Manual Testing
- [ ] Test email sending in development environment
  - Configure test SMTP server (e.g., MailHog, Mailtrap)
  - Verify email is sent with PDF attachment
  - Verify invoice status changes to SENT
  - Test error handling (invalid email, SMTP failure)
- [ ] Test in production environment (if applicable)
  - Configure production SMTP provider
  - Verify email delivery
- **Estimated Time**: 30 minutes

### Phase 4: Documentation & Configuration

#### Task 4.1: Update Environment Variable Documentation
- [ ] Document required SMTP environment variables
  - `SMTP_HOST`: SMTP server hostname
  - `SMTP_PORT`: SMTP server port (typically 587 for TLS)
  - `SMTP_USERNAME`: SMTP username
  - `SMTP_PASSWORD`: SMTP password
  - Optional: `SMTP_FROM_EMAIL`: From email address (defaults to username)
- [ ] Update `.env.example` file (if exists)
- [ ] Update `backend/README.md` with email configuration instructions
- [ ] **Files**:
  - `backend/README.md`
  - `.env.example` (if exists)
- **Estimated Time**: 15 minutes

#### Task 4.2: Update Render Deployment Configuration
- [ ] Add SMTP environment variables to Render backend service
  - Configure via Render dashboard
  - Document in deployment notes
- [ ] **Location**: Render dashboard (manual configuration)
- **Estimated Time**: 10 minutes

#### Task 4.3: Update API Documentation
- [ ] Update OpenAPI/Swagger documentation
  - Update endpoint description
  - Update response examples
  - Document email-related error responses
- [ ] **File**: Auto-generated from controller annotations
- **Estimated Time**: 5 minutes

## Implementation Notes

### Email Sending Strategy
- **Initial Implementation**: Synchronous email sending
  - Simpler to implement
  - Easier error handling
  - User waits for email to send (acceptable for this use case)
- **Future Enhancement**: Consider asynchronous email sending
  - Use message queue (RabbitMQ, SQS)
  - Background job processing
  - Better UX for high-volume scenarios

### Error Handling Strategy
- **Email Success**: Mark invoice as SENT
- **Email Failure**: 
  - Log error details
  - Throw exception to controller
  - Return 500 error to frontend
  - Do NOT mark invoice as SENT
  - User can retry sending

### SMTP Provider Recommendations
- **Development/Testing**: 
  - MailHog (local SMTP testing)
  - Mailtrap (cloud SMTP testing)
- **Production**:
  - SendGrid (recommended, easy setup)
  - AWS SES (cost-effective, scalable)
  - Gmail SMTP (simple, but requires app password)
  - Mailgun (developer-friendly)

### Email Template Design
- Use HTML email template
- Include minimal invoice details (full details in PDF)
- Professional branding
- Responsive design for email clients
- Plain text fallback (optional)

## Dependencies

- ✅ PRD 04 (Invoice Backend) - Required
- ✅ PRD 05 (Invoice Frontend) - Required
- ✅ InvoicePdfService - Required (already exists)
- ✅ Customer email field - Required (already exists)

## Timeline Estimate

**Total Estimated Time**: 4-5 hours

- **Phase 1 (Backend)**: 2.5 hours
- **Phase 2 (Frontend)**: 45 minutes
- **Phase 3 (Testing)**: 1.5 hours
- **Phase 4 (Documentation)**: 30 minutes

## Success Validation

- [ ] Email is sent successfully when clicking "Send via Email"
- [ ] PDF is attached to email
- [ ] Invoice status changes to SENT after successful email
- [ ] Error messages are clear and helpful
- [ ] Integration tests pass
- [ ] Manual testing confirms email delivery
- [ ] Documentation is updated

## Future Enhancements (Out of Scope)

- Asynchronous email sending with queue
- Email templates customization
- Email delivery tracking
- Retry mechanism for failed emails
- Email preview before sending
- Multiple recipient support (CC, BCC)
- Email scheduling

