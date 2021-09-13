package org.bk.notification.service

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
import org.bk.notification.model.ChatMessage
import org.bk.notification.toMono
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

@Repository
class FirestoreChatMessageRepository(
    private val firestore: Firestore
) : ChatMessageRepository {
    override fun save(chatMessage: ChatMessage): Mono<ChatMessage> {
        return Mono.defer {
            val documentReference: DocumentReference = firestore.collection(CHANNELS)
                .document(chatMessage.chatRoomId)
                .collection(NOTIFICATIONS)
                .document(chatMessage.id)
            val result: ApiFuture<WriteResult> = documentReference.set(entityFrom(chatMessage))
            result.toMono().map { chatMessage }
        }
    }

    override fun getLatestSavedChatMessages(count: Int, channelId: String, latestFirst: Boolean): Flux<ChatMessage> {
        return Flux.defer {
            val notificationCollectionRef: CollectionReference =
                firestore.collection(CHANNELS).document(channelId).collection(NOTIFICATIONS)
            val query: Query =
                notificationCollectionRef.orderBy(CREATION_DATE, Query.Direction.DESCENDING).limit(count)
            val result: ApiFuture<QuerySnapshot> = query.get()

            result.toMono()
                .flatMapIterable { querySnapshot: QuerySnapshot ->
                    val docs: List<QueryDocumentSnapshot> = querySnapshot.documents
                    if (latestFirst) docs else docs.reversed()
                }
                .map { queryDocumentSnapshot -> toChatMessage(channelId, queryDocumentSnapshot) }
        }
    }

    private fun toChatMessage(channelId: String, documentSnapshot: DocumentSnapshot): ChatMessage {
        val timestamp: Instant = documentSnapshot.getTimestamp(CREATION_DATE)
            ?.let { ts -> Instant.ofEpochSecond(ts.seconds, ts.nanos.toLong()) }
            ?: Instant.now()
        return ChatMessage(
            id = documentSnapshot.id,
            creationDate = timestamp,
            chatRoomId = channelId,
            payload = documentSnapshot.getString(PAYLOAD) ?: ""
        )
    }

    private fun entityFrom(chatMessage: ChatMessage): Map<String, Any> {
        return mapOf(
            CREATION_DATE to Timestamp.ofTimeSecondsAndNanos(
                chatMessage.creationDate.epochSecond,
                chatMessage.creationDate.nano
            ),
            PAYLOAD to chatMessage.payload
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