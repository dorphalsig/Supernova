package com.supernova.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.supernova.data.database.SupernovaDatabase
import com.supernova.network.DataSyncService
import com.supernova.network.models.SyncResult
import com.supernova.utils.SecureDataStore
import kotlinx.coroutines.flow.last

class DataSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "DataSyncWorker"
        const val KEY_SYNC_RESULT = "sync_result"
        const val KEY_ERROR_MESSAGE = "error_message"
        private const val TAG = "DataSyncWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "DataSyncWorker started")

        val secureStorage = SecureDataStore(applicationContext)
        return try {
            val database = SupernovaDatabase.getDatabase(applicationContext)
            val syncService = DataSyncService(database, secureStorage)

            Log.d(TAG, "Starting sync process...")
            val finalResult = syncService.syncAll().last()

            when (finalResult) {
                is SyncResult.Success -> {
                    Log.d(TAG, "Sync completed successfully")
                    secureStorage.setLastSyncResult(true)
                    val outputData = workDataOf(KEY_SYNC_RESULT to "success")
                    Result.success(outputData)
                }
                is SyncResult.Error -> {
                    Log.e(TAG, "Sync failed: ${finalResult.message}")
                    secureStorage.setLastSyncResult(false)
                    val outputData = workDataOf(
                        KEY_SYNC_RESULT to "error",
                        KEY_ERROR_MESSAGE to finalResult.message
                    )
                    Result.failure(outputData)
                }
                is SyncResult.Progress -> {
                    Log.w(TAG, "Unexpected progress result as final result: ${finalResult.step}")
                    Result.success()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "DataSyncWorker failed with exception", e)
            secureStorage.setLastSyncResult(false)
            val outputData = workDataOf(
                KEY_SYNC_RESULT to "error",
                KEY_ERROR_MESSAGE to (e.message ?: "Unknown error")
            )
            Result.failure(outputData)
        }
    }
}