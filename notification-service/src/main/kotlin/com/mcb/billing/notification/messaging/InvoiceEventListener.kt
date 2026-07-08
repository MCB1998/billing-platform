package com.mcb.billing.notification.messaging

import com.mcb.billing.notification.service.NotificationService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

/**
 * Consumes InvoiceIssued events from the bound RabbitMQ queue. The JSON body is
 * deserialized into {@link InvoiceIssuedEvent} by the configured message converter.
 */
@Component
class InvoiceEventListener(
    private val notificationService: NotificationService,
) {

    @RabbitListener(queues = ["\${billing.messaging.invoice-notifications-queue}"])
    fun onInvoiceIssued(event: InvoiceIssuedEvent) {
        notificationService.handleInvoiceIssued(event)
    }
}
