package org.bk.notification.service

import org.bk.notification.model.ChatMessage
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ChatMessageHandler {
    fun saveChatMessage(chatMessage: ChatMessage): Mono<ChatMessage>
    fun getChatMessages(channelId: String): Flux<ChatMessage>
    fun getOldChatMessages(channelId: String): Flux<ChatMessage>
}