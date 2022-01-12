package org.bk.notification.config

import com.google.cloud.ServiceOptions
import com.google.cloud.bigtable.data.v2.BigtableDataClient
import com.google.cloud.bigtable.data.v2.BigtableDataSettings
import org.bk.notification.service.BigtableChatMessageRepository
import org.bk.notification.service.BigtableChatRoomRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("bigtable")
class BigtableConfiguration {
    @Bean
    fun bigtableDataClient(bigtableProperties: BigtableProperties): BigtableDataClient {
        if (bigtableProperties.emulatorPort != 0) {
            return BigtableDataClient
                    .create(BigtableDataSettings
                            .newBuilderForEmulator(bigtableProperties.emulatorPort)
                            .setProjectId(bigtableProperties.projectId)
                            .setInstanceId(bigtableProperties.instanceId)
                            .build())
        }
        return BigtableDataClient
                .create(BigtableDataSettings
                        .newBuilder()
                        .setInstanceId(bigtableProperties.instanceId)
                        .setProjectId(ServiceOptions.getDefaultProjectId())
                        .build())
    }

    @Bean
    fun bigtableChatRoomRepository(bigtableDataClient: BigtableDataClient): BigtableChatRoomRepository {
        return BigtableChatRoomRepository(bigtableDataClient)
    }

    @Bean
    fun bigtableChatMessageRepository(bigtableDataClient: BigtableDataClient): BigtableChatMessageRepository {
        return BigtableChatMessageRepository(bigtableDataClient)
    }
}