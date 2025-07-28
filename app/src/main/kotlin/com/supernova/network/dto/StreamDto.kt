package com.supernova.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StreamDto(
    val stream_id: Int,
    val name: String,
    val category_id: Int,
    val stream_type: String
)
