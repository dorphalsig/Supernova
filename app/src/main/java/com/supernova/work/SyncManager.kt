package com.supernova.work

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class SyncManager(private val context: Context) {

    companion object {
        private const val DAILY_SYNC_WORK_NAME = "DailySyncWork"
        private const val MIN_DELAY_HOURS = 20L  // Minimum 20 hours between syncs
        private const val MAX_DELAY_HOURS = 28L  // Maximum 28 hours between syncs
    }

    /**
     * Schedules daily background sync with random timing to distribute server load
     */
    fun scheduleDailySync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED) // WiFi only
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(false) // Allow sync even when device is being used
            .build()

        // Random delay between 20-28 hours to distribute load
        val randomDelayHours = Random.nextLong(MIN_DELAY_HOURS, MAX_DELAY_HOURS + 1)

        val dailySyncRequest = PeriodicWorkRequestBuilder<DataSyncWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS,
            flexTimeInterval = 4, // 4-hour flex window
            flexTimeIntervalUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(randomDelayHours, TimeUnit.HOURS)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, // Start with 30 minute delay
                TimeUnit.MINUTES
            )
            .addTag("background_sync")
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                DAILY_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
                dailySyncRequest
            )
    }

    /**
     * Triggers immediate sync (for manual refresh or initial setup)
     */
    fun triggerImmediateSync(): LiveData<WorkInfo?> {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Any network for immediate sync
            .build()

        val immediateSyncRequest = OneTimeWorkRequestBuilder<DataSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                30,
                TimeUnit.SECONDS
            )
            .addTag("immediate_sync")
            .build()

        WorkManager.getInstance(context).enqueue(immediateSyncRequest)

        return WorkManager.getInstance(context)
            .getWorkInfoByIdLiveData(immediateSyncRequest.id)
    }

    /**
     * Cancels all scheduled sync work
     */
    fun cancelAllSync() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(DAILY_SYNC_WORK_NAME)
    }

    /**
     * Gets status of daily sync work
     */
    fun getDailySyncStatus(): LiveData<List<WorkInfo>> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkLiveData(DAILY_SYNC_WORK_NAME)
    }

    /**
     * Reschedules daily sync (useful after configuration changes)
     */
    fun rescheduleDailySync() {
        cancelAllSync()
        scheduleDailySync()
    }
}