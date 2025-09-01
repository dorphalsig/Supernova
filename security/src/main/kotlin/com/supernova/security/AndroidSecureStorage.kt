package com.supernova.security

import com.supernova.domain.security.SecureStorage

class AndroidSecureStorage : SecureStorage {
    override fun put(key: String, value: ByteArray) { }
    override fun get(key: String): ByteArray? = null
    override fun remove(key: String) { }
}
