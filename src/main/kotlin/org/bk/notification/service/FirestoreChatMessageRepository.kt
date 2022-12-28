package org.bk.notification.service

import com.google.api.core.ApiFuture
import com.google.cloud.Timestamp
import com.google.cloud.firestore.*
import org.bk.notification.extensions.loggerFor
import org.bk.notification.model.ChatMessage
import org.bk.notification.model.Page
import org.bk.notification.service.ServiceConstants.CHAT_ROOMS
import org.bk.notification.service.ServiceConstants.NOTIFICATIONS
import org.bk.notification.toMono
import org.slf4j.Logger
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

class FirestoreChatMessageRepository(
    private val firestore: Firestore
) : ChatMessageRepository {
    override fun save(chatMessage: ChatMessage): Mono<ChatMessage> {
        return Mono.defer {
            val documentReference: DocumentReference = firestore.collection(CHAT_ROOMS)
                .document(chatMessage.chatRoomId)
                .collection(NOTIFICATIONS)
                .document(chatMessage.id)
            val result: ApiFuture<WriteResult> = documentReference.set(entityFrom(chatMessage))
            result.toMono().map { chatMessage }
        }
    }

    override fun getLatestSavedChatMessages(count: Long, chatRoomId: String, latestFirst: Boolean): Flux<ChatMessage> {
        return Flux.defer {
            val notificationCollectionRef: CollectionReference =
                firestore.collection(CHAT_ROOMS).document(chatRoomId).collection(NOTIFICATIONS)
            val query: Query =
                notificationCollectionRef.orderBy(CREATION_DATE, Query.Direction.DESCENDING).limit(count.toInt())
            val result: ApiFuture<QuerySnapshot> = query.get()

            result.toMono()
                .flatMapIterable { querySnapshot: QuerySnapshot ->
                    val docs: List<QueryDocumentSnapshot> = querySnapshot.documents
                    if (latestFirst) docs else docs.reversed()
                }
                .map { queryDocumentSnapshot -> toChatMessage(chatRoomId, queryDocumentSnapshot) }
        }
    }

    override fun getPaginatedMessages(chatRoomId: String, from: String, count: Long): Page<ChatMessage> {
        TODO("Not yet implemented")
    }

    override fun deleteChatMessage(chatRoomId: String, chatMessageId: String): Mono<Boolean> {
        val notificationCollectionRef: CollectionReference =
            firestore.collection(CHAT_ROOMS)
                .document(chatRoomId)
                .collection(NOTIFICATIONS)
        val chatMessageDocumentRef: DocumentReference = notificationCollectionRef.document(chatMessageId)
        val chatMessageSnapshotFuture: ApiFuture<DocumentSnapshot> = chatMessageDocumentRef.get()
        return chatMessageSnapshotFuture.toMono()
            .flatMap { document: DocumentSnapshot ->
                if (document.exists()) {
                    document
                        .reference
                        .delete()
                        .toMono()
                        .map { writeResult ->
                            LOGGER.info(
                                "Deleted chatRoomId: {}, chatMessageId: {} at time: {}",
                                chatRoomId, chatMessageId, writeResult.updateTime
                            )
                            true
                        }
                } else {
                    Mono.just(false)
                }
            }
    }

    private fun toChatMessage(chatRoomId: String, documentSnapshot: DocumentSnapshot): ChatMessage {
        val timestamp: Instant = documentSnapshot.getTimestamp(CREATION_DATE)
            ?.let { ts -> Instant.ofEpochSecond(ts.seconds, ts.nanos.toLong()) }
            ?: Instant.now()
        return ChatMessage(
            id = documentSnapshot.id,
            creationDate = timestamp,
            chatRoomId = chatRoomId,
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
        private const val CREATION_DATE = "creationDate"
        private const val PAYLOAD = "payload"
        private val LOGGER: Logger = loggerFor<FirestoreChatRoomRepository>()
    }
}