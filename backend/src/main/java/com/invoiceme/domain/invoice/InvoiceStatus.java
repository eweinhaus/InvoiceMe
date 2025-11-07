package com.invoiceme.domain.invoice;

/**
 * Invoice status enum representing the lifecycle states of an invoice.
 * 
 * DRAFT: Invoice is being created and can be edited
 * SENT: Invoice has been sent to the customer and cannot be edited
 * PAID: Invoice has been fully paid
 */
public enum InvoiceStatus {
    DRAFT,
    SENT,
    PAID
}

