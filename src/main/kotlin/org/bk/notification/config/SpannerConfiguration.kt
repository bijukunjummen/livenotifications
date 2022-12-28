package org.bk.notification.config

import com.google.cloud.NoCredentials
import com.google.cloud.ServiceOptions
import com.google.cloud.spanner.*
import org.bk.notification.service.SpannerChatMessageRepository
import org.bk.notification.service.SpannerChatRoomRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Profile

@Configuration
@Profile("spanner")
class SpannerConfiguration {
    @Bean
    fun spanner(spannerProperties: SpannerProperties): Spanner {
        if (spannerProperties.emulatorHostPort.isNotEmpty()) {
            return SpannerOptions.newBuilder()
                .setCredentials(NoCredentials.getInstance())
                .setEmulatorHost(spannerProperties.emulatorHostPort).build()
                .service
        }
        return SpannerOptions.newBuilder().build().service
    }

    @Bean
    @DependsOn("spannerMigration")
    fun spannerDatabaseClient(spanner: Spanner, spannerProperties: SpannerProperties): DatabaseClient {
        val projectId =
            if (spannerProperties.emulatorHostPort.isNotEmpty()) "sample-project" else ServiceOptions.getDefaultProjectId()
        val databaseId = DatabaseId.of(projectId, spannerProperties.instanceId, spannerProperties.database)
        return spanner.getDatabaseClient(databaseId)
    }

    @Bean
    fun spannerDatabaseAdminClient(spanner: Spanner): DatabaseAdminClient {
        return spanner.databaseAdminClient
    }

    @Bean
    fun spannerChatRoomRepository(databaseClient: DatabaseClient): SpannerChatRoomRepository {
        return SpannerChatRoomRepository(databaseClient)
    }

    @Bean
    fun spannerChatMessageRepository(databaseClient: DatabaseClient): SpannerChatMessageRepository {
        return SpannerChatMessageRepository(databaseClient)
    }

    @Bean
    fun spannerMigration(
        spanner: Spanner,
        databaseAdminClient: DatabaseAdminClient,
        spannerProperties: SpannerProperties
    ): SpannerMigration {
        return SpannerMigration(spanner, databaseAdminClient, spannerProperties)
    }
}