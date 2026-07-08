package com.mcb.billing.invoice.messaging;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Event published when an invoice is issued. {@code eventId} is a fresh UUID per
 * publish so consumers can process it idempotently (deduplicate redeliveries).
 */
public record InvoiceIssuedEvent(
        String eventId,
        String invoiceNumber,
        String customerNumber,
        BigDecimal totalAmount,
        String currency,
        LocalDate dueDate
) {
}
