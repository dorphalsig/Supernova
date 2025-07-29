package com.supernova.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "placeholder")
internal data class PlaceholderEntity(
    @PrimaryKey val id: Int = 0
)
