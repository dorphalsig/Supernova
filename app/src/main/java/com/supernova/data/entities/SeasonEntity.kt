package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "season",
    primaryKeys = ["series_id", "season_number"],
    indices = [Index(value = ["series_id"])]
)
data class SeasonEntity(
    val series_id: Int,
    val season_number: Int,
    val name: String?,
    val overview: String?,
    val cover: String?,
    val cover_big: String?,
    val air_date: String?,
    val vote_average: Float?
)
