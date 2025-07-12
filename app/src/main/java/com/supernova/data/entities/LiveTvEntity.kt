package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "live_tv",
    indices = [
        Index("channel_id"),
        Index("is_live"),
        Index("name"),
        Index("category_id"),
        Index(value = ["category_type", "category_id"]),
        Index(value = ["channel_id", "is_live"], unique = true)
    ]
)
data class LiveTvEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val channel_id: Int,            // Corresponds to stream_id
    val num: Int?,
    val name: String,
    val stream_type: String?,       // 'live'
    val stream_icon: String?,
    val epg_channel_id: String?,
    val added: Long?,               // Unix timestamp
    val custom_sid: String?,
    val tv_archive: Int?,
    val direct_source: String?,
    val tv_archive_duration: Int?,
    val category_type: String?,
    val category_id: Int?,
    val thumbnail: String?,
    val is_live: Boolean = true
)