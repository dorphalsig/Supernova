package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "content_detail",
    indices = [Index("media_type"), Index("tmdb_id")]
)
data class ContentDetailEntity(
    @PrimaryKey val tmdb_id: Int,
    val media_type: String,
    val tagline: String?,
    val status: String?,
    val homepage: String?,
    val genres: String?
)
