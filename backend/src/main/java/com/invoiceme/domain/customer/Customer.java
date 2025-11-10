package com.invoiceme.domain.customer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Entity
@Table(name = "customers")
@NoArgsConstructor
@Getter
@Setter
public class Customer {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 50)
    private String phone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructor for creating new customers
    public Customer(String name, String email, String address, String phone) {
        this.name = name;
        this.email = email;
        this.address = address;
        this.phone = phone;
        validate();
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Validates customer data including email format and required fields.
     * Throws IllegalArgumentException if validation fails.
     */
    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer email is required");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    /**
     * Updates customer details with validation.
     * Only updates non-null fields.
     * 
     * @param name Customer name (optional)
     * @param email Customer email (optional)
     * @param address Customer address (optional)
     * @param phone Customer phone (optional)
     * @return this for method chaining
     */
    public Customer updateDetails(String name, String email, String address, String phone) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (email != null && !email.trim().isEmpty()) {
            this.email = email;
        }
        if (address != null) {
            this.address = address;
        }
        if (phone != null) {
            this.phone = phone;
        }
        validate();
        return this;
    }
}

