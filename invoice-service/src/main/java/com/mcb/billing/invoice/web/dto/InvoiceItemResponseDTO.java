package com.mcb.billing.invoice.web.dto;

import java.math.BigDecimal;

/** Output DTO for a single invoice line. */
public record InvoiceItemResponseDTO(
        String description,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
