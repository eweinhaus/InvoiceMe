package com.invoiceme;

import com.invoiceme.application.invoice.InvoiceCommandService;
import com.invoiceme.application.invoice.InvoiceQueryService;
import com.invoiceme.application.invoice.dto.CreateInvoiceRequest;
import com.invoiceme.application.invoice.dto.InvoiceResponse;
import com.invoiceme.application.invoice.dto.LineItemRequest;
import com.invoiceme.application.invoice.dto.UpdateInvoiceRequest;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InvoiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private InvoiceCommandService commandService;

    @Autowired
    private InvoiceQueryService queryService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        invoiceRepository.deleteAll();
        customerRepository.deleteAll();
        testCustomer = customerRepository.save(new Customer("Test Customer", "test@example.com", null, null));
    }

    @Test
    void testCreateInvoice_WithLineItems_Success() {
        // Given
        CreateInvoiceRequest request = new CreateInvoiceRequest(
                testCustomer.getId(),
                List.of(
                        new LineItemRequest("Service 1", 10, BigDecimal.valueOf(100.00)),
                        new LineItemRequest("Service 2", 5, BigDecimal.valueOf(50.00))
                )
        );

        // When
        InvoiceResponse response = commandService.createInvoice(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.customerId()).isEqualTo(testCustomer.getId());
        assertThat(response.status()).isEqualTo("DRAFT");
        assertThat(response.lineItems()).hasSize(2);
        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1250.00)); // 10*100 + 5*50
        assertThat(response.balance()).isEqualByComparingTo(BigDecimal.valueOf(1250.00));
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();

        // Verify saved in database
        Invoice savedInvoice = invoiceRepository.findById(response.id()).orElseThrow();
        assertThat(savedInvoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(savedInvoice.getLineItems()).hasSize(2);
        assertThat(savedInvoice.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1250.00));
    }

    @Test
    void testGetInvoiceById_Success() {
        // Given
        Invoice invoice = new Invoice(testCustomer);
        invoice.addLineItem("Test Item", 1, BigDecimal.valueOf(100.00));
        Invoice savedInvoice = invoiceRepository.save(invoice);
        UUID invoiceId = savedInvoice.getId();

        // When
        InvoiceResponse response = queryService.getById(invoiceId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(invoiceId);
        assertThat(response.customerId()).isEqualTo(testCustomer.getId());
        assertThat(response.lineItems()).hasSize(1);
        assertThat(response.lineItems().get(0).description()).isEqualTo("Test Item");
        assertThat(response.lineItems().get(0).subtotal()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    void testGetAllInvoices_WithPagination() {
        // Given
        Invoice invoice1 = new Invoice(testCustomer);
        invoice1.addLineItem("Item 1", 1, BigDecimal.valueOf(100.00));
        invoiceRepository.save(invoice1);

        Invoice invoice2 = new Invoice(testCustomer);
        invoice2.addLineItem("Item 2", 2, BigDecimal.valueOf(200.00));
        invoiceRepository.save(invoice2);

        Invoice invoice3 = new Invoice(testCustomer);
        invoice3.addLineItem("Item 3", 3, BigDecimal.valueOf(300.00));
        invoiceRepository.save(invoice3);

        // When
        Page<InvoiceResponse> page1 = queryService.getAll(PageRequest.of(0, 2, Sort.by("createdAt").descending()));

        // Then
        assertThat(page1.getContent()).hasSize(2);
        assertThat(page1.getTotalElements()).isEqualTo(3);
        assertThat(page1.getTotalPages()).isEqualTo(2);
        assertThat(page1.getNumber()).isEqualTo(0);
    }

    @Test
    void testGetInvoicesByStatus_Success() {
        // Given
        Invoice draftInvoice = new Invoice(testCustomer);
        draftInvoice.addLineItem("Item 1", 1, BigDecimal.valueOf(100.00));
        invoiceRepository.save(draftInvoice);

        Invoice sentInvoice = new Invoice(testCustomer);
        sentInvoice.addLineItem("Item 2", 2, BigDecimal.valueOf(200.00));
        sentInvoice.markAsSent();
        invoiceRepository.save(sentInvoice);

        // When
        Page<InvoiceResponse> draftInvoices = queryService.getByStatus(InvoiceStatus.DRAFT, PageRequest.of(0, 20));
        Page<InvoiceResponse> sentInvoices = queryService.getByStatus(InvoiceStatus.SENT, PageRequest.of(0, 20));

        // Then
        assertThat(draftInvoices.getContent()).hasSize(1);
        assertThat(draftInvoices.getContent().get(0).status()).isEqualTo("DRAFT");
        assertThat(sentInvoices.getContent()).hasSize(1);
        assertThat(sentInvoices.getContent().get(0).status()).isEqualTo("SENT");
    }

    @Test
    void testGetInvoicesByCustomerId_Success() {
        // Given
        Customer customer2 = customerRepository.save(new Customer("Customer 2", "customer2@example.com", null, null));

        Invoice invoice1 = new Invoice(testCustomer);
        invoice1.addLineItem("Item 1", 1, BigDecimal.valueOf(100.00));
        invoiceRepository.save(invoice1);

        Invoice invoice2 = new Invoice(customer2);
        invoice2.addLineItem("Item 2", 2, BigDecimal.valueOf(200.00));
        invoiceRepository.save(invoice2);

        // When
        Page<InvoiceResponse> customer1Invoices = queryService.getByCustomerId(testCustomer.getId(), PageRequest.of(0, 20));

        // Then
        assertThat(customer1Invoices.getContent()).hasSize(1);
        assertThat(customer1Invoices.getContent().get(0).customerId()).isEqualTo(testCustomer.getId());
    }

    @Test
    void testUpdateInvoice_DraftStatus_Success() {
        // Given
        Invoice invoice = new Invoice(testCustomer);
        invoice.addLineItem("Original Item", 1, BigDecimal.valueOf(100.00));
        Invoice savedInvoice = invoiceRepository.save(invoice);
        UUID invoiceId = savedInvoice.getId();

        UpdateInvoiceRequest updateRequest = new UpdateInvoiceRequest(
                List.of(
                        new LineItemRequest("Updated Item 1", 2, BigDecimal.valueOf(150.00)),
                        new LineItemRequest("Updated Item 2", 3, BigDecimal.valueOf(200.00))
                )
        );

        // When
        InvoiceResponse response = commandService.updateInvoice(invoiceId, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(invoiceId);
        assertThat(response.lineItems()).hasSize(2);
        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(900.00)); // 2*150 + 3*200

        // Verify updated in database
        Invoice updatedInvoice = invoiceRepository.findById(invoiceId).orElseThrow();
        assertThat(updatedInvoice.getLineItems()).hasSize(2);
        assertThat(updatedInvoice.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(900.00));
    }

    @Test
    void testUpdateInvoice_NotDraft_Fails() {
        // Given
        Invoice invoice = new Invoice(testCustomer);
        invoice.addLineItem("Item", 1, BigDecimal.valueOf(100.00));
        invoice.markAsSent();
        Invoice savedInvoice = invoiceRepository.save(invoice);
        UUID invoiceId = savedInvoice.getId();

        UpdateInvoiceRequest updateRequest = new UpdateInvoiceRequest(
                List.of(new LineItemRequest("New Item", 1, BigDecimal.valueOf(200.00)))
        );

        // When/Then
        assertThatThrownBy(() -> commandService.updateInvoice(invoiceId, updateRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invoice cannot be edited");
    }

    @Test
    void testMarkInvoiceAsSent_ValidTransition_Success() {
        // Given
        Invoice invoice = new Invoice(testCustomer);
        invoice.addLineItem("Item", 10, BigDecimal.valueOf(100.00));
        Invoice savedInvoice = invoiceRepository.save(invoice);
        UUID invoiceId = savedInvoice.getId();

        // When
        InvoiceResponse response = commandService.markAsSent(invoiceId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(invoiceId);
        assertThat(response.status()).isEqualTo("SENT");

        // Verify updated in database
        Invoice updatedInvoice = invoiceRepository.findById(invoiceId).orElseThrow();
        assertThat(updatedInvoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
    }

    @Test
    void testMarkInvoiceAsSent_NoLineItems_Fails() {
        // Given
        Invoice invoice = new Invoice(testCustomer);
        // No line items added
        Invoice savedInvoice = invoiceRepository.save(invoice);
        UUID invoiceId = savedInvoice.getId();

        // When/Then
        assertThatThrownBy(() -> commandService.markAsSent(invoiceId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invoice cannot be marked as SENT");
    }

    @Test
    void testMarkInvoiceAsSent_AlreadySent_Fails() {
        // Given
        Invoice invoice = new Invoice(testCustomer);
        invoice.addLineItem("Item", 1, BigDecimal.valueOf(100.00));
        invoice.markAsSent();
        Invoice savedInvoice = invoiceRepository.save(invoice);
        UUID invoiceId = savedInvoice.getId();

        // When/Then
        assertThatThrownBy(() -> commandService.markAsSent(invoiceId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invoice cannot be marked as SENT");
    }

    @Test
    void testMarkInvoiceAsSent_ZeroTotal_Fails() {
        // Given
        Invoice invoice = new Invoice(testCustomer);
        // Add line item with zero quantity or price to get zero total
        // Actually, we can't add zero quantity due to validation, so we'll test with empty line items
        Invoice savedInvoice = invoiceRepository.save(invoice);
        UUID invoiceId = savedInvoice.getId();

        // When/Then
        assertThatThrownBy(() -> commandService.markAsSent(invoiceId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invoice cannot be marked as SENT");
    }

    @Test
    void testCalculateTotal_Correct() {
        // Given
        Invoice invoice = new Invoice(testCustomer);
        invoice.addLineItem("Item 1", 10, BigDecimal.valueOf(100.00));
        invoice.addLineItem("Item 2", 5, BigDecimal.valueOf(50.00));
        invoice.addLineItem("Item 3", 2, BigDecimal.valueOf(25.00));

        // When
        BigDecimal total = invoice.calculateTotal();

        // Then
        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(1300.00)); // 10*100 + 5*50 + 2*25 = 1000 + 250 + 50
    }

    @Test
    void testGetInvoiceById_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When/Then
        assertThatThrownBy(() -> queryService.getById(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Invoice not found with id: " + nonExistentId);
    }

    @Test
    void testCreateInvoice_CustomerNotFound() {
        // Given
        UUID nonExistentCustomerId = UUID.randomUUID();
        CreateInvoiceRequest request = new CreateInvoiceRequest(
                nonExistentCustomerId,
                List.of(new LineItemRequest("Item", 1, BigDecimal.valueOf(100.00)))
        );

        // When/Then
        assertThatThrownBy(() -> commandService.createInvoice(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Customer not found with id: " + nonExistentCustomerId);
    }

    @Test
    void testInvoiceEntity_CalculateTotal_DomainLogic() {
        // Given
        Invoice invoice = new Invoice(testCustomer);
        invoice.addLineItem("Item 1", 10, BigDecimal.valueOf(100.00));
        invoice.addLineItem("Item 2", 5, BigDecimal.valueOf(50.00));

        // When
        BigDecimal total = invoice.calculateTotal();

        // Then
        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(1250.00));
        assertThat(invoice.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1250.00));
    }

    @Test
    void testInvoiceEntity_CanBeMarkedAsSent_DomainLogic() {
        // Given - valid invoice
        Invoice validInvoice = new Invoice(testCustomer);
        validInvoice.addLineItem("Item", 10, BigDecimal.valueOf(100.00));

        // When/Then
        assertThat(validInvoice.canBeMarkedAsSent()).isTrue();

        // Given - no line items
        Invoice invoiceNoItems = new Invoice(testCustomer);

        // When/Then
        assertThat(invoiceNoItems.canBeMarkedAsSent()).isFalse();

        // Given - already SENT
        Invoice sentInvoice = new Invoice(testCustomer);
        sentInvoice.addLineItem("Item", 1, BigDecimal.valueOf(100.00));
        sentInvoice.markAsSent();

        // When/Then
        assertThat(sentInvoice.canBeMarkedAsSent()).isFalse();
    }

    @Test
    void testInvoiceEntity_MarkAsSent_DomainLogic() {
        // Given
        Invoice invoice = new Invoice(testCustomer);
        invoice.addLineItem("Item", 10, BigDecimal.valueOf(100.00));

        // When
        invoice.markAsSent();

        // Then
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);

        // Given - invalid invoice
        Invoice invalidInvoice = new Invoice(testCustomer);
        // No line items

        // When/Then
        assertThatThrownBy(() -> invalidInvoice.markAsSent())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invoice cannot be marked as SENT");
    }

    @Test
    void testInvoiceEntity_CanBeEdited_DomainLogic() {
        // Given - DRAFT invoice
        Invoice draftInvoice = new Invoice(testCustomer);
        draftInvoice.addLineItem("Item", 1, BigDecimal.valueOf(100.00));

        // When/Then
        assertThat(draftInvoice.canBeEdited()).isTrue();

        // Given - SENT invoice
        Invoice sentInvoice = new Invoice(testCustomer);
        sentInvoice.addLineItem("Item", 1, BigDecimal.valueOf(100.00));
        sentInvoice.markAsSent();

        // When/Then
        assertThat(sentInvoice.canBeEdited()).isFalse();
    }

    @Test
    void testLineItem_CalculateSubtotal() {
        // Given
        com.invoiceme.domain.invoice.LineItem lineItem = new com.invoiceme.domain.invoice.LineItem(
                "Test Item",
                10,
                BigDecimal.valueOf(100.00)
        );

        // When
        BigDecimal subtotal = lineItem.calculateSubtotal();

        // Then
        assertThat(subtotal).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
    }
}

