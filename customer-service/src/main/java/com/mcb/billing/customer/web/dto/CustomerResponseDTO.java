package com.mcb.billing.customer.web.dto;

import java.time.Instant;

/**
 * Output DTO representing a customer as exposed by the API (response body of the
 * customer endpoints).
 *
 * <p>Uses the business key {@code customerNumber} as the external identifier; the
 * internal database {@code id} is intentionally not exposed. No validation
 * annotations here — this is output, not client input.
 */
public record CustomerResponseDTO(
        String customerNumber,
        String companyName,
        String contactPerson,
        String email,
        String phone,
        String street,
        String postalCode,
        String city,
        String country,
        String vatId,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
