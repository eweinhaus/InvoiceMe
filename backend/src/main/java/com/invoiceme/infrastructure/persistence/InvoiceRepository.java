package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    /**
     * Finds invoices by status with pagination.
     * 
     * @param status Invoice status
     * @param pageable Pagination parameters
     * @return Page of invoices with the specified status
     */
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    /**
     * Finds invoices by customer ID with pagination.
     * Uses customer.id property path for the @ManyToOne relationship.
     * 
     * @param customerId Customer ID
     * @param pageable Pagination parameters
     * @return Page of invoices for the specified customer
     */
    Page<Invoice> findByCustomer_Id(UUID customerId, Pageable pageable);
}

