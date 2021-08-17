package org.bk.notification.service

import org.bk.notification.model.Notification
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface NotificationHandler {
    fun saveNotification(channelId: String, notification: Notification): Mono<Notification>
    fun getNotifications(channelId: String): Flux<Notification>
}