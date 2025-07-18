package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "movie_category",
    primaryKeys = ["movie_id", "category_type", "category_id"],
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = ["movie_id"],
            childColumns = ["movie_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["type", "id"],
            childColumns = ["category_type", "category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["category_type", "category_id"])
    ]
)
data class MovieCategoryEntity(
    val movie_id: Int,
    val category_type: String,
    val category_id: Int
)