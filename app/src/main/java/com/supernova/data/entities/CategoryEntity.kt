package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "category",
    indices = [
        Index("type"),
        Index("is_live"),
        Index(value = ["type", "is_live"]),
        Index(value = ["type", "id", "is_live"], unique = true)
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val type: String,           // e.g., 'movie', 'live_tv'
    val id: Int,                // category id within type
    val name: String,
    val parent_id: Int = 0,
    val is_live: Boolean = true
)