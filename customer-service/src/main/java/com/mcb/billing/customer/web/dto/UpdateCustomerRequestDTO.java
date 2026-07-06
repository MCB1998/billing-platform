package com.mcb.billing.customer.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Input DTO for updating a customer (full replace, the JSON body of
 * {@code PUT /customers/{customerNumber}}).
 *
 * <p>Contains the fields a client may change. The {@code customerNumber} (immutable
 * business key), {@code id}, {@code active} and the timestamps are not part of it.
 * Kept as a separate type from {@link CreateCustomerRequestDTO} even though the
 * fields currently match, so the two can evolve independently.
 */
public record UpdateCustomerRequestDTO(

        @NotBlank
        @Size(max = 200)
        String companyName,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @Size(max = 200)
        String contactPerson,

        @Size(max = 50)
        String phone,

        @Size(max = 200)
        String street,

        @Size(max = 20)
        String postalCode,

        @Size(max = 100)
        String city,

        @Size(max = 100)
        String country,

        @Size(max = 30)
        String vatId
) {
}
