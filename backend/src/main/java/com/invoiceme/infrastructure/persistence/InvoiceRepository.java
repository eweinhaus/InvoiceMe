package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    /**
     * Finds all invoices with pagination.
     * Eagerly fetches customer to avoid N+1 queries and ensure customerName is available.
     * 
     * @param pageable Pagination parameters
     * @return Page of invoices with customer loaded
     */
    @EntityGraph(attributePaths = {"customer"})
    @Override
    Page<Invoice> findAll(Pageable pageable);

    /**
     * Finds invoices by status with pagination.
     * Eagerly fetches customer to avoid N+1 queries and ensure customerName is available.
     * 
     * @param status Invoice status
     * @param pageable Pagination parameters
     * @return Page of invoices with the specified status
     */
    @EntityGraph(attributePaths = {"customer"})
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    /**
     * Finds invoices by customer ID with pagination.
     * Uses customer.id property path for the @ManyToOne relationship.
     * Eagerly fetches customer to avoid N+1 queries and ensure customerName is available.
     * 
     * @param customerId Customer ID
     * @param pageable Pagination parameters
     * @return Page of invoices for the specified customer
     */
    @EntityGraph(attributePaths = {"customer"})
    Page<Invoice> findByCustomer_Id(UUID customerId, Pageable pageable);

    /**
     * Finds an invoice by ID with customer eagerly fetched.
     * Used for PDF generation where customer details are needed.
     * 
     * @param id Invoice ID
     * @return Invoice with customer loaded
     */
    @EntityGraph(attributePaths = {"customer"})
    @Query("SELECT i FROM Invoice i WHERE i.id = :id")
    Optional<Invoice> findByIdWithCustomer(@Param("id") UUID id);
}

