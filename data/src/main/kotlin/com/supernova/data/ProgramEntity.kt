package com.supernova.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "program")
data class ProgramEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "epg_channel_id") val epgChannelId: Int,
    @ColumnInfo(name = "start") val start: Long,
    @ColumnInfo(name = "end") val end: Long,
    val title: String,
    val description: String
)
