package com.invoiceme.application.customer;

import com.invoiceme.application.customer.dto.CustomerRequest;
import com.invoiceme.application.customer.dto.CustomerResponse;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerCommandService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerResponse createCustomer(CustomerRequest request) {
        // Check if email already exists before saving
        if (customerRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Customer with email " + request.email() + " already exists");
        }
        
        Customer customer = customerMapper.toEntity(request);
        customer.validate();
        
        try {
            Customer savedCustomer = customerRepository.save(customer);
            return customerMapper.toResponse(savedCustomer);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage() != null && (e.getMessage().contains("email") || e.getMessage().contains("unique"))) {
                throw new IllegalArgumentException("Customer with email " + request.email() + " already exists", e);
            }
            throw e;
        }
    }

    public CustomerResponse updateCustomer(UUID id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));
        
        // Check if email already exists (and is different from current email)
        if (!customer.getEmail().equals(request.email())) {
            if (customerRepository.findByEmail(request.email()).isPresent()) {
                throw new IllegalArgumentException("Customer with email " + request.email() + " already exists");
            }
        }
        
        customer.updateDetails(request.name(), request.email(), request.address(), request.phone());
        
        try {
            Customer updatedCustomer = customerRepository.save(customer);
            return customerMapper.toResponse(updatedCustomer);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage() != null && (e.getMessage().contains("email") || e.getMessage().contains("unique"))) {
                throw new IllegalArgumentException("Customer with email " + request.email() + " already exists", e);
            }
            throw e;
        }
    }

    public void deleteCustomer(UUID id) {
        if (!customerRepository.existsById(id)) {
            throw new EntityNotFoundException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }
}

