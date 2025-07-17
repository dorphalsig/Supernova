package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recommendation",
    indices = [Index("userId"), Index("streamId")]
)
data class RecommendationEntity(
    @PrimaryKey(autoGenerate = true)
    val recId: Int = 0,
    val userId: Int,
    val streamId: Int,
    val recoAt: Long,
    val score: Float?,
    val source: String?,
    val moodId: Int?
)
