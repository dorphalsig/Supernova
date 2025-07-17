package com.supernova.data.entities

import androidx.room.Embedded
import androidx.room.Relation

data class SeriesWithDetails(
    @Embedded val series: SeriesEntity,
    @Relation(parentColumn = "series_id", entityColumn = "tmdb_id")
    val details: ContentDetailEntity?
)
