package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "series_category",
    primaryKeys = ["series_id", "category_type", "category_id"],
    foreignKeys = [
        ForeignKey(
            entity = SeriesEntity::class,
            parentColumns = ["series_id"],
            childColumns = ["series_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["type", "id"],
            childColumns = ["category_type", "category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SeriesCategoryEntity(
    val series_id: Int,
    val category_type: String,
    val category_id: Int
)