package com.supernova.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Fts4(contentEntity = StreamEntity::class)
@Entity(tableName = "stream_fts")
data class StreamFts(
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    val streamId: Int,
    val title: String?,
    val plot: String?,
    val director: String?,
    val cast: String?,
    val genre: String?
)
