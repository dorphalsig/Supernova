package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "series_category",
    indices = [
        Index("series_id"),
        Index(value = ["category_type", "category_id"]),
        Index("is_live"),
        Index(value = ["series_id", "is_live"]),
        Index(value = ["series_id", "category_type", "category_id", "is_live"], unique = true)
    ]
)
data class SeriesCategoryEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val series_id: Int,
    val category_type: String,
    val category_id: Int,
    val is_live: Boolean = true
)