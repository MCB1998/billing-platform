package com.mcb.billing.invoice.web.dto;

import com.mcb.billing.invoice.domain.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Output DTO representing an invoice as exposed by the API. */
public record InvoiceResponseDTO(
        String invoiceNumber,
        String customerNumber,
        InvoiceStatus status,
        String currency,
        BigDecimal totalAmount,
        LocalDate issueDate,
        LocalDate dueDate,
        List<InvoiceItemResponseDTO> items,
        Instant createdAt,
        Instant updatedAt
) {
}
