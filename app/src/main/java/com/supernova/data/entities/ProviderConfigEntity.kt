package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "provider_configs",
    indices = [
        Index(value = ["baseUrl"]),
        Index(value = ["username"])
    ]
)
data class ProviderConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val baseUrl: String,
    val username: String,
    val password: String,
    val updatedAt: Long
)
