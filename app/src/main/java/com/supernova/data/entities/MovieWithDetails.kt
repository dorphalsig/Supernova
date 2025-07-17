package com.supernova.data.entities

import androidx.room.Embedded
import androidx.room.Relation

data class MovieWithDetails(
    @Embedded val movie: MovieEntity,
    @Relation(parentColumn = "movie_id", entityColumn = "tmdb_id")
    val details: ContentDetailEntity?
)
