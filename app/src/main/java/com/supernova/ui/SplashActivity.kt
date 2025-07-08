package com.supernova.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.supernova.data.database.SupernovaDatabase
import com.supernova.utils.AvatarPreloader
import com.supernova.utils.SecureStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var secureStorage: SecureStorage
    private lateinit var avatarPreloader: AvatarPreloader
    private val splashTimeOut = 3000L // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        secureStorage = SecureStorage(this)
        avatarPreloader = AvatarPreloader(this)

        // Start preloading avatars if user is configured
        if (secureStorage.isConfigured()) {
            preloadProfileAvatars()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, splashTimeOut)
    }

    private fun preloadProfileAvatars() {
        lifecycleScope.launch {
            try {
                val database = SupernovaDatabase.getDatabase(this@SplashActivity)
                val profiles = database.profileDao().getAllProfiles().first()
                avatarPreloader.preloadProfileAvatars(profiles)
            } catch (e: Exception) {
                // Silent fail - preloading is not critical for app functionality
            }
        }
    }

    private fun navigateToNextScreen() {
        val intent = if (secureStorage.isConfigured()) {
            Intent(this, ProfileSelectionActivity::class.java)
        } else {
            Intent(this, ConfigurationActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}