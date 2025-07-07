package com.supernova.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "supernova_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_PORTAL = "portal"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_IS_CONFIGURED = "is_configured"
        private const val KEY_PARENTAL_LOCK = "parental_lock"
    }

    fun saveCredentials(portal: String, username: String, password: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_PORTAL, portal)
            putString(KEY_USERNAME, username)
            putString(KEY_PASSWORD, password)
            putBoolean(KEY_IS_CONFIGURED, true)
            apply()
        }
    }

    fun getPortal(): String? {
        return sharedPreferences.getString(KEY_PORTAL, null)
    }

    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    fun getPassword(): String? {
        return sharedPreferences.getString(KEY_PASSWORD, null)
    }

    fun isConfigured(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_CONFIGURED, false)
    }

    fun setParentalLock(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_PARENTAL_LOCK, enabled).apply()
    }

    fun isParentalLockEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_PARENTAL_LOCK, false)
    }

    fun clearCredentials() {
        with(sharedPreferences.edit()) {
            remove(KEY_PORTAL)
            remove(KEY_USERNAME)
            remove(KEY_PASSWORD)
            remove(KEY_IS_CONFIGURED)
            apply()
        }
    }
}