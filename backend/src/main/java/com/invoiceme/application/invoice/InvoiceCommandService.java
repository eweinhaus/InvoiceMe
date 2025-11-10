package com.invoiceme.application.invoice;

import com.invoiceme.application.invoice.dto.CreateInvoiceRequest;
import com.invoiceme.application.invoice.dto.InvoiceResponse;
import com.invoiceme.application.invoice.dto.UpdateInvoiceRequest;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.LineItem;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.email.EmailException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceCommandService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceMapper invoiceMapper;
    private final EmailService emailService;
    private final InvoicePdfService invoicePdfService;

    /**
     * Creates a new invoice in DRAFT status.
     * 
     * @param request Create invoice request with customer ID and line items
     * @return InvoiceResponse with created invoice data
     * @throws EntityNotFoundException if customer not found
     */
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        // Find customer
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + request.customerId()));

        // Create invoice entity
        Invoice invoice = new Invoice(customer);

        // Add line items using domain method
        for (com.invoiceme.application.invoice.dto.LineItemRequest lineItemRequest : request.lineItems()) {
            invoice.addLineItem(
                    lineItemRequest.description(),
                    lineItemRequest.quantity(),
                    lineItemRequest.unitPrice()
            );
        }

        // Ensure total is calculated (addLineItem already calls this, but explicit for clarity)
        invoice.calculateTotal();
        invoice.calculateBalance(); // Also calculate balance

        // Save invoice
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Ensure total is set on saved entity (in case of any persistence issues)
        if (savedInvoice.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            savedInvoice.calculateTotal();
            savedInvoice = invoiceRepository.save(savedInvoice);
        }

        // Map to response
        return invoiceMapper.toResponse(savedInvoice);
    }

    /**
     * Updates an invoice (only if in DRAFT status).
     * 
     * @param id Invoice ID
     * @param request Update invoice request with new line items
     * @return InvoiceResponse with updated invoice data
     * @throws EntityNotFoundException if invoice not found
     * @throws IllegalStateException if invoice cannot be edited (not in DRAFT status)
     */
    public InvoiceResponse updateInvoice(UUID id, UpdateInvoiceRequest request) {
        // Find invoice
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + id));

        // Validate invoice can be edited
        if (!invoice.canBeEdited()) {
            throw new IllegalStateException(
                    "Invoice cannot be edited. Only invoices in DRAFT status can be edited. " +
                    "Current status: " + invoice.getStatus()
            );
        }

        // Map line items from request to domain objects
        List<LineItem> lineItems = request.lineItems().stream()
                .map(lineItemRequest -> new LineItem(
                        lineItemRequest.description(),
                        lineItemRequest.quantity(),
                        lineItemRequest.unitPrice()
                ))
                .collect(Collectors.toList());

        // Update line items using domain method
        invoice.updateLineItems(lineItems);

        // Save updated invoice
        Invoice updatedInvoice = invoiceRepository.save(invoice);

        // Map to response
        return invoiceMapper.toResponse(updatedInvoice);
    }

    /**
     * Sends invoice via email to customer and marks it as SENT.
     * Generates PDF, sends email with PDF attachment, and only marks as SENT if email sends successfully.
     * 
     * @param id Invoice ID
     * @return InvoiceResponse with updated invoice data
     * @throws EntityNotFoundException if invoice not found
     * @throws IllegalStateException if invoice cannot be marked as SENT
     * @throws EmailException if email sending fails (invoice will not be marked as SENT)
     */
    public InvoiceResponse sendInvoiceViaEmail(UUID id) {
        // Find invoice with customer eagerly loaded (needed for email and PDF generation)
        Invoice invoice = invoiceRepository.findByIdWithCustomer(id)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + id));

        // Validate invoice can be marked as sent
        if (!invoice.canBeMarkedAsSent()) {
            throw new IllegalStateException(
                    "Invoice cannot be marked as SENT. " +
                    "Invoice must be in DRAFT status, have at least one line item, and total amount must be greater than 0."
            );
        }

        // Generate PDF
        byte[] pdfBytes = invoicePdfService.generatePdf(id);

        // Send email with PDF attachment
        // If email fails, exception will be thrown and invoice will not be marked as SENT
        emailService.sendInvoiceEmail(invoice, pdfBytes);

        // Only mark as SENT if email was sent successfully
        invoice.markAsSent();

        // Save updated invoice
        Invoice updatedInvoice = invoiceRepository.save(invoice);

        // Map to response
        return invoiceMapper.toResponse(updatedInvoice);
    }
}

