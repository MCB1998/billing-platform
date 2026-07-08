package com.mcb.billing.invoice.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Input DTO for a single line of a new invoice. */
public record CreateInvoiceItemDTO(

        @NotBlank
        @Size(max = 300)
        String description,

        @Positive
        int quantity,

        @NotNull
        @PositiveOrZero
        BigDecimal unitPrice
) {
}
