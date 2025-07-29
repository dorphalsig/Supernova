package com.supernova.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stream")
internal data class StreamEntity(
    @PrimaryKey val id: Int = 0
)
