package com.supernova.security.securestorage
interface SecureStorage {
    suspend fun put(key: String, value: ByteArray)
    suspend fun get(key: String): ByteArray?
}
