package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "episode",
    indices = [
        Index("series_id"),
        Index("season_number"),
        Index("episode_num"),
        Index(value = ["series_id", "season_number"])
    ]
)
data class EpisodeEntity(
    @PrimaryKey
    val episode_id: Int,
    val series_id: Int,
    val season_number: Int,
    val episode_num: Int,
    val title: String,
    val file_url: String?,
    val added_at: Long?,
    val container_extension: String?,
    val plot: String?,
    val release_date: String?,
    val duration_secs: Int?,
    val rating: Float?,
    val movie_image: String?,
    val cover_big: String?,
    val tmdb_id: Int?
)
