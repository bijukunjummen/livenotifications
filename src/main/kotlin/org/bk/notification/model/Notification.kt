package org.bk.notification.model

import com.fasterxml.jackson.databind.JsonNode
import java.time.Instant

data class Notification(
    val id: String,
    val creationDate: Instant,
    val payload: JsonNode
)
