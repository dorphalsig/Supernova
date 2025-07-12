package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "series_category",
    primaryKeys = ["series_id", "category_type", "category_id"],
    indices = [
        Index("series_id"),
        Index(value = ["category_type", "category_id"]),
        Index("is_live"),
        Index(value = ["series_id", "is_live"])
    ]
)
data class SeriesCategoryEntity(
    val series_id: Int,
    val category_type: String,
    val category_id: Int,
    val is_live: Boolean = true
)