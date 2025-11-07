package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    
    /**
     * Finds a customer by email address.
     * 
     * @param email Customer email address
     * @return Optional containing the customer if found
     */
    Optional<Customer> findByEmail(String email);
}

