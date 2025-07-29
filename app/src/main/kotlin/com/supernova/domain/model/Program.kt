package com.supernova.domain.model

import java.time.LocalDateTime

data class Program(
    val id: Int,
    val epgChannelId: Int,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val title: String,
    val description: String
)
