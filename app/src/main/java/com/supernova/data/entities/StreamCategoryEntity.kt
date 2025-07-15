package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "stream_category",
    primaryKeys = ["streamId", "categoryId"],
    indices = [
        Index("streamId"),
        Index("categoryId")
    ]
)
data class StreamCategoryEntity(
    val streamId: Int,
    val categoryId: Int
)
