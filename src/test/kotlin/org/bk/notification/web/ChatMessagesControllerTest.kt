package org.bk.notification.web

import org.assertj.core.api.Assertions.assertThat
import org.bk.notification.exception.ChatRoomNotFoundException
import org.bk.notification.model.ChatMessage
import org.bk.notification.service.ChatMessageHandler
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

@WebFluxTest(controllers = [ChatController::class, ChatMessageExceptionHandler::class])
class ChatMessagesControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var chatMessageHandler: ChatMessageHandler

    @Test
    fun `get a notifications stream`() {
        whenever(chatMessageHandler.getOldChatMessages(any()))
                .thenReturn(
                        Flux.just(
                                sampleChatMessage("id-old-1", "some-channel"),
                                sampleChatMessage("id-old-2", "some-channel")
                        )
                )
        whenever(chatMessageHandler.getChatMessages(any()))
                .thenReturn(
                        Flux.just(
                                sampleChatMessage("id-1", "some-channel"),
                                sampleChatMessage("id-2", "some-channel")
                        )
                )

        webTestClient.get()
                .uri("/messages/some-channel")
                .exchange()
                .expectHeader()
                .contentType("text/event-stream;charset=UTF-8")
                .expectBody<String>()
                .consumeWith { result ->
                    assertThat(result.responseBody)
                            .contains("data:")
                            .contains("id-old-1")
                            .contains("id-old-2")
                            .contains("id-1")
                            .contains("id-2")
                }
    }

    @Test
    fun `get a notifications stream in a invalid room`() {
        whenever(chatMessageHandler.getOldChatMessages(any()))
                .thenReturn(
                        Flux.error(ChatRoomNotFoundException(msg = "Room not found"))
                )
        whenever(chatMessageHandler.getChatMessages(any()))
                .thenReturn(
                        Flux.just(
                                sampleChatMessage("id-1", "some-channel"),
                                sampleChatMessage("id-2", "some-channel")
                        )
                )

        webTestClient.get()
                .uri("/messages/some-channel")
                .exchange()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.msg").isEqualTo("Room not found")
    }

    private fun sampleChatMessage(id: String, chatRoomId: String): ChatMessage =
            ChatMessage(
                    id = id,
                    chatRoomId = chatRoomId,
                    creationDate = Instant.now(),
                    payload = ""
            )
}