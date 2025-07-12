package com.supernova.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.supernova.utils.SecureDataStore
import com.supernova.utils.SecureDataStore.getBoolean
import com.supernova.utils.SecureStorageKeys
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private val splashTimeOut = 3000L // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({
            lifecycleScope.launch { navigateToNextScreen() }
        }, splashTimeOut)
    }

    private suspend fun navigateToNextScreen() {
        val configured = getBoolean(SecureStorageKeys.IS_CONFIGURED)
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