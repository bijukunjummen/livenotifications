package org.bk.notification.config

import com.google.cloud.ServiceOptions
import com.google.cloud.spanner.*
import org.bk.notification.toMono

class SpannerMigration(
    private val spanner: Spanner,
    private val databaseAdminClient: DatabaseAdminClient,
    private val spannerProperties: SpannerProperties
) {

    init {
        migrate()
    }

    fun migrate() {
        val projectId = if (spannerProperties.emulatorHostPort.isNotEmpty())
            "sample-project"
        else
            ServiceOptions.getDefaultProjectId()

        val instance: Instance = createInstanceIfNotFound(projectId, spannerProperties.instanceId)
        val retrievedDatabase: Database? = createDatabaseIfNotFound(instance)
        println("Database id ${retrievedDatabase?.id}")
    }

    fun createDatabaseIfNotFound(instance: Instance): Database? {
        try {
            return databaseAdminClient.getDatabase(instance.id.instance, spannerProperties.database)
        } catch (dbNotFound: DatabaseNotFoundException) {
            val databaseId = DatabaseId.of(instance.id, spannerProperties.database)
            val database = databaseAdminClient.newDatabaseBuilder(databaseId).build()
            val statements = listOf(
                """
                        CREATE TABLE ChatRooms(
                        ChatRoomId STRING(100) NOT NULL, 
                        ChatRoomName STRING(100)
                        ) PRIMARY KEY(ChatRoomId)
                    """.trimIndent(),
                """
                        CREATE TABLE ChatRoomMessages(
                        ChatMessageId String(100) NOT NULL,
                        ChatRoomId STRING(100), 
                        CreationDate TIMESTAMP,
                        Payload STRING(MAX),
                        CONSTRAINT FK_ChatRoom FOREIGN KEY (ChatRoomId) REFERENCES ChatRooms (ChatRoomId)
                        ) PRIMARY KEY(ChatMessageId)
                    """.trimIndent()
            )
            return databaseAdminClient.createDatabase(database, statements).toMono().block()
        }
    }

    private fun createInstanceIfNotFound(projectId: String, instanceId: String): Instance {
        try {
            return spanner.instanceAdminClient.getInstance(spannerProperties.instanceId)
        } catch (e: InstanceNotFoundException) {
            return spanner.instanceAdminClient
                .createInstance(
                    InstanceInfo.newBuilder(
                        InstanceId.of(projectId, instanceId)
                    )
                        .setInstanceConfigId(InstanceConfigId.of(projectId, spannerProperties.configId))
                        .setDisplayName(instanceId)
                        .setProcessingUnits(100)
                        .build()
                )
                .get()
        }
    }
}