package org.bk.notification.service

import org.assertj.core.api.Assertions.assertThat
import org.bk.notification.config.SpannerConfiguration
import org.bk.notification.config.SpannerProperties
import org.bk.notification.model.ChatMessage
import org.bk.notification.model.ChatRoom
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.SpannerEmulatorContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import reactor.test.StepVerifier
import java.time.Instant


@ExtendWith(SpringExtension::class)
@EnableConfigurationProperties(value = [SpannerProperties::class])
@ContextConfiguration(
    classes = [SpannerConfiguration::class],
    initializers = [SpannerNotificationsIntegrationTest.PropertiesInitializer::class]
)
@ActiveProfiles("spanner")
@Testcontainers
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpannerNotificationsIntegrationTest {

    @Autowired
    private lateinit var chatMessageRepository: SpannerChatMessageRepository

    @Autowired
    private lateinit var chatRoomRepository: SpannerChatRoomRepository

    @Test
    fun `save and retrieve from spanner`() {
        val notification = sampleNotification("id-1", "some-channel")
        val chatRoom = ChatRoom("some-channel", "some-channel")
        StepVerifier.create(chatRoomRepository.save(chatRoom))
            .assertNext { savedRoom ->
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
        private val emulator = SpannerEmulatorContainer(
            DockerImageName.parse("gcr.io/cloud-spanner-emulator/emulator:1.4.1")
        )
    }

    internal class PropertiesInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of(
                "spanner.emulator-host-port=${emulator.emulatorGrpcEndpoint}",
                "spanner.instanceId=test-instance",
                "spanner.database=sample"
            ).applyTo(applicationContext.environment)
        }
    }
}