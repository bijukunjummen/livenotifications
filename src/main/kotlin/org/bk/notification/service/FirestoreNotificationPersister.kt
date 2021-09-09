package org.bk.notification.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.core.ApiFuture
import com.google.cloud.Timestamp
import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Query
import com.google.cloud.firestore.QueryDocumentSnapshot
import com.google.cloud.firestore.QuerySnapshot
import com.google.cloud.firestore.WriteResult
import org.bk.notification.model.Notification
import org.bk.notification.toMono
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

@Repository
class FirestoreNotificationPersister(
    private val firestore: Firestore,
    private val objectMapper: ObjectMapper
) : NotificationPersister {
    override fun save(notification: Notification): Mono<Notification> {
        val documentReference: DocumentReference = firestore.collection(CHANNELS)
            .document(notification.channelId)
            .collection(NOTIFICATIONS)
            .document(notification.id)
        val result: ApiFuture<WriteResult> = documentReference.set(entityFrom(notification))
        return result.toMono().map { result -> notification }
    }

    override fun getLatestSavedNotifications(count: Int, channelId: String, latestFirst: Boolean): Flux<Notification> {
        val notificationCollectionRef: CollectionReference =
            firestore.collection(CHANNELS).document(channelId).collection(NOTIFICATIONS)
        val query: Query = notificationCollectionRef.orderBy(CREATION_DATE, Query.Direction.DESCENDING).limit(count)
        val result: ApiFuture<QuerySnapshot> = query.get()

        return result.toMono()
            .flatMapIterable { querySnapshot: QuerySnapshot ->
                val docs: List<QueryDocumentSnapshot> = querySnapshot.documents
                if (latestFirst) docs else docs.reversed()
            }
            .map { queryDocumentSnapshot -> toNotification(channelId, queryDocumentSnapshot) }
    }

    private fun toNotification(channelId: String, documentSnapshot: DocumentSnapshot): Notification {
        val timestamp: Instant = documentSnapshot.getTimestamp(CREATION_DATE)
            ?.let { ts -> Instant.ofEpochSecond(ts.seconds, ts.nanos.toLong()) }
            ?: Instant.now()
        return Notification(
            id = documentSnapshot.id,
            creationDate = timestamp,
            channelId = channelId,
            payload = objectMapper.readTree(documentSnapshot.getString(PAYLOAD))
        )
    }

    private fun entityFrom(notification: Notification): Map<String, Any> {
        return mapOf(
            CREATION_DATE to Timestamp.ofTimeSecondsAndNanos(
                notification.creationDate.epochSecond,
                notification.creationDate.nano
            ),
            PAYLOAD to objectMapper.writeValueAsString(notification.payload)
        )
    }

    companion object {
        private const val CHANNELS = "notification_channels"
        private const val NOTIFICATIONS = "notifications"
        private const val ID = "id"
        private const val CREATION_DATE = "creationDate"
        private const val CHANNEL_ID = "channelId"
        private const val PAYLOAD = "payload"
    }
}