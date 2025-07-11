package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie")
data class MovieEntity(
    @PrimaryKey
    val movie_id: Int,              // Corresponds to stream_id
    val num: Int?,
    val name: String,
    val title: String?,
    val year: Int?,
    val stream_type: String?,       // e.g., 'movie'
    val stream_icon: String?,
    val rating: Float?,
    val rating_5based: Float?,
    val added: Long?,               // Unix timestamp
    val container_extension: String?,
    val custom_sid: String?,
    val direct_source: String?
)