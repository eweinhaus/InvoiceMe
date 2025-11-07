package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.payment.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}

