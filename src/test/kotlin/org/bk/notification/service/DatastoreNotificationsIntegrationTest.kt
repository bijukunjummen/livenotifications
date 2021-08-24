package org.bk.notification.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.google.api.gax.core.NoCredentialsProvider
import com.google.cloud.NoCredentials
import com.google.cloud.datastore.DatastoreOptions
import org.assertj.core.api.Assertions.assertThat
import org.bk.notification.model.Notification
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.testcontainers.containers.DatastoreEmulatorContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import reactor.test.StepVerifier
import java.time.Instant

@JsonTest
@Testcontainers
class DatastoreNotificationsIntegrationTest {

    private lateinit var persister: DatastoreNotificationPersister

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun beforeEach() {
        val builder: DatastoreOptions.Builder = DatastoreOptions
            .newBuilder()
        builder.setHost(emulator.emulatorEndpoint)
        builder.setCredentials(NoCredentials.getInstance())

        val datastore = try {
            builder.build().service
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
        persister = DatastoreNotificationPersister(datastore, objectMapper)
    }

    @Test
    fun `save and retrieve from datastore`() {
        val notification = sampleNotification("id-1", "some-channel")
        StepVerifier.create(persister.save(notification))
            .assertNext { savedNotification ->
                assertThat(savedNotification).isEqualTo(notification)
            }
            .verifyComplete()

        StepVerifier.create(persister.getOldNotifications(channelId = "some-channel"))
            .assertNext { n ->
                assertThat(n).isEqualTo(notification)
            }
            .verifyComplete()
    }

    private fun sampleNotification(id: String, channelId: String): Notification =
        Notification(
            id = id,
            channelId = channelId,
            creationDate = Instant.now(),
            payload = JsonNodeFactory.instance.objectNode()
        )

    companion object {
        @JvmStatic
        @Container
        private val emulator = DatastoreEmulatorContainer(
            DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:316.0.0-emulators")
        )
    }
}