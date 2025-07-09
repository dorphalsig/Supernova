package com.supernova.work

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class SyncManager(private val context: Context) {

    companion object {
        private const val DAILY_SYNC_WORK_NAME = DataSyncWorker.WORK_NAME
    }

    /**
     * Schedules periodic sync every 24 hours and triggers an immediate run.
     * Any existing schedule is cancelled and replaced.
     */
    fun scheduleDailySync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<DataSyncWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                DAILY_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                periodicRequest
            )

        val immediateRequest = OneTimeWorkRequestBuilder<DataSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(immediateRequest)
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