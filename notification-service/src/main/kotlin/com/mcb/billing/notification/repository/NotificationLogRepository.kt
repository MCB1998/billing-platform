package com.mcb.billing.notification.repository

import com.mcb.billing.notification.domain.NotificationLog
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationLogRepository : JpaRepository<NotificationLog, Long> {

    /** Idempotency check: has this event already been processed? */
    fun existsByEventId(eventId: String): Boolean

    /** All notifications, newest first. */
    fun findAllByOrderByCreatedAtDesc(): List<NotificationLog>
}
