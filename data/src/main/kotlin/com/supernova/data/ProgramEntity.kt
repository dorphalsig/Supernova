package com.supernova.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "program")
data class ProgramEntity(
    @PrimaryKey val id: Int,
    val epgChannelId: Int,
    val start: String,
    val end: String,
    val title: String,
    val description: String
)
