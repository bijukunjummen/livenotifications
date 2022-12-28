package org.bk.notification.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spanner")
data class SpannerProperties(
    val instanceId: String = "",
    val database: String = "",
    val emulatorHostPort: String = "",
    val configId: String = "us-west1"
)