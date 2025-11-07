package com.invoiceme;

import com.invoiceme.application.customer.CustomerCommandService;
import com.invoiceme.application.customer.CustomerQueryService;
import com.invoiceme.application.customer.dto.CustomerRequest;
import com.invoiceme.application.customer.dto.CustomerResponse;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CustomerCommandService commandService;

    @Autowired
    private CustomerQueryService queryService;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    void testCreateCustomer_Success() {
        // Given
        CustomerRequest request = new CustomerRequest(
                "John Doe",
                "john.doe@example.com",
                "123 Main St, City, State 12345",
                "+1-555-123-4567"
        );

        // When
        CustomerResponse response = commandService.createCustomer(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("John Doe");
        assertThat(response.email()).isEqualTo("john.doe@example.com");
        assertThat(response.address()).isEqualTo("123 Main St, City, State 12345");
        assertThat(response.phone()).isEqualTo("+1-555-123-4567");
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();

        // Verify saved in database
        Customer savedCustomer = customerRepository.findById(response.id()).orElseThrow();
        assertThat(savedCustomer.getName()).isEqualTo("John Doe");
        assertThat(savedCustomer.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void testGetCustomerById_Success() {
        // Given
        Customer customer = new Customer("Jane Smith", "jane.smith@example.com", null, null);
        Customer savedCustomer = customerRepository.save(customer);
        UUID customerId = savedCustomer.getId();

        // When
        CustomerResponse response = queryService.getById(customerId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(customerId);
        assertThat(response.name()).isEqualTo("Jane Smith");
        assertThat(response.email()).isEqualTo("jane.smith@example.com");
    }

    @Test
    void testGetCustomerById_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When/Then
        assertThatThrownBy(() -> queryService.getById(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Customer not found with id: " + nonExistentId);
    }

    @Test
    void testGetAllCustomers_WithPagination() {
        // Given
        customerRepository.save(new Customer("Alice", "alice@example.com", null, null));
        customerRepository.save(new Customer("Bob", "bob@example.com", null, null));
        customerRepository.save(new Customer("Charlie", "charlie@example.com", null, null));

        // When
        Page<CustomerResponse> page1 = queryService.getAll(PageRequest.of(0, 2, Sort.by("name")));

        // Then
        assertThat(page1.getContent()).hasSize(2);
        assertThat(page1.getTotalElements()).isEqualTo(3);
        assertThat(page1.getTotalPages()).isEqualTo(2);
        assertThat(page1.getNumber()).isEqualTo(0);
        assertThat(page1.getContent().get(0).name()).isEqualTo("Alice");
        assertThat(page1.getContent().get(1).name()).isEqualTo("Bob");

        // When - second page
        Page<CustomerResponse> page2 = queryService.getAll(PageRequest.of(1, 2, Sort.by("name")));

        // Then
        assertThat(page2.getContent()).hasSize(1);
        assertThat(page2.getNumber()).isEqualTo(1);
        assertThat(page2.getContent().get(0).name()).isEqualTo("Charlie");
    }

    @Test
    void testGetAllCustomers_WithSorting() {
        // Given
        customerRepository.save(new Customer("Zebra", "zebra@example.com", null, null));
        customerRepository.save(new Customer("Alice", "alice@example.com", null, null));
        customerRepository.save(new Customer("Bob", "bob@example.com", null, null));

        // When - ascending
        Page<CustomerResponse> ascending = queryService.getAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));

        // Then
        assertThat(ascending.getContent()).hasSize(3);
        assertThat(ascending.getContent().get(0).name()).isEqualTo("Alice");
        assertThat(ascending.getContent().get(1).name()).isEqualTo("Bob");
        assertThat(ascending.getContent().get(2).name()).isEqualTo("Zebra");

        // When - descending
        Page<CustomerResponse> descending = queryService.getAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name")));

        // Then
        assertThat(descending.getContent()).hasSize(3);
        assertThat(descending.getContent().get(0).name()).isEqualTo("Zebra");
        assertThat(descending.getContent().get(1).name()).isEqualTo("Bob");
        assertThat(descending.getContent().get(2).name()).isEqualTo("Alice");
    }

    @Test
    void testUpdateCustomer_Success() {
        // Given
        Customer customer = new Customer("John Doe", "john@example.com", "Old Address", "Old Phone");
        Customer savedCustomer = customerRepository.save(customer);
        UUID customerId = savedCustomer.getId();

        CustomerRequest updateRequest = new CustomerRequest(
                "John Updated",
                "john.updated@example.com",
                "New Address",
                "New Phone"
        );

        // When
        CustomerResponse response = commandService.updateCustomer(customerId, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(customerId);
        assertThat(response.name()).isEqualTo("John Updated");
        assertThat(response.email()).isEqualTo("john.updated@example.com");
        assertThat(response.address()).isEqualTo("New Address");
        assertThat(response.phone()).isEqualTo("New Phone");

        // Verify updated in database
        Customer updatedCustomer = customerRepository.findById(customerId).orElseThrow();
        assertThat(updatedCustomer.getName()).isEqualTo("John Updated");
        assertThat(updatedCustomer.getEmail()).isEqualTo("john.updated@example.com");
        // updatedAt should be after or equal to createdAt (may be same if update happens quickly)
        assertThat(updatedCustomer.getUpdatedAt()).isAfterOrEqualTo(savedCustomer.getCreatedAt());
    }

    @Test
    void testUpdateCustomer_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        CustomerRequest request = new CustomerRequest("Test", "test@example.com", null, null);

        // When/Then
        assertThatThrownBy(() -> commandService.updateCustomer(nonExistentId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Customer not found with id: " + nonExistentId);
    }

    @Test
    void testDeleteCustomer_Success() {
        // Given
        Customer customer = new Customer("To Delete", "delete@example.com", null, null);
        Customer savedCustomer = customerRepository.save(customer);
        UUID customerId = savedCustomer.getId();

        // When
        commandService.deleteCustomer(customerId);

        // Then
        assertThat(customerRepository.existsById(customerId)).isFalse();
    }

    @Test
    void testDeleteCustomer_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When/Then
        assertThatThrownBy(() -> commandService.deleteCustomer(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Customer not found with id: " + nonExistentId);
    }

    @Test
    void testCreateCustomer_InvalidEmail() {
        // Given
        CustomerRequest request = new CustomerRequest(
                "John Doe",
                "invalid-email",
                null,
                null
        );

        // When/Then
        assertThatThrownBy(() -> commandService.createCustomer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
    }

    @Test
    void testCreateCustomer_MissingRequiredFields() {
        // Given - missing name
        CustomerRequest requestWithoutName = new CustomerRequest(
                null,
                "test@example.com",
                null,
                null
        );

        // When/Then
        assertThatThrownBy(() -> commandService.createCustomer(requestWithoutName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer name is required");

        // Given - missing email
        CustomerRequest requestWithoutEmail = new CustomerRequest(
                "Test Name",
                null,
                null,
                null
        );

        // When/Then
        assertThatThrownBy(() -> commandService.createCustomer(requestWithoutEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer email is required");
    }

    @Test
    void testCreateCustomer_DuplicateEmail() {
        // Given
        Customer existingCustomer = new Customer("Existing", "duplicate@example.com", null, null);
        customerRepository.save(existingCustomer);

        CustomerRequest request = new CustomerRequest(
                "New Customer",
                "duplicate@example.com",
                null,
                null
        );

        // When/Then
        assertThatThrownBy(() -> commandService.createCustomer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void testUpdateCustomer_DuplicateEmail() {
        // Given
        Customer customer1 = new Customer("Customer 1", "customer1@example.com", null, null);
        Customer customer2 = new Customer("Customer 2", "customer2@example.com", null, null);
        customerRepository.save(customer1);
        Customer savedCustomer2 = customerRepository.save(customer2);

        CustomerRequest request = new CustomerRequest(
                "Customer 2 Updated",
                "customer1@example.com", // Try to use customer1's email
                null,
                null
        );

        // When/Then
        assertThatThrownBy(() -> commandService.updateCustomer(savedCustomer2.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void testCustomerEntity_ValidateMethod() {
        // Given
        Customer customer = new Customer();

        // When/Then - missing name
        customer.setEmail("test@example.com");
        assertThatThrownBy(() -> customer.validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer name is required");

        // When/Then - missing email
        customer.setName("Test");
        customer.setEmail(null);
        assertThatThrownBy(() -> customer.validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer email is required");

        // When/Then - invalid email format
        customer.setEmail("invalid-email");
        assertThatThrownBy(() -> customer.validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
    }

    @Test
    void testCustomerEntity_UpdateDetailsMethod() {
        // Given
        Customer customer = new Customer("Original", "original@example.com", "Original Address", "Original Phone");
        customerRepository.save(customer);

        // When
        customer.updateDetails("Updated", "updated@example.com", "Updated Address", "Updated Phone");

        // Then
        assertThat(customer.getName()).isEqualTo("Updated");
        assertThat(customer.getEmail()).isEqualTo("updated@example.com");
        assertThat(customer.getAddress()).isEqualTo("Updated Address");
        assertThat(customer.getPhone()).isEqualTo("Updated Phone");
    }

    @Test
    void testCustomerEntity_UpdateDetailsPartial() {
        // Given
        Customer customer = new Customer("Original", "original@example.com", "Original Address", "Original Phone");
        customerRepository.save(customer);

        // When - only update name and email
        customer.updateDetails("Updated", "updated@example.com", null, null);

        // Then
        assertThat(customer.getName()).isEqualTo("Updated");
        assertThat(customer.getEmail()).isEqualTo("updated@example.com");
        assertThat(customer.getAddress()).isEqualTo("Original Address"); // Unchanged
        assertThat(customer.getPhone()).isEqualTo("Original Phone"); // Unchanged
    }
}

