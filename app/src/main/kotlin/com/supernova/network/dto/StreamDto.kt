package com.supernova.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StreamDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "category_id") val categoryId: Int,
    @Json(name = "stream_type") val streamType: String,
    @Json(name = "is_live") val isLive: Boolean,
    @Json(name = "number") val number: Int
)
