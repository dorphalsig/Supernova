package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "watch_history",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["streamId"]),
        Index(value = ["episodeId"]),
        Index(value = ["watchedAt"])
    ]
)
data class WatchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val historyId: Int = 0,
    val userId: Int,
    val streamId: Int?,
    val episodeId: Int?,
    val watchedAt: Long,
    val durationSec: Int?,
    val progress: Float?
)
