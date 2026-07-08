package com.mcb.billing.notification.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Declares the RabbitMQ topology. Spring AMQP's RabbitAdmin auto-declares these
 * beans in the broker on startup: a topic exchange, a durable queue and a binding
 * that routes "invoice.issued" messages into the queue.
 */
@Configuration
class RabbitConfig(
    @Value("\${billing.messaging.exchange}") private val exchangeName: String,
    @Value("\${billing.messaging.invoice-notifications-queue}") private val queueName: String,
    @Value("\${billing.messaging.invoice-issued-routing-key}") private val routingKey: String,
) {

    @Bean
    fun billingEventsExchange(): TopicExchange = TopicExchange(exchangeName)

    /** Durable: the queue (and its persistent messages) survives a broker restart. */
    @Bean
    fun invoiceNotificationsQueue(): Queue = QueueBuilder.durable(queueName).build()

    @Bean
    fun invoiceIssuedBinding(queue: Queue, exchange: TopicExchange): Binding =
        BindingBuilder.bind(queue).to(exchange).with(routingKey)

    /**
     * Serialize/deserialize message bodies as JSON. Uses Spring Boot's configured
     * ObjectMapper (which has the Kotlin and JavaTime modules registered), so Kotlin
     * data classes and types like LocalDate/BigDecimal deserialize correctly.
     */
    @Bean
    fun jsonMessageConverter(objectMapper: ObjectMapper): MessageConverter =
        Jackson2JsonMessageConverter(objectMapper)
}
