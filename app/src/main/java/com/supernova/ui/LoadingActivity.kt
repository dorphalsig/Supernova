package com.supernova.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.supernova.R
import com.supernova.databinding.ActivityLoadingBinding
import com.supernova.utils.SecureDataStore
import com.supernova.utils.SecureStorageKeys
import com.supernova.work.DataSyncWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.random.Random

class LoadingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoadingBinding
    private val handler = Handler(Looper.getMainLooper())
    private var phraseRotationRunnable: Runnable? = null
    private val minimumDisplayTime = 10000L // 10 seconds
    private val timeoutMillis = 20 * 60 * 1000L
    private var startTime = 0L

    private val loadingPhrases: Array<String> by lazy {
        resources.getStringArray(R.array.random_loading_phrases)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        startTime = System.currentTimeMillis()

        // Load GIF using Coil
        binding.loadingAnimationImageView.load(R.drawable.loading_animation) {
            crossfade(true)
        }

        startPhraseRotation()
        waitForSyncCompletion()
    }

    private fun startPhraseRotation() {
        scheduleNextPhrase()
    }

    private fun scheduleNextPhrase() {
        phraseRotationRunnable = Runnable {
            val randomPhrase = loadingPhrases.random()
            binding.loadingPhraseTextView.text = randomPhrase
            val nextDelay = Random.nextLong(3000, 5001)
            handler.postDelayed(phraseRotationRunnable!!, nextDelay)
        }
        val initialDelay = Random.nextLong(3000, 5001)
        handler.postDelayed(phraseRotationRunnable!!, initialDelay)
    }

    private fun waitForSyncCompletion() {
        lifecycleScope.launch {
            val wasSyncedBefore = SecureDataStore.getBoolean(SecureStorageKeys.LAST_SYNC_SUCCESS)
            val workInfo = withTimeoutOrNull(timeoutMillis) {
                var result: WorkInfo? = null
                while (result == null) {
                    val infos = WorkManager.getInstance(this@LoadingActivity)
                        .getWorkInfosForUniqueWork(DataSyncWorker.WORK_NAME)
                        .await()
                    val info = infos.firstOrNull()
                    if (info?.state?.isFinished == true) {
                        result = info
                    } else {
                        delay(1000)
                    }
                }
                result
            }

            val elapsed = System.currentTimeMillis() - startTime
            val remaining = minimumDisplayTime - elapsed
            if (remaining > 0) delay(remaining)

            when {
                workInfo == null -> showTimeoutDialog()
                workInfo.state == WorkInfo.State.SUCCEEDED -> navigateToProfileSelection()
                workInfo.state == WorkInfo.State.FAILED -> {
                    val errorMsg = workInfo.outputData.getString(DataSyncWorker.KEY_ERROR_MESSAGE)
                        ?: "Sync failed"
                    handleSyncFailure(errorMsg, wasSyncedBefore)
                }
                else -> navigateToProfileSelection()
            }
        }
    }

    private fun showTimeoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sync Timeout")
            .setMessage("Data sync is taking too long. Cancel?")
            .setCancelable(false)
            .setPositiveButton("Cancel") { _, _ ->
                startActivity(Intent(this, ConfigurationActivity::class.java))
                finish()
            }
            .setNegativeButton("Wait") { _, _ ->
                startTime = System.currentTimeMillis()
                waitForSyncCompletion()
            }
            .show()
    }

    private fun handleSyncFailure(message: String, wasSyncedBefore: Boolean) {
        if (!wasSyncedBefore) {
            AlertDialog.Builder(this)
                .setTitle("Sync Failed")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK") { _, _ ->
                    startActivity(Intent(this, ConfigurationActivity::class.java))
                    finish()
                }
                .show()
        } else {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            navigateToProfileSelection()
        }
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
}
