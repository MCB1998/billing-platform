package com.mcb.billing.notification.service

import com.mcb.billing.notification.domain.NotificationChannel
import com.mcb.billing.notification.domain.NotificationStatus
import com.mcb.billing.notification.messaging.InvoiceIssuedEvent
import com.mcb.billing.notification.repository.NotificationLogRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Unit test for the idempotent-consumer logic. The repository is mocked, so this
 * runs without Spring or a database - it only asserts the service's decisions.
 */
class NotificationServiceTest {

    private val repository = mock<NotificationLogRepository>()
    private val service = NotificationService(repository)

    private fun sampleEvent() = InvoiceIssuedEvent(
        eventId = "evt-123",
        invoiceNumber = "INV-00001",
        customerNumber = "C-00001",
        totalAmount = BigDecimal("199.90"),
        currency = "EUR",
        dueDate = LocalDate.of(2026, 8, 1),
    )

    @Test
    fun `records a notification for a new event`() {
        val event = sampleEvent()
        whenever(repository.existsByEventId(event.eventId)).thenReturn(false)

        service.handleInvoiceIssued(event)

        val captor = argumentCaptor<com.mcb.billing.notification.domain.NotificationLog>()
        verify(repository).save(captor.capture())
        val saved = captor.firstValue
        assertThat(saved.eventId).isEqualTo("evt-123")
        assertThat(saved.eventType).isEqualTo("INVOICE_ISSUED")
        assertThat(saved.invoiceNumber).isEqualTo("INV-00001")
        assertThat(saved.customerNumber).isEqualTo("C-00001")
        assertThat(saved.channel).isEqualTo(NotificationChannel.EMAIL)
        assertThat(saved.status).isEqualTo(NotificationStatus.SENT)
        assertThat(saved.message).isEqualTo(
            "Invoice INV-00001 over 199.90 EUR was issued and is due 2026-08-01.",
        )
    }

    @Test
    fun `skips a duplicate event and never saves`() {
        val event = sampleEvent()
        whenever(repository.existsByEventId(event.eventId)).thenReturn(true)

        service.handleInvoiceIssued(event)

        verify(repository, never()).save(any())
    }
}
