package com.supernova.network.dto

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json

@JsonClass(generateAdapter = true)
data class StreamDto(
    @Json(name = "stream_id") val streamId: Int,
    val name: String,
    @Json(name = "category_id") val categoryId: Int,
    @Json(name = "stream_type") val streamType: String
)
