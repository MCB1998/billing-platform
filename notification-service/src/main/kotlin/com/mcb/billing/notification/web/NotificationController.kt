package com.mcb.billing.notification.web

import com.mcb.billing.notification.service.NotificationService
import com.mcb.billing.notification.web.dto.NotificationResponseDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** REST endpoint to view the notifications that were recorded from events. */
@RestController
@RequestMapping("/notifications")
class NotificationController(
    private val notificationService: NotificationService,
) {

    @GetMapping
    fun list(): List<NotificationResponseDTO> = notificationService.listNotifications()
}
