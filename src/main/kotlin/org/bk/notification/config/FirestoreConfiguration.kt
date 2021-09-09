package org.bk.notification.config

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class FirestoreConfiguration {

    @Bean
    @Profile("!local")
    fun firestore(): Firestore {
        val builder: FirestoreOptions.Builder = FirestoreOptions
            .newBuilder()
        return builder.build().service
    }

    @Bean
    @Profile("local")
    fun firestoreLocal(@Value("\${firestore.host:}") firestoreHost: String): Firestore {
        val builder: FirestoreOptions.Builder = FirestoreOptions
            .newBuilder()
        if (firestoreHost.isNotEmpty()) {
            builder.setHost(firestoreHost)
        }
        return builder.build().service
    }
}