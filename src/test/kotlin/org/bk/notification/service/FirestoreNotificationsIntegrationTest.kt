package org.bk.notification.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.NoCredentials
import com.google.cloud.firestore.FirestoreOptions
import org.assertj.core.api.Assertions.assertThat
import org.bk.notification.model.ChatMessage
import org.bk.notification.model.ChatRoom
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.testcontainers.containers.FirestoreEmulatorContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import reactor.test.StepVerifier
import java.time.Instant

@JsonTest
@Testcontainers
class FirestoreNotificationsIntegrationTest {

    private lateinit var chatMessageRepository: FirestoreChatMessageRepository
    private lateinit var chatRoomRepository: FirestoreChatRoomRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun beforeEach() {
        val builder: FirestoreOptions.Builder = FirestoreOptions
            .newBuilder()
        builder.setEmulatorHost("${emulator.emulatorEndpoint}")
        builder.setHost("${emulator.emulatorEndpoint}")
        builder.setCredentials(NoCredentials.getInstance())

        val firestore = try {
            builder.build().service
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
        chatMessageRepository = FirestoreChatMessageRepository(firestore)
        chatRoomRepository = FirestoreChatRoomRepository(firestore)
    }

    @Test
    fun `save and retrieve from firestore`() {
        val notification = sampleNotification("id-1", "some-channel")
        val chatRoom = ChatRoom("some-channel", "some-channel")
        StepVerifier.create(chatRoomRepository.save(chatRoom))
            .assertNext {savedRoom ->
                assertThat(savedRoom).isEqualTo(chatRoom)
            }
            .verifyComplete()

        StepVerifier.create(chatMessageRepository.save(notification))
            .assertNext { savedNotification ->
                assertThat(savedNotification).isEqualTo(notification)
            }
            .verifyComplete()

        StepVerifier.create(chatMessageRepository.getLatestSavedChatMessages(chatRoomId = "some-channel"))
            .assertNext { n ->
                assertThat(n).isEqualTo(notification)
            }
            .verifyComplete()
    }

    private fun sampleNotification(id: String, channelId: String): ChatMessage =
        ChatMessage(
            id = id,
            chatRoomId = channelId,
            creationDate = Instant.now(),
            payload = "some payload"
        )

    companion object {
        @JvmStatic
        @Container
        private val emulator = FirestoreEmulatorContainer(
            DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:316.0.0-emulators")
        )
    }
}