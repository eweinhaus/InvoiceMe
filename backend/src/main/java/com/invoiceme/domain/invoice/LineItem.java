package com.invoiceme.domain.invoice;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * LineItem value object representing a single line item on an invoice.
 * This is an embeddable value object with business logic for calculating subtotals.
 */
@Embeddable
@NoArgsConstructor
@Getter
@Setter
public class LineItem {

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Constructor with validation.
     * 
     * @param description Line item description
     * @param quantity Quantity (must be > 0)
     * @param unitPrice Unit price (must be >= 0)
     * @throws IllegalArgumentException if validation fails
     */
    public LineItem(String description, Integer quantity, BigDecimal unitPrice) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price must be greater than or equal to 0");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }
        
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the subtotal for this line item.
     * Uses BigDecimal arithmetic to ensure precision.
     * 
     * @return Subtotal (quantity * unitPrice)
     */
    public BigDecimal calculateSubtotal() {
        if (quantity == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }
}

