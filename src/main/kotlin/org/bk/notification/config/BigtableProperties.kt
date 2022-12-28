package org.bk.notification.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "bigtable")
data class BigtableProperties(
    val instanceId: String = "bus-instance",
    val emulatorPort: Int = 0,
    val projectId: String = "project-id"
)