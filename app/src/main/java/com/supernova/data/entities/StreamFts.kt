package com.supernova.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Fts4
@Entity(tableName = "stream_fts")
data class StreamFts(
    @ColumnInfo(name = "rowid")
    val stream_id: Int,
    val title: String?,
    val plot: String?,
    val director: String?,
    val cast: String?,
    val genre: String?
)
