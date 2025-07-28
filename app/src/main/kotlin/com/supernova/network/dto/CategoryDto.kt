package com.supernova.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CategoryDto(
    val category_id: Int,
    val category_name: String
)
