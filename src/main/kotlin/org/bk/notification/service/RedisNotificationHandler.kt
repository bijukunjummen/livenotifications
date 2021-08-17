package org.bk.notification.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.bk.notification.model.Notification
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class RedisNotificationHandler(
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val objectMapper: ObjectMapper
) : NotificationHandler {
    override fun saveNotification(channelId: String, notification: Notification): Mono<Notification> {
        return redisTemplate.convertAndSend(channelId, objectMapper.writeValueAsString(notification))
            .thenReturn(notification)
    }

    override fun getNotifications(channelId: String): Flux<Notification> {
        return redisTemplate.listenToChannel(channelId)
            .map { message ->
                val notification: Notification = objectMapper.readValue(message.message)
                notification
            }
    }
}