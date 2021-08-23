package org.bk.notification.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.datastore.*
import org.bk.notification.model.Notification
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import kotlin.streams.asStream

@Repository
class DatastoreNotificationPersister(
    private val datastore: Datastore,
    private val objectMapper: ObjectMapper
) : NotificationPersister {
    override fun save(notification: Notification): Mono<Notification> {
        return Mono.fromSupplier {
            val entity: Entity = entityFromNotification(notification)
            datastore.put(entity)
            notification
        }
    }

    override fun getOldNotifications(count: Int, channelId: String): Flux<Notification> {
        return Mono
            .fromSupplier {
                val query: EntityQuery = Query.newEntityQueryBuilder().setKind(KIND)
                    .setFilter(StructuredQuery.PropertyFilter.eq("channelId", channelId))
                    .setOrderBy(StructuredQuery.OrderBy.desc("creationDate"))
                    .setLimit(count)
                    .build()
                val results: QueryResults<Entity> = datastore.run(query)
                results.asSequence().map { entity -> toNotification(entity) }.toList().reversed()
            }
            .flatMapIterable { seq -> seq }
    }

    private fun toNotification(entity: Entity): Notification {
        return Notification(
            id = entity.key.name,
            creationDate = Instant.ofEpochMilli(entity.getLong("creationDate")),
            channelId = entity.getString("channelId"),
            payload = objectMapper.readTree(entity.getString("payload"))
        )
    }

    private fun entityFromNotification(notification: Notification): Entity {
        val key: Key = datastore.newKeyFactory().setKind(KIND).newKey(notification.id)
        val payload: String = objectMapper.writeValueAsString(notification.payload)
        return Entity.newBuilder(key)
            .set("channelId", notification.channelId)
            .set("creationDate", notification.creationDate.toEpochMilli())
            .set("payload", payload)
            .build()
    }

    companion object {
        private const val KIND = "notifications"
    }
}