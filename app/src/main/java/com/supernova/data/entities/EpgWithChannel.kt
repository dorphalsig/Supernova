package com.supernova.data.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class EpgWithChannel(
    @Embedded val epg: EpgEntity,
    @ColumnInfo(name = "name") val channel_name: String?
)
