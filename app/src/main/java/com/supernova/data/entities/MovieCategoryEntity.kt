package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "movie_category",
    primaryKeys = ["movie_id", "category_type", "category_id"],
    indices = [
        Index("movie_id"),
        Index(value = ["category_type", "category_id"]),
        Index("is_live"),
        Index(value = ["movie_id", "is_live"])
    ]
)
data class MovieCategoryEntity(
    val movie_id: Int,
    val category_type: String,
    val category_id: Int,
    val is_live: Boolean = true
)