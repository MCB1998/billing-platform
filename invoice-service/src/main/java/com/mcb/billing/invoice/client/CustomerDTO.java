package com.mcb.billing.invoice.client;

/**
 * The invoice-service's own view of a customer, as returned by the customer-service.
 *
 * <p>Each service defines its own contract view - we do not share the customer-service's
 * DTO. Only the fields we care about are declared; unknown JSON fields are ignored.
 */
public record CustomerDTO(
        String customerNumber,
        String companyName,
        boolean active
) {
}
