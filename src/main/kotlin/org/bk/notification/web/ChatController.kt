package org.bk.notification.web

import org.bk.notification.model.ChatMessage
import org.bk.notification.service.ChatMessageHandler
import org.bk.notification.web.model.ChatMessageRequest
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/messages")
class ChatController(private val chatMessageHandler: ChatMessageHandler) {

    @PostMapping("/{chatRoomId}")
    fun addMessagesToRoom(
        @PathVariable("chatRoomId") chatRoomId: String,
        @RequestBody request: ChatMessageRequest
    ): Mono<ChatMessage> {
        return chatMessageHandler.saveChatMessage(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                chatRoomId = chatRoomId,
                creationDate = Instant.now(),
                payload = request.payload
            )
        )
    }

    @GetMapping(path = ["/{channelId}"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getNotifications(@PathVariable("channelId") channelId: String): Flux<ServerSentEvent<ChatMessage>> {
        return Flux.concat(
            chatMessageHandler.getOldChatMessages(channelId),
            chatMessageHandler.getChatMessages(channelId)
        ).map { chatMessage -> ServerSentEvent.builder<ChatMessage>().data(chatMessage).build() }
    }
}