package com.supernova.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.supernova.utils.SecureDataStore
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var secureStorage: SecureDataStore
    private val splashTimeOut = 3000L // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        secureStorage = SecureDataStore(this)

        Handler(Looper.getMainLooper()).postDelayed({
            lifecycleScope.launch { navigateToNextScreen() }
        }, splashTimeOut)
    }

    private suspend fun navigateToNextScreen() {
        val configured = secureStorage.isConfigured()
        val lastSuccess = getBoolean(SecureStorageKeys.LAST_SYNC_SUCCESS)
        val intent = if (!configured || !lastSuccess) {
            Intent(this, ConfigurationActivity::class.java)
        } else {
            Intent(this, ProfileSelectionActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}