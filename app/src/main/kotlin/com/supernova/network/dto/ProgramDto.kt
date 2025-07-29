package com.supernova.network.dto

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json

@JsonClass(generateAdapter = true)
data class ProgramDto(
    val id: Int,
    @Json(name = "epg_channel_id") val epgChannelId: Int,
    val start: String,
    val end: String,
    val title: String,
    val description: String
)
