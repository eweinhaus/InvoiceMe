package com.invoiceme.application.payment;

import com.invoiceme.application.payment.dto.PaymentRequest;
import com.invoiceme.application.payment.dto.PaymentResponse;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.payment.Payment;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command service for Payment write operations.
 * Implements CQRS pattern for payment creation.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentCommandService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentMapper paymentMapper;

    /**
     * Records a payment for an invoice.
     * Validates payment amount, applies payment to invoice, and saves both entities.
     * 
     * @param request Payment request with invoice ID, amount, and payment date
     * @return PaymentResponse with created payment data
     * @throws EntityNotFoundException if invoice not found
     * @throws IllegalArgumentException if payment amount is invalid or exceeds invoice balance
     */
    public PaymentResponse recordPayment(PaymentRequest request) {
        // Find invoice
        Invoice invoice = invoiceRepository.findById(request.invoiceId())
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + request.invoiceId()));

        // Calculate invoice balance before validation (query existing payments)
        calculateInvoiceBalance(invoice);

        // Create payment entity from request
        Payment payment = paymentMapper.toEntity(request);
        payment.setInvoice(invoice);

        // Validate payment amount using domain method
        payment.validateAmount(invoice);

        // Apply payment to invoice (updates balance and may transition to PAID)
        payment.applyToInvoice();

        // Save payment
        Payment savedPayment = paymentRepository.save(payment);

        // Save invoice (balance and status may have changed)
        invoiceRepository.save(invoice);

        // Map to response
        return paymentMapper.toResponse(savedPayment);
    }

    /**
     * Calculates invoice balance by querying existing payments.
     * Updates the invoice's balance field.
     * 
     * @param invoice Invoice to calculate balance for
     */
    private void calculateInvoiceBalance(Invoice invoice) {
        // Query all payments for this invoice
        var payments = paymentRepository.findByInvoice_Id(invoice.getId(), 
                org.springframework.data.domain.Pageable.unpaged());
        
        // Sum payment amounts
        var totalPayments = payments.getContent().stream()
                .map(Payment::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        // Calculate balance: totalAmount - sum of payments
        var balance = invoice.getTotalAmount().subtract(totalPayments)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        
        // Update invoice balance
        invoice.setBalance(balance);
    }
}

