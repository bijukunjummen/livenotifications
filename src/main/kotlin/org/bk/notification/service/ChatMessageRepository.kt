package org.bk.notification.service

import org.bk.notification.model.ChatMessage
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Responsible for persisting old chat messages to a persistent store
 */
interface ChatMessageRepository {
    /**
     * Save a notification
     */
    fun save(chatMessage: ChatMessage): Mono<ChatMessage>

    /**
     * Get most recent notifications
     *
     * @param count of recent notifications
     */
    fun getLatestSavedChatMessages(count: Int = 25, channelId: String, latestFirst: Boolean = true): Flux<ChatMessage>


    /**
     * Delete a chat message from a room
     *
     * @param chatMessageId id of the chat message
     *
     * @return if the delete is successful
     */
    fun deleteChatMessage(chatRoomId: String, chatMessageId: String): Mono<Boolean>

}