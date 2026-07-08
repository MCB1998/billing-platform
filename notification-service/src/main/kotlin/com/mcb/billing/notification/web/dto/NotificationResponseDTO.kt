package com.mcb.billing.notification.web.dto

import java.time.Instant

/** Output DTO for a recorded notification. */
data class NotificationResponseDTO(
    val eventType: String,
    val invoiceNumber: String,
    val customerNumber: String,
    val channel: String,
    val message: String,
    val status: String,
    val createdAt: Instant?,
)
