package org.bk.notification.config

import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class DatastoreConfiguration {

    @Bean
    @Profile("!local")
    fun datastore(): Datastore {
        val builder: DatastoreOptions.Builder = DatastoreOptions
            .newBuilder()
        return builder.build().service
    }

    @Bean
    @Profile("local")
    fun datastoreLocal(@Value("\${datastore.host:}") datastoreHost: String): Datastore {
        val builder: DatastoreOptions.Builder = DatastoreOptions
            .newBuilder()
        if (datastoreHost.isNotEmpty()) {
            builder.setHost(datastoreHost)
        }
        return builder.build().service
    }
}