package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_profile",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val userId: Int = 0,
    val username: String,
    val email: String?,
    val pin4: String?,
    val createdAt: Long
)
