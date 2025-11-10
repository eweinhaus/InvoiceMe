package com.invoiceme.infrastructure.email;

import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    private Customer testCustomer;
    private Invoice testInvoice;
    private byte[] testPdfBytes;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer("Test Customer", "test@example.com", "123 Main St", "555-1234");
        testInvoice = new Invoice(testCustomer);
        testInvoice.setId(UUID.randomUUID()); // Set ID for invoice
        testInvoice.setCreatedAt(LocalDateTime.now()); // Set createdAt for email template
        testInvoice.addLineItem("Test Item", 1, BigDecimal.valueOf(100.00));
        testInvoice.calculateTotal();
        testPdfBytes = "fake pdf content".getBytes();
        
        // Set fromEmail using reflection
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@invoiceme.com");
    }

    @Test
    void testSendInvoiceEmail_Success() throws MessagingException {
        // Given
        MimeMessage mockMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mockMessage);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));

        // When
        emailService.sendInvoiceEmail(testInvoice, testPdfBytes);

        // Then
        verify(javaMailSender, times(1)).createMimeMessage();
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendInvoiceEmail_NullInvoice_ThrowsException() {
        // When/Then
        assertThatThrownBy(() -> emailService.sendInvoiceEmail(null, testPdfBytes))
                .isInstanceOf(EmailException.class)
                .hasMessageContaining("Invoice cannot be null");
    }

    @Test
    void testSendInvoiceEmail_NullCustomer_ThrowsException() {
        // Given
        Invoice invoiceWithoutCustomer = new Invoice();
        invoiceWithoutCustomer.setId(UUID.randomUUID());
        invoiceWithoutCustomer.setCreatedAt(LocalDateTime.now());
        invoiceWithoutCustomer.setCustomer(null);

        // When/Then
        assertThatThrownBy(() -> emailService.sendInvoiceEmail(invoiceWithoutCustomer, testPdfBytes))
                .isInstanceOf(EmailException.class)
                .hasMessageContaining("Customer email is required");
    }

    @Test
    void testSendInvoiceEmail_NullCustomerEmail_ThrowsException() {
        // Given - create customer with email, then set to null using reflection
        Customer customer = new Customer("Test", "test@example.com", null, null);
        Invoice invoice = new Invoice(customer);
        invoice.setId(UUID.randomUUID());
        invoice.setCreatedAt(LocalDateTime.now());
        // Use reflection to set email to null (bypassing validation)
        ReflectionTestUtils.setField(customer, "email", null);

        // When/Then
        assertThatThrownBy(() -> emailService.sendInvoiceEmail(invoice, testPdfBytes))
                .isInstanceOf(EmailException.class)
                .hasMessageContaining("Customer email is required");
    }

    @Test
    void testSendInvoiceEmail_NullPdfBytes_ThrowsException() {
        // When/Then
        assertThatThrownBy(() -> emailService.sendInvoiceEmail(testInvoice, null))
                .isInstanceOf(EmailException.class)
                .hasMessageContaining("PDF bytes cannot be null or empty");
    }

    @Test
    void testSendInvoiceEmail_EmptyPdfBytes_ThrowsException() {
        // When/Then
        assertThatThrownBy(() -> emailService.sendInvoiceEmail(testInvoice, new byte[0]))
                .isInstanceOf(EmailException.class)
                .hasMessageContaining("PDF bytes cannot be null or empty");
    }

    @Test
    void testSendInvoiceEmail_MessagingException_ThrowsEmailException() throws MessagingException {
        // Given - Simulate MessagingException by throwing MailException that wraps it
        MimeMessage mockMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mockMessage);
        // Throw a MailException that would occur during message preparation
        doThrow(new MailException("Test messaging error") {
            @Override
            public Throwable getCause() {
                return new MessagingException("Test error");
            }
        }).when(javaMailSender).send(any(MimeMessage.class));

        // When/Then
        assertThatThrownBy(() -> emailService.sendInvoiceEmail(testInvoice, testPdfBytes))
                .isInstanceOf(EmailException.class)
                .hasMessageContaining("Failed to send email");
    }

    @Test
    void testSendInvoiceEmail_MailException_ThrowsEmailException() throws MessagingException {
        // Given
        MimeMessage mockMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mockMessage);
        doThrow(new MailException("Test mail error") {}).when(javaMailSender).send(any(MimeMessage.class));

        // When/Then
        assertThatThrownBy(() -> emailService.sendInvoiceEmail(testInvoice, testPdfBytes))
                .isInstanceOf(EmailException.class)
                .hasMessageContaining("Failed to send email");
    }
}

