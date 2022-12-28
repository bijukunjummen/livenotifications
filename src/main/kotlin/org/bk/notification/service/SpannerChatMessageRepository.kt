package org.bk.notification.service

import com.google.cloud.Timestamp
import com.google.cloud.spanner.DatabaseClient
import com.google.cloud.spanner.ResultSet
import com.google.cloud.spanner.Statement
import org.bk.notification.model.ChatMessage
import org.bk.notification.model.Page
import org.bk.notification.toMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

class SpannerChatMessageRepository(private val databaseClient: DatabaseClient) : ChatMessageRepository {
    override fun save(chatMessage: ChatMessage): Mono<ChatMessage> {
        return Mono.defer {
            databaseClient.readWriteTransaction()
                .run { transaction ->
                    val sql = """
                            INSERT INTO $CHAT_MESSAGES_TABLE
                            ($CHAT_MESSAGE_ID, $CHAT_ROOM_ID, $PAYLOAD, $CREATION_DATE)
                            VALUES
                            (@chatMessageId, @chatRoomId, @payload, @creationDate)
                        """.trimIndent()

                    val statement = Statement.newBuilder(sql)
                        .bind("chatMessageId")
                        .to(chatMessage.id)
                        .bind("chatRoomId")
                        .to(chatMessage.chatRoomId)
                        .bind("payload")
                        .to(chatMessage.payload)
                        .bind("creationDate")
                        .to(
                            Timestamp.ofTimeSecondsAndNanos(
                                chatMessage.creationDate.epochSecond,
                                chatMessage.creationDate.nano
                            )
                        )
                        .build()
                    transaction
                        .executeUpdateAsync(statement)
                        .toMono()
                        .thenReturn(chatMessage)
                }
        }
    }

    override fun getLatestSavedChatMessages(count: Long, chatRoomId: String, latestFirst: Boolean): Flux<ChatMessage> {
        val selectQuery = """
            SELECT $CHAT_MESSAGE_ID, $CHAT_ROOM_ID, $PAYLOAD, $CREATION_DATE 
            FROM $CHAT_MESSAGES_TABLE WHERE $CHAT_ROOM_ID=@chatRoomId
            ORDER BY $CREATION_DATE DESC            
            LIMIT $count
        """.trimIndent()
        val statement = Statement.newBuilder(selectQuery)
            .bind("chatRoomId")
            .to(chatRoomId)
            .build()
        return Mono.just(
            databaseClient.singleUse().executeQuery(statement)
        )
            .flatMapIterable { resultSet ->
                val list = mutableListOf<ChatMessage>()
                while (resultSet.next()) {
                    val chatMessageId = resultSet.getString(CHAT_MESSAGE_ID)
                    val roomId = resultSet.getString(CHAT_ROOM_ID)
                    val creationDate: Timestamp = resultSet.getTimestamp(CREATION_DATE)
                    val creationDateInstant: Instant = Instant
                        .ofEpochSecond(creationDate.seconds, creationDate.nanos.toLong())
                    val payload = resultSet.getString(PAYLOAD)
                    list.add(ChatMessage(chatMessageId, creationDateInstant, roomId, payload))
                }
                if (latestFirst) list else list.reversed()
            }
    }

    override fun getPaginatedMessages(chatRoomId: String, offset: String, count: Long): Page<ChatMessage> {
        val offset = if (offset.isNotEmpty()) offset.toLong() else 0L

        val selectQuery = """
            SELECT $CHAT_MESSAGE_ID, $CHAT_ROOM_ID, $PAYLOAD, $CREATION_DATE 
            FROM $CHAT_MESSAGES_TABLE WHERE $CHAT_ROOM_ID=@chatRoomId
            ORDER BY $CREATION_DATE DESC            
            LIMIT $count
            OFFSET $offset
        """.trimIndent()
        val statement = Statement.newBuilder(selectQuery)
            .bind("chatRoomId")
            .to(chatRoomId)
            .build()
        val resultSet: ResultSet = databaseClient.singleUse().executeQuery(statement)
        val list = mutableListOf<ChatMessage>()
        var newOffset = offset
        while (resultSet.next()) {
            val chatMessageId = resultSet.getString(CHAT_MESSAGE_ID)
            val roomId = resultSet.getString(CHAT_ROOM_ID)
            val creationDate: Timestamp = resultSet.getTimestamp(CREATION_DATE)
            val creationDateInstant: Instant = Instant
                .ofEpochSecond(creationDate.seconds, creationDate.nanos.toLong())
            val payload = resultSet.getString(PAYLOAD)
            list.add(ChatMessage(chatMessageId, creationDateInstant, roomId, payload))
            newOffset += 1
        }
        return Page(list, newOffset.toString())
    }

    override fun deleteChatMessage(chatRoomId: String, chatMessageId: String): Mono<Boolean> {
        TODO("Not yet implemented")
    }

    companion object {
        private const val CHAT_MESSAGES_TABLE = "ChatRoomMessages"
        private const val CHAT_MESSAGE_ID = "ChatMessageId"
        private const val CHAT_ROOM_ID = "ChatRoomId"
        private const val CREATION_DATE = "CreationDate"
        private const val PAYLOAD = "Payload"
    }
}