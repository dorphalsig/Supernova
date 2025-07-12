package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "series",
    indices = [
        Index("series_id"),
        Index("is_live"),
        Index("name"),
        Index("year"),
        Index("genre")
    ]
)
data class SeriesEntity(
    @PrimaryKey
    val series_id: Int,
    val num: Int?,
    val name: String,
    val title: String?,
    val year: String?,
    val stream_type: String?,
    val cover: String?,
    val plot: String?,
    val cast: String?,
    val director: String?,
    val genre: String?,
    val release_date: String?,
    val releaseDate: String?,
    val last_modified: String?,
    val rating: String?,
    val rating_5based: Float?,
    val backdrop_path: String?,        // JSON array as string
    val youtube_trailer: String?,
    val episode_run_time: String?,
    val is_live: Boolean = true
)