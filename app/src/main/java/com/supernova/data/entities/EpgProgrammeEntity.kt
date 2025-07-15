package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "epg_programme",
    indices = [
        Index("epgChannelId"),
        Index("startAt"),
        Index("endAt")
    ]
)
data class EpgProgrammeEntity(
    @PrimaryKey(autoGenerate = true)
    val programmeId: Int = 0,
    val epgChannelId: String,
    val startAt: Long,
    val endAt: Long,
    val title: String,
    val subTitle: String? = null,
    val description: String? = null,
    val category: String? = null,
    val iconUrl: String? = null,
    val rating: String? = null
)
