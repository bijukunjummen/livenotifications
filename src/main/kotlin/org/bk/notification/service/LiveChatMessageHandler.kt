package org.bk.notification.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.bk.notification.exception.ChatRoomNotFoundException
import org.bk.notification.model.ChatMessage
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class LiveChatMessageHandler(
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val objectMapper: ObjectMapper
) : ChatMessageHandler {
    override fun saveChatMessage(chatMessage: ChatMessage): Mono<ChatMessage> {
        val roomId = chatMessage.chatRoomId
        return chatRoomRepository.getRoom(roomId)
            .switchIfEmpty { Mono.error(ChatRoomNotFoundException("Chat room $roomId missing")) }
            .flatMap {
                Mono.zip(
                    //Save to redis AND firestore and return..
                    redisTemplate.convertAndSend(
                        chatMessage.chatRoomId,
                        objectMapper.writeValueAsString(chatMessage)
                    ),
                    chatMessageRepository.save(chatMessage)
                )
                    .thenReturn(chatMessage)
            }
    }

    override fun getChatMessages(chatRoomId: String): Flux<ChatMessage> {
        return chatRoomRepository.getRoom(chatRoomId)
            .switchIfEmpty { Mono.error(ChatRoomNotFoundException("Chat room $chatRoomId missing")) }
            .flatMapMany {
                redisTemplate.listenToChannel(chatRoomId)
                    .map { message ->
                        val chatMessage: ChatMessage = objectMapper.readValue(message.message)
                        chatMessage
                    }
            }
    }

    override fun getOldChatMessages(chatRoomId: String): Flux<ChatMessage> {
        return chatMessageRepository.getLatestSavedChatMessages(chatRoomId = chatRoomId)
    }
}