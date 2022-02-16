package org.bk.notification.service

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.WriteResult
import org.bk.notification.model.ChatRoom
import org.bk.notification.toMono
import reactor.core.publisher.Mono

class FirestoreChatRoomRepository(
        private val firestore: Firestore
) : ChatRoomRepository {
    override fun save(chatRoom: ChatRoom): Mono<ChatRoom> {
        return Mono.defer {
            val documentReference: DocumentReference =
                    firestore.collection(ServiceConstants.CHAT_ROOMS).document(chatRoom.id)

            val result: ApiFuture<WriteResult> = documentReference.set(mapOf(NAME to chatRoom.name))
            result.toMono().map { chatRoom }
        }
    }

    override fun getRoom(chatRoomId: String): Mono<ChatRoom> {
        return Mono.defer {
            val chatRoomRef: DocumentReference =
                    firestore.collection(ServiceConstants.CHAT_ROOMS).document(chatRoomId)
            val result: ApiFuture<DocumentSnapshot> = chatRoomRef.get()

            result.toMono()
                    .flatMap { queryDocumentSnapshot ->
                        if (queryDocumentSnapshot.exists()) {
                            Mono.just(
                                    ChatRoom(
                                            id = queryDocumentSnapshot.id,
                                            name = queryDocumentSnapshot.getString(NAME) ?: ""
                                    )
                            )
                        } else {
                            Mono.empty()
                        }
                    }
        }
    }

    companion object {
        private const val NAME = "name"
    }
}