package com.supernova.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stream")
data class StreamEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val categoryId: Int,
    val streamType: String
)
