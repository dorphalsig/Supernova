package com.supernova.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.guava.await

/**
 * Simple sync worker stub used for splash screen initialization.
 */
class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = Result.success()
}

/** Triggers [SyncWorker] once and returns the enqueue result. */
object OneShotSyncWorker {
    suspend fun trigger(context: Context): Result<Unit> = try {
        val request = OneTimeWorkRequestBuilder<SyncWorker>().build()
        val op: Operation = WorkManager.getInstance(context)
            .enqueueUniqueWork("one_shot_sync", ExistingWorkPolicy.KEEP, request)
        val state = op.result.await()
        if (state == Operation.SUCCESS) Result.success(Unit)
        else Result.failure(IllegalStateException("enqueue failed"))
    } catch (t: Throwable) {
        Result.failure(t)
    }
}
