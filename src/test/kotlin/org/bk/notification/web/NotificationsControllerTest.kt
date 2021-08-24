package org.bk.notification.web

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.assertj.core.api.Assertions.assertThat
import org.bk.notification.model.Notification
import org.bk.notification.service.NotificationHandler
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import reactor.core.publisher.Flux
import java.time.Instant

@WebFluxTest(controllers = [NotificationController::class])
class NotificationsControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var notificationHandler: NotificationHandler

    @Test
    fun `get a notifications stream`() {
        whenever(notificationHandler.getOldNotifications(any()))
            .thenReturn(
                Flux.just(
                    sampleNotification("id-old-1", "some-channel"),
                    sampleNotification("id-old-2", "some-channel")
                )
            )
        whenever(notificationHandler.getNotifications(any()))
            .thenReturn(
                Flux.just(
                    sampleNotification("id-1", "some-channel"),
                    sampleNotification("id-2", "some-channel")
                )
            )

        webTestClient.get()
            .uri("/notifications/some-channel")
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

    private fun sampleNotification(id: String, channelId: String): Notification =
        Notification(
            id = id,
            channelId = channelId,
            creationDate = Instant.now(),
            payload = JsonNodeFactory.instance.objectNode()
        )
}