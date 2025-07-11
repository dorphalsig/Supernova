package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channel")
data class ChannelEntity(
    @PrimaryKey
    val channel_id: String,
    val name: String?
)
