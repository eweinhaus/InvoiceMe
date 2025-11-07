package com.invoiceme.application.invoice;

import com.invoiceme.application.invoice.dto.CreateInvoiceRequest;
import com.invoiceme.application.invoice.dto.InvoiceResponse;
import com.invoiceme.application.invoice.dto.LineItemRequest;
import com.invoiceme.application.invoice.dto.LineItemResponse;
import com.invoiceme.application.invoice.dto.UpdateInvoiceRequest;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.LineItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    /**
     * Maps Invoice entity to InvoiceResponse DTO.
     * Includes customer name from the customer relationship.
     */
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "status", expression = "java(invoice.getStatus().name())")
    @Mapping(target = "lineItems", source = "lineItems")
    InvoiceResponse toResponse(Invoice invoice);

    /**
     * Maps CreateInvoiceRequest to Invoice entity.
     * Customer reference must be set in service (ignored here).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "lineItems", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Invoice toEntity(CreateInvoiceRequest request);

    /**
     * Updates Invoice entity with UpdateInvoiceRequest.
     * Only updates line items - total will be recalculated in service.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateInvoiceRequest request, @MappingTarget Invoice invoice);

    /**
     * Maps LineItemRequest to LineItem value object.
     */
    LineItem lineItemRequestToLineItem(LineItemRequest request);

    /**
     * Maps list of LineItemRequest to list of LineItem.
     */
    List<LineItem> lineItemRequestListToLineItemList(List<LineItemRequest> lineItemRequests);

    /**
     * Maps LineItem value object to LineItemResponse DTO.
     * Calculates subtotal using domain method.
     */
    @Mapping(target = "subtotal", expression = "java(lineItem.calculateSubtotal())")
    LineItemResponse lineItemToLineItemResponse(LineItem lineItem);

    /**
     * Maps list of LineItem to list of LineItemResponse.
     */
    List<LineItemResponse> lineItemListToLineItemResponseList(List<LineItem> lineItems);
}

