package org.bk.notification.service

import org.bk.notification.model.Notification
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Responsible for persisting old notifications to a persistent store
 */
interface NotificationPersister {
    /**
     * Save a notification
     */
    fun save(notification: Notification): Mono<Notification>

    /**
     * Get most recent notifications
     *
     * @param count of recent notifications
     */
    fun getLatestSavedNotifications(count: Int = 25, channelId: String, latestFirst:Boolean = true): Flux<Notification>
}