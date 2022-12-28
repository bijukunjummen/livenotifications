package org.bk.notification.model

data class Page<T>(val data: List<T>, val lastResult: String)