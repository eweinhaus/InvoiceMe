package com.invoiceme.application.invoice;

import com.invoiceme.application.invoice.dto.InvoiceResponse;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceQueryService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;

    /**
     * Gets an invoice by ID.
     * Calculates balance if payments are available.
     * 
     * @param id Invoice ID
     * @return InvoiceResponse with invoice data
     * @throws EntityNotFoundException if invoice not found
     */
    public InvoiceResponse getById(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + id));

        // Calculate balance (for now, balance equals totalAmount - will be enhanced when Payment entity is available)
        invoice.calculateBalance();

        return invoiceMapper.toResponse(invoice);
    }

    /**
     * Gets all invoices with pagination.
     * 
     * @param pageable Pagination parameters
     * @return Page of InvoiceResponse
     */
    public Page<InvoiceResponse> getAll(Pageable pageable) {
        return invoiceRepository.findAll(pageable)
                .map(invoice -> {
                    invoice.calculateBalance();
                    return invoiceMapper.toResponse(invoice);
                });
    }

    /**
     * Gets invoices by status with pagination.
     * 
     * @param status Invoice status
     * @param pageable Pagination parameters
     * @return Page of InvoiceResponse with specified status
     */
    public Page<InvoiceResponse> getByStatus(InvoiceStatus status, Pageable pageable) {
        return invoiceRepository.findByStatus(status, pageable)
                .map(invoice -> {
                    invoice.calculateBalance();
                    return invoiceMapper.toResponse(invoice);
                });
    }

    /**
     * Gets invoices by customer ID with pagination.
     * 
     * @param customerId Customer ID
     * @param pageable Pagination parameters
     * @return Page of InvoiceResponse for specified customer
     */
    public Page<InvoiceResponse> getByCustomerId(UUID customerId, Pageable pageable) {
        return invoiceRepository.findByCustomer_Id(customerId, pageable)
                .map(invoice -> {
                    invoice.calculateBalance();
                    return invoiceMapper.toResponse(invoice);
                });
    }
}

