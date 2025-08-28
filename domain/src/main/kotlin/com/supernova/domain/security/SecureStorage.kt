package com.supernova.domain.security

interface SecureStorage {
    fun put(key: String, value: ByteArray)
    fun get(key: String): ByteArray?
    fun remove(key: String)
}
