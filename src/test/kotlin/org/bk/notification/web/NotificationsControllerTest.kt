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
        whenever(notificationHandler.getNotifications(any()))
            .thenReturn(Flux.just(sampleNotification("id-1"), sampleNotification("id-2")))

        webTestClient.get()
            .uri("/notifications/some-channel")
            .exchange()
            .expectHeader()
            .contentType("text/event-stream;charset=UTF-8")
            .expectBody<String>()
            .consumeWith { result ->
                assertThat(result.responseBody).contains("data:").contains("id-1").contains("id-2")
            }
    }

    private fun sampleNotification(id: String): Notification =
        Notification(id = id, creationDate = Instant.now(), payload = JsonNodeFactory.instance.objectNode())
}