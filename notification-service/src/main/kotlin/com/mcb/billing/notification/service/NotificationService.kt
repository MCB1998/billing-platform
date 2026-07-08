package com.mcb.billing.notification.service

import com.mcb.billing.notification.domain.NotificationChannel
import com.mcb.billing.notification.domain.NotificationLog
import com.mcb.billing.notification.domain.NotificationStatus
import com.mcb.billing.notification.messaging.InvoiceIssuedEvent
import com.mcb.billing.notification.repository.NotificationLogRepository
import com.mcb.billing.notification.web.dto.NotificationResponseDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val repository: NotificationLogRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Processes an InvoiceIssued event: skips duplicates (idempotent consumer),
     * otherwise records a notification. The unique constraint on eventId is the
     * ultimate guard if a duplicate slips past the check under concurrency.
     */
    @Transactional
    fun handleInvoiceIssued(event: InvoiceIssuedEvent) {
        if (repository.existsByEventId(event.eventId)) {
            log.info("Event {} already processed - skipping duplicate", event.eventId)
            return
        }

        val message = "Invoice ${event.invoiceNumber} over ${event.totalAmount} ${event.currency} " +
            "was issued and is due ${event.dueDate}."

        repository.save(
            NotificationLog(
                eventId = event.eventId,
                eventType = "INVOICE_ISSUED",
                invoiceNumber = event.invoiceNumber,
                customerNumber = event.customerNumber,
                channel = NotificationChannel.EMAIL,
                message = message,
                status = NotificationStatus.SENT,
            ),
        )
        log.info("Recorded notification for invoice {} / customer {}", event.invoiceNumber, event.customerNumber)
    }

    @Transactional(readOnly = true)
    fun listNotifications(): List<NotificationResponseDTO> =
        repository.findAllByOrderByCreatedAtDesc().map { it.toResponse() }

    private fun NotificationLog.toResponse() = NotificationResponseDTO(
        eventType = eventType,
        invoiceNumber = invoiceNumber,
        customerNumber = customerNumber,
        channel = channel.name,
        message = message,
        status = status.name,
        createdAt = createdAt,
    )
}
