package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "movie",
    indices = [Index("name"), Index("year"), Index("genres")]
)
data class MovieEntity(
    /** Provider stream identifier. */
    @PrimaryKey val movie_id: Int,

    /** Channel number if provided by the API. */
    val num: Int?,

    /** Display name of the movie. */
    val name: String,

    /** Optional title metadata. */
    val title: String?,

    /** Release year. */
    val year: Int?,

    /** Provider reported stream type, e.g., "movie". */
    val stream_type: String?,

    /** URL to the movie poster or icon. */
    val stream_icon: String?,

    /** Numerical rating from the provider. */
    val rating: Float?,

    /** Rating on a five point scale. */
    val rating_5based: Float?,

    /** Unix timestamp when added to catalog. */
    val added: Long?,

    /** Container file extension such as "mp4". */
    val container_extension: String?,

    /** Custom stream identifier supplied by provider. */
    val custom_sid: String?,

    /** Direct streaming source URL if available. */
    val direct_source: String?,

    // TMDB metadata

    /** JSON array of backdrop image paths. */
    val backdrop_path: String?,

    /** Path to the poster image. */
    val poster_path: String?,

    /** Plot overview from TMDB. */
    val overview: String?,

    /** Comma separated list of genres. */
    val genres: String?,

    /** Runtime in minutes. */
    val runtime: Int?,

    /** Comma separated list of spoken language codes. */
    val spoken_languages: String?,
)

