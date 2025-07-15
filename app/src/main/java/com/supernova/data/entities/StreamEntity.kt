package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stream",
    indices = [
        Index("stream_type"),
        Index("year"),
        Index("tmdb_id"),
        Index("epg_channel_id")
    ]
)
data class StreamEntity(
    @PrimaryKey
    val stream_id: Int,
    val title: String?,
    val year: Int?,
    val stream_type: String?,
    val thumbnail_url: String?,
    val banner_url: String?,
    val tmdb_id: Int?,
    val media_type: String?,
    val tmdb_synced_at: Long?,
    val provider_id: Long?,
    val container_extension: String?,
    val epg_channel_id: String?,
    val tv_archive: Int?,
    val tv_archive_duration: Int?,
    val direct_source: String?,
    val custom_sid: String?,
    val rating: Float?,
    val rating_5based: Float?,
    val added: Long?,
    val plot: String?,
    val cast: String?,
    val director: String?,
    val genre: String?
)
