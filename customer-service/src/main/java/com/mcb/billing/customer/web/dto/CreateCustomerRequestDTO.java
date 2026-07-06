package com.mcb.billing.customer.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Input DTO for creating a customer (the JSON body of {@code POST /customers}).
 *
 * <p>Deliberately contains only client-provided fields. Server-managed fields
 * ({@code id}, {@code customerNumber}, {@code active}, timestamps) are not part
 * of the request. The Bean Validation annotations are checked by the controller
 * via {@code @Valid} before the request is mapped to an entity.
 */
public record CreateCustomerRequestDTO(

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
