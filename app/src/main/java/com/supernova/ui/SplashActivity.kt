package com.supernova.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.supernova.data.database.SupernovaDatabase
import com.supernova.databinding.ActivitySplashBinding
import com.supernova.utils.SecureDataStore
import com.supernova.utils.SecureDataStore.getBoolean
import com.supernova.utils.SecureStorageKeys
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private val splashTimeOut = 3000L // 3 seconds
    private lateinit var binding: ActivitySplashBinding
    private var startTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startTime = System.currentTimeMillis()

        binding.logoImageView.alpha = 0f
        binding.logoImageView.animate().alpha(1f).setDuration(1000).start()

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch { navigateToNextScreen() }
    }

    private suspend fun navigateToNextScreen(): Intent {
        val intent = try {
            val configured = getBoolean(SecureStorageKeys.IS_CONFIGURED)
            val lastSuccess = getBoolean(SecureStorageKeys.LAST_SYNC_SUCCESS)
            if (!configured || !lastSuccess) {
                Intent(this, ConfigurationActivity::class.java)
            } else {
                val db = SupernovaDatabase.getDatabase(this)
                val profileCount = db.profileDao().getProfileCount()
                if (profileCount == 0) {
                    Intent(this, ProfileCreationActivity::class.java)
                } else {
                    Intent(this, ProfileSelectionActivity::class.java)
                }
            }
        } catch (_: Exception) {
            Intent(this, ConfigurationActivity::class.java)
        }

        val elapsed = System.currentTimeMillis() - startTime
        val remaining = splashTimeOut - elapsed
        if (remaining > 0) delay(remaining)

        binding.progressBar.visibility = View.GONE
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
        return intent
    }
}