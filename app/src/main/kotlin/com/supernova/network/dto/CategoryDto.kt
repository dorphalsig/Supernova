package com.supernova.network.dto

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json

@JsonClass(generateAdapter = true)
data class CategoryDto(
    @Json(name = "category_id") val categoryId: Int,
    @Json(name = "category_name") val categoryName: String
)
