package com.mcb.billing.notification.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

/** Channel a notification would be sent through. */
enum class NotificationChannel { EMAIL }

/** Outcome of the notification (SENT is simulated - we don't really send yet). */
enum class NotificationStatus { SENT }

/**
 * A record that an event was received and a notification was created. Write-once,
 * so all fields are read-only (`val`). The Kotlin `jpa` compiler plugin gives this
 * class a no-arg constructor and opens it, as Hibernate requires.
 *
 * <p>{@code eventId} is unique - the idempotent-consumer guard against duplicate
 * (at-least-once) deliveries.
 */
@Entity
@Table(
    name = "notification_log",
    uniqueConstraints = [UniqueConstraint(name = "uk_notification_log_event_id", columnNames = ["event_id"])],
)
class NotificationLog(

    @Column(name = "event_id", nullable = false, unique = true, length = 100)
    val eventId: String,

    @Column(name = "event_type", nullable = false, length = 50)
    val eventType: String,

    @Column(name = "invoice_number", nullable = false, length = 50)
    val invoiceNumber: String,

    @Column(name = "customer_number", nullable = false, length = 50)
    val customerNumber: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val channel: NotificationChannel,

    @Column(nullable = false, length = 500)
    val message: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val status: NotificationStatus,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null
}
