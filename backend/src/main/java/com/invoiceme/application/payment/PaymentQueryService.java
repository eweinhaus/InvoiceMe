package com.invoiceme.application.payment;

import com.invoiceme.application.payment.dto.PaymentResponse;
import com.invoiceme.domain.payment.Payment;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Query service for Payment read operations.
 * Implements CQRS pattern for payment queries.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    /**
     * Gets a payment by ID.
     * 
     * @param id Payment ID
     * @return PaymentResponse with payment data
     * @throws EntityNotFoundException if payment not found
     */
    public PaymentResponse getById(UUID id) {
        Payment payment = paymentRepository.findByIdWithInvoiceAndCustomer(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + id));

        return paymentMapper.toResponse(payment);
    }

    /**
     * Gets payments by invoice ID with pagination.
     * 
     * @param invoiceId Invoice ID
     * @param pageable Pagination parameters
     * @return Page of PaymentResponse for specified invoice
     */
    public Page<PaymentResponse> getByInvoiceId(UUID invoiceId, Pageable pageable) {
        return paymentRepository.findByInvoiceIdWithInvoiceAndCustomer(invoiceId, pageable)
                .map(paymentMapper::toResponse);
    }

    /**
     * Gets all payments with pagination.
     * 
     * @param pageable Pagination parameters
     * @return Page of PaymentResponse
     */
    public Page<PaymentResponse> getAll(Pageable pageable) {
        return paymentRepository.findAllWithInvoiceAndCustomer(pageable)
                .map(paymentMapper::toResponse);
    }
}

