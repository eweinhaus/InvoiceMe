package com.invoiceme.domain.invoice;

import com.invoiceme.domain.customer.Customer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Invoice entity with rich domain behavior.
 * Implements business logic for invoice lifecycle, calculations, and validations.
 */
@Entity
@Table(name = "invoices")
@NoArgsConstructor
@Getter
@Setter
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "invoice_line_items", joinColumns = @JoinColumn(name = "invoice_id"))
    @OrderColumn(name = "line_order")
    private List<LineItem> lineItems = new ArrayList<>();

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating a new invoice.
     * Invoice is created in DRAFT status.
     * 
     * @param customer The customer this invoice belongs to
     */
    public Invoice(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required");
        }
        this.customer = customer;
        this.status = InvoiceStatus.DRAFT;
        this.lineItems = new ArrayList<>();
        this.totalAmount = BigDecimal.ZERO;
        this.balance = BigDecimal.ZERO;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Calculates the total amount by summing all line item subtotals.
     * Updates the totalAmount field and returns it.
     * 
     * @return The calculated total amount
     */
    public BigDecimal calculateTotal() {
        if (lineItems == null || lineItems.isEmpty()) {
            this.totalAmount = BigDecimal.ZERO;
            return BigDecimal.ZERO;
        }

        BigDecimal total = lineItems.stream()
                .map(LineItem::calculateSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalAmount = total.setScale(2, RoundingMode.HALF_UP);
        return this.totalAmount;
    }

    /**
     * Calculates the balance (totalAmount - sum of payments).
     * For now, returns totalAmount if payments are not available.
     * This will be enhanced when Payment entity is implemented (PRD 06).
     * 
     * @return The calculated balance
     */
    public BigDecimal calculateBalance() {
        // TODO: When Payment entity is available, query payments and subtract from totalAmount
        // For now, balance equals totalAmount (no payments yet)
        this.balance = this.totalAmount;
        return this.balance;
    }

    /**
     * Adds a line item to the invoice and recalculates the total.
     * 
     * @param description Line item description
     * @param quantity Quantity (must be > 0)
     * @param unitPrice Unit price (must be >= 0)
     * @return this for method chaining
     */
    public Invoice addLineItem(String description, Integer quantity, BigDecimal unitPrice) {
        LineItem lineItem = new LineItem(description, quantity, unitPrice);
        this.lineItems.add(lineItem);
        calculateTotal();
        return this;
    }

    /**
     * Updates line items and recalculates total.
     * 
     * @param newLineItems New list of line items
     */
    public void updateLineItems(List<LineItem> newLineItems) {
        if (newLineItems == null || newLineItems.isEmpty()) {
            throw new IllegalArgumentException("Invoice must have at least one line item");
        }
        this.lineItems.clear();
        this.lineItems.addAll(newLineItems);
        calculateTotal();
    }

    /**
     * Checks if the invoice can be marked as SENT.
     * Validates: has line items, total > 0, status is DRAFT.
     * 
     * @return true if invoice can be marked as SENT, false otherwise
     */
    public boolean canBeMarkedAsSent() {
        if (this.status != InvoiceStatus.DRAFT) {
            return false;
        }
        if (this.lineItems == null || this.lineItems.isEmpty()) {
            return false;
        }
        if (this.totalAmount == null || this.totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return true;
    }

    /**
     * Marks the invoice as SENT.
     * Validates that the invoice can be marked as SENT before transitioning.
     * 
     * @throws IllegalStateException if invoice cannot be marked as SENT
     */
    public void markAsSent() {
        if (!canBeMarkedAsSent()) {
            throw new IllegalStateException(
                    "Invoice cannot be marked as SENT. " +
                    "Invoice must be in DRAFT status, have at least one line item, and total amount must be greater than 0."
            );
        }
        this.status = InvoiceStatus.SENT;
    }

    /**
     * Checks if the invoice can be edited.
     * Only DRAFT invoices can be edited.
     * 
     * @return true if invoice is in DRAFT status, false otherwise
     */
    public boolean canBeEdited() {
        return this.status == InvoiceStatus.DRAFT;
    }

    /**
     * Applies a payment to the invoice.
     * Updates the balance and may transition status to PAID if balance reaches zero.
     * 
     * @param amount Payment amount (must be > 0 and <= balance)
     * @throws IllegalArgumentException if amount is invalid
     */
    public void applyPayment(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0");
        }
        if (this.balance == null) {
            calculateBalance();
        }
        if (amount.compareTo(this.balance) > 0) {
            throw new IllegalArgumentException(
                    "Payment amount (" + amount + ") cannot exceed invoice balance (" + this.balance + ")"
            );
        }

        this.balance = this.balance.subtract(amount).setScale(2, RoundingMode.HALF_UP);
        
        // If balance reaches zero, mark invoice as PAID
        if (this.balance.compareTo(BigDecimal.ZERO) == 0) {
            this.status = InvoiceStatus.PAID;
        }
    }
}

