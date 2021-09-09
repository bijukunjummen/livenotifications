package org.bk.notification.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.google.cloud.firestore.Firestore
import org.assertj.core.api.Assertions.assertThat
import org.bk.notification.model.Notification
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.mock.mockito.MockBean
import reactor.test.StepVerifier
import java.time.Instant

@JsonTest
class FirestoreNotificationPersisterTest {
    private lateinit var persister: FirestoreNotificationPersister

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var firestore: Firestore

    @BeforeEach
    fun setUp() {
        persister = FirestoreNotificationPersister(firestore, objectMapper)
    }

    @Test
    fun `save notification`() {
        val original = sampleNotification("id-1", "some-channel")
        StepVerifier.create(persister.save(original))
            .assertNext { saved ->
                assertThat(saved).isEqualTo(original)
            }
            .expectComplete()
    }

    private fun sampleNotification(id: String, channelId: String): Notification =
        Notification(
            id = id,
            channelId = channelId,
            creationDate = Instant.now(),
            payload = JsonNodeFactory.instance.objectNode()
        )
}