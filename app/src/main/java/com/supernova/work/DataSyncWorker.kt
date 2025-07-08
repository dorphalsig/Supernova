package com.supernova.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.supernova.data.database.SupernovaDatabase
import com.supernova.network.DataSyncService
import com.supernova.network.models.SyncResult
import com.supernova.utils.SecureStorage
import kotlinx.coroutines.flow.last


class DataSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "DataSyncWorker"
        const val KEY_SYNC_RESULT = "sync_result"
        const val KEY_ERROR_MESSAGE = "error_message"
    }

    override suspend fun doWork(): Result {
        return try {
            val database = SupernovaDatabase.getDatabase(applicationContext)
            val secureStorage = SecureStorage(applicationContext)
            val syncService = DataSyncService(database, secureStorage)

            // Perform sync and get the final result
            val finalResult = syncService.syncAll().last()

            when (finalResult) {
                is SyncResult.Success -> {
                    val outputData = workDataOf(KEY_SYNC_RESULT to "success")
                    Result.success(outputData)
                }
                is SyncResult.Error -> {
                    val outputData = workDataOf(
                        KEY_SYNC_RESULT to "error",
                        KEY_ERROR_MESSAGE to finalResult.message
                    )
                    Result.failure(outputData)
                }
                is SyncResult.Progress -> {
                    // This shouldn't happen as we take the last result
                    Result.success()
                }
            }
        } catch (e: Exception) {
            val outputData = workDataOf(
                KEY_SYNC_RESULT to "error",
                KEY_ERROR_MESSAGE to (e.message ?: "Unknown error")
            )
            Result.failure(outputData)
        }
    }
}