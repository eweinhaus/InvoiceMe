package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.payment.Payment;
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
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /**
     * Finds payments by invoice ID with pagination.
     * Uses invoice.id property path for the @ManyToOne relationship.
     * 
     * @param invoiceId Invoice ID
     * @param pageable Pagination parameters
     * @return Page of payments for the specified invoice
     */
    Page<Payment> findByInvoice_Id(UUID invoiceId, Pageable pageable);

    /**
     * Finds a payment by ID with invoice and customer eagerly fetched.
     * 
     * @param id Payment ID
     * @return Payment with invoice and customer loaded
     */
    @EntityGraph(attributePaths = {"invoice", "invoice.customer"})
    @Query("SELECT p FROM Payment p WHERE p.id = :id")
    Optional<Payment> findByIdWithInvoiceAndCustomer(@Param("id") UUID id);

    /**
     * Finds all payments with invoice and customer eagerly fetched.
     * 
     * @param pageable Pagination parameters
     * @return Page of payments with invoice and customer loaded
     */
    @EntityGraph(attributePaths = {"invoice", "invoice.customer"})
    @Query("SELECT p FROM Payment p")
    Page<Payment> findAllWithInvoiceAndCustomer(Pageable pageable);

    /**
     * Finds payments by invoice ID with invoice and customer eagerly fetched.
     * 
     * @param invoiceId Invoice ID
     * @param pageable Pagination parameters
     * @return Page of payments with invoice and customer loaded
     */
    @EntityGraph(attributePaths = {"invoice", "invoice.customer"})
    @Query("SELECT p FROM Payment p WHERE p.invoice.id = :invoiceId")
    Page<Payment> findByInvoiceIdWithInvoiceAndCustomer(@Param("invoiceId") UUID invoiceId, Pageable pageable);
}

