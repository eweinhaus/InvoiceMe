package com.invoiceme.domain.payment;

import com.invoiceme.domain.invoice.Invoice;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment entity with rich domain behavior.
 * Implements business logic for payment validation and application to invoices.
 */
@Entity
@Table(name = "payments")
@NoArgsConstructor
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @NotNull
    private Invoice invoice;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Constructor for creating a new payment.
     * 
     * @param invoice The invoice this payment applies to
     * @param amount Payment amount (must be > 0)
     * @param paymentDate Payment date
     */
    public Payment(Invoice invoice, BigDecimal amount, LocalDateTime paymentDate) {
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice is required");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0");
        }
        this.invoice = invoice;
        this.amount = amount;
        this.paymentDate = paymentDate != null ? paymentDate : LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.paymentDate == null) {
            this.paymentDate = now;
        }
    }

    /**
     * Validates the payment amount against the invoice balance.
     * 
     * @param invoice The invoice to validate against
     * @throws IllegalArgumentException if amount is invalid or exceeds invoice balance
     */
    public void validateAmount(Invoice invoice) {
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice is required for payment validation");
        }
        if (this.amount == null || this.amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0");
        }
        
        // Calculate invoice balance if not already calculated
        if (invoice.getBalance() == null) {
            invoice.calculateBalance();
        }
        
        BigDecimal invoiceBalance = invoice.getBalance();
        if (this.amount.compareTo(invoiceBalance) > 0) {
            throw new IllegalArgumentException(
                    "Payment amount (" + this.amount + ") cannot exceed invoice balance (" + invoiceBalance + ")"
            );
        }
    }

    /**
     * Applies this payment to the invoice.
     * Delegates to Invoice's applyPayment() method which updates balance and may transition to PAID.
     */
    public void applyToInvoice() {
        if (this.invoice == null) {
            throw new IllegalStateException("Cannot apply payment: invoice is not set");
        }
        this.invoice.applyPayment(this.amount);
    }
}



