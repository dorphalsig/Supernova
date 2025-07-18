package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.supernova.data.entities.ChannelEntity

@Entity(
    tableName = "epg",
    indices = [
        Index("start"),
        Index("end"),
        Index("channel_id"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = ChannelEntity::class,
            parentColumns = ["channel_id"],
            childColumns = ["channel_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EpgEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val channel_id: String,
    val start: Long,
    val end: Long,
    val title: String?,
    val description: String?
)
