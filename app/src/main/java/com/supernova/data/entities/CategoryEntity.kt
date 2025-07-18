package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "category",
    primaryKeys = ["type", "id"]
)
data class CategoryEntity(
    val type: String,           // e.g., 'movie', 'live_tv'
    val id: Int,                // category id within type
    val name: String,
    val parent_id: Int = 0
)