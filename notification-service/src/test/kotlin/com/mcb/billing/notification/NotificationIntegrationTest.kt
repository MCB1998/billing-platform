package com.mcb.billing.notification

import com.mcb.billing.notification.messaging.InvoiceIssuedEvent
import com.mcb.billing.notification.repository.NotificationLogRepository
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDate

/**
 * End-to-end integration test against a REAL PostgreSQL and a REAL RabbitMQ broker
 * (both via Testcontainers). It exercises the whole async path: publish an event to
 * the exchange -> the @RabbitListener consumes it -> the service records a row.
 *
 * The 'postgres' profile makes Flyway apply V1 and Hibernate validate the schema.
 * Both containers are wired in via @ServiceConnection (datasource + spring.rabbitmq.*).
 *
 * Note: with disabledWithoutDocker = true this is SKIPPED when Testcontainers can't
 * find Docker (e.g. a Windows host without the default socket); it runs in CI (Linux).
 */
@SpringBootTest
@ActiveProfiles("postgres")
@Testcontainers(disabledWithoutDocker = true)
class NotificationIntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine")

        @Container
        @ServiceConnection
        @JvmStatic
        val rabbit = RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"))
    }

    @Autowired
    lateinit var rabbitTemplate: RabbitTemplate

    @Autowired
    lateinit var repository: NotificationLogRepository

    @Value("\${billing.messaging.exchange}")
    lateinit var exchange: String

    @Value("\${billing.messaging.invoice-issued-routing-key}")
    lateinit var routingKey: String

    private fun event(eventId: String) = InvoiceIssuedEvent(
        eventId = eventId,
        invoiceNumber = "INV-00042",
        customerNumber = "C-00007",
        totalAmount = BigDecimal("50.00"),
        currency = "EUR",
        dueDate = LocalDate.of(2026, 9, 1),
    )

    @Test
    fun `consumes a published event and records a notification in real postgres`() {
        rabbitTemplate.convertAndSend(exchange, routingKey, event("evt-it-new"))

        await().atMost(Duration.ofSeconds(10)).untilAsserted {
            val recorded = repository.findAll().singleOrNull { it.eventId == "evt-it-new" }
            assertThat(recorded).isNotNull
            assertThat(recorded!!.invoiceNumber).isEqualTo("INV-00042")
            assertThat(recorded.customerNumber).isEqualTo("C-00007")
            assertThat(recorded.message).contains("INV-00042", "50.00 EUR")
        }
    }

    @Test
    fun `processes a duplicate delivery only once`() {
        // Same eventId twice - the idempotent consumer must record it a single time.
        rabbitTemplate.convertAndSend(exchange, routingKey, event("evt-it-dup"))
        rabbitTemplate.convertAndSend(exchange, routingKey, event("evt-it-dup"))

        await().atMost(Duration.ofSeconds(10)).untilAsserted {
            assertThat(repository.findAll().count { it.eventId == "evt-it-dup" }).isEqualTo(1)
        }
    }
}
