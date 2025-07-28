package com.supernova.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProgramDto(
    val id: Int,
    val epg_channel_id: Int,
    val start: String,
    val end: String,
    val title: String,
    val description: String
)
