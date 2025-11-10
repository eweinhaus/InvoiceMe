package com.invoiceme.infrastructure.email;

import com.invoiceme.application.invoice.EmailService;
import com.invoiceme.domain.invoice.Invoice;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * Implementation of EmailService for sending invoice emails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(Locale.US);

    @Override
    public void sendInvoiceEmail(Invoice invoice, byte[] pdfBytes) throws EmailException {
        if (invoice == null) {
            throw new EmailException("Invoice cannot be null");
        }

        if (invoice.getCustomer() == null || invoice.getCustomer().getEmail() == null) {
            throw new EmailException("Customer email is required to send invoice");
        }

        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new EmailException("PDF bytes cannot be null or empty");
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set from address
            String from = fromEmail != null && !fromEmail.isEmpty() 
                ? fromEmail 
                : "noreply@invoiceme.com";
            helper.setFrom(from);

            // Set to address
            helper.setTo(invoice.getCustomer().getEmail());

            // Set subject
            String invoiceNumber = formatInvoiceNumber(invoice.getId());
            helper.setSubject("Invoice #" + invoiceNumber + " from InvoiceMe");

            // Set HTML body
            String htmlBody = generateEmailBody(invoice);
            helper.setText(htmlBody, true);

            // Attach PDF
            String filename = "invoice-" + invoice.getId().toString().substring(0, 8) + ".pdf";
            helper.addAttachment(filename, new ByteArrayDataSource(pdfBytes, "application/pdf"));

            // Send email
            javaMailSender.send(message);
            log.info("Invoice email sent successfully to {} for invoice {}", 
                invoice.getCustomer().getEmail(), invoiceNumber);

        } catch (MessagingException e) {
            log.error("Failed to create email message for invoice {}", invoice.getId(), e);
            throw new EmailException("Failed to create email message: " + e.getMessage(), e);
        } catch (MailException e) {
            log.error("Failed to send email for invoice {}", invoice.getId(), e);
            throw new EmailException("Failed to send email: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending email for invoice {}", invoice.getId(), e);
            throw new EmailException("Unexpected error sending email: " + e.getMessage(), e);
        }
    }

    private String generateEmailBody(Invoice invoice) {
        String invoiceNumber = formatInvoiceNumber(invoice.getId());
        String customerName = invoice.getCustomer().getName();
        String invoiceDate = invoice.getCreatedAt().format(DATE_FORMATTER);
        String totalAmount = CURRENCY_FORMATTER.format(invoice.getTotalAmount());
        String balance = CURRENCY_FORMATTER.format(invoice.getBalance());

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2563eb; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9fafb; }
                    .invoice-details { background-color: white; padding: 15px; margin: 20px 0; border-radius: 5px; }
                    .detail-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #e5e7eb; }
                    .detail-row:last-child { border-bottom: none; }
                    .detail-label { font-weight: bold; color: #6b7280; }
                    .detail-value { color: #111827; }
                    .footer { text-align: center; padding: 20px; color: #6b7280; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>InvoiceMe</h1>
                    </div>
                    <div class="content">
                        <p>Dear %s,</p>
                        <p>Thank you for your business! Please find your invoice attached to this email.</p>
                        
                        <div class="invoice-details">
                            <div class="detail-row">
                                <span class="detail-label">Invoice Number:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Date:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Total Amount:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Balance Due:</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>
                        
                        <p>Please review the attached PDF for complete invoice details including line items.</p>
                        <p>If you have any questions, please don't hesitate to contact us.</p>
                        <p>Best regards,<br>The InvoiceMe Team</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated email. Please do not reply to this message.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(customerName, invoiceNumber, invoiceDate, totalAmount, balance);
    }

    private String formatInvoiceNumber(UUID id) {
        return "INV-" + id.toString().substring(0, 8).toUpperCase();
    }
}

