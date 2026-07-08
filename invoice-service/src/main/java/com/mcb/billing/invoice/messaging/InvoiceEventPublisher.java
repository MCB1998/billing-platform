package com.mcb.billing.invoice.messaging;

import com.mcb.billing.invoice.domain.Invoice;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Publishes invoice domain events to the topic exchange.
 *
 * <p>v1 publishes directly. This shares the "dual write" caveat (the DB commit and
 * the broker send are not atomic); a production system would use the transactional
 * outbox pattern. Left as a deliberate future improvement.
 */
@Component
public class InvoiceEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public InvoiceEventPublisher(RabbitTemplate rabbitTemplate,
                                 @Value("${billing.messaging.exchange}") String exchange,
                                 @Value("${billing.messaging.invoice-issued-routing-key}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publishInvoiceIssued(Invoice invoice) {
        InvoiceIssuedEvent event = new InvoiceIssuedEvent(
                UUID.randomUUID().toString(),
                invoice.getInvoiceNumber(),
                invoice.getCustomerNumber(),
                invoice.getTotalAmount(),
                invoice.getCurrency(),
                invoice.getDueDate());
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
