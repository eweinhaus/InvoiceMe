package com.invoiceme;

import com.invoiceme.application.invoice.InvoiceCommandService;
import com.invoiceme.application.invoice.InvoiceQueryService;
import com.invoiceme.application.invoice.dto.CreateInvoiceRequest;
import com.invoiceme.application.invoice.dto.InvoiceResponse;
import com.invoiceme.application.invoice.dto.LineItemRequest;
import com.invoiceme.application.payment.PaymentCommandService;
import com.invoiceme.application.payment.PaymentQueryService;
import com.invoiceme.application.payment.dto.PaymentRequest;
import com.invoiceme.application.payment.dto.PaymentResponse;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.domain.payment.Payment;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PaymentCommandService paymentCommandService;

    @Autowired
    private PaymentQueryService paymentQueryService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceCommandService invoiceCommandService;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer testCustomer;
    private Invoice testInvoice;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        invoiceRepository.deleteAll();
        customerRepository.deleteAll();
        
        testCustomer = customerRepository.save(new Customer("Test Customer", "test@example.com", null, null));
        
        // Create invoice in SENT status with line items
        CreateInvoiceRequest invoiceRequest = new CreateInvoiceRequest(
                testCustomer.getId(),
                List.of(
                        new LineItemRequest("Service 1", 10, BigDecimal.valueOf(100.00))
                )
        );
        InvoiceResponse invoiceResponse = invoiceCommandService.createInvoice(invoiceRequest);
        invoiceCommandService.markAsSent(invoiceResponse.id());
        testInvoice = invoiceRepository.findById(invoiceResponse.id()).orElseThrow();
    }

    @Test
    void testRecordPayment_Success() {
        // Given
        PaymentRequest request = new PaymentRequest(
                testInvoice.getId(),
                BigDecimal.valueOf(300.00),
                LocalDateTime.now()
        );

        // When
        PaymentResponse response = paymentCommandService.recordPayment(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.invoiceId()).isEqualTo(testInvoice.getId());
        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(300.00));
        assertThat(response.paymentDate()).isNotNull();
        assertThat(response.createdAt()).isNotNull();

        // Verify saved in database
        Payment savedPayment = paymentRepository.findById(response.id()).orElseThrow();
        assertThat(savedPayment.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(300.00));
        assertThat(savedPayment.getInvoice().getId()).isEqualTo(testInvoice.getId());

        // Verify invoice balance updated
        Invoice updatedInvoice = invoiceRepository.findById(testInvoice.getId()).orElseThrow();
        assertThat(updatedInvoice.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(700.00)); // 1000 - 300
        assertThat(updatedInvoice.getStatus()).isEqualTo(InvoiceStatus.SENT); // Still SENT, balance > 0
    }

    @Test
    void testGetPaymentById_Success() {
        // Given
        Payment payment = new Payment(testInvoice, BigDecimal.valueOf(500.00), LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);

        // When
        PaymentResponse response = paymentQueryService.getById(savedPayment.getId());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(savedPayment.getId());
        assertThat(response.invoiceId()).isEqualTo(testInvoice.getId());
        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
    }

    @Test
    void testGetAllPayments_WithPagination() {
        // Given
        Payment payment1 = new Payment(testInvoice, BigDecimal.valueOf(100.00), LocalDateTime.now());
        Payment payment2 = new Payment(testInvoice, BigDecimal.valueOf(200.00), LocalDateTime.now());
        paymentRepository.save(payment1);
        paymentRepository.save(payment2);

        // When
        Page<PaymentResponse> response = paymentQueryService.getAll(
                PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "paymentDate"))
        );

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
    }

    @Test
    void testGetPaymentsByInvoiceId_Success() {
        // Given
        Invoice invoice2 = new Invoice(testCustomer);
        invoice2.addLineItem("Service 2", 5, BigDecimal.valueOf(50.00));
        Invoice savedInvoice2 = invoiceRepository.save(invoice2);

        Payment payment1 = new Payment(testInvoice, BigDecimal.valueOf(100.00), LocalDateTime.now());
        Payment payment2 = new Payment(savedInvoice2, BigDecimal.valueOf(200.00), LocalDateTime.now());
        paymentRepository.save(payment1);
        paymentRepository.save(payment2);

        // When
        Page<PaymentResponse> response = paymentQueryService.getByInvoiceId(
                testInvoice.getId(),
                PageRequest.of(0, 20)
        );

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).invoiceId()).isEqualTo(testInvoice.getId());
    }

    @Test
    void testRecordPayment_UpdatesInvoiceBalance() {
        // Given
        PaymentRequest request1 = new PaymentRequest(
                testInvoice.getId(),
                BigDecimal.valueOf(300.00),
                LocalDateTime.now()
        );
        PaymentRequest request2 = new PaymentRequest(
                testInvoice.getId(),
                BigDecimal.valueOf(500.00),
                LocalDateTime.now()
        );

        // When
        paymentCommandService.recordPayment(request1);
        paymentCommandService.recordPayment(request2);

        // Then
        Invoice updatedInvoice = invoiceRepository.findById(testInvoice.getId()).orElseThrow();
        assertThat(updatedInvoice.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(200.00)); // 1000 - 300 - 500
        assertThat(updatedInvoice.getStatus()).isEqualTo(InvoiceStatus.SENT); // Still SENT, balance > 0
    }

    @Test
    void testRecordPayment_TransitionsInvoiceToPaid() {
        // Given
        PaymentRequest request = new PaymentRequest(
                testInvoice.getId(),
                BigDecimal.valueOf(1000.00), // Full payment
                LocalDateTime.now()
        );

        // When
        paymentCommandService.recordPayment(request);

        // Then
        Invoice updatedInvoice = invoiceRepository.findById(testInvoice.getId()).orElseThrow();
        assertThat(updatedInvoice.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(updatedInvoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    void testRecordPayment_TransitionsInvoiceToPaid_WithPartialPayments() {
        // Given
        PaymentRequest request1 = new PaymentRequest(
                testInvoice.getId(),
                BigDecimal.valueOf(600.00),
                LocalDateTime.now()
        );
        PaymentRequest request2 = new PaymentRequest(
                testInvoice.getId(),
                BigDecimal.valueOf(400.00), // Completes payment
                LocalDateTime.now()
        );

        // When
        paymentCommandService.recordPayment(request1);
        Invoice afterFirstPayment = invoiceRepository.findById(testInvoice.getId()).orElseThrow();
        assertThat(afterFirstPayment.getStatus()).isEqualTo(InvoiceStatus.SENT); // Still SENT
        assertThat(afterFirstPayment.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(400.00));

        paymentCommandService.recordPayment(request2);

        // Then
        Invoice updatedInvoice = invoiceRepository.findById(testInvoice.getId()).orElseThrow();
        assertThat(updatedInvoice.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(updatedInvoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    void testRecordPayment_AmountExceedsBalance_Fails() {
        // Given
        PaymentRequest request = new PaymentRequest(
                testInvoice.getId(),
                BigDecimal.valueOf(1500.00), // Exceeds balance of 1000.00
                LocalDateTime.now()
        );

        // When/Then
        assertThatThrownBy(() -> paymentCommandService.recordPayment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed invoice balance");
    }

    @Test
    void testRecordPayment_ZeroOrNegativeAmount_Fails() {
        // Given - Zero amount
        PaymentRequest requestZero = new PaymentRequest(
                testInvoice.getId(),
                BigDecimal.ZERO,
                LocalDateTime.now()
        );

        // When/Then
        assertThatThrownBy(() -> paymentCommandService.recordPayment(requestZero))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment amount must be greater than 0");

        // Given - Negative amount
        PaymentRequest requestNegative = new PaymentRequest(
                testInvoice.getId(),
                BigDecimal.valueOf(-100.00),
                LocalDateTime.now()
        );

        // When/Then
        assertThatThrownBy(() -> paymentCommandService.recordPayment(requestNegative))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment amount must be greater than 0");
    }

    @Test
    void testRecordPayment_InvoiceNotFound() {
        // Given
        UUID nonExistentInvoiceId = UUID.randomUUID();
        PaymentRequest request = new PaymentRequest(
                nonExistentInvoiceId,
                BigDecimal.valueOf(100.00),
                LocalDateTime.now()
        );

        // When/Then
        assertThatThrownBy(() -> paymentCommandService.recordPayment(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Invoice not found");
    }

    @Test
    void testFullFlow_CreateCustomer_CreateInvoice_RecordPayment_VerifyBalance() {
        // Given - Customer already created in setUp
        // Invoice already created in setUp

        // When - Record partial payment
        PaymentRequest partialPayment = new PaymentRequest(
                testInvoice.getId(),
                BigDecimal.valueOf(300.00),
                LocalDateTime.now()
        );
        paymentCommandService.recordPayment(partialPayment);

        // Then - Verify balance updated
        Invoice afterPartial = invoiceRepository.findById(testInvoice.getId()).orElseThrow();
        assertThat(afterPartial.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(700.00));
        assertThat(afterPartial.getStatus()).isEqualTo(InvoiceStatus.SENT);

        // When - Record full payment
        PaymentRequest fullPayment = new PaymentRequest(
                testInvoice.getId(),
                BigDecimal.valueOf(700.00),
                LocalDateTime.now()
        );
        paymentCommandService.recordPayment(fullPayment);

        // Then - Verify complete lifecycle
        Invoice afterFull = invoiceRepository.findById(testInvoice.getId()).orElseThrow();
        assertThat(afterFull.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(afterFull.getStatus()).isEqualTo(InvoiceStatus.PAID);

        // Verify payments exist
        Page<PaymentResponse> payments = paymentQueryService.getByInvoiceId(
                testInvoice.getId(),
                PageRequest.of(0, 20)
        );
        assertThat(payments.getContent()).hasSize(2);
    }

    @Test
    void testGetPaymentById_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When/Then
        assertThatThrownBy(() -> paymentQueryService.getById(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Payment not found");
    }

    @Test
    void testPaymentValidateAmount_DomainLogic() {
        // Given
        Invoice invoice = new Invoice(testCustomer);
        invoice.addLineItem("Item", 1, BigDecimal.valueOf(1000.00));
        invoice.calculateBalance(); // Balance = 1000.00
        Invoice savedInvoice = invoiceRepository.save(invoice);

        Payment validPayment = new Payment(savedInvoice, BigDecimal.valueOf(500.00), LocalDateTime.now());
        Payment invalidPayment = new Payment(savedInvoice, BigDecimal.valueOf(1500.00), LocalDateTime.now());

        // When/Then - Valid payment
        validPayment.validateAmount(savedInvoice); // Should not throw

        // When/Then - Invalid payment (exceeds balance)
        assertThatThrownBy(() -> invalidPayment.validateAmount(savedInvoice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed invoice balance");

        // When/Then - Zero payment (test via validateAmount directly, constructor prevents creating zero payment)
        Payment zeroPayment = new Payment();
        zeroPayment.setInvoice(savedInvoice);
        zeroPayment.setAmount(BigDecimal.ZERO);
        assertThatThrownBy(() -> zeroPayment.validateAmount(savedInvoice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment amount must be greater than 0");
    }

    @Test
    void testPaymentApplyToInvoice_DomainLogic() {
        // Given
        Invoice invoice = new Invoice(testCustomer);
        invoice.addLineItem("Item", 1, BigDecimal.valueOf(1000.00));
        invoice.calculateBalance(); // Balance = 1000.00
        invoice.markAsSent(); // Transition to SENT status
        Invoice savedInvoice = invoiceRepository.save(invoice);

        Payment payment = new Payment(savedInvoice, BigDecimal.valueOf(500.00), LocalDateTime.now());

        // When
        payment.applyToInvoice();

        // Then
        assertThat(savedInvoice.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
        assertThat(savedInvoice.getStatus()).isEqualTo(InvoiceStatus.SENT); // Still SENT

        // When - Full payment
        Payment fullPayment = new Payment(savedInvoice, BigDecimal.valueOf(500.00), LocalDateTime.now());
        fullPayment.applyToInvoice();

        // Then
        assertThat(savedInvoice.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(savedInvoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
    }
}

