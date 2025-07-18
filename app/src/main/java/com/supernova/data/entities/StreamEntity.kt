package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Consolidated stream metadata across movies, series and live TV.
 */
@Entity(
    tableName = "stream",
    indices = [
        Index("streamType"),
        Index("year"),
        Index("tmdbId"),
        Index("epgChannelId")
    ]
)
data class StreamEntity(
    @PrimaryKey
    val streamId: Int,
    val title: String?,
    val year: Int?,
    val streamType: String?,
    val thumbnailUrl: String?,
    val bannerUrl: String?,
    val tmdbId: Int?,
    val mediaType: String?,
    val tmdbSyncedAt: Long?,
    val providerId: Long?,
    val containerExtension: String?,
    val epgChannelId: String?,
    val tvArchive: Int?,
    val tvArchiveDuration: Int?,
    val directSource: String?,
    val customSid: String?,
    val rating: Float?,
    val rating5Based: Float?,
    val added: Long?,
    val plot: String?,
    val cast: String?,
    val director: String?,
    val genre: String?
)
