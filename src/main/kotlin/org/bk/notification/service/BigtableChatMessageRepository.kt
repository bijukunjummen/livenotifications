package org.bk.notification.service

import com.google.api.core.ApiFuture
import com.google.api.gax.rpc.ServerStream
import com.google.cloud.bigtable.data.v2.BigtableDataClient
import com.google.cloud.bigtable.data.v2.models.Mutation
import com.google.cloud.bigtable.data.v2.models.Query
import com.google.cloud.bigtable.data.v2.models.Row
import com.google.cloud.bigtable.data.v2.models.RowMutation
import org.bk.notification.model.ChatMessage
import org.bk.notification.toMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

class BigtableChatMessageRepository(private val bigtableDataClient: BigtableDataClient) : ChatMessageRepository {
    override fun save(chatMessage: ChatMessage): Mono<ChatMessage> {
        val key = "MESSAGES/R#${chatMessage.chatRoomId}/M#${chatMessage.id}"
        val microEpoch = chatMessage.creationDate.toEpochMilli() * 1000
        val reversedEpoch = (FUTURE_2100_MICRO - microEpoch)
        val paddedReversedEpoch = reversedEpoch.toString().padStart(18, '0')
        val TS_KEY = "MESSAGES/R#${chatMessage.chatRoomId}/TS#$paddedReversedEpoch/M#${chatMessage.id}"
        val creationTs: Long = chatMessage.creationDate.toEpochMilli() * 1_000
        val mutation: Mutation = Mutation.create()
                .setCell(CHAT_MESSAGE_DETAILS_FAMILY, ID_QUALIFIER, chatMessage.id)
                .setCell(CHAT_MESSAGE_DETAILS_FAMILY, CHAT_ROOM_ID_QUALIFIER, chatMessage.chatRoomId)
                .setCell(CHAT_MESSAGE_DETAILS_FAMILY, CREATION_DATE_QUALIFIER, creationTs, chatMessage.creationDate.toString())
                .setCell(CHAT_MESSAGE_DETAILS_FAMILY, PAYLOAD_QUALIFIER, chatMessage.payload)

        val mutationTs: Mutation = Mutation.create()
                .setCell(CHAT_MESSAGE_DETAILS_FAMILY, ID_QUALIFIER, chatMessage.id)

        val rowMutation = RowMutation.create(TABLE_ID, key, mutation)
        val rowMutationReversedTime = RowMutation.create(TABLE_ID, TS_KEY, mutationTs)
        val result: ApiFuture<Void> = bigtableDataClient.mutateRowAsync(rowMutation)
        val resultReversedTime: ApiFuture<Void> = bigtableDataClient.mutateRowAsync(rowMutationReversedTime)
        return Mono.zipDelayError(result.toMono(), resultReversedTime.toMono())
                .thenReturn(chatMessage)
    }

    override fun getLatestSavedChatMessages(count: Long, chatRoomId: String, latestFirst: Boolean): Flux<ChatMessage> {
        return Flux.defer {
            val keyPrefix = "MESSAGES/R#${chatRoomId}/TS#"
            val query: Query = Query.create(TABLE_ID).limit(count).prefix(keyPrefix)
            val rows: ServerStream<Row> = bigtableDataClient.readRows(query)
            val list: List<ChatMessage> = rows.map { row ->
                val chatMessageId: String = row.getCells(CHAT_MESSAGE_DETAILS_FAMILY).get(0).value.toStringUtf8()
                val key = "MESSAGES/R#${chatRoomId}/M#${chatMessageId}"
                bigtableDataClient.readRow(TABLE_ID, key).let { row -> toChatMessage(row) }
            }.toList()

            if (latestFirst) {
                Flux.fromIterable(list)
            } else {
                Flux.fromIterable(list.reversed())
            }
        }
    }

    private fun toChatMessage(row: Row): ChatMessage {
        val id: String = row.getCells(CHAT_MESSAGE_DETAILS_FAMILY, ID_QUALIFIER).first().value.toStringUtf8()
        val chatRoomId: String = row.getCells(CHAT_MESSAGE_DETAILS_FAMILY, CHAT_ROOM_ID_QUALIFIER)
                .first()
                .value
                .toStringUtf8()
        val creationDateAsString: String = row.getCells(CHAT_MESSAGE_DETAILS_FAMILY, CREATION_DATE_QUALIFIER)
                .first()
                .value
                .toStringUtf8()

        val payload = row.getCells(CHAT_MESSAGE_DETAILS_FAMILY, PAYLOAD_QUALIFIER).first().value.toStringUtf8()
        return ChatMessage(
                id = id,
                creationDate = Instant.parse(creationDateAsString),
                chatRoomId = chatRoomId,
                payload = payload)
    }

    override fun deleteChatMessage(chatRoomId: String, chatMessageId: String): Mono<Boolean> {
        TODO("Not implemented")
    }

    companion object {
        private const val TABLE_ID = "chat_messages"
        private const val CHAT_MESSAGE_DETAILS_FAMILY = "chatMessageDetails"
        private const val CREATION_DATE_QUALIFIER = "creationDate"
        private const val ID_QUALIFIER = "id"
        private const val CHAT_ROOM_ID_QUALIFIER = "chatRoomId"
        private const val PAYLOAD_QUALIFIER = "payload"
        private val FUTURE_2100_MICRO = Instant.parse("2100-01-01T00:00:00.00Z").toEpochMilli() * 1000
    }
}