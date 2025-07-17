package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "reaction",
    indices = [Index(value = ["userId"]), Index(value = ["streamId"])]
)
data class ReactionEntity(
    @PrimaryKey(autoGenerate = true)
    val reactionId: Int = 0,
    val userId: Int,
    val streamId: Int?,
    val reactionType: String,
    val createdAt: Long = System.currentTimeMillis()
)
