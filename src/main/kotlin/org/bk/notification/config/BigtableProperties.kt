package org.bk.notification.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "bigtable")
class BigtableProperties(
        val instanceId: String = "",
        val emulatorPort: Int = 0
)