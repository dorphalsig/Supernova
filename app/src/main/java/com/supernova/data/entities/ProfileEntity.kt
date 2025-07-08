package com.supernova.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val pin: Int? = null,
    val avatar: String // Changed from ByteArray to String (URL)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProfileEntity

        if (id != other.id) return false
        if (name != other.name) return false
        if (pin != other.pin) return false
        if (avatar != other.avatar) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + (pin ?: 0)
        result = 31 * result + avatar.hashCode()
        return result
    }
}