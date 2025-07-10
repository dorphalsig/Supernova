package com.supernova.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.security.crypto.MasterKey
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
import kotlinx.coroutines.flow.update
import java.io.InputStream
import java.io.OutputStream
import java.io.File

/**
 * Replacement for the previous [SecureStorage] class using an encrypted Jetpack
 * DataStore backed by Tink. A hardware-backed key is used when available with
 * a software fallback.
 */
class SecureDataStore(context: Context) {

    private val gson = Gson()

    private val aead: Aead

    private val dataStore: DataStore<SecurePrefs>

    init {
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
            produceFile = { java.io.File(context.filesDir, "secure_prefs.pb") },
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        )
    }

    suspend fun saveCredentials(portal: String, username: String, password: String) {
        dataStore.updateData {
            it.copy(
                portal = portal,
                username = username,
                password = password,
                isConfigured = true
            )
        }
    }

    suspend fun getPortal(): String? = dataStore.data.first().portal.ifEmpty { null }
    suspend fun getUsername(): String? = dataStore.data.first().username.ifEmpty { null }
    suspend fun getPassword(): String? = dataStore.data.first().password.ifEmpty { null }
    suspend fun isConfigured(): Boolean = dataStore.data.first().isConfigured

    suspend fun setParentalLock(enabled: Boolean) {
        dataStore.updateData { it.copy(parentalLock = enabled) }
    }

    suspend fun isParentalLockEnabled(): Boolean = dataStore.data.first().parentalLock

    suspend fun clearCredentials() {
        dataStore.updateData { SecurePrefs() }
    }

    suspend fun setLastSyncResult(success: Boolean) {
        dataStore.updateData {
            it.copy(
                lastSyncSuccess = success,
                lastSyncTime = System.currentTimeMillis()
            )
        }
    }

    suspend fun isLastSyncSuccessful(): Boolean = dataStore.data.first().lastSyncSuccess
    suspend fun getLastSyncTime(): Long = dataStore.data.first().lastSyncTime
}

data class SecurePrefs(
    val portal: String = "",
    val username: String = "",
    val password: String = "",
    val isConfigured: Boolean = false,
    val parentalLock: Boolean = false,
    val lastSyncSuccess: Boolean = false,
    val lastSyncTime: Long = 0L
)

private class SecurePrefsSerializer(
    private val aead: Aead,
    private val gson: Gson
) : Serializer<SecurePrefs> {
    override val defaultValue: SecurePrefs = SecurePrefs()

    override suspend fun readFrom(input: InputStream): SecurePrefs {
        return try {
            val bytes = input.readBytes()
            if (bytes.isEmpty()) {
                defaultValue
            } else {
                val decrypted = aead.decrypt(bytes, null)
                gson.fromJson(String(decrypted), SecurePrefs::class.java)
            }
        } catch (_: Exception) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: SecurePrefs, output: OutputStream) {
        val json = gson.toJson(t).toByteArray()
        val encrypted = aead.encrypt(json, null)
        output.write(encrypted)
    }
}

