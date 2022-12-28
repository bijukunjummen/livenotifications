package org.bk.notification.service

import org.bk.notification.model.ChatMessage
import org.bk.notification.model.Page
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Responsible for persisting old chat messages to a persistent store
 */
interface ChatMessageRepository {
    /**
     * Save a notification
     *
     * @param chatMessage chat message to save
     *
     * @return the saved chat message
     */
    fun save(chatMessage: ChatMessage): Mono<ChatMessage>

    /**
     * Get most recent notifications
     *
     * @param chatRoomId chat room id
     * @param latestFirst
     * @param count of recent notifications
     *
     * @return list of recent chat messages
     */
    fun getLatestSavedChatMessages(count: Long = 25, chatRoomId: String, latestFirst: Boolean = true): Flux<ChatMessage>

    /**
     * Get paginated notifications
     *
     * @param chatRoomId chat room id
     * @param from from which row onwards - exclusive
     * @param count of recent notifications
     *
     * @return a page of chat messages
     */
    fun getPaginatedMessages(
        chatRoomId: String,
        from: String = "",
        count: Long = 25,
    ): Page<ChatMessage>


    /**
     * Delete a chat message from a room
     *
     * @param chatMessageId id of the chat message
     *
     * @return if the delete is successful
     */
    fun deleteChatMessage(chatRoomId: String, chatMessageId: String): Mono<Boolean>

}