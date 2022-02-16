package org.bk.notification

import org.bk.notification.config.BigtableProperties
import org.bk.notification.config.SpannerProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(value = [BigtableProperties::class, SpannerProperties::class])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
