package com.supernova.ui.screens

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.supernova.data.database.SupernovaDatabase
import com.supernova.utils.SecureDataStore
import com.supernova.utils.SecureStorageKeys
import com.supernova.work.SyncManager
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var db: SupernovaDatabase
    private lateinit var sync: SyncManager

    @Before
    fun setup() {
        mockkObject(SecureDataStore)
        db = mockk(relaxed = true)
        sync = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun loadsSyncInfoFromSecureStore() = runTest {
        coEvery { SecureDataStore.getBoolean(SecureStorageKeys.LAST_SYNC_SUCCESS) } returns true
        coEvery { SecureDataStore.getLong(SecureStorageKeys.LAST_SYNC_TIME) } returns 100L
        val vm = SettingsViewModel(db, sync)
        assertTrue(vm.lastSyncSuccess.first()!!)
        assertEquals(100L, vm.lastSyncTime.first())
    }

    @Test
    fun triggerManualSyncCallsManager() = runTest {
        val vm = SettingsViewModel(db, sync)
        vm.triggerManualSync()
        verify { sync.triggerImmediateSync() }
    }
}
