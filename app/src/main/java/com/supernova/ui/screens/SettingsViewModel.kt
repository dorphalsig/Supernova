package com.supernova.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.supernova.BuildConfig
import com.supernova.data.database.SupernovaDatabase
import com.supernova.utils.SecureDataStore
import com.supernova.utils.SecureStorageKeys
import com.supernova.work.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val database: SupernovaDatabase,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

    private val _lastSyncSuccess = MutableStateFlow<Boolean?>(null)
    val lastSyncSuccess: StateFlow<Boolean?> = _lastSyncSuccess.asStateFlow()

    private val _syncState = MutableStateFlow<WorkInfo.State?>(null)
    val syncState: StateFlow<WorkInfo.State?> = _syncState.asStateFlow()

    private val _showResetConfirmation = MutableStateFlow(false)
    val showResetConfirmation: StateFlow<Boolean> = _showResetConfirmation.asStateFlow()

    val buildInfo: String = "${BuildConfig.VERSION_NAME} (${android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"})"

    init {
        viewModelScope.launch {
            _lastSyncTime.value = SecureDataStore.getLong(SecureStorageKeys.LAST_SYNC_TIME).takeIf { it != 0L }
            _lastSyncSuccess.value = SecureDataStore.getBoolean(SecureStorageKeys.LAST_SYNC_SUCCESS)
        }
    }

    fun triggerManualSync() {
        val liveData = syncManager.triggerImmediateSync()
        liveData.observeForever { info ->
            _syncState.value = info?.state
            if (info?.state?.isFinished == true) {
                liveData.removeObserver { }
            }
        }
    }

    fun confirmReset() { _showResetConfirmation.value = true }
    fun dismissReset() { _showResetConfirmation.value = false }

    fun resetAllData() {
        viewModelScope.launch {
            database.clearAllTables()
            SecureDataStore.clear()
            _showResetConfirmation.value = false
        }
    }
}

class SettingsViewModelFactory(
    private val database: SupernovaDatabase,
    private val syncManager: SyncManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(database, syncManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
