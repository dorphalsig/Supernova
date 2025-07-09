package com.supernova.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.supernova.R
import com.supernova.data.database.SupernovaDatabase
import com.supernova.databinding.ActivityLoadingBinding
import com.supernova.network.DataSyncService
import com.supernova.utils.SecureStorage
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlin.random.Random

class LoadingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoadingBinding
    private lateinit var secureStorage: SecureStorage
    private val handler = Handler(Looper.getMainLooper())
    private var phraseRotationRunnable: Runnable? = null
    private var minimumDisplayTime = 30000L // 30 seconds minimum
    private var startTime = 0L
    
    private val loadingPhrases by lazy {
        (1..200).map { index ->
            val resourceId = resources.getIdentifier("random_loading_phrase_$index", "string", packageName)
            if (resourceId != 0) getString(resourceId) else "Initializing systems..."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        secureStorage = SecureStorage(this)
        startTime = System.currentTimeMillis()

        val isCompletingSync = intent.getBooleanExtra(EXTRA_COMPLETING_SYNC, false)
        
        // Update title based on mode
        if (isCompletingSync) {
            binding.setupTitleTextView.text = getString(R.string.completing_data_sync)
        }

        startPhraseRotation()
        if (isCompletingSync) {
            waitForSyncCompletion()
        } else {
            startInitialSetup()
        }
    }

    private fun startPhraseRotation() {
        scheduleNextPhrase()
    }

    private fun scheduleNextPhrase() {
        phraseRotationRunnable = Runnable {
            // Pick random phrase
            val randomPhrase = loadingPhrases.random()
            binding.loadingPhraseTextView.text = randomPhrase

            // Schedule next phrase in 3-5 seconds
            val nextDelay = Random.nextLong(3000, 5001)
            handler.postDelayed(phraseRotationRunnable!!, nextDelay)
        }
        
        // Initial delay before first phrase change
        val initialDelay = Random.nextLong(3000, 5001)
        handler.postDelayed(phraseRotationRunnable!!, initialDelay)
    }

    private fun startInitialSetup() {
        lifecycleScope.launch {
            try {
                // Get credentials from intent
                val portal = intent.getStringExtra(EXTRA_PORTAL) ?: ""
                val username = intent.getStringExtra(EXTRA_USERNAME) ?: ""
                val password = intent.getStringExtra(EXTRA_PASSWORD) ?: ""

                secureStorage.saveCredentials(portal, username, password)
                val database = SupernovaDatabase.getDatabase(this@LoadingActivity)
                val syncService = DataSyncService(database, secureStorage)
                
                val syncResult = syncService.syncAll().last()
                
                // Add delay to ensure user sees the loading screen
                kotlinx.coroutines.delay(2000)
                navigateToProfileCreation()
                
            } catch (e: Exception) {
                navigateToProfileCreation()
            }
        }
    }

    private fun waitForSyncCompletion() {
        lifecycleScope.launch {
            try {
                // Check if sync is still running by trying to get sync status
                val database = SupernovaDatabase.getDatabase(this@LoadingActivity)
                val syncService = DataSyncService(database, secureStorage)
                
                // Continue any pending sync
                val syncResult = syncService.syncAll().last()
                
                // Ensure minimum display time
                val elapsedTime = System.currentTimeMillis() - startTime
                val remainingTime = minimumDisplayTime - elapsedTime
                if (remainingTime > 0) {
                    kotlinx.coroutines.delay(remainingTime)
                }
                
                navigateToProfileSelection()
                
            } catch (e: Exception) {
                // Even if sync fails, continue after minimum time
                val elapsedTime = System.currentTimeMillis() - startTime
                val remainingTime = minimumDisplayTime - elapsedTime
                if (remainingTime > 0) {
                    kotlinx.coroutines.delay(remainingTime)
                }
                navigateToProfileSelection()
            }
        }
    }

    private fun navigateToProfileCreation() {
        val intent = Intent(this, ProfileCreationActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToProfileSelection() {
        val intent = Intent(this, ProfileSelectionActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        phraseRotationRunnable?.let { handler.removeCallbacks(it) }
    }

    companion object {
        const val EXTRA_PORTAL = "extra_portal"
        const val EXTRA_USERNAME = "extra_username"
        const val EXTRA_PASSWORD = "extra_password"
        const val EXTRA_COMPLETING_SYNC = "extra_completing_sync"
    }
}
