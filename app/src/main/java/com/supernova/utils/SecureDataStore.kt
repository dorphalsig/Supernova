package com.supernova.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.security.crypto.MasterKeys
import com.google.crypto.tink.Aead
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AesGcmKeyManager
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object SecureDataStore {
    private lateinit var gson: Gson
    private lateinit var aead: Aead
    private lateinit var dataStore: DataStore<MutableMap<String, String>>
    var initialized = false

    fun init(context: Context) {
        if (initialized) return
        gson = Gson()
        AeadConfig.register()
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, "secure_prefs_keyset", "secure_prefs")
            .withKeyTemplate(AesGcmKeyManager.aes256GcmTemplate())
            .withMasterKeyUri("android-keystore://$masterKeyAlias")
            .build()
            .keysetHandle
        aead = keysetHandle.getPrimitive(Aead::class.java)
        val serializer = SecurePrefsSerializer(aead, gson)
        dataStore = DataStoreFactory.create(
            serializer = serializer,
            produceFile = { File(context.filesDir, "secure_prefs.pb") },
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        )
        initialized = true
    }

    private fun checkInit() {
        check(initialized) { "SecureDataStore must be initialized by calling init(context) before use." }
    }

    suspend fun putString(key: String, value: String) {
        checkInit()
        dataStore.updateData { prefs ->
            prefs.toMutableMap().apply { this[key] = value }
        }
    }

    suspend fun getString(key: String): String? {
        checkInit()
        return dataStore.data.first()[key]
    }

    suspend fun putBoolean(key: String, value: Boolean) = putString(key, value.toString())

    suspend fun getBoolean(key: String): Boolean = getString(key)?.toBoolean() ?: false

    suspend fun putLong(key: String, value: Long) = putString(key, value.toString())

    suspend fun getLong(key: String): Long = getString(key)?.toLongOrNull() ?: 0L

}

private class SecurePrefsSerializer(
    private val aead: Aead,
    private val gson: Gson
) : Serializer<MutableMap<String, String>> {
    override val defaultValue: MutableMap<String, String> = mutableMapOf()

    override suspend fun readFrom(input: InputStream): MutableMap<String, String> {
        return try {
            val bytes = input.readBytes()
            if (bytes.isEmpty()) {
                mutableMapOf()
            } else {
                val decrypted = aead.decrypt(bytes, null)
                @Suppress("UNCHECKED_CAST")
                gson.fromJson(String(decrypted), MutableMap::class.java) as MutableMap<String, String>
            }
        } catch (_: Exception) {
            mutableMapOf()
        }
    }

    override suspend fun writeTo(t: MutableMap<String, String>, output: OutputStream) {
        val json = gson.toJson(t).toByteArray()
        val encrypted = aead.encrypt(json, null)
        output.write(encrypted)
    }
}
