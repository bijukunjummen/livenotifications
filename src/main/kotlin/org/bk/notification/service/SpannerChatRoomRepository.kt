package org.bk.notification.service

import com.google.cloud.spanner.DatabaseClient
import com.google.cloud.spanner.Statement
import org.bk.notification.model.ChatRoom
import org.bk.notification.toMono
import reactor.core.publisher.Mono

class SpannerChatRoomRepository(private val databaseClient: DatabaseClient) : ChatRoomRepository {
    override fun save(chatRoom: ChatRoom): Mono<ChatRoom> {
        return Mono.defer {
            databaseClient.readWriteTransaction()
                .run { transaction ->
                    val sql = "INSERT INTO $CHAT_ROOM_TABLE ($CHAT_ROOM_ID, $CHAT_ROOM_NAME) VALUES(@id, @roomName)"
                    val statement = Statement.newBuilder(sql)
                        .bind("id")
                        .to(chatRoom.id)
                        .bind("roomName")
                        .to(chatRoom.name)
                        .build()
                    transaction
                        .executeUpdateAsync(statement)
                        .toMono()
                        .thenReturn(chatRoom)
                }
        }
    }

    override fun getRoom(chatRoomId: String): Mono<ChatRoom> {
        return Mono.defer {
            val selectQuery = """
                SELECT $CHAT_ROOM_ID, $CHAT_ROOM_NAME FROM $CHAT_ROOM_TABLE WHERE $CHAT_ROOM_ID=@id 
            """.trimIndent()

            val statement = Statement.newBuilder(selectQuery)
                .bind("id")
                .to(chatRoomId)
                .build()
            val chatRoom: ChatRoom? = databaseClient.singleUse()
                .executeQuery(statement).use { resultSet ->
                    if (resultSet.next()) {
                        val id = resultSet.getString(CHAT_ROOM_ID)
                        val name = resultSet.getString(CHAT_ROOM_NAME)
                        ChatRoom(id, name)
                    } else {
                        null
                    }
                }
            if (chatRoom != null) {
                Mono.just(chatRoom)
            } else {
                Mono.empty()
            }
        }
    }

    companion object {
        private const val CHAT_ROOM_ID = "ChatRoomId"
        private const val CHAT_ROOM_NAME = "ChatRoomName"
        private const val CHAT_ROOM_TABLE = "ChatRooms"
    }
}