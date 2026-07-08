package com.mcb.billing.notification.messaging

import java.math.BigDecimal
import java.time.LocalDate

/**
 * The notification-service's own view of the InvoiceIssued event. Each service
 * defines its own copy of the contract (no shared code); unknown JSON fields are
 * ignored. {@code eventId} drives idempotent processing.
 */
data class InvoiceIssuedEvent(
    val eventId: String,
    val invoiceNumber: String,
    val customerNumber: String,
    val totalAmount: BigDecimal,
    val currency: String,
    val dueDate: LocalDate,
)
