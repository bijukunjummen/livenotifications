package org.bk.notification

import org.bk.notification.config.BigtableProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(BigtableProperties::class)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
