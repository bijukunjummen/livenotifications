package org.bk.notification.web

import org.bk.notification.model.Notification
import org.bk.notification.model.NotificationRequest
import org.bk.notification.service.NotificationHandler
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/notifications")
class NotificationController(private val notificationHandler: NotificationHandler) {

    @PostMapping("/{channelId}")
    fun addNotifications(
        @PathVariable("channelId") channelId: String,
        @RequestBody request: NotificationRequest
    ): Mono<Notification> {
        return notificationHandler.saveNotification(
            channelId,
            Notification(id = UUID.randomUUID().toString(), creationDate = Instant.now(), payload = request.payload)
        )
    }

    @GetMapping("/{channelId}")
    fun getNotifications(@PathVariable("channelId") channelId: String): Flux<ServerSentEvent<Notification>> {
        return notificationHandler.getNotifications(channelId)
            .map { notification -> ServerSentEvent.builder<Notification>().data(notification).build() }
    }
}