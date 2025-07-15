package com.supernova.data.entities

import androidx.room.Fts4
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Fts4(contentEntity = EpgProgrammeEntity::class)
@Entity(tableName = "epg_programme_fts")
data class EpgProgrammeFts(
    val title: String?,
    val subTitle: String?,
    val description: String?,
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    val rowid: Int
)
