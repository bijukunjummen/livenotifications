package org.bk.notification.config

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FirestoreConfiguration {
    @Bean
    fun firestore(@Value("\${firestore.host:}") firestoreHost: String): Firestore {
        val builder: FirestoreOptions.Builder = FirestoreOptions
            .newBuilder()
        if (firestoreHost.isNotEmpty()) {
            builder.setEmulatorHost(firestoreHost)
            builder.setHost(firestoreHost)
        }
        return builder.build().service
    }
}