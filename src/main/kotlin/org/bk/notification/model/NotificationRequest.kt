package org.bk.notification.model

import com.fasterxml.jackson.databind.JsonNode

data class NotificationRequest(
    val payload: JsonNode
)