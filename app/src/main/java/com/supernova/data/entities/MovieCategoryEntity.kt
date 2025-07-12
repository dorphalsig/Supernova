package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "movie_category",
    indices = [
        Index("movie_id"),
        Index(value = ["category_type", "category_id"]),
        Index("is_live"),
        Index(value = ["movie_id", "is_live"]),
        Index(value = ["movie_id", "category_type", "category_id", "is_live"], unique = true)
    ]
)
data class MovieCategoryEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val movie_id: Int,
    val category_type: String,
    val category_id: Int,
    val is_live: Boolean = true
)