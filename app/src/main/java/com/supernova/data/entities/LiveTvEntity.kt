package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "live_tv",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["type", "id"],
            childColumns = ["category_type", "category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["category_type", "category_id"])
    ]
)
data class LiveTvEntity(
    @PrimaryKey
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
    val thumbnail: String?
)