package com.mcb.billing.invoice.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * Input DTO for creating an invoice (body of {@code POST /invoices}).
 *
 * <p>{@code @Valid} on the list cascades validation into each item; {@code @NotEmpty}
 * rejects an invoice without line items. Server-managed fields (invoiceNumber, status,
 * totalAmount, timestamps) are not part of the request.
 */
public record CreateInvoiceRequestDTO(

        @NotBlank
        @Size(max = 50)
        String customerNumber,

        @Size(max = 3)
        String currency,           // optional; defaults to EUR

        @NotNull
        @FutureOrPresent
        LocalDate dueDate,

        @NotEmpty
        @Valid
        List<CreateInvoiceItemDTO> items
) {
}
