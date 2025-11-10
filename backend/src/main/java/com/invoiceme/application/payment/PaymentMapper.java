package com.invoiceme.application.payment;

import com.invoiceme.application.payment.dto.PaymentRequest;
import com.invoiceme.application.payment.dto.PaymentResponse;
import com.invoiceme.domain.payment.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.UUID;

/**
 * MapStruct mapper for Payment entity and DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {

    /**
     * Maps Payment entity to PaymentResponse DTO.
     * Includes invoice ID, invoice number, and customer name from the invoice relationship.
     * Invoice number is generated from invoice ID (first 8 characters).
     */
    @Mapping(target = "invoiceId", source = "invoice.id")
    @Mapping(target = "invoiceNumber", expression = "java(generateInvoiceNumber(payment))")
    @Mapping(target = "customerName", source = "invoice.customer.name")
    PaymentResponse toResponse(Payment payment);

    /**
     * Maps PaymentRequest to Payment entity.
     * Invoice reference must be set in service (ignored here).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Payment toEntity(PaymentRequest request);

    /**
     * Generates invoice number from payment's invoice ID.
     * Helper method for MapStruct expression.
     */
    default String generateInvoiceNumber(Payment payment) {
        if (payment == null || payment.getInvoice() == null || payment.getInvoice().getId() == null) {
            return null;
        }
        UUID invoiceId = payment.getInvoice().getId();
        return "INV-" + invoiceId.toString().substring(0, 8).toUpperCase();
    }
}

