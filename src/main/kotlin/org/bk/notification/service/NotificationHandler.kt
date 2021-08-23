package org.bk.notification.service

import org.bk.notification.model.Notification
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface NotificationHandler {
    fun saveNotification(notification: Notification): Mono<Notification>
    fun getNotifications(channelId: String): Flux<Notification>
    fun getOldNotifications(channelId: String): Flux<Notification>
}