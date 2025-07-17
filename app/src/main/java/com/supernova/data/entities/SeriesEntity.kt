package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "series")
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
    // TMDB metadata
    val poster_path: String?,
    val overview: String?,
    val genres: String?,
    val first_air_date: String?,
    val last_air_date: String?,
    val number_of_seasons: Int?,
    val number_of_episodes: Int?
)