package com.invoiceme.application.invoice;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.email.EmailException;

/**
 * Service for sending invoice emails to customers.
 */
public interface EmailService {

    /**
     * Sends an invoice email to the customer with PDF attachment.
     * 
     * @param invoice The invoice to send
     * @param pdfBytes The PDF bytes to attach
     * @throws EmailException if email sending fails
     */
    void sendInvoiceEmail(Invoice invoice, byte[] pdfBytes) throws EmailException;
}


