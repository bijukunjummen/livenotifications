package org.bk.notification.config

import com.google.cloud.spanner.InstanceConfigId
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "spanner")
@ConstructorBinding
data class SpannerProperties(
        val instanceId: String = "",
        val database: String = "",
        val emulatorHostPort: String = "",
        val configId: String = "us-west1"

)