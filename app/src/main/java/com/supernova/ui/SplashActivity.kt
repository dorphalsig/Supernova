package com.supernova.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.supernova.utils.SecureStorage

class SplashActivity : AppCompatActivity() {

    private lateinit var secureStorage: SecureStorage
    private val splashTimeOut = 3000L // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        secureStorage = SecureStorage(this)

        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, splashTimeOut)
    }

    private fun navigateToNextScreen() {
        val intent = if (!secureStorage.isConfigured() || !secureStorage.isLastSyncSuccessful()) {
            Intent(this, ConfigurationActivity::class.java)
        } else {
            Intent(this, ProfileSelectionActivity::class.java)
        }

        startActivity(intent)
        finish()
    }}