package org.bk.notification.config

import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DatastoreConfiguration {

    @Value("\${datastore.host:}")
    private lateinit var datastoreHost: String

    @Bean
    fun datastore(): Datastore {
        val builder: DatastoreOptions.Builder = DatastoreOptions
            .newBuilder()

        if (datastoreHost.isNotEmpty()) {
            builder.setHost(datastoreHost)
        }
        return builder.build().service
    }

}