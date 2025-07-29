package com.supernova.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "program")
internal data class ProgramEntity(
    @PrimaryKey val id: Int = 0
)
